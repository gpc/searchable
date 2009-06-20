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
package org.codehaus.groovy.grails.plugins.searchable.compass.search

/**
 * @author Maurice Nicholson
 */
class SearchableHitsOnlySearchResultFactoryTests extends GroovyTestCase {

    void testBuildSearchResult() {
        def searchResultFactory = new SearchableHitsOnlySearchResultFactory()

        def collectedHits = [1, 2, 3, 4, 5]
        def searchResult = searchResultFactory.buildSearchResult(null, collectedHits, null, [:])
        assert searchResult.is(collectedHits)

        collectedHits = "THIS IS A SEARCH RESULT HIT"
        searchResult = searchResultFactory.buildSearchResult(null, collectedHits, null, [:])
        assert searchResult.is(collectedHits)
    }

}