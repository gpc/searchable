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
package org.codehaus.groovy.grails.plugins.searchable

import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*

/**
 * @author Maurice Nicholson
 */
class SearchableUtilsTests extends GroovyTestCase {
    def currentSearchableValues
    def postDc
    def commentDc
    def userDc

    void setUp() {
        currentSearchableValues = [(Post): Post.searchable, (Comment): Comment.searchable, (User): User.searchable]
        postDc = new DefaultGrailsDomainClass(Post)
        commentDc = new DefaultGrailsDomainClass(Comment)
        userDc = new DefaultGrailsDomainClass(User)
    }

    void tearDown() {
        currentSearchableValues.each { c, v -> c.searchable = v }
        currentSearchableValues = null
        postDc = null
        commentDc = null
        userDc = null
    }

    void testIsSearchableGrailsDomainClassArg() {
        // when true
        assert SearchableUtils.isSearchable(postDc)
        // when false
        Post.searchable = false
        assert SearchableUtils.isSearchable(postDc) == false
        // when closure (any closure)
        Post.searchable = { -> }
        assert SearchableUtils.isSearchable(postDc)
    }

    void testIsSearchableClassArg() {
        // when true
        assert SearchableUtils.isSearchable(Post)
        // when false
        Post.searchable = false
        assert SearchableUtils.isSearchable(Post) == false
        // when closure (any closure)
        Post.searchable = { -> }
        assert SearchableUtils.isSearchable(Post)
    }

    void testGetSearchablePropertyAssociatedClassPropertyArg() {
        // when other side is searchable
        // one
        assert SearchableUtils.getSearchablePropertyAssociatedClass(postDc.getPropertyByName("author"), [userDc, postDc]) == User
        // many
        assert SearchableUtils.getSearchablePropertyAssociatedClass(userDc.getPropertyByName("posts"), [userDc, postDc]) == Post

        // when other side NOT searchable
        // one
        assert SearchableUtils.getSearchablePropertyAssociatedClass(postDc.getPropertyByName("author"), [postDc]) == null
        // many
        assert SearchableUtils.getSearchablePropertyAssociatedClass(userDc.getPropertyByName("posts"), [userDc]) == null
    }

    void testGetSearchablePropertyAssociatedClassPropertyNameArg() {
        // when other side is searchable
        // one
        assert SearchableUtils.getSearchablePropertyAssociatedClass(postDc, "author", [userDc, postDc]) == User
        // many
        assert SearchableUtils.getSearchablePropertyAssociatedClass(userDc, "posts", [userDc, postDc]) == Post

        // when other side NOT searchable
        // one
        assert SearchableUtils.getSearchablePropertyAssociatedClass(postDc, "author", [postDc]) == null
        // many
        assert SearchableUtils.getSearchablePropertyAssociatedClass(userDc, "posts", [userDc]) == null
    }

    void testIsIncludedProperty() {
        assert SearchableUtils.isIncludedProperty("firstname", true)
        assert SearchableUtils.isIncludedProperty("password", true)
        assert !SearchableUtils.isIncludedProperty("password", [except: "password"])
        assert !SearchableUtils.isIncludedProperty("password", [except: ["password", "securityNumber"]])
        assert SearchableUtils.isIncludedProperty("firstname", [except: "password"])
        assert SearchableUtils.isIncludedProperty("firstname", [except: ["password", "securityNumber"]])
        assert !SearchableUtils.isIncludedProperty("firstname", [only: "surname"])
        assert !SearchableUtils.isIncludedProperty("firstname", [only: ["surname", "idHash"]])
        assert SearchableUtils.isIncludedProperty("firstname", [only: "firstname"])
        assert SearchableUtils.isIncludedProperty("firstname", [only: ["firstname", "surname"]])

        // Simple pattern matching support
        assert SearchableUtils.isIncludedProperty("addressLine1", [only: 'address*'])
        assert SearchableUtils.isIncludedProperty("addressLine1", [only: ['name', 'address*', 'building']])
        assert SearchableUtils.isIncludedProperty("addressLine1", [only: 'addressLine?'])
        assert SearchableUtils.isIncludedProperty("addressLine1", [only: ['building*', 'addressLine?']])
        assert !SearchableUtils.isIncludedProperty("addressLine1", [only: 'house*'])
        assert !SearchableUtils.isIncludedProperty("addressLine1", [except: ['address*', 'name']])
        assert !SearchableUtils.isIncludedProperty("addressLine1", [except: ['name', 'addressLine?']])
        assert SearchableUtils.isIncludedProperty("addressLine1", [except: 'house*'])
    }

