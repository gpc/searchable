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
package org.codehaus.groovy.grails.plugins.searchable.compass.mapping

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.component.*
import org.compass.core.config.CompassConfiguration
import org.compass.core.config.ConfigurationException
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.test.domain.nosearchableproperty.NoSearchableProperty
import org.codehaus.groovy.grails.orm.hibernate.GrailsHibernateDomainClass

/**
*
*
* @author Maurice Nicholson
*/
class SearchableClassPropertySearchableGrailsDomainClassMappingStrategyTests extends GroovyTestCase {
    def strategy
    def oldSearchable

    void setUp() {
        strategy = new SearchableClassPropertySearchableGrailsDomainClassMappingStrategy()
        oldSearchable = Post.searchable
    }

    void tearDown() {
        strategy = null
        Post.searchable = oldSearchable
    }

    void testIsSearchable() {
        Post.searchable = true
        assert strategy.isSearchable(new DefaultGrailsDomainClass(Post))
        Post.searchable = [only: 'title']
        assert strategy.isSearchable(new DefaultGrailsDomainClass(Post))
        Post.searchable = [except: 'version']
        assert strategy.isSearchable(new DefaultGrailsDomainClass(Post))
        Post.searchable = { -> }
        assert strategy.isSearchable(new DefaultGrailsDomainClass(Post))
        Post.searchable = false
        assert !strategy.isSearchable(new DefaultGrailsDomainClass(Post))

        assert !strategy.isSearchable(new DefaultGrailsDomainClass(NoSearchableProperty))
    }

    void testConfigureMapping() {
        strategy.mappingDescriptionProviderManager = new MySearchableGrailsDomainClassCompassMappingDescriptionProviderManager(classToHandle: Post)
        strategy.compassClassMappingXmlBuilder = [
            buildClassMappingXml: { CompassMappingDescription description ->
                new ByteArrayInputStream("this is the mapping".getBytes())
            }
        ] as SearchableCompassClassMappingXmlBuilder
        def conf = new MyCompassConfiguration()
        def domainClassMap = [:]
        for (type in [Post, User, Comment]) {
            domainClassMap[type] = new DefaultGrailsDomainClass(type)
        }
        strategy.configureMapping(conf, null, domainClassMap[Post], domainClassMap.keySet())
        assert conf.inputStream.text == "this is the mapping"
        assert conf.resourceName == Post.class.name.replaceAll("\\.", "/") + ".cpm.xml"
    }
}

class MyCompassConfiguration extends CompassConfiguration {
    def inputStream
    def resourceName
    CompassConfiguration addInputStream(InputStream inputStream, String resourceName) throws ConfigurationException {
        assert this.inputStream == null
        assert this.resourceName == null
        this.inputStream = inputStream
        this.resourceName = resourceName
        this
    }
}

class MySearchableGrailsDomainClassCompassMappingDescriptionProviderManager extends SearchableGrailsDomainClassCompassMappingDescriptionProviderManager {
    def classToHandle
    boolean handles(GrailsDomainClass grailsDomainClass) {
        grailsDomainClass.clazz == classToHandle
    }
    CompassMappingDescription getCompassMappingDescription(GrailsDomainClass grailsDomainClass, Collection searchableClasses) {
        new CompassMappingDescription(mappedClass: grailsDomainClass.clazz)
    }
}
