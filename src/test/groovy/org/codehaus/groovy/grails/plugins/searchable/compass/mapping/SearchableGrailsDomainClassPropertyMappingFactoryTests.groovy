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

import org.codehaus.groovy.grails.plugins.searchable.compass.converter.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.component.*
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

/**
*
*
* @author Maurice Nicholson
*/
class SearchableGrailsDomainClassPropertyMappingFactoryTests extends GroovyTestCase {
    SearchableGrailsDomainClassPropertyMappingFactory factory
    Map domainClassMap

    void setUp() {
        domainClassMap = [:]
        for (type in [ComponentOwner, SearchableComp]) {
            domainClassMap[type] = new DefaultGrailsDomainClass(type)
        }
        factory = new SearchableGrailsDomainClassPropertyMappingFactory()
        // Restrict the convertable types for unit testing
        factory.converterLookupHelper = [
            hasConverter: { type ->
                return type in [String, int, boolean, Date]
            }
        ] as CompassConverterLookupHelper
    }

    void tearDown() {
        factory = null
        domainClassMap = null
    }

    // Simple types become searchable properties
    void testSearchableProperty() {
        def gcl = new GroovyClassLoader()
        def a = gcl.parseClass("""
class A {
    Long id, version
    int number
    String text
    Date createdAt
}
""")
        def gdc = new DefaultGrailsDomainClass(a)

        def propertyMapping = factory.getGrailsDomainClassPropertyMapping(gdc.getPropertyByName("number"), [gdc])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes.size() == 0

        propertyMapping = factory.getGrailsDomainClassPropertyMapping(gdc.getPropertyByName("text"), [gdc])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes.size() == 0

        propertyMapping = factory.getGrailsDomainClassPropertyMapping(gdc.getPropertyByName("createdAt"), [gdc])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes.size() == 0

        // with default format
        factory.defaultFormats = [(Date): 'xyzabc123'] // any string
        propertyMapping = factory.getGrailsDomainClassPropertyMapping(gdc.getPropertyByName("createdAt"), [gdc])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes == [format: 'xyzabc123']
    }

    // a simple String key + value map is mapped as a searchable property with
    // the plugin's stringmap converter
    void testGrailsStringMapSearchableProperty() {
        def gcl = new GroovyClassLoader()
        def a = gcl.parseClass("""
class A {
    Long id, version
    Map map
}
        """)
        def gdc = new DefaultGrailsDomainClass(a)

        def propertyMapping = factory.getGrailsDomainClassPropertyMapping(gdc.getPropertyByName("map"), [gdc])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes == [converter: 'stringmap', managedId: false]
    }

    // Complex (user) types become searchable references
    void testSearchableReferenceOne() {
        def gcl = new GroovyClassLoader()
        def a = gcl.parseClass("""
class A {
    Long id, version
}
        """)
        def b = gcl.parseClass("""
class B {
    Long id, version
    A a
}
        """)
        def agdc = new DefaultGrailsDomainClass(a)
        def bgdc = new DefaultGrailsDomainClass(b)

        // One, where other side is searchable
        def propertyMapping = factory.getGrailsDomainClassPropertyMapping(bgdc.getPropertyByName("a"), [agdc, bgdc])
        assert propertyMapping
        assert propertyMapping.reference
        assert propertyMapping.propertyType == a

        // One, where other side is NOT searchable
        propertyMapping = factory.getGrailsDomainClassPropertyMapping(bgdc.getPropertyByName("a"), [bgdc])
        assert propertyMapping == null
    }

    // Complex (user) types become searchable references
    void testSearchableReferenceMany() {
        def gcl = new GroovyClassLoader()
        def a = gcl.parseClass("""
class A {
    Long id, version
}
        """)
        def b = gcl.parseClass("""
class B {
    Long id, version
    Set a
    static hasMany = [a: A]
}
        """)
        def agdc = new DefaultGrailsDomainClass(a)
        def bgdc = new DefaultGrailsDomainClass(b)

        // Many, where other side is searchable
        def propertyMapping = factory.getGrailsDomainClassPropertyMapping(bgdc.getPropertyByName("a"), [agdc, bgdc])
        assert propertyMapping
        assert propertyMapping.reference
        assert propertyMapping.propertyType == a

        // Many, where other side is NOT searchable
        propertyMapping = factory.getGrailsDomainClassPropertyMapping(bgdc.getPropertyByName("a"), [bgdc])
        assert propertyMapping == null
    }

    void testGrailsReferenceMapProperty() {
        def gcl = new GroovyClassLoader()
        def a = gcl.parseClass("""
class A {
    Long id, version
}        """)
        def b = gcl.parseClass("""
class B {
    Long id, version
    Map refs
    static hasMany = [refs: A]
}        """)
        def agdc = new DefaultGrailsDomainClass(a)
        def bgdc = new DefaultGrailsDomainClass(b)

        // where other side is searchable
        def propertyMapping = factory.getGrailsDomainClassPropertyMapping(bgdc.getPropertyByName("refs"), [agdc, bgdc])
        assert propertyMapping
        assert propertyMapping.reference
        assert propertyMapping.propertyType == a

        // where other side is not searchable
        propertyMapping = factory.getGrailsDomainClassPropertyMapping(bgdc.getPropertyByName("refs"), [bgdc])
        assert propertyMapping == null
    }

    void testSearchableComponent() {
        def gcl = new GroovyClassLoader()
        def a = gcl.parseClass("""
class A {
    Long id, version
}        """)
        def b = gcl.parseClass("""
class B {
    Long id, version
    A a
    static embedded = ['a']
}        """)
        def agdc = new DefaultGrailsDomainClass(a)
        def bgdc = new DefaultGrailsDomainClass(b)

        // Embedded component searchable
        def propertyMapping = factory.getGrailsDomainClassPropertyMapping(bgdc.getPropertyByName("a"), [agdc, bgdc])
        assert propertyMapping
        assert propertyMapping.component
        assert propertyMapping.propertyType == a

        // Embedded component NOT searchable
        propertyMapping = factory.getGrailsDomainClassPropertyMapping(bgdc.getPropertyByName("a"), [bgdc])
        assert propertyMapping == null
    }
}