    void testGetIntegerOption() {
//        assert SearchableUtils.getIntegerOption("theoption", null, null) == null
        assert SearchableUtils.getIntegerOption("theoption", [:], null) == null
        assert SearchableUtils.getIntegerOption("theoption", [:], 42) == 42
        assert SearchableUtils.getIntegerOption("theoption", [theoption: 5], 42) == 5
        assert SearchableUtils.getIntegerOption("theoption", [differentoption: 10], 42) == 42

        // string values
        assert SearchableUtils.getIntegerOption("theoption", [:], null) == null
        assert SearchableUtils.getIntegerOption("theoption", [:], 42) == 42
        assert SearchableUtils.getIntegerOption("theoption", [theoption: "5"], 42) == 5
        assert SearchableUtils.getIntegerOption("theoption", [differentoption: "10"], 42) == 42

//        assert SearchableUtils.getIntegerOption("theoption", null, null) == null
        assert SearchableUtils.getIntegerOption("theoption", [] as Object[], null) == null
        assert SearchableUtils.getIntegerOption("theoption", ["other arg"] as Object[], 42) == 42
        assert SearchableUtils.getIntegerOption("theoption", ["other arg", [theoption: 5]] as Object[], 42) == 5
        assert SearchableUtils.getIntegerOption("theoption", ["other arg", [differentoption: 10]] as Object[], 42) == 42

        // string values
        assert SearchableUtils.getIntegerOption("theoption", [] as Object[], null) == null
        assert SearchableUtils.getIntegerOption("theoption", ["other arg"] as Object[], 42) == 42
        assert SearchableUtils.getIntegerOption("theoption", ["other arg", [theoption: "5"]] as Object[], 42) == 5
        assert SearchableUtils.getIntegerOption("theoption", ["other arg", [differentoption: "10"]] as Object[], 42) == 42
    }

    void testGetBooleanOption() {
//        assert SearchableUtils.getBooleanOption("theoption", null, null) == null
        assert SearchableUtils.getBooleanOption("theoption", [:], null) == null
        assert SearchableUtils.getBooleanOption("theoption", [:], true) == true
        assert SearchableUtils.getBooleanOption("theoption", [theoption: false], true) == false
        assert SearchableUtils.getBooleanOption("theoption", [differentoption: false], true) == true

        assert SearchableUtils.getBooleanOption("theoption", [:], null) == null
        assert SearchableUtils.getBooleanOption("theoption", [:], true) == true
        assert SearchableUtils.getBooleanOption("theoption", [theoption: "false"], true) == false
        assert SearchableUtils.getBooleanOption("theoption", [differentoption: "false"], true) == true

//        assert SearchableUtils.getBooleanOption("theoption", null as Object[], null) == null
        assert SearchableUtils.getBooleanOption("theoption", [] as Object[], null) == null
        assert SearchableUtils.getBooleanOption("theoption", ["other arg"] as Object[], true) == true
        assert SearchableUtils.getBooleanOption("theoption", ["other arg", [theoption: false]] as Object[], true) == false
        assert SearchableUtils.getBooleanOption("theoption", ["other arg", [differentoption: false]] as Object[], true) == true

        assert SearchableUtils.getBooleanOption("theoption", [] as Object[], null) == null
        assert SearchableUtils.getBooleanOption("theoption", ["other arg"] as Object[], true) == true
        assert SearchableUtils.getBooleanOption("theoption", ["other arg", [theoption: "false"]] as Object[], true) == false
        assert SearchableUtils.getBooleanOption("theoption", ["other arg", [differentoption: "false"]] as Object[], true) == true
    }

    void testConvertPageAndSizeToOffsetAndMax() {
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([:], 10) == [offset: 0, max: 10]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([:], 20) == [offset: 0, max: 20]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([size: 20], 10) == [offset: 0, max: 20]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: 3], 10) == [offset: 20, max: 10]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: 3], 15) == [offset: 30, max: 15]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: 3, size: 20], 15) == [offset: 40, max: 20]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: 3, max: 20], 15) == [offset: 40, max: 20]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: 0, max: 20], 15) == [offset: 0, max: 20]

        // string args values
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([:], 10) == [offset: 0, max: 10]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([:], 20) == [offset: 0, max: 20]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([size: "20"], 10) == [offset: 0, max: 20]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: "3"], 10) == [offset: 20, max: 10]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: "3"], 15) == [offset: 30, max: 15]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: "3", size: "20"], 15) == [offset: 40, max: 20]
        assert SearchableUtils.convertPageAndSizeToOffsetAndMax([page: "3", max: "20"], 15) == [offset: 40, max: 20]
    }

    void testGetTotalPages() {
        assert SearchableUtils.getTotalPages([total: 100, offset: 0, max: 10]) == 10
        assert SearchableUtils.getTotalPages([total: 100, offset: 10, max: 10]) == 10
        assert SearchableUtils.getTotalPages([total: 100, offset: 0, max: 20]) == 5
        assert SearchableUtils.getTotalPages([total: 100, offset: 20, max: 20]) == 5
        assert SearchableUtils.getTotalPages([total: 101, offset: 20, max: 20]) == 6
    }

    void testGetCurrentPage() {
        assert SearchableUtils.getCurrentPage([total: 100, offset: 0, max: 10]) == 1
        assert SearchableUtils.getCurrentPage([total: 100, offset: 10, max: 10]) == 2
        assert SearchableUtils.getCurrentPage([total: 100, offset: 0, max: 20]) == 1
        assert SearchableUtils.getCurrentPage([total: 100, offset: 20, max: 20]) == 2
        assert SearchableUtils.getCurrentPage([total: 101, offset: 100, max: 20]) == 6
    }

    void testGetOffsetForPage() {
        assert SearchableUtils.getOffsetForPage(1, [total: 100, offset: 0, max: 10]) == 0
        assert SearchableUtils.getOffsetForPage(2, [total: 100, offset: 0, max: 10]) == 10
        assert SearchableUtils.getOffsetForPage(10, [total: 100, offset: 0, max: 10]) == 90
        assert SearchableUtils.getOffsetForPage(0, [total: 100, offset: 0, max: 10]) == 0
        assert SearchableUtils.getOffsetForPage(3, [total: 105, offset: 0, max: 10]) == 20
        assert SearchableUtils.getOffsetForPage(11, [total: 105, offset: 0, max: 10]) == 100
    }
}