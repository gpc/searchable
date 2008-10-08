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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.id.name

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class NameTests extends SearchableFunctionalTestCase {
    def compass

    public getDomainClasses() {
        return [A]
    }

    void testIdName() {
        def c = new A(id: 1l, value: 'blah')
        assert c.save(), c.errors
        c.index()

        assert A.findById(1l) // find with GORM by "id" property

        assert A.search('instanceId:1', result: 'top') // find in index with "instanceId" property
    }
}
