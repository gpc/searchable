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
package org.codehaus.groovy.grails.plugins.searchable.lucene;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Lucene utils
 *
 * @author Maurice Nicholson
 */
public class LuceneUtils {
    private static final Log LOG = LogFactory.getLog(LuceneUtils.class);
    public static final String SPECIAL_QUERY_CHARACTERS = "\\+-!():^[]\"{}~*?";

    /**
     * Return the Lucene tokens for the given Analyzer and text.
     * Note: copied directry from "lia.AnalyzerUtils" from the "Lucene in Action"
     * book source code
     *
     * @param analyzer a Lucene Analyzer
     * @param text the text
     * @return Tokens
     * @throws IOException
     */
    public static Token[] tokensFromAnalysis(Analyzer analyzer, String text) throws IOException {
      TokenStream stream =
          analyzer.tokenStream("contents", new StringReader(text));
      ArrayList tokenList = new ArrayList();
      while (true) {
        Token token = stream.next();
        if (token == null) break;

        tokenList.add(token);
      }

      return (Token[]) tokenList.toArray(new Token[0]);
    }

    /**
     * Returns a list of terms by analysing the given text with Lucene's StandardAnalyzer
     *
     * @param text the text to analyse
     * @return a list of text terms
     */
    public static String[] termsForText(String text) {
        return termsForText(text, (Analyzer) null);
    }

    /**
     * Returns a list of terms by analysing the given text
     *
     * @param text the text to analyse
     * @param analyzerClass the Analyzer class to use, may be null in which case Lucene's StandardAnalyzer is used
     * @return a list of text terms
     */
    public static String[] termsForText(String text, Class analyzerClass) {
        if (analyzerClass == null) {
            return termsForText(text, (Analyzer) null);
        }
        try {
            return termsForText(text, (Analyzer) analyzerClass.newInstance());
        } catch (Exception ex) {
            // Convert to unchecked
            LOG.error("Failed to create instance of Analyzer class [" + analyzerClass + "]: " + ex, ex);
            throw new IllegalStateException("Failed to create instance of Analyzer class [" + analyzerClass + "]: " + ex);
        }
    }

    /**
     * Returns a list of terms by analysing the given text
     *
     * @param text the text to analyse
     * @param analyzer the Analyzer instance to use, may be null in which case Lucene's StandardAnalyzer is used
     * @return a list of text terms
     */
    public static String[] termsForText(String text, Analyzer analyzer) {
        if (analyzer == null) {
            analyzer = new StandardAnalyzer();
        }
        try {
            List terms = new ArrayList();
            Token[] tokens = tokensFromAnalysis(analyzer, text);
            for (int i = 0; i < tokens.length; i++) {
                terms.add(tokens[i].termText());
            }
            return (String[]) terms.toArray(new String[terms.size()]);
        } catch (IOException ex) {
            // Convert to unchecked
            LOG.error("Unable to analyze the given text: " + ex, ex);
            throw new IllegalArgumentException("Unable to analyze the given text: " + ex);
        }
    }

    /**
     * Escape special characters in the given string that would otherwise
     * cause a parse exception
     * 
     * @param query the query to escape
     * @return the escaped query
     */
    public static String escapeQuery(String query) {
        // Note we use the Lucene QueryParser instead of the Compass subclass
        // because Groovy does not inherit static methods (?)
        if (query == null) return null;
        return QueryParser.escape(query);
    }

    /**
     * Returns the query string with special characters removed
     * 
     * @param query the query to clean
     * @return the cleaned query
     */
    public static String cleanQuery(String query) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (isSpecialQueryCharacter(c)) continue;
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Does the given query string contain special characters, ie, those with
     * special meaning to Lucene's query parser
     * @param query the query
     * @return true if it contains special characters
     */
    public static boolean queryHasSpecialCharacters(String query) {
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (isSpecialQueryCharacter(c)) return true;
        }
        return false;
    }

    private static boolean isSpecialQueryCharacter(char c) {
        return SPECIAL_QUERY_CHARACTERS.indexOf(c) > -1;
    }
}
