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

/**
*
*
* @author Maurice Nicholson
*/
class GrailsDomainClassPropertyMappingStrategyFactoryTests extends GroovyTestCase {
    def factory
    def domainClassMap

    void setUp() {
        domainClassMap = [:]
        for (type in [Post, Comment, User, ComponentOwner, SearchableComp, StringMapOwner, ReferenceMapOwner, MapReferee]) {
            domainClassMap[type] = new DefaultGrailsDomainClass(type)
        }
        factory = new GrailsDomainClassPropertyMappingStrategyFactory()
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
        def strategy

        strategy = getStrategy(Post, "post", [Post, Comment, User])
        assert strategy
        assert strategy.mapping == [property: true]
        assert isType(strategy, "property")

        strategy = getStrategy(Post, "createdAt", [Post, Comment, User])
        assert strategy
        assert strategy.mapping == [property: true]
        assert isType(strategy, "property")

        // with default format
        factory.defaultFormats = [(Date): 'xyzabc123'] // any string
        strategy = getStrategy(Post, "createdAt", [Post, Comment, User])
        assert strategy
        assert strategy.mapping == [property: [format: 'xyzabc123']]
        assert isType(strategy, "property")
    }

    void testGrailsStringMapSearchableProperty() {
        def strategy

        strategy = getStrategy(StringMapOwner, "myStringMap", [StringMapOwner])
        assert strategy
        assert strategy.mapping == [property: [converter: 'stringmap', managedId: false]]
        assert isType(strategy, "property")
    }

    void testSearchableReference() {
        def strategy

        // One, where other side is searchable
        strategy = getStrategy(Post, "author", [Post, Comment, User])
        assert strategy
        assert strategy.mapping == [reference: [refAlias: 'User']]
        assert isType(strategy, "reference")

        // One, where other side is NOT searchable
        strategy = getStrategy(Post, "author", [Post, Comment])
        assert strategy == null

        // Many, where other side is searchable
        strategy = getStrategy(Post, "comments", [Post, Comment, User])
        assert strategy
        assert strategy.mapping == [reference: [refAlias: 'Comment']]
        assert isType(strategy, "reference")

        // Many, where other side is NOT searchable
        strategy = getStrategy(Post, "comments", [Post, User])
        assert strategy == null
    }

    void testGrailsReferenceMapProperty() {
        def strategy

        // where other side is searchable
        strategy = getStrategy(ReferenceMapOwner, "referenceMap", [ReferenceMapOwner, MapReferee])
        assert strategy
        assert strategy.mapping == [reference: [refAlias: 'MapReferee']]
        assert isType(strategy, "reference")

        // where other side is searchable
        strategy = getStrategy(ReferenceMapOwner, "referenceMap", [ReferenceMapOwner])
        assert strategy == null
    }

    void testSearchableComponent() {
        def strategy

        // Embedded component searchable
        strategy = getStrategy(ComponentOwner, "searchableCompOne", [ComponentOwner, SearchableComp])
        assert strategy
        assert strategy.mapping == [component: [refAlias: 'SearchableComp']]
        assert isType(strategy, "component")

        // Embedded component NOT searchable
        strategy = getStrategy(ComponentOwner, "comp", [ComponentOwner, SearchableComp])
        assert strategy == null
    }

    private getStrategy(type, name, searchableClasses) {
        factory.getGrailsDomainClassPropertyMappingStrategy(domainClassMap[type].getPropertyByName(name), searchableClasses.collect { domainClassMap[it]})
    }

    private isType(strategy, name) {
        for (typeName in ['property', 'reference', 'component']) {
            if (typeName == name) {
                assert strategy.properties[typeName] == true
            } else {
                assert strategy.properties[typeName] == false
            }
        }
        return true
    }
}