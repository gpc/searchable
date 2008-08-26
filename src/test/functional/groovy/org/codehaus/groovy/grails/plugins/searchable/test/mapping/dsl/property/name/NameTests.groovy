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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.name

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class NameTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [A]
    }

    void testName() {
        new A(id: 1l, value1: "has an alternate property name in index", value2: "has its own name in the index").index()
        assert A.searchTop("value2:own")
        assert A.searchTop("value1_internal:alternate")
        assert !A.searchTop("value1:alternate")
    }
}
