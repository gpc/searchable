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

import org.compass.core.config.*
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.component.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.*
import org.codehaus.groovy.grails.plugins.searchable.compass.converter.DefaultCompassConverterLookupHelper
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils

/**
* @author Maurice Nicholson
*/
class SimpleSearchableGrailsDomainClassCompassClassMapperTests extends GroovyTestCase {
    def classMapper
    def currentSearchableValues

    void setUp() {
        currentSearchableValues = [:]
        for (c in [Post, Comment, User, Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild]) {
            currentSearchableValues[c] = c.searchable
        }

        def parent = SearchableGrailsDomainClassCompassClassMapperFactory.getDefaultSearchableGrailsDomainClassCompassClassMapper([], [:])
        classMapper = parent.classMappers.find { it instanceof SimpleSearchableGrailsDomainClassCompassClassMapper }
    }

    void tearDown() {
        currentSearchableValues.each { c, v -> c.searchable = v }
        currentSearchableValues = null
        classMapper = null
    }

    void testHandlesSearchableValue() {
        assert classMapper.handlesSearchableValue([:])
        assert classMapper.handlesSearchableValue(true)
        assert classMapper.handlesSearchableValue(false)
        assert classMapper.handlesSearchableValue(null) == false
        assert classMapper.handlesSearchableValue({ -> }) == false
    }

