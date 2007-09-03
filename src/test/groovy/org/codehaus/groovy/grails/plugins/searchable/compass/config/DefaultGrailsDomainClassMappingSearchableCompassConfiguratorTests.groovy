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
package org.codehaus.groovy.grails.plugins.searchable.compass.config

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.*
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.compass.core.config.CompassConfiguration
import org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping.SearchableGrailsDomainClassMappingConfigurator

/**
*
*
* @author Maurice Nicholson
*/
class DefaultGrailsDomainClassMappingSearchableCompassConfiguratorTests extends GroovyTestCase {
    def configurator

    void setUp() {
        configurator = new DefaultGrailsDomainClassMappingSearchableCompassConfigurator()
    }

    void tearDown() {
        configurator = null
    }

    void testConfigure() {
        configurator.grailsApplication = getApplication([Post, Comment, User])

        // These represent different mapping strategies; a different one for each class
        def postMapper = new TestSearchableGrailsDomainClassMappingStrategy(classShouldMap: Post)
        def commentMapper = new TestSearchableGrailsDomainClassMappingStrategy(classShouldMap: Comment)
        def userMapper = new TestSearchableGrailsDomainClassMappingStrategy(classShouldMap: User)

        // Set the strategies and let the configurator invoke them all
        configurator.classMappingStrategies = [postMapper, commentMapper, userMapper] as SearchableGrailsDomainClassMappingConfigurator[]
        configurator.configure(new CompassConfiguration(), [:])

        // Verify they were all mapped by their respective strategy
        assert postMapper.classDidMap == Post
        assert commentMapper.classDidMap == Comment
        assert userMapper.classDidMap == User
    }

    private getApplication(classes) {
        def app = new DefaultGrailsApplication(classes as Class[], new GroovyClassLoader())
        app.initialise()
        return app
    }
}

class TestSearchableGrailsDomainClassMappingStrategy implements SearchableGrailsDomainClassMappingConfigurator {
    def classShouldMap
    def classDidMap
    boolean isSearchable(GrailsDomainClass grailsDomainClass) {
        classShouldMap == grailsDomainClass.clazz
    }
    void configureMapping(CompassConfiguration compassConfiguration, Map configurationContext, GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses) {
        classDidMap = grailsDomainClass.clazz
    }
    String getName() {
        "dummy"
    }
}