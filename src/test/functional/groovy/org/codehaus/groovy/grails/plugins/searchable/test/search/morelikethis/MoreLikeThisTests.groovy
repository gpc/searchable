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
package org.codehaus.groovy.grails.plugins.searchable.test.search.morelikethis

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase;

/**
 * @author Maurice Nicholson
 */
class MoreLikeThisTests extends SearchableFunctionalTestCase {
    def searchableService

    Collection<Class<?>> getDomainClasses() {
        return [A, B]
    }

    void testMoreLikeThisAllClasses() {
        B b1 = new B(id: 1l, value: "RE: Testing more like this Nice post! I especially love the common common word part")
        B b2 = new B(id: 2l, value: "RE: Testing more like this Yeah I'd go along with the above")
        def bs = new HashSet()
        bs.add(b1)
        bs.add(b2)
        def a = new A(id: 1l, value: "Testing more like this Post has a common common word in it", bs: bs)
        (bs + a).each { it.index() }

        def result = searchableService.moreLikeThis(a, [max: 101, minResourceFreq: 1, minTermFreq: 1])
        assert result.results.size() == 2, result.results.size()
        def types = result.results*.class.unique()
        assert types as List == [B]

        def every = searchableService.moreLikeThis(b1, [result: 'every', max: 101, minResourceFreq: 1, minTermFreq: 1])
        assert every*.class.unique().containsAll([A, B]), every*.class.unique()
        assert !every.contains(b1)
    }

    void testMoreLikeThisSingleClass() {
        B b1 = new B(id: 1l, value: "RE: Testing more like this Nice post! I especially love the common common word part")
        B b2 = new B(id: 2l, value: "RE: Testing more like this Yeah I'd go along with the above")
        def bs = new HashSet()
        bs.add(b1)
        bs.add(b2)
        def a1 = new A(id: 1l, value: "Testing more like this Post has a common common word in it", bs: bs)

        B b3 = new B(id: 3l, value: "RE: Testing more like this, part 2 Another great post. I'm loving your common common words")
        B b4 = new B(id: 4l, value: "RE: Testing more like this, part 2 What he said")
        bs = new HashSet()
        bs.add(b3)
        bs.add(b4)
        def a2 = new A(id: 2l, value: "Testing more like this, part 2 More common common words in this post", bs: bs)

        (a1.bs + a1 + a2.bs + a2).each { it.index() }

        def result = A.moreLikeThis(a1, [max: 101, minResourceFreq: 1, minTermFreq: 1])
        assert result.results.size() == 1, result.results.size()
        def types = result.results*.class.unique()
        assert types as List == [A]

        def every = searchableService.moreLikeThis(b1, [result: 'every', max: 101, minResourceFreq: 1, minTermFreq: 1])
        assert every*.class.unique().containsAll([A, B]), every*.class.unique()
        assert !every.contains(b1)
        assert every.size() == 5, every.size()
    }

    void testMoreLikeThisAsClassStaticMethod() {
        B b1 = new B(id: 1l, value: "RE: Testing more like this Nice post! I especially love the common common word part")
        B b2 = new B(id: 2l, value: "RE: Testing more like this Yeah I'd go along with the above")
        def bs = new HashSet()
        bs.add(b1)
        bs.add(b2)
        def a1 = new A(id: 1l, value: "Testing more like this Post has a common common word in it", bs: bs)

        B b3 = new B(id: 3l, value: "RE: Testing more like this, part 2 Another great post. I'm loving your common common words")
        B b4 = new B(id: 4l, value: "RE: Testing more like this, part 2 What he said")
        bs = new HashSet()
        bs.add(b3)
        bs.add(b4)
        def a2 = new A(id: 2l, value: "Testing more like this, part 2 More common common words in this post", bs: bs)

        (a1.bs + a1 + a2.bs + a2).each { it.index() }

        def every = B.moreLikeThis(b1, [result: 'every', max: 101, minResourceFreq: 1, minTermFreq: 1])
        assert every*.class.unique() as List == [B]
        assert !every.contains(b1)
        assert every.size() == 3, every.size()

        def top = B.moreLikeThis(b3.id, minResourceFreq: 1, minTermFreq: 1, result: 'top')
        assert top instanceof B
        assert top != b3

        // todo count
    }

    void testMoreLikeThisInstanceMethod() {
        def a, b
        for (i in 0..10) {
            a = new A(id: i as Long, value: "quack quack quack").index()
            b = new B(id: i as Long, value: "quack quack quack").index()
        }

        // without options
        def sr = a.moreLikeThis()
        assertTrue sr.results.size() > 1
        assertFalse sr.results.contains(a)
        assertTrue sr.results*.class.unique() == [A]

        // with options
        sr = b.moreLikeThis(properties: ['value'], maxWordLen: 10)
        assertTrue sr.results.size() > 1
        assertFalse sr.results.contains(b)
        assertTrue sr.results*.class.unique() == [B]
    }
}
