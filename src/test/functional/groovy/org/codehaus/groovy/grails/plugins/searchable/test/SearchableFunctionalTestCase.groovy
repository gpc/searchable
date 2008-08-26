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
package org.codehaus.groovy.grails.plugins.searchable.test

import org.codehaus.groovy.grails.plugins.searchable.compass.DefaultSearchableMethodFactory
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.plugins.searchable.compass.spring.DefaultSearchableCompassFactoryBean
import org.springframework.core.io.DefaultResourceLoader
import org.codehaus.groovy.grails.plugins.searchable.SearchableMethodFactory
import org.codehaus.groovy.grails.plugins.searchable.compass.domain.DynamicDomainMethodUtils
import org.compass.core.util.ClassUtils
import org.compass.core.CompassTemplate
import org.compass.core.Compass;

/**
 * @author Maurice Nicholson
 */
abstract class SearchableFunctionalTestCase extends GroovyTestCase {
    Compass compass
    def grailsApplication
    def searchableMethodFactory
    def searchableService

    void setUp() {
        def cl = new GroovyClassLoader() //Thread.currentThread().getContextClassLoader())
        Thread.currentThread().setContextClassLoader(cl)

        def registry = GroovySystem.metaClassRegistry
        if(!(registry.getMetaClassCreationHandler() instanceof ExpandoMetaClassCreationHandle)) {
            registry.setMetaClassCreationHandle(new ExpandoMetaClassCreationHandle());
        }

        // attach psuedo Grails domain class methods
        def clazzes = getDomainClasses()
        addMinimalGrailsDomainClassMethodsInjections(clazzes)

        // build grails app
        grailsApplication = buildGrailsApplication(clazzes, cl)

        // build compass
        compass = buildCompass(grailsApplication, cl)

        // attach searchable dynamic methods
        searchableMethodFactory = new DefaultSearchableMethodFactory(compass: compass, grailsApplication: grailsApplication)
        DynamicDomainMethodUtils.attachDynamicMethods(searchableMethodFactory, grailsApplication.domainClasses, compass)

        // build searchable service
        searchableService = buildSearchableService(cl, searchableMethodFactory, compass)
    }

    void tearDown() {
        compass = null
        grailsApplication = null
        searchableService = null

        def classes = getDomainClasses()
        for (clazz in classes) {
            GroovySystem.metaClassRegistry.removeMetaClass(clazz)
        }
    }

    /**
     * Provide a List of user domain classes
     */
    // todo rename to getDomainClazzes to be in keeping with Grails lingo
    // todo make return type Collection since part of API
    // todo make protected since part of API
    abstract getDomainClasses();

    protected buildGrailsApplication(classes, ClassLoader cl) {
        grailsApplication = new DefaultGrailsApplication(classes as Class[], cl)
        grailsApplication.initialise()
        grailsApplication
    }

    protected buildCompass(grailsApplication, cl) {
        def fb = new DefaultSearchableCompassFactoryBean()
        fb.resourceLoader = new DefaultResourceLoader()
        fb.grailsApplication = grailsApplication
        fb.compassClassMappingXmlBuilder = ClassUtils.forName("org.codehaus.groovy.grails.plugins.searchable.compass.mapping.DefaultSearchableCompassClassMappingXmlBuilder").newInstance()
        fb.compassConnection = "ram://testindex"
        fb.compassSettings = getCompassSettings(cl)
        fb.afterPropertiesSet()

        def compass = fb.getObject()
        compass.spellCheckManager?.deleteIndex()

        return compass
    }

    protected buildSearchableService(GroovyClassLoader cl, SearchableMethodFactory methodFactory, Compass compass) {
        String resourceBaseName = this.getClass().getName().replaceAll("\\.", "/")
        def url = cl.getResource(resourceBaseName + ".class")
        if (!url) {
            url = cl.getResource(this.getClass().getName().replaceAll("\\.", "/") + ".groovy")
        }
        assert url != null, "Failed to locate this class as resource! ${this.getClass().getName()}"
        for (def dir = new File(URLDecoder.decode(url.getFile())); dir; dir = dir.getParentFile()) {
            if (new File(dir, "grails-app").isDirectory()) {
                searchableService = cl.parseClass(new File(dir, "grails-app/services/SearchableService.groovy")).newInstance()
                break
            }
        }
        searchableService.searchableMethodFactory = methodFactory
        searchableService.compass = compass
        return searchableService
    }

    protected addMinimalGrailsDomainClassMethodsInjections(clazzes) {
        for (clazz in clazzes) {
            def metaClass = clazz.metaClass
            metaClass.ident << { Object[] args ->
                return delegate.id
            }
        }
    }

    protected Map getCompassSettings(ClassLoader cl) {
        def is = getClassPackageResourceAsStream(cl, "compass-settings.properties")
        if (!is) return null

        Properties props = new Properties()
        props.load(is)
        return props
    }

    protected InputStream getClassPackageResourceAsStream(ClassLoader cl, String resourceName) {
        String resourceBaseName = this.getClass().getPackage().getName().replaceAll("\\.", "/")
        return cl.getResourceAsStream(resourceBaseName + "/" + resourceName)
    }
}