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
import org.apache.lucene.index.Term

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

    void testTermsForQueryString() {
        assert LuceneUtils.termsForQueryString("this is a query", SimpleAnalyzer.class) == ["this", "is", "a", "query"] as String[]        
        assert LuceneUtils.termsForQueryString("+wanted -unwanted dontcare", SimpleAnalyzer.class) == ["wanted", "unwanted", "dontcare"] as String[]
        assert LuceneUtils.termsForQueryString("field:someterm anotherterm +(this OR that)", SimpleAnalyzer.class) == ["someterm", "anotherterm", "this", "that"] as String[]
        assert LuceneUtils.termsForQueryString("what what what", SimpleAnalyzer.class) == ["what", "what", "what"] as String[]
    }

    void testRealTermsForQueryString() {
        Term[] terms = LuceneUtils.realTermsForQueryString('$defaultfield$', "this is a query", SimpleAnalyzer.class)
        assert terms[0].text() == "this"
        assert terms[0].field() == '$defaultfield$'
        assert terms[1].text() == "is"
        assert terms[1].field() == '$defaultfield$'
        assert terms[2].text() == "a"
        assert terms[2].field() == '$defaultfield$'
        assert terms[3].text() == "query"
        assert terms[3].field() == '$defaultfield$'

        terms = LuceneUtils.realTermsForQueryString('$defaultfield$', "+wanted -unwanted dontcare", SimpleAnalyzer.class)
        assert terms*.text() == ["wanted", "unwanted", "dontcare"]
        assert terms*.field() == ['$defaultfield$', '$defaultfield$', '$defaultfield$']

        terms = LuceneUtils.realTermsForQueryString('$defaultfield$', "field:someterm anotherterm +(this OR that:that)", SimpleAnalyzer.class)
        assert terms*.text() == ["someterm", "anotherterm", "this", "that"]
        assert terms*.field() == ['field', '$defaultfield$', '$defaultfield$', 'that']

        terms = LuceneUtils.realTermsForQueryString('$defaultfield$', "what what what", SimpleAnalyzer.class)
        assert terms*.text() == ["what", "what", "what"]
        assert terms*.field() == ['$defaultfield$', '$defaultfield$', '$defaultfield$']
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