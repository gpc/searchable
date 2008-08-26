/*
 * Copyright 2008 the original author or authors.
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
package org.codehaus.groovy.grails.plugins.searchable.test.search.search.suggestquery

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class SearchSuggestQueryOptionTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [A]
    }

    void testServiceSearchMethodWithSuggestQueryOption() {
        new A(id:1l, value: "deep blue sea").index()

        searchableService.rebuildSpellingSuggestions()

        // this feature is not enabled by default
        def result = searchableService.search("see blu")
        assert result.suggestedQuery == null

        result = searchableService.search("see blu", suggestQuery: true)
        assert result.suggestedQuery == "sea blue", result.suggestedQuery
        assert result.results.size() == 0

        // when enabled, it tries to suggest queries that are user-friendly and emulate-capitalisation by default
        result = searchableService.search("deep OR BLU", suggestQuery: true)
        assert result.suggestedQuery == "deep OR BLUE", result.suggestedQuery
        assert result.results.size() == 1 // matched "deep" not "BLU"

        // user-friendly and emulate-capitalisation  can be disabled passing nested options
        result = searchableService.search("deep OR BLU", suggestQuery: [userFriendly: false])
        assert result.suggestedQuery == "deep blue", result.suggestedQuery
        assert result.results.size() == 1 // matched "deep" not "BLU"

        result = searchableService.search("deep OR BLU", suggestQuery: [emulateCapitalisation: false])
        assert result.suggestedQuery == "deep OR blue", result.suggestedQuery
        assert result.results.size() == 1 // matched "deep" not "BLU"

        // some options are passed on from search method to suggest query
        shouldFail {
            result = searchableService.search("[this is a bad query]", suggestQuery: true)
        }
        result = searchableService.search("[this is a bad query]", escape: true, suggestQuery: true) // escape option fixes it
    }

    void testDomainClassSearchMethodWithSuggestQueryOption() {
        new A(id:1l, value: "deep blue sea").index()

        searchableService.rebuildSpellingSuggestions()

        // this feature is not enabled by default
        def result = A.search("see blu")
        assert result.suggestedQuery == null

        result = A.search("see blu", suggestQuery: true)
        assert result.suggestedQuery == "sea blue", result.suggestedQuery
        assert result.results.size() == 0

        // when enabled, it tries to suggest queries that are user-friendly and emulate-capitalisation by default
        result = A.search("deep OR BLU", suggestQuery: true)
        assert result.suggestedQuery == "deep OR BLUE", result.suggestedQuery
        assert result.results.size() == 1 // matched "deep" not "BLU"
    }
}