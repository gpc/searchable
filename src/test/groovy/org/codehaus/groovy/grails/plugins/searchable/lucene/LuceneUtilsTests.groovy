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
package org.codehaus.groovy.grails.plugins.searchable.lucene

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.WhitespaceAnalyzer
import org.apache.lucene.analysis.SimpleAnalyzer

/**
*
*
* @author Maurice Nicholson
*/
class LuceneUtilsTests extends GroovyTestCase {

    void testTermsForText() {
        // Default analyzer (Lucene's StandardAnalyzer)
        assert LuceneUtils.termsForText("The Quick Brown Fox Jumps Over The Lazy Dog") == ['quick', 'brown', 'fox', 'jumps', 'over', 'lazy', 'dog']
        assert LuceneUtils.termsForText("The Quick Brown Fox Jumps Over The Lazy Dog", (Class) null) == ['quick', 'brown', 'fox', 'jumps', 'over', 'lazy', 'dog']
        assert LuceneUtils.termsForText("The Quick Brown Fox Jumps Over The Lazy Dog", (Analyzer) null) == ['quick', 'brown', 'fox', 'jumps', 'over', 'lazy', 'dog']

        // Another analyzer
        assert LuceneUtils.termsForText("The Quick Brown Fox Jumps Over The Lazy Dog", SimpleAnalyzer.class) == ['the', 'quick', 'brown', 'fox', 'jumps', 'over', 'the', 'lazy', 'dog']
        assert LuceneUtils.termsForText("The Quick Brown Fox Jumps Over The Lazy Dog", new SimpleAnalyzer()) == ['the', 'quick', 'brown', 'fox', 'jumps', 'over', 'the', 'lazy', 'dog']
    }

    void testEscapeQuery() {
        assert LuceneUtils.escapeQuery('"This is a "bad query"') == /\"This is a \"bad query\"/
    }

    void testCleanQuery() {
        assert LuceneUtils.cleanQuery('"This is a "bad query"') == "This is a bad query"
        assert LuceneUtils.cleanQuery('"This [is] ^a "bad~ {}query*?"') == "This is a bad query"
    }

    void testQueryHasSpecialCharacters() {
        assert LuceneUtils.queryHasSpecialCharacters("This +has *special] characters-")
        assert LuceneUtils.queryHasSpecialCharacters("No \$special characters%") == false
    }
}