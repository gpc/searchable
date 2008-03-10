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
import org.codehaus.groovy.grails.plugins.searchable.TestUtils
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.component.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.compass.core.Compass
import org.codehaus.groovy.grails.commons.GrailsDomainConfigurationUtil
import org.codehaus.groovy.grails.commons.GrailsClass
import org.compass.core.spi.InternalCompass
import org.compass.core.mapping.CompassMapping
import org.compass.core.mapping.osem.ClassMapping
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

/**
* @author Maurice Nicholson
*/
class SearchableGrailsDomainClassCompassMappingUtilsTests extends GroovyTestCase {
    def domainClassMap
    def domainClasses

    void setUp() {
        domainClasses = TestUtils.getDomainClasses(Post, User, Comment, ComponentOwner, SearchableComp, Comp, NonSearchableComp)
        domainClassMap = [:]
        for (type in [Post, User, Comment, ComponentOwner, SearchableComp, Comp, NonSearchableComp]) {
            domainClassMap[type] = domainClasses.find { it.clazz == type }
        }
    }

    void tearDown() {
        domainClassMap = null
        domainClasses = null
    }

    void testIsRoot() {
        assert isRoot(Post, [Post, User, Comment])
        assert isRoot(User, [Post, User, Comment])
        assert isRoot(Comment, [Post, User, Comment])

        assert isRoot(ComponentOwner, [ComponentOwner, SearchableComp, Comp])
        assert isRoot(SearchableComp, [ComponentOwner, SearchableComp, Comp])
        assert isRoot(Comp, [ComponentOwner, SearchableComp, Comp]) == false
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
            CompassClassPropertyMapping.getComponentInstance("anotherProperty", Object),
            CompassClassPropertyMapping.getPropertyInstance("childOnlyProperty")
        ]
        parentMappings = [
            CompassClassPropertyMapping.getPropertyInstance("commonProperty"),
            CompassClassPropertyMapping.getPropertyInstance("someProperty", [boost: 1.3]),
            CompassClassPropertyMapping.getReferenceInstance("anotherProperty", Object)
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
            CompassClassPropertyMapping.getComponentInstance("anotherProperty", Object),
            CompassClassPropertyMapping.getReferenceInstance("anotherProperty", Object)
        ]
        parentMappings = [
            CompassClassPropertyMapping.getReferenceInstance("anotherProperty", Object)
        ]
        SearchableGrailsDomainClassCompassMappingUtils.mergePropertyMappings(childMappings, parentMappings)
        assert parentMappings.size() == 1
        assert parentMappings.find { it.propertyName == "anotherProperty" }.every { it.reference && it.attributes.size() == 0 }
        assert childMappings.size() == 2
        assert childMappings.find { it.propertyName == "anotherProperty" && it.component }.every { it.attributes.size() == 0 }
        assert childMappings.find { it.propertyName == "anotherProperty" && it.reference }.every { it.attributes.size() == 0 }
    }

    void testGetPolyMappingAliases() {
        def gcl = new GroovyClassLoader()
        def getClassMapping = { Class clazz ->
            ClassMapping classMapping = new ClassMapping()
            classMapping.clazz = clazz
            classMapping.alias = clazz.simpleName + "alias"
            classMapping.name = clazz.name
            classMapping
        }

        // with inheritance
        CompassMapping mapping = new CompassMapping()
        mapping.addMapping(getClassMapping(Parent.class))
        mapping.addMapping(getClassMapping(SearchableChildOne.class))
        mapping.addMapping(getClassMapping(SearchableChildTwo.class))
        mapping.addMapping(getClassMapping(SearchableGrandChild.class))
        def compass = [
            getMapping: {
                mapping
            }
        ] as InternalCompass

        DefaultGrailsApplication application = new DefaultGrailsApplication([Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild] as Class[], gcl)
        application.initialise()
        SearchableGrailsDomainClassCompassMappingUtils.getPolyMappingAliases(compass, Parent.class, application) as List == ["SearchableChildOnealias", "SearchableChildTwoalias", "Parentalias", "SearchableGrandChildalias"]
        SearchableGrailsDomainClassCompassMappingUtils.getPolyMappingAliases(compass, SearchableChildOne.class, application) as List == ["SearchableChildOnealias", "SearchableGrandChildalias"]
        SearchableGrailsDomainClassCompassMappingUtils.getPolyMappingAliases(compass, SearchableChildTwo.class, application) as List == ["SearchableChildTwoalias"]
        SearchableGrailsDomainClassCompassMappingUtils.getPolyMappingAliases(compass, SearchableGrandChild.class, application) as List == ["SearchableGrandChildalias"]

        // without inheritance
        mapping = new CompassMapping()
        mapping.addMapping(getClassMapping(Post.class))
        mapping.addMapping(getClassMapping(Comment.class))
        mapping.addMapping(getClassMapping(User.class))
        compass = [
            getMapping: {
                mapping
            }
        ] as InternalCompass

        application = new DefaultGrailsApplication([Post, Comment, User] as Class[], gcl)
        application.initialise()
        SearchableGrailsDomainClassCompassMappingUtils.getPolyMappingAliases(compass, Post.class, application) as List == ["Postalias"]
        SearchableGrailsDomainClassCompassMappingUtils.getPolyMappingAliases(compass, User.class, application) as List == ["Useralias"]
        SearchableGrailsDomainClassCompassMappingUtils.getPolyMappingAliases(compass, Comment.class, application) as List == ["Commentalias"]
    }
}