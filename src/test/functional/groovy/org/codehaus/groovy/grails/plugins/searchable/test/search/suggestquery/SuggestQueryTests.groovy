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
package org.codehaus.groovy.grails.plugins.searchable.test.search.suggestquery

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class SuggestQueryTests extends SearchableFunctionalTestCase {
    def searchableService

    public getDomainClasses() {
        return [A, B]
    }

    void testSuggestionRetainsStopWords() {
        new A(id: 1l, value: "white toast").index()
        new B(id: 1l, value: "blue sea").index()
        searchableService.rebuildSpellingSuggestions()

        // should be ok with stop words, which are normally removed when parsing
        assert searchableService.suggestQuery("is the sky as blue as the sea") == 'is the sky as blue as the sea', searchableService.suggestQuery("is the sky as blue as the sea")
    }

    void testAllowSameOption() {
        new A(id: 1l, value: "white bread toast with jam").index()
        searchableService.rebuildSpellingSuggestions()

        def suggestion = searchableService.suggestQuery("quack")
        assert suggestion == 'quack', suggestion

        suggestion = searchableService.suggestQuery("white bread (toast OR jam)")
        assert suggestion == "white bread (toast OR jam)", suggestion

        suggestion = searchableService.suggestQuery("quack", allowSame: false) // boolean
        assert suggestion == null, suggestion

        suggestion = searchableService.suggestQuery("white bread (toast OR jam)", allowSame: "false") // string
        assert suggestion == null, suggestion
    }

    void testServiceSuggestQueryMethod() {
        new A(id: 1l, value: "white toast").index()
        new B(id: 1l, value: "blue sea").index()

        searchableService.rebuildSpellingSuggestions()

        shouldFail {
            searchableService.suggestQuery {
                queryString("closures are not supported for suggestions!")
            }
        }

        // suggested queries are user-friendly by default
        assert searchableService.suggestQuery("what test") == 'white toast', searchableService.suggestQuery("what test")
        assert searchableService.suggestQuery("see blue") == 'sea blue', searchableService.suggestQuery("see blue")

        // the service method version works across all domain classes
        assert searchableService.suggestQuery("white see") == 'white sea', searchableService.suggestQuery("white see")
        assert searchableService.suggestQuery("see what") == 'sea white', searchableService.suggestQuery("see what")

        assert searchableService.suggestQuery("see OR (what white)") == 'sea OR (white white)', searchableService.suggestQuery("see OR (what white)")

        assert searchableService.suggestQuery("field:see OR (what gem:white)") == 'field:sea OR (white gem:white)', searchableService.suggestQuery("field:see OR (what gem:white)")

        // capitals are emulated by default
        assert searchableService.suggestQuery("See What") == 'Sea White', searchableService.suggestQuery("See What")
        assert searchableService.suggestQuery("see WHAT") == 'sea WHITE', searchableService.suggestQuery("see WHAT")

        // Specifically NOT user-friendly
        assert searchableService.suggestQuery("what test", [userFriendly: false]) == '+white +toast', searchableService.suggestQuery("what test", [userFriendly: false])

        // Specifically NOT emulated caps
        assert searchableService.suggestQuery("see WHAT", [emulateCapitalisation: false]) == 'sea white', searchableService.suggestQuery("see WHAT", [emulateCapitalisation: false])
    }

    void testDomainClassSuggestQueryMethod() {
        new A(id: 1l, value: "white toast").index()
        new B(id: 1l, value: "blue sea").index()

        searchableService.rebuildSpellingSuggestions()

        shouldFail {
            A.suggestQuery {
                queryString("closures are not supported for suggestions!")
            }
        }

        // the domain class method only makes suggestions based on that class's instances
        assert A.suggestQuery("what test") == 'white toast', A.suggestQuery("what test")
        assert B.suggestQuery("what test") == 'what test', B.suggestQuery("what test")

        assert B.suggestQuery("see blue") == 'sea blue', B.suggestQuery("see blue")
        assert A.suggestQuery("see blue") == 'see blue', A.suggestQuery("see blue")

        assert A.suggestQuery("white see") == 'white see', A.suggestQuery("white see")

        assert A.suggestQuery("see what") == 'see white', A.suggestQuery("see what")
        assert B.suggestQuery("see what") == 'sea what', B.suggestQuery("see what")
    }
}