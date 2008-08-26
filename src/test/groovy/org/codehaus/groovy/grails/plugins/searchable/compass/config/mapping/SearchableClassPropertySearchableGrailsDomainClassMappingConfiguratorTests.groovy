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
package org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.TestUtils
import org.compass.core.config.CompassConfiguration
import org.compass.core.config.ConfigurationException
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompassClassMapping
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.SearchableCompassClassMappingXmlBuilder
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompositeSearchableGrailsDomainClassCompassClassMapper
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.SearchableGrailsDomainClassCompassClassMapper

/**
*
*
* @author Maurice Nicholson
*/
class SearchableClassPropertySearchableGrailsDomainClassMappingConfiguratorTests extends GroovyTestCase {
    def classMappingConfigurator

    void setUp() {
        classMappingConfigurator = new SearchableClassPropertySearchableGrailsDomainClassMappingConfigurator()
    }

    void tearDown() {
        classMappingConfigurator = null
    }

    void testGetMappedBy() {
        def mappedBy = classMappingConfigurator.getMappedBy(TestUtils.getDomainClasses(SearchablePropertyIsTrue))
        assert mappedBy.collect { it.clazz } == [SearchablePropertyIsTrue]

        mappedBy = classMappingConfigurator.getMappedBy(TestUtils.getDomainClasses(SearchablePropertyIsOnlyMap))
        assert mappedBy.collect { it.clazz } == [SearchablePropertyIsOnlyMap]

        mappedBy = classMappingConfigurator.getMappedBy(TestUtils.getDomainClasses(SearchablePropertyIsExceptMap))
        assert mappedBy.collect { it.clazz } == [SearchablePropertyIsExceptMap]

        mappedBy = classMappingConfigurator.getMappedBy(TestUtils.getDomainClasses(SearchablePropertyIsClosure))
        assert mappedBy.collect { it.clazz } == [SearchablePropertyIsClosure]

        mappedBy = classMappingConfigurator.getMappedBy(TestUtils.getDomainClasses(SearchablePropertyIsFalse))
        assert mappedBy.isEmpty()

        mappedBy = classMappingConfigurator.getMappedBy(TestUtils.getDomainClasses(NoSearchableProperty))
        assert mappedBy.isEmpty()

        mappedBy = classMappingConfigurator.getMappedBy(TestUtils.getDomainClasses(ComponentOwner, Comp, SearchableComp, NonSearchableComp))
        assert mappedBy.size() == 3
        assert mappedBy.collect { it.clazz }.containsAll([ComponentOwner, SearchableComp, Comp])
    }

    void testConfigureMapping() {
        classMappingConfigurator.classMapper = [
            getCompassClassMapping: {GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses ->
                new CompassClassMapping(mappedClass: grailsDomainClass.clazz)
            }
        ] as SearchableGrailsDomainClassCompassClassMapper

        classMappingConfigurator.compassClassMappingXmlBuilder = [
            buildClassMappingXml: { CompassClassMapping description ->
                new ByteArrayInputStream("this is the mapping".getBytes())
            }
        ] as SearchableCompassClassMappingXmlBuilder

        def conf = new MyCompassConfiguration()
        def classes = [new DefaultGrailsDomainClass(SearchablePropertyIsTrue)]
        classMappingConfigurator.configureMappings(conf, null, classes, classes)
        assert conf.inputStream.text == "this is the mapping"
        assert conf.resourceName == SearchablePropertyIsTrue.class.name.replaceAll("\\.", "/") + ".cpm.xml"
    }
}

private class MyCompassConfiguration extends CompassConfiguration {
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

private class MySearchableGrailsDomainClassCompassMappingDescriptionProviderManager extends CompositeSearchableGrailsDomainClassCompassClassMapper {
    def classToHandle
    boolean handles(GrailsDomainClass grailsDomainClass) {
        grailsDomainClass.clazz == classToHandle
    }
    CompassClassMapping getCompassMappingDescription(GrailsDomainClass grailsDomainClass, Collection searchableClasses) {
        new CompassClassMapping(mappedClass: grailsDomainClass.clazz)
    }
}

class SearchablePropertyIsTrue {
    static searchable = true
    Long id, version
}

class SearchablePropertyIsFalse {
    static searchable = false
    Long id, version
}

class SearchablePropertyIsOnlyMap {
    static searchable = [only: 'title']
    Long id, version
}

class SearchablePropertyIsExceptMap {
    static searchable = [except: 'version']
    Long id, version
}

class SearchablePropertyIsClosure {
    static searchable = { -> }
    Long id, version
}

class NoSearchableProperty {
    Long id
    Long version
    String desscription
}

class ComponentOwner {
    static embedded = ['searchableComp', 'comp', 'nonSearchableComp']
    static searchable = true
    Long id, version
    SearchableComp searchableComp
    Comp comp
    NonSearchableComp nonSearchableComp

}

class Comp {
    Long id, version
}

class SearchableComp {
    static searchable = true
    Long id, version
}

class NonSearchableComp {
    static searchable = false
    Long id, version
}