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
package org.codehaus.groovy.grails.plugins.searchable.test.inheritance

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
* @author Maurice Nicholson
*/
class InheritanceTests extends SearchableFunctionalTestCase {
    def searchableService

    def getDomainClasses() {
        return [P, C1, C2, NSC, GC, A]
    }

    void testPolymorphicAssociation() {
        def poly1 = new C2(id: 10l, version: 0l, commonProperty: 'I the poly instance', childTwoProperty: 'number two')
        def c1 = new C1(id: 11l, version: 0l, commonProperty: 'I am a specific class instance', childOneProperty: 'number one')
        def a = new A(id: 50l, name: 'Bill', polyInstance: poly1, specificInstance: c1)
        [poly1, c1, a].each { it.index() }

        def every = A.searchEvery("bill")
        assert every.size() == 1
        a = every[0]
        assert a instanceof A && a.id == 50l
        assert a.name == 'Bill'
        assert a.specificInstance instanceof C1
        assert a.specificInstance.childOneProperty == 'number one'
        assert a.polyInstance instanceof C2
        assert a.polyInstance.childTwoProperty == 'number two'
    }

    void testPolymorphicQueryWithService() {
        def p1 = new P(id: 10l, commonProperty: "I am the searchable parent")
        def c1 = new C1(id: 10l, version: 0l, commonProperty: 'I am a searchable child one', childOneProperty: 'Child One')
        def c2 = new C2(id: 11l, version: 0l, commonProperty: 'I am a searchable child two', childTwoProperty: 'Child Two')
        def g1 = new GC(id: 11l, version: 0l, commonProperty: 'I am a searchable grandchild one', grandChildProperty: 'youngest')
        [p1, c1, c2, g1].each { it.index() }

        def objects = searchableService.searchEvery("searchable")
        assert objects.size() == 4, objects.size()
        assert objects.find { it instanceof P }.every { it.commonProperty == 'I am the searchable parent' }
        assert objects.find { it.id == 10l && it instanceof C1 }.every { it.commonProperty == 'I am a searchable child one' && it.childOneProperty == 'Child One' }
        assert objects.find { it.id == 11l && it instanceof C2 }.every { it.commonProperty == 'I am a searchable child two' && it.childTwoProperty == 'Child Two' }
        assert objects.find { it.id == 11l && it instanceof GC }.every { it.commonProperty == 'I am a searchable grandchild one' && it.grandChildProperty == 'youngest' }

        objects = searchableService.searchEvery("searchable", [class: P])
        assert objects.size() == 4, objects.size()

        objects = searchableService.searchEvery("searchable", [class: C1])
        assert objects.size() == 2, objects.size()

        objects = searchableService.searchEvery("searchable", [class: C2])
        assert objects.size() == 1, objects.size()

        objects = searchableService.searchEvery("searchable", [class: GC])
        assert objects.size() == 1
    }

    void testPolymorphicQueryWithClassMethods() {
        def p1 = new P(id: 10l, commonProperty: "I am the searchable parent")
        def c1 = new C1(id: 10l, version: 0l, commonProperty: 'I am a searchable child one', childOneProperty: 'Child One')
        def c2 = new C2(id: 11l, version: 0l, commonProperty: 'I am a searchable child two', childTwoProperty: 'Child Two')
        def g1 = new GC(id: 11l, version: 0l, commonProperty: 'I am a searchable grandchild one', grandChildProperty: 'youngest')
        [p1, c1, c2, g1].each { it.index() }

        def objects = P.searchEvery("searchable")
        assert objects.size() == 4

        objects = C1.searchEvery("searchable")
        assert objects.size() == 2

        objects = C2.searchEvery("searchable")
        assert objects.size() == 1

        objects = GC.searchEvery("searchable")
        assert objects.size() == 1
    }

    // todo test searchable parent interface 
}