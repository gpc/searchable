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
package org.codehaus.groovy.grails.plugins.searchable.test.search.termfreqs

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class TermFreqsTests extends SearchableFunctionalTestCase {
    def searchableService

    Collection<Class<?>> getDomainClasses() {
        return [A, B]
    }

    void testServiceTermFreqsMethod() {
        def allAs = [], allBs = []
        for (i in 0..<100) {
            def bs = new HashSet()
            def a = new A(id: i as Long, avalue: "thisisana thisisana. ablah aterm astuff apadding awhatever", bs: bs)
            allAs << a
            for (j in 0..<5) {
                def b = new B(id: allBs.size() as Long, bvalue: "thisisab thisisab. bblah bterm bstuff bpadding bwhatever", a: a)
                allBs << b
            }
        }
        (allAs + allBs).each { it.index() }

        // options to test size, class, normalise, property/properties, sort

        // no options
        def termFreqs = searchableService.termFreqs()
        assert termFreqs.length > 0, termFreqs.length
        assert termFreqs.term.contains("thisisana")
        assert termFreqs.term.contains("thisisab")
        assert termFreqs.freq == termFreqs.freq.sort().reverse()
        assert termFreqs.length > 5
        assert termFreqs.freq.min() >= 1
        assert termFreqs.freq.max() >= 1

        // with options
        termFreqs = searchableService.termFreqs(size: 5, normalize: 0..1, class: A, sort: 'term')
        assert termFreqs.term.contains("aterm")
        assert !termFreqs.term.contains("bterm")
        assert termFreqs.term == termFreqs.term.sort()
        assert termFreqs.length == 5
        assert termFreqs.freq.min() >= 0, termFreqs.freq.min()
        assert termFreqs.freq.max() <= 1, termFreqs.freq.max()

        termFreqs = searchableService.termFreqs(size: 5, normalize: 0..1, class: A, sort: 'term', properties: ['avalue'])
        assert !termFreqs.any { it.term.startsWith("b") }
        assert !termFreqs.term.contains("thisisab")
        assert termFreqs.term == termFreqs.term.sort()
        assert termFreqs.length == 5
        assert termFreqs.freq.min() >= 0
        assert termFreqs.freq.max() <= 1

        // specify property as first String arg
        termFreqs = searchableService.termFreqs('avalue', size: 5, normalize: 0..1, class: A)
        assert !termFreqs.any { it.term.startsWith("b") }
        assert !termFreqs.term.contains("thisisab")
        assert termFreqs.length == 5
        assert termFreqs.freq.min() >= 0
        assert termFreqs.freq.max() <= 1

        // specify properties as first String args; here across multiple domain objects
        termFreqs = searchableService.termFreqs('avalue', 'bvalue', normalize: 0..1)
        assert termFreqs.term.contains("thisisana")
        assert termFreqs.term.contains("thisisab")
        assert termFreqs.freq.min() >= 0
        assert termFreqs.freq.max() <= 1
    }
}
