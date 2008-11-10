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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.multiplemappings

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
// todo this isn't a feature of the DSL as the others in this parent package - re-house them
class MultipleMappingsTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [A]
    }

    void testMultipleMappings() {
        new A(id: 1l, value: "The VALUE").index()
        assert A.search("value:value", result: 'top')
        assert A.search(result: 'top') {
            term("value_un_tokenized", "The VALUE")
        }
    }
}