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

import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClass

/**
* @author Maurice Nicholson
*/
class CompositeSearchableGrailsDomainClassCompassClassMapperTests extends GroovyTestCase {
    SearchableGrailsDomainClassCompassClassMapper classMapper

    void setUp() {
        classMapper = new CompositeSearchableGrailsDomainClassCompassClassMapper()
    }

    void tearDown() {
        classMapper = null
    }

    void testHandlesSearchableValue() {
        // When not handling
        classMapper.classMappers = [
            [handlesSearchableValue: {false}] as SearchableGrailsDomainClassCompassClassMapper,
            [handlesSearchableValue: {false}] as SearchableGrailsDomainClassCompassClassMapper
        ]
        assert !classMapper.handlesSearchableValue(true) // value is unimportant

        // When handling
        classMapper.classMappers = [
            [handlesSearchableValue: {false}] as SearchableGrailsDomainClassCompassClassMapper,
            [handlesSearchableValue: {true}] as SearchableGrailsDomainClassCompassClassMapper
        ]
        assert classMapper.handlesSearchableValue(true) // value is unimportant
    }

    void testGetCompassClassPropertyMappings() {
        // This method is used "internally" so we assume that there is a handler available
        def udc = new DefaultGrailsDomainClass(User.class)
        classMapper.classMappers = [
            [handlesSearchableValue: {false}] as SearchableGrailsDomainClassCompassClassMapper,
            [   handlesSearchableValue: {true},
                getCompassClassPropertyMappings: { Object[] args -> return ["it", "was", "called"] }
            ] as SearchableGrailsDomainClassCompassClassMapper
        ]
        assert classMapper.getCompassClassPropertyMappings(udc, [udc], true, []) == ["it", "was", "called"]
    }

    void testGetCompassClassMapping() {
        def udc = new DefaultGrailsDomainClass(User.class)
        def pdc = new DefaultGrailsDomainClass(Comment.class)
        def cdc = new DefaultGrailsDomainClass(Post.class)
        def searchableGrailsDomainClasses = [udc, pdc, cdc]
        def excludedProperties = ['createdAt']
        def searchableValue = Post.searchable

        classMapper.defaultExcludedProperties = excludedProperties
        classMapper.classMappers = [
            [handlesSearchableValue: {false}] as SearchableGrailsDomainClassCompassClassMapper,
            [   handlesSearchableValue: {true},
                getCompassClassMapping: { grailsDomainClassArg, searchableGrailsDomainClassesArg, searchableValueArg, excludedPropertiesArg ->
                    assert grailsDomainClassArg == pdc
                    assert searchableGrailsDomainClassesArg == searchableGrailsDomainClasses
                    assert searchableValueArg == searchableValue
                    assert excludedPropertiesArg == excludedProperties
                    new CompassClassMapping(mappedClass: Post)
                }
            ] as SearchableGrailsDomainClassCompassClassMapper
        ]
        def classMapping = classMapper.getCompassClassMapping(pdc, searchableGrailsDomainClasses)
        assert classMapping.mappedClass == Post
    }

    void testGetCompassClassMappingWithValueAndExcludedProperties() {
        def udc = new DefaultGrailsDomainClass(User.class)
        def pdc = new DefaultGrailsDomainClass(Comment.class)
        def cdc = new DefaultGrailsDomainClass(Post.class)
        def searchableGrailsDomainClasses = [udc, pdc, cdc]
        def excludedProperties = ['createdAt']

        classMapper.classMappers = [
            [handlesSearchableValue: {false}] as SearchableGrailsDomainClassCompassClassMapper,
            [   handlesSearchableValue: {true},
                getCompassClassMapping: { grailsDomainClassArg, searchableGrailsDomainClassesArg, searchableValueArg, excludedPropertiesArg ->
                    assert grailsDomainClassArg == pdc
                    assert searchableGrailsDomainClassesArg == searchableGrailsDomainClasses
                    assert searchableValueArg == 'testvalue'
                    assert excludedPropertiesArg == excludedProperties
                    new CompassClassMapping(mappedClass: Post)
                }
            ] as SearchableGrailsDomainClassCompassClassMapper
        ]
        def classMapping = classMapper.getCompassClassMapping(pdc, searchableGrailsDomainClasses, "testvalue", excludedProperties)
        assert classMapping.mappedClass == Post
    }
}