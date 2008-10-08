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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.id.composite

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class CompositeIdTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [A]
    }

    void testCompositeId() {
        // demonstrates that the number of unique domain class instances items in the index is defined
        // by the number of unique keys
        // It would be more accurate to test the generated mapping or inspect the internal Compass mapping or
        // resource, but I'd rather not tie the impl to Compass

        new A(a: "aa", b: "bb", value: "tomato").index()
        assert A.countHits("tomato") == 1

        // same composite id as first, count is still 1
        new A(a: "aa", b: "bb", value: "tomato").index()
        assert A.countHits("tomato") == 1

        // different composite id, count increases
        new A(a: "aaa", b: "bb", value: "tomato").index()
        assert A.countHits("tomato") == 2

        // another new composite id, count increases
        new A(a: "aa", b: "bbb", value: "tomato").index()
        assert A.countHits("tomato") == 3

        // another new composite id, count increases
        new A(a: "aaa", b: "bbb", value: "tomato").index()
        assert A.countHits("tomato") == 4

        // duplicate composite id, count is same
        new A(a: "aaa", b: "bbb", value: "tomato").index()
        assert A.countHits("tomato") == 4
    }
}
