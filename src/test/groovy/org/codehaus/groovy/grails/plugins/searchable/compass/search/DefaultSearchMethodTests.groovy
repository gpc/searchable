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

import org.compass.core.CompassHits
import org.compass.core.CompassHighlighter

/**
 * @author Maurice Nicholson
 */
class DefaultSearchMethodTests extends GroovyTestCase {

    void testSearchWithHighlighterOption() {
        // no offset
        def highlights = []
        def callback = new DefaultSearchMethod.SearchCompassCallback(null, null)
        callback.doWithHighlighter([size: {100}] as Collection, [highlighter: {i -> [fragment: {name -> "highlighter for hit " + name}] as CompassHighlighter}] as CompassHits, [:], [withHighlighter: { hl, i, sr ->
            highlights[i] = hl.fragment(String.valueOf(i))
        }])
        assert highlights.size() == 100
        assert highlights[0] == "highlighter for hit 0"
        assert highlights[42] == "highlighter for hit 42"

        // with offset
        highlights = []
        callback = new DefaultSearchMethod.SearchCompassCallback(null, null)
        callback.doWithHighlighter([size: {10}] as Collection, [length: {1000}, highlighter: {i -> [fragment: {name -> "highlighter for hit " + i}] as CompassHighlighter}] as CompassHits, [:], [offset: 20, withHighlighter: { hl, i, sr ->
            highlights[i] = hl.fragment(String.valueOf(i))
        }])
        assert highlights.size() == 10
        assert highlights[0] == "highlighter for hit 20"
        assert highlights[4] == "highlighter for hit 24"
    }
}