    void testGetCompassClassMapping() {
        // TODO refactor to sanity check tests
/*        assert domainClassMap[Post].getPropertyByName("title").type == String
        assert domainClassMap[Post].getPropertyByName("post").type == String
        assert domainClassMap[Post].getPropertyByName("createdAt").type == Date
        assert domainClassMap[Post].getPropertyByName("author").type == User
        assert domainClassMap[Post].getRelatedClassType("comments") == Comment

        assert domainClassMap[Comment].getPropertyByName("post").type == Post
        assert domainClassMap[Comment].getPropertyByName("createdAt").type == Date

        assert domainClassMap[User].getRelatedClassType("posts") == Post

        assert domainClassMap[ComponentOwner].getPropertyByName("searchableCompOne").type == SearchableComp
        assert domainClassMap[ComponentOwner].getPropertyByName("searchableCompOne").embedded
        assert domainClassMap[ComponentOwner].getPropertyByName("searchableCompTwo").type == SearchableComp
        assert domainClassMap[ComponentOwner].getPropertyByName("searchableCompTwo").embedded
        assert domainClassMap[ComponentOwner].getPropertyByName("comp").type == Comp
        assert domainClassMap[ComponentOwner].getPropertyByName("comp").embedded
*/
        def classMapping
        def propertyMapping

        // When "searchable = true" across all domain classes
        classMapping = getClassMapping(Post, [Comment, Post, User], true)
        assert classMapping.mappedClass == Post
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 6
        assert classMapping.propertyMappings.findAll { it.propertyName in ['version', 'title', 'post', 'createdAt'] }.every { it.property && it.attributes.size() == 0 }
        assert classMapping.propertyMappings.find { it.propertyName == 'comments' }.every { it.reference && it.attributes == [refAlias: CompassMappingUtils.getDefaultAlias(Comment)] }
        assert classMapping.propertyMappings.find { it.propertyName == 'author' }.every { it.reference && it.attributes == [refAlias: CompassMappingUtils.getDefaultAlias(User)] }

        classMapping = getClassMapping(Comment, [Comment, Post, User], true)
        assert classMapping.mappedClass == Comment
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 5
        assert classMapping.propertyMappings.findAll { it.propertyName in ['version', 'summary', 'comment', 'createdAt'] }.every { it.property && it.attributes.size() == 0 }
        propertyMapping = classMapping.propertyMappings.find { it.propertyName == 'post' }
        assert propertyMapping.reference
        assert propertyMapping.attributes == [refAlias: CompassMappingUtils.getDefaultAlias(Post)]

        // When "searchable = true" across only *some* domain classes
        // in this case a "one" relationship from the searchable class (author: User)
        classMapping = getClassMapping(Post, [Comment, Post], true)
        assert classMapping.mappedClass == Post
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 5
        assert classMapping.propertyMappings.findAll { it.propertyName in ['version', 'title', 'post', 'createdAt'] }.every { it.property && it.attributes.size() == 0 }
        propertyMapping = classMapping.propertyMappings.find { it.propertyName == 'comments' }
        assert propertyMapping.reference
        assert propertyMapping.attributes == [refAlias: CompassMappingUtils.getDefaultAlias(Comment)]

        // and here a "many" relationship from the searchable class (comments: Comment)
        classMapping = getClassMapping(Post, [Post], true)
        assert classMapping.mappedClass == Post
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 4
        assert classMapping.propertyMappings.findAll { it.propertyName in ['version', 'title', 'post', 'createdAt'] }.every { it.property && it.attributes.size() == 0 }

        // NOT mapping password!
        classMapping = getClassMapping(User, [Comment, Post, User], true, ["password"])
        assert classMapping.mappedClass == User
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 5
        assert classMapping.propertyMappings.findAll { it.propertyName in ['version', 'username', 'email', 'createdAt'] }.every { it.property && it.attributes.size() == 0 }
        propertyMapping = classMapping.propertyMappings.find { it.propertyName == 'posts' }
        assert propertyMapping.reference
        assert propertyMapping.attributes == [refAlias: CompassMappingUtils.getDefaultAlias(Post)]

        // Possible to override this behavoir with other property excludes
        classMapping = getClassMapping(User, [Comment, Post, User], true, ["createdAt", "version"])
        assert classMapping.mappedClass == User
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 4
        assert classMapping.propertyMappings.findAll { it.propertyName in ['username', 'password', 'email'] }.every { it.property && it.attributes.size() == 0 }
        propertyMapping = classMapping.propertyMappings.find { it.propertyName == 'posts' }
        assert propertyMapping.reference
        assert propertyMapping.attributes == [refAlias: CompassMappingUtils.getDefaultAlias(Post)]

        // Components
        classMapping = getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], true)
        assert classMapping.mappedClass == ComponentOwner
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 4
        assert classMapping.propertyMappings.findAll { it.propertyName in ['version', 'componentOwnerName'] }.every { it.property && it.attributes.size() == 0 }
        propertyMapping = classMapping.propertyMappings.find { it.propertyName == 'searchableCompOne' }
        assert propertyMapping.component
        assert propertyMapping.attributes == [refAlias: CompassMappingUtils.getDefaultAlias(SearchableComp)]
        propertyMapping = classMapping.propertyMappings.find { it.propertyName == 'searchableCompTwo' }
        assert propertyMapping.component
        assert propertyMapping.attributes == [refAlias: CompassMappingUtils.getDefaultAlias(SearchableComp)]

        // ...other side of the relationship
        classMapping = getClassMapping(SearchableComp, [ComponentOwner, SearchableComp], true)
        assert classMapping.root == false
        assert classMapping.mappedClass == SearchableComp
        assert classMapping.propertyMappings.size() == 2
        assert classMapping.propertyMappings.findAll { it.propertyName in ['version', 'componentOwnerName'] }.every { it.property && it.attributes.size() == 0 }

        // When searchable = false
        classMapping = getClassMapping(Post, [Comment, Post], false)
        assert classMapping == null

        classMapping = getClassMapping(Comment, [Post, Comment], false)
        assert classMapping == null

        classMapping = getClassMapping(User, [User], false)
        assert classMapping == null

        // When searchable = [only: ... ]
        classMapping = getClassMapping(Post, [Post, Comment, User], [only: ["title", "post"]])
        assert classMapping.mappedClass == Post
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 2
        assert classMapping.propertyMappings.findAll { it.propertyName in ['title', 'post'] }.every { it.property && it.attributes.size() == 0 }

        classMapping = getClassMapping(Post, [Post, Comment, User], [only: "comments"])
        assert classMapping.mappedClass == Post
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 1
        assert classMapping.propertyMappings[0].propertyName == 'comments'
        assert classMapping.propertyMappings[0].attributes == [refAlias: CompassMappingUtils.getDefaultAlias(Comment)]

        classMapping = getClassMapping(User, [Post, Comment, User], [only: ["username", "email"]])
        assert classMapping.mappedClass == User
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 2
        assert classMapping.propertyMappings.findAll { it.propertyName in ['username', 'email'] }.every { it.property && it.attributes.size() == 0 }

        // "only" overrides any excluded properties
        classMapping = getClassMapping(User, [Post, Comment, User], [only: ["username", "password"]], ["password"])
        assert classMapping.mappedClass == User
        assert classMapping.root == true
        assert classMapping.propertyMappings.size() == 2
        assert classMapping.propertyMappings.findAll { it.propertyName in ['username', 'password'] }.every { it.property && it.attributes.size() == 0 }

        // When searchable = [except: ...]
        classMapping = getClassMapping(Comment, [Comment, Post, User], [except: "comment"])
        assert classMapping.propertyMappings*.propertyName as Set == ["version", "summary", "post", "createdAt"] as Set

        classMapping = getClassMapping(Comment, [Comment, Post, User], [except: ["comment", "summary"]])
        assert classMapping.propertyMappings*.propertyName as Set == ["version", "post", "createdAt"] as Set

        // "except" overrides any excluded properties
        classMapping = getClassMapping(User, [Comment, Post, User], [except: "createdAt"], ["password"])
        assert classMapping.propertyMappings*.propertyName as Set == ["version", "username", "email", "password", "posts"] as Set

        // with default format
        classMapper.domainClassPropertyMappingStrategyFactory.defaultFormats = [(Date): 'yyyy-MMM-dd, HH:mm', (Long): '0000']
        classMapping = getClassMapping(Comment, [Comment, Post, User], true, [])
        assert classMapping.propertyMappings.find { it.propertyName == 'version' }.attributes == [format: '0000']
        assert classMapping.propertyMappings.findAll { it.propertyName in ['summary', 'comment'] }.every { it.attributes.size() == 0 }
        assert classMapping.propertyMappings.find { it.propertyName == 'createdAt' }.attributes == [format: 'yyyy-MMM-dd, HH:mm']
    }

    void testGetCompassClassMappingWithInheritedMappings() {
        CompassClassMapping mapping

        Parent.searchable = true
        mapping = getClassMapping(Parent, [Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild])
        assert mapping.getPropertyMappings().find { it.propertyName == 'commonProperty' }.every { it.property && it.attributes.size() == 0 }

        // SearchableChildTwo overrides Parent's "commonProperty(boost: 1.5)" with default mapping
        SearchableChildTwo.searchable = true
        Parent.searchable = {
            commonProperty(boost: 1.5)
        }
        mapping = getClassMapping(SearchableChildTwo, [Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild])
        assert mapping.getPropertyMappings().find { it.propertyName == 'commonProperty' }.every { it.property && it.attributes.size() == 0 }
        assert mapping.getPropertyMappings().find { it.propertyName == 'childTwoProperty' }.every { it.property && it.attributes.size() == 0 }
    }

    CompassClassMapping getClassMapping(clazz, searchableClazzes, searchableValue) {
        getClassMapping(clazz, searchableClazzes, searchableValue, [])
    }

    CompassClassMapping getClassMapping(clazz, searchableClazzes, searchableValue, excludedProperties = []) {
        def domainClasses = getDomainClasses(searchableClazzes)
        def domainClass = domainClasses.find { it.clazz == clazz }
        classMapper.getCompassClassMapping(domainClass, domainClasses, searchableValue, excludedProperties)
    }

    CompassClassMapping getClassMapping(clazz, searchableClazzes) {
        def domainClasses = getDomainClasses(searchableClazzes)
        def gdc = domainClasses.find { it.clazz == clazz}
        classMapper.getCompassClassMapping(gdc, domainClasses)
    }

    def getDomainClasses(clazzes) {
        def domainClasses = []
        for (clazz in clazzes) {
            def DefaultGrailsDomainClass domainClass = new DefaultGrailsDomainClass(clazz)
            domainClasses << domainClass
        }
        configureDomainClassRelationships(domainClasses)
        domainClasses
    }

    def configureDomainClassRelationships(domainClasses) {
        def domainClassMap = getDomainClassMap(domainClasses)
        GrailsDomainConfigurationUtil.configureDomainClassRelationships(domainClasses as GrailsClass[], domainClassMap)
    }

    def getDomainClassMap(domainClasses) {
        def domainClassMap = [:]
        for (dc in domainClasses) {
            domainClassMap[dc.clazz.name] = dc
        }
        domainClassMap
    }
}
