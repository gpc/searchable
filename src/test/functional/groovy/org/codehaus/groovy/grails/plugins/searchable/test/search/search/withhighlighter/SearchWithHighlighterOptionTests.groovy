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
package org.codehaus.groovy.grails.plugins.searchable.test.search.search.withhighlighter

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class SearchWithHighlighterOptionTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [A]
    }

    void testSearchWithHighlighterSingleTerm() {
        new A(id: 1l, text: "some text to highlight").index()
        new A(id: 2l, text: "highlight should occur here too").index()
        new A(id: 3l, text: "this highlight could also have highlight result").index()
        new A(id: 4l, text: "no highlighting here").index()

        def results = A.search("highlight", withHighlighter: { highlighter, index, sr ->
            if (!sr.highlights) {
                sr.highlights = []
            }
            sr.highlights[index] = highlighter.fragment("text")
        })
        assert results.results.size() == 3
        assert results.highlights.size() == 3
        assert results.highlights.contains("some text to <b>highlight</b>")
        assert results.highlights.contains("<b>highlight</b> should occur here too")
        assert results.highlights.contains("this <b>highlight</b> could also have <b>highlight</b> result")
    }

    void testSearchWithHighlighterMultiTerm() {
        new A(id: 1l, text: "the quick brown fox jumps over the lazy dog").index()

        def highlight
        def captureHighlight = { highlighter, index, sr ->
            highlight = highlighter.fragment("text")
        }

        // separate words
        A.search("quick fox", withHighlighter: captureHighlight)
        assert highlight == "the <b>quick</b> brown <b>fox</b> jumps over the lazy dog"

        // contiguous words - words are still highlighted separately - ah well
        A.search("quick brown", withHighlighter: captureHighlight)
        assert highlight == "the <b>quick</b> <b>brown</b> fox jumps over the lazy dog"
    }

    void testSearchWithHighlighterAndOffsetAndMax() {
        def list = (1..100).collect {
            new A(id: it as Long, text: "highlight me ${it}")
        }
        A.index(list)

        def highlights = []
        def captureHighlights = { highlighter, index, sr ->
            highlights << highlighter.fragment("text")
        }
        A.search("me", withHighlighter: captureHighlights)
        assert highlights.size() == 10
        assert highlights[0] == "highlight <b>me</b> 1"
        assert highlights[9] == "highlight <b>me</b> 10"

        highlights = []
        A.search("me", offset: 20, max: 20, withHighlighter: captureHighlights)
        assert highlights.size() == 20
        assert highlights[0] == "highlight <b>me</b> 21"
        assert highlights[9] == "highlight <b>me</b> 30"
        assert highlights[19] == "highlight <b>me</b> 40"
    }
}