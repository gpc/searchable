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

/**
* @author Maurice Nicholson
*/
class SearchableGrailsDomainClassCompassMappingUtilsTests extends GroovyTestCase {
    def domainClassMap

    void setUp() {
        domainClassMap = [:]
        for (type in [Post, User, Comment, ComponentOwner, SearchableComp]) {
            domainClassMap[type] = new DefaultGrailsDomainClass(type)
        }
    }

    void tearDown() {
        domainClassMap = null
    }

    void testIsRoot() {
        assert isRoot(Post, [Post, User, Comment])
        assert isRoot(User, [Post, User, Comment])
        assert isRoot(Comment, [Post, User, Comment])

        assert isRoot(ComponentOwner, [ComponentOwner, SearchableComp])
        assert isRoot(SearchableComp, [ComponentOwner, SearchableComp]) == false
    }

    private isRoot(type, searchableClasses) {
        SearchableGrailsDomainClassCompassMappingUtils.isRoot(domainClassMap[type], searchableClasses.collect { domainClassMap[it] })
    }

    void testMergePropertyMappings() {
        def childMappings = []
        def parentMappings = []
        SearchableGrailsDomainClassCompassMappingUtils.mergePropertyMappings(childMappings, parentMappings)
        assert childMappings == []
        assert parentMappings == []

        parentMappings = [
            CompassClassPropertyMapping.getPropertyInstance("someProperty", [boost: 1.3])
        ]
        SearchableGrailsDomainClassCompassMappingUtils.mergePropertyMappings(childMappings, parentMappings)
        assert childMappings.size() == 1
        assert parentMappings.size() == 1
        assert childMappings == parentMappings

        childMappings = [
            CompassClassPropertyMapping.getPropertyInstance("someProperty"),
            CompassClassPropertyMapping.getComponentInstance("anotherProperty"),
            CompassClassPropertyMapping.getPropertyInstance("childOnlyProperty")
        ]
        parentMappings = [
            CompassClassPropertyMapping.getPropertyInstance("commonProperty"),
            CompassClassPropertyMapping.getPropertyInstance("someProperty", [boost: 1.3]),
            CompassClassPropertyMapping.getReferenceInstance("anotherProperty")
        ]
        SearchableGrailsDomainClassCompassMappingUtils.mergePropertyMappings(childMappings, parentMappings)
        assert parentMappings.size() == 3
        assert parentMappings.find { it.propertyName == "commonProperty" }.every { it.property && it.attributes.size() == 0 }
        assert parentMappings.find { it.propertyName == "someProperty" }.every { it.property && it.attributes == [boost: 1.3] }
        assert parentMappings.find { it.propertyName == "anotherProperty" }.every { it.reference && it.attributes.size() == 0 }
        assert childMappings.size() == 4
        assert childMappings.find { it.propertyName == "commonProperty" }.every { it.property && it.attributes.size() == 0 }
        assert childMappings.find { it.propertyName == "someProperty" }.every { it.property && it.attributes.size() == 0 }
        assert childMappings.find { it.propertyName == "anotherProperty" }.every { it.component && it.attributes.size() == 0 }
        assert childMappings.find { it.propertyName == "childOnlyProperty" }.every { it.property && it.attributes.size() == 0 }

        // mutiple mappings for a single property
        childMappings = [
            CompassClassPropertyMapping.getComponentInstance("anotherProperty"),
            CompassClassPropertyMapping.getReferenceInstance("anotherProperty")
        ]
        parentMappings = [
            CompassClassPropertyMapping.getReferenceInstance("anotherProperty")
        ]
        SearchableGrailsDomainClassCompassMappingUtils.mergePropertyMappings(childMappings, parentMappings)
        assert parentMappings.size() == 1
        assert parentMappings.find { it.propertyName == "anotherProperty" }.every { it.reference && it.attributes.size() == 0 }
        assert childMappings.size() == 2
        assert childMappings.find { it.propertyName == "anotherProperty" && it.component }.every { it.attributes.size() == 0 }
        assert childMappings.find { it.propertyName == "anotherProperty" && it.reference }.every { it.attributes.size() == 0 }
    }
}