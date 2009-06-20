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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.reverse

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class ReverseTests extends SearchableFunctionalTestCase {

    Collection<Class<?>> getDomainClasses() {
        return [NoReverse, StringReverse]
    }

    void testReverse() {
        new NoReverse(id: 1l, value: "not reversed").index()
        def nr = NoReverse.searchTop("reversed")
        assert nr.value == "not reversed"

        new StringReverse(id: 1l, value: "not reversed").index()
        def sr = StringReverse.searchTop("reversed")
        assert !sr
        sr = StringReverse.searchTop("desrever")
        assert sr.value == "not reversed", sr.value
    }
}
