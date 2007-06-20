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

import org.codehaus.groovy.grails.plugins.searchable.test.*
import org.compass.core.CompassHits

/**
* @author Maurice Nicholson
*/
class SearchableSubsetSearchResultFactoryTests extends GroovyTestCase {

    void testBuildSearchResult() {
        def searchResultFactory = new SearchableSubsetSearchResultFactory()

        def theLength = 100
        def theData = (0..<theLength).collect { "This is hit object #${it}" }
        def theScores = (theLength..<0).collect { it / 100 as float }
        def hits = [
            data: { int i ->
                theData[i]
            },

            score: { int i ->
                theScores[i]
            },

            length: { -> theLength }
        ] as CompassHits

        // First page
        def collectedHits = theData[0..<10]
        def searchResult = searchResultFactory.buildSearchResult(hits, collectedHits, [offset: 0, max: 10])
        assert searchResult.results == collectedHits
        assert searchResult.total == hits.length()
        assert searchResult.offset == 0
        assert searchResult.max == 10
        assert searchResult.scores.size() == 10
        assert searchResult.scores.min() > 0.9
        assert searchResult.scores.max() == 1

        // Another page
        collectedHits = theData[15..<30]
        searchResult = searchResultFactory.buildSearchResult(hits, collectedHits, [offset: 15, max: 15])
        assert searchResult.results == collectedHits
        assert searchResult.total == hits.length()
        assert searchResult.offset == 15
        assert searchResult.max == 15
        assert searchResult.scores.size() == 15
        assert searchResult.scores.min() > 0.7
        assert searchResult.scores.max() == 0.85 as float
    }
}