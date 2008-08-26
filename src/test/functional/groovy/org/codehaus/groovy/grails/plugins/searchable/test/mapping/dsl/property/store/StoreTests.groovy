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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.store

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase
import org.compass.core.Resource

/**
 * @author Maurice Nicholson
 */
class StoreTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [Yes, No, Compress]
    }

    void testStoreYes() {
        new Yes(id: 1l, value: "value").index()
        def yes = searchableService.searchTop("value")
        assert yes.value == "value"
    }

    void testStoreNo() {
        new No(id: 1l, value: "value").index()
        def no = searchableService.searchTop("value")
        assert no.value == null // not stored in index, so not populated in object
    }

    void testStoreCompress() {
        new Compress(id: 1l, value: "value").index()
        def compress = searchableService.searchTop("value")
        assert compress.value == "value"

        def s = compass.openSession()
        def tx = s.beginTransaction()

        Resource resource = s.loadResource(Compress, 1l)
        assert resource.getProperty("value").isCompressed()

        tx.commit()
        s.close()
    }
}
