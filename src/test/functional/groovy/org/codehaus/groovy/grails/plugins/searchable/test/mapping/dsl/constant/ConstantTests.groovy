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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.constant

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class ConstantTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [A]
    }

    void testConstant() {
        new A(id: 1l, value: "whatever").index()

        assert A.search(result: 'top', 'my_constant:my')

        assert A.search(result: 'top') {
            must {
                term('other_constant', 'other 1')
                term('other_constant', 'other 2')
            }
        }
    }
}