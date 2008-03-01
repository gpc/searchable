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
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.component.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.stringmap.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.referencemap.*
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils

/**
*
*
* @author Maurice Nicholson
*/
class SearchableGrailsDomainClassPropertyMappingFactoryTests extends GroovyTestCase {
    def factory
    def domainClassMap

    void setUp() {
        domainClassMap = [:]
        for (type in [Post, Comment, User, ComponentOwner, SearchableComp, StringMapOwner, ReferenceMapOwner, MapReferee]) {
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

    void testSearchableProperty() {
        def propertyMapping

        propertyMapping = getPropertyMapping(Post, "post", [Post, Comment, User])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes.size() == 0

        propertyMapping = getPropertyMapping(Post, "createdAt", [Post, Comment, User])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes.size() == 0

        // with default format
        factory.defaultFormats = [(Date): 'xyzabc123'] // any string
        propertyMapping = getPropertyMapping(Post, "createdAt", [Post, Comment, User])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes == [format: 'xyzabc123']
    }

    void testGrailsStringMapSearchableProperty() {
        def propertyMapping

        propertyMapping = getPropertyMapping(StringMapOwner, "myStringMap", [StringMapOwner])
        assert propertyMapping
        assert propertyMapping.property
        assert propertyMapping.attributes == [converter: 'stringmap', managedId: false]
    }

    void testSearchableReference() {
        def propertyMapping

        // One, where other side is searchable
        propertyMapping = getPropertyMapping(Post, "author", [Post, Comment, User])
        assert propertyMapping
        assert propertyMapping.reference
        assert propertyMapping.propertyType == User

        // One, where other side is NOT searchable
        propertyMapping = getPropertyMapping(Post, "author", [Post, Comment])
        assert propertyMapping == null

        // Many, where other side is searchable
        propertyMapping = getPropertyMapping(Post, "comments", [Post, Comment, User])
        assert propertyMapping
        assert propertyMapping.reference
        assert propertyMapping.propertyType == Comment

        // Many, where other side is NOT searchable
        propertyMapping = getPropertyMapping(Post, "comments", [Post, User])
        assert propertyMapping == null
    }

    void testGrailsReferenceMapProperty() {
        def propertyMapping

        // where other side is searchable
        propertyMapping = getPropertyMapping(ReferenceMapOwner, "referenceMap", [ReferenceMapOwner, MapReferee])
        assert propertyMapping
        assert propertyMapping.reference
        assert propertyMapping.propertyType == MapReferee

        // where other side is searchable
        propertyMapping = getPropertyMapping(ReferenceMapOwner, "referenceMap", [ReferenceMapOwner])
        assert propertyMapping == null
    }

    void testSearchableComponent() {
        def propertyMapping

        // Embedded component searchable
        propertyMapping = getPropertyMapping(ComponentOwner, "searchableCompOne", [ComponentOwner, SearchableComp])
        assert propertyMapping
        assert propertyMapping.component
        assert propertyMapping.propertyType == SearchableComp

        // Embedded component NOT searchable
        propertyMapping = getPropertyMapping(ComponentOwner, "comp", [ComponentOwner, SearchableComp])
        assert propertyMapping == null
    }

    private getPropertyMapping(type, name, searchableClasses) {
        factory.getGrailsDomainClassPropertyMapping(domainClassMap[type].getPropertyByName(name), searchableClasses.collect { domainClassMap[it]})
    }
}