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
package org.codehaus.groovy.grails.plugins.searchable.test.index.reindex

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class ReindexTests extends SearchableFunctionalTestCase {
    def searchableService

    public getDomainClasses() {
        return [A, B]
    }

    // searchableService.reindex() // all searchable instances - same as reindexAll
    void testServiceReindexNoArgs() {
        def a1 = new A(id: 1l, value: "value").save()
        def a2 = new A(id: 2l, value: "value").save()
        def b1 = new B(id: 1l, value: "value").save()
        def b2 = new B(id: 2l, value: "value").save()

        assert searchableService.countHits("value") == 0

        searchableService.reindex() // reindex/index are interchangable if the objects are not currently indexed

        assert searchableService.countHits("value") == 4

        a1.value = "jam"
        a1.save()
        b2.value = "honey"
        b2.save()

        searchableService.reindex()

        def hits = searchableService.search("alias:A", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 1l && it.value == "jam" }
        assert hits.any { it.id == 2l && it.value == "value" }

        hits = searchableService.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 1l && it.value == "value" }
        assert hits.any { it.id == 2l && it.value == "honey" }
    }

    // searchableService.reindex([class: Post]) // all class instances
    void testServiceReindexWithClassArg() {
        def a1 = new A(id: 1l, value: "value").save()
        def a2 = new A(id: 2l, value: "value").save()
        def b1 = new B(id: 1l, value: "value").save()
        def b2 = new B(id: 2l, value: "value").save()

        assert searchableService.countHits("value") == 0

        searchableService.reindex() // first, just index everything

        assert searchableService.countHits("value") == 4

        a1.value = "jam"
        a1.save()
        b2.value = "honey"
        b2.save()

        searchableService.reindex(class: A)

        def hits = searchableService.search("alias:A", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 1l && it.value == "jam" }
        assert hits.any { it.id == 2l && it.value == "value" }

        // B instances have not been re-indexed
        hits = searchableService.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 1l && it.value == "value" }
        assert hits.any { it.id == 2l && it.value == "value" }
    }

    // searchableService.reindex(x, y, z) // given object(s)
    void testServiceReindexWithInstanceArgs() {
        def a1 = new A(id: 1l, value: "value").save()
        def a2 = new A(id: 2l, value: "value").save()
        def a3 = new A(id: 3l, value: "value").save()
        def b1 = new B(id: 1l, value: "value").save()
        def b2 = new B(id: 2l, value: "value").save()

        assert searchableService.countHits("value") == 0

        searchableService.reindex() // first, just index everything

        assert searchableService.countHits("value") == 5

        a1.value = "jam"
        a1.save()
        b2.value = "honey"
        b2.save()
        a3.value = "peanut butter"
        a3.save()

        searchableService.reindex(a1) // single

        assert searchableService.countHits("alias:B AND value") == 2 // no Bs were reindexed
        def hits = searchableService.search("alias:A", result: 'every')
        assert hits.size() == 3
        assert hits.any { it.id == 1l && it.value == "jam" } // was reindexed
        assert hits.any { it.id == 3l && it.value == "value" } // not reindexed

        searchableService.reindex(a3, b2) // list

        hits = searchableService.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 1l && it.value == "value" }
        assert hits.any { it.id == 2l && it.value == "honey" }

        hits = searchableService.search("alias:A", result: 'every')
        assert hits.size() == 3
        assert hits.any { it.id == 1l && it.value == "jam" }
        assert hits.any { it.id == 3l && it.value == "peanut butter" }
    }

    // searchableService.reindex(1, 2, 3, [class: Post]) // id'd objects
    void testServiceReindexWithClassAndIdArgs() {
        def a1 = new A(id: 1l, value: "value").save()
        def a2 = new A(id: 2l, value: "value").save()
        def a3 = new A(id: 3l, value: "value").save()
        def b1 = new B(id: 1l, value: "value").save()
        def b2 = new B(id: 2l, value: "value").save()

        assert searchableService.countHits("value") == 0

        searchableService.reindex() // first, just index everything

        assert searchableService.countHits("value") == 5

        a1.value = "jam"
        a1.save()
        b2.value = "honey"
        b2.save()
        a3.value = "peanut butter"
        a3.save()

        searchableService.reindex(class: B, 2l) // single

        assert searchableService.countHits("alias:A AND value") == 3 // no As were reindexed
        def hits = searchableService.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 2l && it.value == "honey" } // was reindexed

        searchableService.reindex(class: A, 1l, 3l) // list

        hits = searchableService.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 1l && it.value == "value" }
        assert hits.any { it.id == 2l && it.value == "honey" }

        hits = searchableService.search("alias:A", result: 'every')
        assert hits.size() == 3
        assert hits.any { it.id == 1l && it.value == "jam" }
        assert hits.any { it.id == 3l && it.value == "peanut butter" }
    }

    // Thing.reindex() // all Thing instances
    void testDomainClassStaticReindexNoArgs() {
        def a1 = new A(id: 1l, value: "value").save()
        def a2 = new A(id: 2l, value: "value").save()
        def a3 = new A(id: 3l, value: "value").save()
        def b1 = new B(id: 1l, value: "value").save()
        def b2 = new B(id: 2l, value: "value").save()

        assert A.countHits("value") == 0
        assert B.countHits("value") == 0

        A.reindex() // first, just index everything
        B.reindex() // first, just index everything

        assert A.countHits("value") == 3, A.countHits("value")
        assert B.countHits("value") == 2

        a1.value = "jam"
        a1.save()
        b2.value = "honey"
        b2.save()
        a3.value = "peanut butter"
        a3.save()

        B.reindex()

        assert A.countHits("value") == 3 // no As were reindexed
        assert B.countHits("honey") == 1
        assert B.countHits("value") == 1
    }

    // Thing.reindex(1,2,3) // id'd instances
    void testDomainClassStaticReindexWithIdArgs() {
        def a1 = new A(id: 1l, value: "value").save()
        def a2 = new A(id: 2l, value: "value").save()
        def a3 = new A(id: 3l, value: "value").save()
        def b1 = new B(id: 1l, value: "value").save()
        def b2 = new B(id: 2l, value: "value").save()

        assert A.countHits("value") == 0
        assert B.countHits("value") == 0

        A.reindex() // first, just index everything
        B.reindex() // first, just index everything

        assert A.countHits("value") == 3
        assert B.countHits("value") == 2

        a1.value = "jam"
        a1.save()
        b2.value = "honey"
        b2.save()
        a3.value = "peanut butter"
        a3.save()

        B.reindex(2l) // single

        assert searchableService.countHits("alias:A AND value") == 3 // no As were reindexed
        def hits = searchableService.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 2l && it.value == "honey" } // was reindexed

        A.reindex(1l, 3l) // list

        hits = B.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 1l && it.value == "value" }
        assert hits.any { it.id == 2l && it.value == "honey" }

        hits = A.search("alias:A", result: 'every')
        assert hits.size() == 3
        assert hits.any { it.id == 1l && it.value == "jam" }
        assert hits.any { it.id == 3l && it.value == "peanut butter" }
    }

    // Thing.reindex(x,y,z) // given instances
    void testDomainClassStaticReindexWithInstanceArgs() {
        def a1 = new A(id: 1l, value: "value").save()
        def a2 = new A(id: 2l, value: "value").save()
        def a3 = new A(id: 3l, value: "value").save()
        def b1 = new B(id: 1l, value: "value").save()
        def b2 = new B(id: 2l, value: "value").save()

        assert A.countHits("value") == 0
        assert B.countHits("value") == 0

        A.reindex() // first, just index everything
        B.reindex() // first, just index everything

        assert A.countHits("value") == 3
        assert B.countHits("value") == 2

        a1.value = "jam"
        a1.save()
        b2.value = "honey"
        b2.save()
        a3.value = "peanut butter"
        a3.save()

        B.reindex(b2) // single

        assert searchableService.countHits("alias:A AND value") == 3 // no As were reindexed
        def hits = searchableService.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 2l && it.value == "honey" } // was reindexed

        A.reindex(a1, a3) // list

        hits = B.search("alias:B", result: 'every')
        assert hits.size() == 2
        assert hits.any { it.id == 1l && it.value == "value" }
        assert hits.any { it.id == 2l && it.value == "honey" }

        hits = A.search("alias:A", result: 'every')
        assert hits.size() == 3
        assert hits.any { it.id == 1l && it.value == "jam" }
        assert hits.any { it.id == 3l && it.value == "peanut butter" }
    }

    // thing.reindex() // instance
    void testClassInstanceReindex() {
        def a1 = new A(id: 1l, value: "value").save()
        def a2 = new A(id: 2l, value: "value").save()
        def b1 = new B(id: 1l, value: "value").save()
        def b2 = new B(id: 2l, value: "value").save()

        assert A.countHits("value") == 0
        assert B.countHits("value") == 0

        a1.reindex()
        a2.reindex()

        assert A.countHits("value") == 2
        assert B.countHits("value") == 0

        a1.value = "ham"
        a1.save()
        a1.reindex()

        assert A.countHits("value") == 1
        assert A.search("value", result: 'top').id == 2l
        assert A.countHits("ham") == 1
        assert A.search("ham", result: 'top').id == 1l
        assert B.countHits("value") == 0
    }
}