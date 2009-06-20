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
package org.codehaus.groovy.grails.plugins.searchable.test.index.unindex

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class UnindexTests extends SearchableFunctionalTestCase {
    def searchableService

    Collection<Class<?>> getDomainClasses() {
        return [A, B]
    }

    // searchableService.unindex() // everything
    void testServiceUnindexNoArgsNoOptions() {
        new A(id: 1l, value: "value").index()
        new A(id: 2l, value: "value").index()
        new B(id: 1l, value: "value").index()
        new B(id: 2l, value: "value").index()

        assert searchableService.countHits("value") == 4

        searchableService.unindex()

        assert searchableService.countHits("value") == 0
    }

    // searchableService.unindex([class: Post]) // all class instances
    void testServiceUnindexWithClassOption() {
        new A(id: 1l, value: "value").index()
        new A(id: 2l, value: "value").index()
        new B(id: 1l, value: "value").index()
        new B(id: 2l, value: "value").index()

        assert searchableService.countHits("value") == 4

        searchableService.unindex(class: A)

        def hits = searchableService.search(result: 'every', 'value')
        assert hits.size() == 2
        assert hits.every { it instanceof B }
    }

    // searchableService.unindex(x, y, z) // given object(s)
    void testServiceUnindexWithInstanceArgs() {
        def a1 = new A(id: 1l, value: "value")
        a1.index()
        def a2 = new A(id: 2l, value: "value")
        a2.index()
        def a3 = new A(id: 3l, value: "value")
        a3.index()
        def b1 = new B(id: 1l, value: "value")
        b1.index()
        def b2 = new B(id: 2l, value: "value")
        b2.index()

        assert searchableService.countHits("value") == 5

        searchableService.unindex(a3) // single

        assert searchableService.countHits("value", class: A) == 2
        assert searchableService.countHits("value", class: B) == 2

        searchableService.unindex(a1, b2) // list

        def hits = searchableService.search(result: 'every', 'value')
        assert hits.size() == 2
        assert hits.any { it instanceof A && it.id == 2l}
        assert hits.any { it instanceof B && it.id == 1l }
    }

    // searchableService.unindex(1, 2, 3, [class: Post]) // id'd objects
    void testServiceUnindexWithClassOptionAndIdArgs() {
        def a1 = new A(id: 1l, value: "value")
        a1.index()
        def a2 = new A(id: 2l, value: "value")
        a2.index()
        def a3 = new A(id: 3l, value: "value")
        a3.index()
        def b1 = new B(id: 1l, value: "value")
        b1.index()
        def b2 = new B(id: 2l, value: "value")
        b2.index()

        assert searchableService.countHits("value") == 5

        searchableService.unindex(class: B, 1l) // single

        assert searchableService.countHits("value", class: A) == 3
        assert searchableService.countHits("value", class: B) == 1

        searchableService.unindex(class: A, 2l, 3l) // list

        def hits = searchableService.search(result: 'every', 'value')
        assert hits.size() == 2
        assert hits.any { it instanceof A && it.id == 1l}
        assert hits.any { it instanceof B && it.id == 2l }
    }

    // Thing.unindex() // all Things
    void testDomainStaticUnindexNoArgs() {
        new A(id: 1l, value: "value").index()
        new A(id: 2l, value: "value").index()
        new B(id: 1l, value: "value").index()
        new B(id: 2l, value: "value").index()

        assert A.countHits("value") == 2
        assert B.countHits("value") == 2

        A.unindex()

        assert A.countHits("value") == 0
        assert B.countHits("value") == 2
    }

    // Thing.unindex(1,2,3) // id'd instances
    void testDomainClassStaticUnindexWithIdArgs() {
        new A(id: 1l, value: "value").index()
        new A(id: 2l, value: "value").index()
        new B(id: 1l, value: "value").index()
        new B(id: 2l, value: "value").index()

        assert A.countHits("value") == 2
        assert B.countHits("value") == 2

        A.unindex(2l) // single

        assert A.countHits("value") == 1
        assert B.countHits("value") == 2

        B.unindex(2l, 1l) // list

        assert A.countHits("value") == 1
        assert B.countHits("value") == 0
    }

    // Thing.unindex(x,y,z) // given instances
    void testDomainClassStaticUnindexWithInstanceArgs() {
        def a1 = new A(id: 1l, value: "value")
        a1.index()
        def a2 = new A(id: 2l, value: "value")
        a2.index()
        def b1 = new B(id: 1l, value: "value")
        b1.index()
        def b2 = new B(id: 2l, value: "value")
        b2.index()

        assert A.countHits("value") == 2
        assert B.countHits("value") == 2

        A.unindex(a2) // single

        assert A.countHits("value") == 1
        assert B.countHits("value") == 2

        B.unindex(b2, b1) // list

        assert A.countHits("value") == 1
        assert B.countHits("value") == 0
    }

    // thing.unindex() // instance
    void testDomainClassInstanceUnindex() {
        def a1 = new A(id: 1l, value: "value")
        a1.index()
        def a2 = new A(id: 2l, value: "value")
        a2.index()
        def b1 = new B(id: 1l, value: "value")
        b1.index()
        def b2 = new B(id: 2l, value: "value")
        b2.index()

        assert A.countHits("value") == 2
        assert B.countHits("value") == 2

        a2.unindex()

        assert A.countHits("value") == 1
        assert B.countHits("value") == 2
    }
}