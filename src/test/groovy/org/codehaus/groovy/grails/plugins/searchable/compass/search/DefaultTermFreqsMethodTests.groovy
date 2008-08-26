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

import org.codehaus.groovy.grails.plugins.searchable.compass.search.DefaultTermFreqsMethod.TermFreqsArgs
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Comment
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post
import org.compass.core.CompassTermFreqsBuilder

/**
*
* @author Maurice Nicholson
*/
class DefaultTermFreqsMethodTests extends GroovyTestCase {

    void testTermFreqsArgsParse() {
        TermFreqsArgs tfa

        // without default properties
        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name']] as Object[], [:])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs([[size: 100, properties: ['title', 'desc']]] as Object[], [:])
        assert tfa.properties == ['title', 'desc'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs(['title', 'desc'] as Object[], [:])
        assert tfa.properties == ['title', 'desc'] as String[]
        assert tfa.size == null
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs(['name', [sort: 'freq']] as Object[], [:])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == null
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == CompassTermFreqsBuilder.Sort.FREQ

        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name', class: Post.class]] as Object[], [:])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null
        assert tfa.clazz == Post.class

        // "match" is what "class" is called internally (required for moreLikeThis which takes 2 class args)
        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name', match: Post.class]] as Object[], [:])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null
        assert tfa.clazz == Post.class

        // with default properties
        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name']] as Object[], [property: 'all'])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs([[size: 100, properties: ['title', 'desc']]] as Object[], [property: 'all'])
        assert tfa.properties == ['title', 'desc'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs(['title', 'desc'] as Object[], [property: 'all'])
        assert tfa.properties == ['title', 'desc'] as String[]
        assert tfa.size == null
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs(['name', [sort: 'freq']] as Object[], [property: 'all'])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == null
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == CompassTermFreqsBuilder.Sort.FREQ

        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name']] as Object[], [property: 'all', class: Comment.class])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null
        assert tfa.clazz == Comment.class

        // sort
        tfa = TermFreqsArgs.parseArgs([[sort: 'term']] as Object[], [:])
        assert tfa.sort == CompassTermFreqsBuilder.Sort.TERM

        // "normalize" US English spelling
        tfa = TermFreqsArgs.parseArgs([[normalize: 0..1]] as Object[], [:])
        assert tfa.normalizeRange == 0..1

        // "normalise" British English spelling ;-)
        tfa = TermFreqsArgs.parseArgs([[normalise: 0..1]] as Object[], [:])
        assert tfa.normalizeRange == 0..1
    }
}
