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
package org.codehaus.groovy.grails.plugins.searchable.test.index.index

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class IndexTests extends SearchableFunctionalTestCase {
    def searchableService

    Collection<Class<?>> getDomainClasses() {
        return [A, B]
    }

    /**
     * searchableService.index() // all searchable class instances, same as service.indexAll()
     */
    void testServiceIndexNoArgs() {
        new A(id: 1l, value: "instance of A_class").save()
        new B(id: 1l, value: "instance of B_class").save()

        assert searchableService.countHits("instance") == 0

        searchableService.index()

        assert searchableService.countHits("instance") == 2
    }

    /**
     * service.index([class: Post]) // all Post instances
     */
    void testServiceIndexClassArg() {
        new A(id: 1l, value: "instance of A_class").save()
        new A(id: 2l, value: "second instance of A_class").save()
        new B(id: 1l, value: "instance of B_class").save()
        new B(id: 2l, value: "second instance of B_class").save()

        assert searchableService.countHits("instance") == 0

        searchableService.index(class: A)

        def hits = searchableService.search(result: 'every', "instance")
        assert hits.size() == 2
        assert hits*.class.unique() == [A]
    }

    /**
     * service.index(x, y, z) // given object(s)
     */
    void testServiceIndexObjectArgs() {
        def a1 = new A(id: 1l, value: "a A_class instance").save()
        def a2 = new A(id: 2l, value: "another A_class instance").save()
        def b1 = new B(id: 1l, value: "a B_class instance").save()
        def b2 = new B(id: 2l, value: "another B_class instance").save()

        assert searchableService.countHits("instance") == 0

        def result = searchableService.index(b1) // single
        assert result.equals(b1), result // returns given object

        def hits = searchableService.search("instance", result: 'every')
        assert hits.size() == 1
        assert hits.every { it instanceof B && it.id == 1l }

        result = searchableService.index(a1, b2) // list
        assert result == [a1, b2], result // returns same list

        hits = searchableService.search("instance", result: 'every')
        assert hits.size() == 3
        assert hits.any { it instanceof A && it.id == 1l }
        assert hits.any { it instanceof B && it.id == 1l }
        assert hits.any { it instanceof B && it.id == 2l }
    }

    /**
     * service.index(1, 2, 3, [class: Post]) // id'd objects
     */
    void testServiceIndexIdArgsWithClass() {
        new A(id: 1l, value: "first A_class instance").save()
        new A(id: 2l, value: "second A_class instance").save()
        new A(id: 3l, value: "third A_class instance").save()
        new B(id: 1l, value: "first B_class instance").save()
        new B(id: 2l, value: "second B_class instance").save()

        assert searchableService.countHits("instance") == 0

        def result = searchableService.index(3l, class: A) // single
        assert result.class == A && result.id == 3l

        def hits = searchableService.search(result: 'every', "instance")
        assert hits.size() == 1
        assert hits.any { it instanceof A && it.id == 3l }

        result = searchableService.index(1l, 2l, class: A) // list
        assert result*.class.every { it == A } && result*.id == [1l, 2l]

        hits = searchableService.search(result: 'every', "instance")
        assert hits.size() == 3
        assert hits*.class.every { it == A }
        assert hits*.id.sort() == [1l, 2l, 3l]
    }

    /**
     * Thing.index() // all Thing instances (same as Thing.indexAll())
     */
    void testDomainClassStaticIndexNoArgs() {
        new A(id: 1l, value: "instance of A_class").save()
        new A(id: 2l, value: "second instance of A_class").save()
        new B(id: 1l, value: "instance of B_class").save()
        new B(id: 2l, value: "second instance of B_class").save()

        assert searchableService.countHits("instance") == 0

        A.index()

        def hits = searchableService.search(result: 'every', "instance")
        assert hits.size() == 2
        assert hits*.class.unique() == [A]
    }

    /**
     * Thing.index(1,2,3) // id'd instances
     */
    void testDomainClassStaticIndexIdsArgs() {
        new A(id: 1l, value: "first A_class instance").save()
        new A(id: 2l, value: "second A_class instance").save()
        new A(id: 3l, value: "third A_class instance").save()
        new B(id: 1l, value: "first B_class instance").save()
        new B(id: 2l, value: "second B_class instance").save()

        assert A.countHits("instance") == 0
        assert B.countHits("instance") == 0

        def result = A.index(3l) // single
        assert result.class == A && result.id == 3l

        assert A.countHits("instance") == 1
        assert A.search("instance", result: 'top').id == 3l
        assert B.countHits("instance") == 0

        result = A.index(1l, 2l) // list
        assert result*.class.every { it == A } && result*.id == [1l, 2l]

        assert A.countHits("instance") == 3 // just As
        assert A.search("instance", result: 'every')*.id.sort() == [1l, 2l, 3l]
        assert B.countHits("instance") == 0 // no Bs
    }

    /**
     * Thing.index(x,y,z) // given instances
     */
    void testDomainClassStaticIndexInstancesArgs() {
        def a1 = new A(id: 1l, value: "a A_class instance").save()
        def a2 = new A(id: 2l, value: "another A_class instance").save()
        def b1 = new B(id: 1l, value: "a B_class instance").save()
        def b2 = new B(id: 2l, value: "another B_class instance").save()

        assert A.countHits("instance") == 0
        assert B.countHits("instance") == 0

        def result = A.index(a1) // single
        assert result == a1

        assert A.countHits("instance") == 1
        assert A.search("instance", result: 'top').id == 1l
        assert B.countHits("instance") == 0

        result = B.index(b1, b2) // list
        assert result == [b1, b2]

        assert A.countHits("instance") == 1
        assert B.countHits("instance") == 2
        assert B.search("instance", result: 'every')*.id.sort() == [1l, 2l]
    }

    // thing.index() // instance
    void testDomainClassInstanceIndex() {
        def a1 = new A(id: 1l, value: "a A_class instance").save()
        def a2 = new A(id: 2l, value: "another A_class instance").save()
        def b1 = new B(id: 1l, value: "a B_class instance").save()
        def b2 = new B(id: 2l, value: "another B_class instance").save()

        assert A.countHits("instance") == 0
        assert B.countHits("instance") == 0

        def result = a1.index()
        assert result == a1

        result = b2.index()
        assert result == b2

        def hits = A.search("instance", result: 'every')
        assert hits.size() == 1
        assert hits.every { it.id == 1l }

        hits = B.search("instance", result: 'every')
        assert hits.size() == 1
        assert hits.every { it.id == 2l }
    }
}