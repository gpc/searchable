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
import org.codehaus.groovy.grails.plugins.searchable.compass.converter.DefaultCompassConverterLookupHelper

/**
* @author Maurice Nicholson
*/
class SimpleSearchableGrailsDomainClassCompassMappingDescriptionProviderTests extends GroovyTestCase {
    def provider
    def domainClassMap
    def currentSearchableValues

    void setUp() {
        currentSearchableValues = [(Post): Post.searchable, (Comment): Comment.searchable, (User): User.searchable]
        domainClassMap = [:]
        for (clazz in [Post, Comment, User, Comp, SearchableComp, ComponentOwner]) {
            domainClassMap[clazz] = new DefaultGrailsDomainClass(clazz)
        }
        provider = new SimpleSearchableGrailsDomainClassCompassMappingDescriptionProvider(
            domainClassPropertyMappingStrategyFactory: new GrailsDomainClassPropertyMappingStrategyFactory(
                converterLookupHelper: new DefaultCompassConverterLookupHelper(converterLookup: new CompassConfiguration().setConnection("ram://dummy").buildCompass().converterLookup)
            )
        )
    }

    void tearDown() {
        currentSearchableValues.each { c, v -> c.searchable = v }
        currentSearchableValues = null
        domainClassMap = null
        provider = null
    }

    void testHandlesSearchableValue() {
        assert provider.handlesSearchableValue([:])
        assert provider.handlesSearchableValue(true)
        assert provider.handlesSearchableValue(false)
        assert provider.handlesSearchableValue(null) == false
        assert provider.handlesSearchableValue({ -> }) == false
    }

    void testGetCompassMappingDescription() {
        // TODO refactor to sanity check tests
        assert domainClassMap[Post].getPropertyByName("title").type == String
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

        // When "searchable = true" across all domain classes
        def desc = getMapping(Post, [Comment, Post, User], true)
        assert desc.mappedClass == Post
        assert desc.root == true
        assert desc.properties == [version: [property: true], title: [property: true], post: [property: true], createdAt: [property: true], comments: [reference: [refAlias: 'Comment']], author: [reference: [refAlias: 'User']]]

        desc = getMapping(Comment, [Comment, Post, User], true)
        assert desc.mappedClass == Comment
        assert desc.root == true
        assert desc.properties == [version: [property: true], summary: [property: true], comment: [property: true], createdAt: [property: true], post: [reference: [refAlias: 'Post']]]

        // When "searchable = true" across only *some* domain classes
        // in this case a "one" relationship from the searchable class (author: User)
        desc = getMapping(Post, [Comment, Post], true)
        assert desc.mappedClass == Post
        assert desc.root == true
        assert desc.properties == [version: [property: true], title: [property: true], post: [property: true], createdAt: [property: true], comments: [reference: [refAlias: 'Comment']]]

        // and here a "many" relationship from the searchable class (comments: Comment)
        desc = getMapping(Post, [Post], true)
        assert desc.mappedClass == Post
        assert desc.root == true
        assert desc.properties == [version: [property: true], title: [property: true], post: [property: true], createdAt: [property: true]]

        // NOT mapping password!
        desc = getMapping(User, [Comment, Post, User], true, ["password"])
        assert desc.mappedClass == User
        assert desc.root == true
        assert desc.properties == [version: [property: true], username: [property: true], email: [property: true], createdAt: [property: true], posts: [reference: [refAlias: 'Post']]]

        // Possible to override this behavoir with other property excludes
        desc = getMapping(User, [Comment, Post, User], true, ["createdAt", "version"])
        assert desc.mappedClass == User
        assert desc.root == true
        assert desc.properties == [username: [property: true], password: [property: true], email: [property: true], posts: [reference: [refAlias: 'Post']]]

        // Components
        desc = getMapping(ComponentOwner, [ComponentOwner, SearchableComp], true)
        assert desc.mappedClass == ComponentOwner
        assert desc.root == true
        assert desc.properties == [version: [property: true], componentOwnerName: [property: true], searchableCompOne: [component: [refAlias: 'SearchableComp']], searchableCompTwo: [component: [refAlias: 'SearchableComp']]]

        // ...other side of the relationship
        desc = getMapping(SearchableComp, [ComponentOwner, SearchableComp], true)
        assert desc.root == false
        assert desc.mappedClass == SearchableComp
        assert desc.properties == [version: [property: true], searchableCompName: [property: true]]

        // When searchable = false
        desc = getMapping(Post, [Comment], false)
        assert desc == null

        desc = getMapping(Comment, [Post], false)
        assert desc == null

        desc = getMapping(User, [User], false)
        assert desc == null

        // When searchable = [only: ... ]
        desc = getMapping(Post, [Post, Comment, User], [only: ["title", "post"]])
        assert desc.mappedClass == Post
        assert desc.root == true
        assert desc.properties == [title: [property: true], post: [property: true]]

        desc = getMapping(Post, [Post, Comment, User], [only: "comments"])
        assert desc.mappedClass == Post
        assert desc.root == true
        assert desc.properties == [comments: [reference: [refAlias: 'Comment']]]

        desc = getMapping(User, [Post, Comment, User], [only: ["username", "email"]])
        assert desc.mappedClass == User
        assert desc.root == true
        assert desc.properties == [username: [property: true], email: [property: true]]

        // "only" overrides any excluded properties
        desc = getMapping(User, [Post, Comment, User], [only: ["username", "password"]], ["password"])
        assert desc.mappedClass == User
        assert desc.root == true
        assert desc.properties == [username: [property: true], password: [property: true]]

        // When searchable = [except: ...]
        desc = getMapping(Comment, [Comment, Post, User], [except: "comment"])
        assert desc.properties.keySet() == ["version", "summary", "post", "createdAt"] as Set

        desc = getMapping(Comment, [Comment, Post, User], [except: ["comment", "summary"]])
        assert desc.properties.keySet() == ["version", "post", "createdAt"] as Set

        // "except" overrides any excluded properties
        desc = getMapping(User, [Comment, Post, User], [except: "createdAt"], ["password"])
        assert desc.properties.keySet() == ["version", "username", "email", "password", "posts"] as Set

        // with default format
        provider.domainClassPropertyMappingStrategyFactory.defaultFormats = [(Date): 'yyyy-MMM-dd, HH:mm', (Long): '0000']
        desc = getMapping(Comment, [Comment, Post, User], true, [])
        assert desc.properties == [version: [property: [format: '0000']], summary: [property: true], comment: [property: true], createdAt: [property: [format: 'yyyy-MMM-dd, HH:mm']], post: [reference: [refAlias: 'Post']]]
    }

    def getMapping(clazz, searchableClasses, searchableValue, excludedProperties = []) {
        provider.getCompassMappingDescription(domainClassMap[clazz], searchableClasses.collect { domainClassMap[it] }, searchableValue, excludedProperties)
    }
}
