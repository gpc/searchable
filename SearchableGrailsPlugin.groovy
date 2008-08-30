/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.codehaus.groovy.grails.plugins.support.GrailsPluginUtils
import org.codehaus.groovy.grails.plugins.searchable.*
import org.codehaus.groovy.grails.plugins.searchable.compass.*
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.*
import org.codehaus.groovy.grails.plugins.searchable.compass.spring.*

import org.apache.commons.logging.LogFactory

import org.compass.gps.impl.SingleCompassGps

import org.compass.gps.device.hibernate.HibernateGpsDevice
import org.codehaus.groovy.grails.plugins.searchable.compass.domain.DynamicDomainMethodUtils
import org.springframework.core.JdkVersion

/**
* @author Maurice Nicholson
*/
class SearchableGrailsPlugin {
    static LOG = LogFactory.getLog("org.codehaus.groovy.grails.plugins.searchable.SearchableGrailsPlugin")

    def version = SearchableConstants.SEARCHABLE_PLUGIN_VERSION
    def author = 'Maurice Nicholson'
    def authorEmail = 'maurice@freeshell.org'
    def title = 'Adds rich search functionality to Grails domain models. This version is recommended for JDK 1.5+'
    def description = '''
Adds rich search functionality to Grails domain models.
Built on Compass (http://www.compass-project.org/) and Lucene (http://lucene.apache.org/)
This version is recommended for JDK 1.5+
'''
    def documentation = 'http://grails.org/Searchable+Plugin'

    def grailsVersion = GrailsPluginUtils.grailsVersion
    def dependsOn = [dataSource: grailsVersion,
                     domainClass: grailsVersion,
                     i18n: grailsVersion,
                     core:  grailsVersion,
                     hibernate: grailsVersion]
//	def watchedResources = "file:./grails-app/doai/*Codec.groovy"
    def config

    def doWithDynamicMethods = { applicationContext ->
        def compass = applicationContext.getBean("compass")
        def searchableMethodFactory = applicationContext.getBean("searchableMethodFactory")
        DynamicDomainMethodUtils.attachDynamicMethods(searchableMethodFactory, application.domainClasses, compass)
    }

    // Build Compass and Compass::GPS
    def doWithSpring = {
        if (!JdkVersion.isAtLeastJava15()) {
            LOG.error("This version of the Searchable Plugin is only compatible with JDK 1.5+. See the documentation at ${documentation} for the JDK 1.4 alternative")
            System.out.println("This version of the Searchable Plugin is only compatible with JDK 1.5+. See the documentation at ${documentation} for the JDK 1.4 alternative")
        }

        // Configuration
        config = getConfiguration(parentCtx)

        // Compass
        LOG.debug("Defining Compass and Compass::GPS beans")
        compass(DefaultSearchableCompassFactoryBean) { bean ->
            grailsApplication = application
            compassConnection = config.compassConnection
            compassSettings = config.compassSettings
            defaultExcludedProperties = config.defaultExcludedProperties
            defaultFormats = config.defaultFormats
            compassClassMappingXmlBuilder = new DefaultSearchableCompassClassMappingXmlBuilder()
        }

        // Compass::GPS
//        lifecycleInjector(org.compass.gps.device.hibernate.lifecycle.DefaultHibernateEntityCollectionLifecycleInjector, true) {}
        compassGpsDevice(HibernateGpsDevice) {
//        compassGpsDevice(SpringHibernate3GpsDevice) {
            name = "hibernate"
            sessionFactory = sessionFactory
            fetchCount = 5000
//            lifecycleInjector = lifecycleInjector
        }
        compassGps(SingleCompassGps) {
            compass = compass
            gpsDevices = [compassGpsDevice]
        }

        def defaultMethodOptions = config.defaultMethodOptions
        if (!defaultMethodOptions && config.defaultSearchOptions) {
            LOG.warn(
                "The Searchable Plugin SearchableConfiguration#defaultSearchOptions property is deprecated and " +
                "will be removed in the next version. Please upgrade to the latest version of the SearchableConfiguration " +
                "file (remember to make a backup first!) and read the deprecation notice for details"
            );
            System.out.println(
                "The Searchable Plugin SearchableConfiguration#defaultSearchOptions property is deprecated and " +
                "will be removed in the next version. Please upgrade to the latest version of the SearchableConfiguration " +
                "file (remember to make a backup first!) and read the deprecation notice for details"
            );
            defaultMethodOptions = [search: config.defaultSearchOptions]
        }
        searchableMethodFactory(DefaultSearchableMethodFactory) {
            compass = compass
            compassGps = compassGps
            defaultMethodOptions = defaultMethodOptions
            grailsApplication = application
        }
        LOG.debug("Done defining Compass and Compass::GPS beans")
    }

    // Post initialization spring config
    def doWithApplicationContext = {
        def compass = applicationContext.getBean("compass")
        if (!SearchableCompassUtils.hasMappings(compass)) {
            return false
        }

        // release locks?
        if (compass.searchEngineIndexManager.isLocked()) {
            if (config.releaseLocksOnStartup != false) {
                compass.searchEngineIndexManager.releaseLocks()
                LOG.warn("The index was forcefully unlocked. The index is probably out of sync and needs re-building")
            }
        }

        // start the gps, mirroring any changes made through Hibernate API
        // to be mirrored to the search engine
        def mirrorChanges = config.mirrorChanges != false
        def compassGps = applicationContext.getBean("compassGps")
        if (mirrorChanges) {
            compassGps.start()
            LOG.debug("Started Compass::GPS")
        }

        // index the database?
        def bulkIndex = config.bulkIndexOnStartup in [null, true]
        def forkBulkIndex = config.bulkIndexOnStartup in ["fork"]
        if (bulkIndex) {
            CompassGpsUtils.index(compassGps, null)
        } else if (forkBulkIndex) {
            Thread.start {
                CompassGpsUtils.index(compassGps, null)
            }
        } else {
            LOG.debug("Not performing bulk index")
        }
    }

/*    def doWithWebDescriptor = {
        // TODO Implement additions to web.xml (optional)
    }*/

/*    def onChange = { event ->
         LOG.debug("onChange called")
        // TODO Implement code that is executed when this class plugin class is changed
        // the event contains: event.application and event.applicationContext objects
    }*/

/*    def onApplicationChange = { event ->
        LOG.debug("onApplicationChange called")
        // TODO Implement code that is executed when any class in a GrailsApplication changes
        // the event contain: event.source, event.application and event.applicationContext objects

        // TODO destroy and rebuild Compass and Compass::GPS
    }*/

    // Get a configuration instance
    private getConfiguration = { resourceLoader ->
       try {
           LOG.debug("Trying to load config from 'SearchableConfiguration.class'")
           def obj = Class.forName('SearchableConfiguration', true, Thread.currentThread().contextClassLoader).newInstance()
           return obj.properties
       } catch (ClassNotFoundException e) {
           LOG.debug("Not found: ${e.message}")
           return [:]
       }
    }
}
