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
import org.codehaus.groovy.grails.plugins.searchable.compass.config.*
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.*
import org.codehaus.groovy.grails.plugins.searchable.compass.spring.*
import org.codehaus.groovy.grails.plugins.searchable.compass.search.*

import org.apache.commons.logging.LogFactory

import org.compass.gps.impl.SingleCompassGps
import org.compass.spring.device.hibernate.dep.SpringHibernate3GpsDevice

import org.springframework.beans.factory.config.*
import org.springframework.util.ClassUtils

import grails.spring.BeanBuilder

/**
* @author Maurice Nicholson
*/
class SearchableGrailsPlugin {
    static LOG = LogFactory.getLog("org.codehaus.groovy.grails.plugins.searchable.SearchableGrailsPlugin")

    def version = SearchableConstants.SEARCHABLE_PLUGIN_VERSION
    def author = 'Maurice Nicholson'
    def authorEmail = 'maurice@freeshell.org'
    def title = 'Adds rich search functionality to Grails domain models.'
    def description = '''
Adds rich search functionality to Grails domain models.
Built on Compass (http://www.compass-project.org/) and Lucene (http://lucene.apache.org/)
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
        for (grailsDomainClass in application.domainClasses) {
            if (!SearchableCompassUtils.isRootMappedClass(grailsDomainClass, compass)) {
                continue
            }
            LOG.debug("Adding searchable methods to [${grailsDomainClass.clazz.name}]")

            // ------------------------------------------------------------
            // class methods

            /**
             * search: Returns a subset of the instances of this class matching the given query
             */
            grailsDomainClass.metaClass.'static'.search << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "search").invoke(*args)
            }

            /**
             * searchTop: Returns the top (most relevant) instance of this class matching the given query
             */
            grailsDomainClass.metaClass.'static'.searchTop << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "searchTop").invoke(*args)
            }

            /**
             * searchEvery: Returns all instance of this class matching the given query
             */
            grailsDomainClass.metaClass.'static'.searchEvery << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "searchEvery").invoke(*args)
            }

            /**
             * Returns the number of hits for the given query matching instances of this class
             */
            grailsDomainClass.metaClass.'static'.countHits << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "countHits").invoke(*args)
            }

            /**
             * Get term frequencies for the given args
             */
            grailsDomainClass.metaClass.'static'.termFreqs << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "termFreqs").invoke(*args)
            }

            /**
             * index: Adds class instances to the search index
             */
            grailsDomainClass.metaClass.'static'.index << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "index").invoke(*args)
            }

            /**
             * indexAll: Adds class instances to the search index
             */
            grailsDomainClass.metaClass.'static'.indexAll << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "indexAll").invoke(*args)
            }

            /**
             * unindex: Removes class instances from the search index
             */
            grailsDomainClass.metaClass.'static'.unindex << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "unindex").invoke(*args)
            }

            /**
             * unindexAll: Removes class instances from the search index
             */
            grailsDomainClass.metaClass.'static'.unindexAll << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "unindexAll").invoke(*args)
            }

            /**
             * reindexAll: Updates the search index
             */
            grailsDomainClass.metaClass.'static'.reindexAll << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "reindexAll").invoke(delegate)
            }

            /**
             * reindex: Updates the search index
             */
            grailsDomainClass.metaClass.'static'.reindex << { Object[] args ->
                searchableMethodFactory.getMethod(delegate, "reindex").invoke(delegate)
            }

            // ------------------------------------------------------------
            // instance methods

            /**
             * Adds the instance to the search index
             */
            grailsDomainClass.metaClass.index << { Object[] args ->
                searchableMethodFactory.getMethod("index").invoke(delegate)
            }

            /**
             * unindex instance method: removes the instance from the search index
             */
            grailsDomainClass.metaClass.unindex << { Object[] args ->
                searchableMethodFactory.getMethod("unindex").invoke(delegate)
            }

            /**
             * reindex instance method: updates the search index to reflect the current instance data
             */
            grailsDomainClass.metaClass.reindex << { Object[] args ->
                searchableMethodFactory.getMethod("reindex").invoke(delegate)
            }
        }
    }

    // Build Compass and Compass::GPS
    def doWithSpring = {
        // Configuration
        config = getConfiguration(parentCtx)

        // Compass
        LOG.debug("Defining Compass and Compass::GPS beans")
        compass(DefaultSearchableCompassFactoryBean) { bean ->
            grailsApplication = application
            compassConnection = config?.compassConnection
            compassSettings = config?.compassSettings
            defaultExcludedProperties = config?.defaultExcludedProperties
            defaultFormats = config?.defaultFormats
            compassClassMappingXmlBuilder = new DefaultSearchableCompassClassMappingXmlBuilder()
        }

        // Compass::GPS
        compassGpsDevice(SpringHibernate3GpsDevice) {
            name = "hibernate"
            sessionFactory = sessionFactory
            fetchCount = 5000
        }
        compassGps(SingleCompassGps) {
            compass = compass
            gpsDevices = [compassGpsDevice]
        }

        searchableMethodFactory(DefaultSearchableMethodFactory) {
            compass = compass
            compassGps = compassGps
            searchDefaults = config?.defaultSearchOptions
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

        // start the gps, mirroring any changes made through Hibernate API
        // to be mirrored to the search engine
        def mirrorChanges = !config || config.mirrorChanges == true
        def compassGps = applicationContext.getBean("compassGps")
        if (mirrorChanges) {
            compassGps.start()
            LOG.debug("Started Compass::GPS")
        }

        // index the database?
        def bulkIndex = !config || (config.bulkIndexOnStartup instanceof Boolean && config.bulkIndexOnStartup == true)
        def forkBulkIndex = !config || (config.bulkIndexOnStartup instanceof String && config.bulkIndexOnStartup == "fork")
        if (bulkIndex) {
            CompassGpsUtils.index(compassGps)
        } else if (forkBulkIndex) {
            Thread.start {
                CompassGpsUtils.index(compassGps)
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
        def locations = ["file:.", "/WEB-INF"]
        for (location in locations) {
            def name = location + "/grails-app/conf/SearchableConfiguration.groovy"
            LOG.debug("Trying to load config from ${name}")
            // TODO null check required because of the differences in grails run-app and test-app
            // TODO raise issue
            def resource = resourceLoader?.getResource(name)
            if (resource?.exists()) {
                def config = new GroovyClassLoader().parseClass(resource.inputStream).newInstance()
                LOG.debug("Config instance: ${config?.properties}")
                return config
            }
            LOG.debug("Not found: ${name}")
        }
        return null
    }
}
