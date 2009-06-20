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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.supportunmarhall

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase
import org.compass.core.Resource

/**
 * @author Maurice Nicholson
 */
class SupportUnmarshallTests extends SearchableFunctionalTestCase {
    def compass

    Collection<Class<?>> getDomainClasses() {
        return [Supported, Unsupported]
    }

    void testSupported() {
        new Supported(id: 1l, value: "unmarshall supported").index()

        // search result object is populated from search index
        def hit = Supported.searchTop("unmarshall")
        assert hit
        assert hit.id == 1l
        assert hit.value == "unmarshall supported"
    }

    void testUnsupported() {
        new Unsupported(id: 1l, value: "unmarshall not supported").index()

        // search result object is NOT populated from search index
        def hit = Unsupported.searchTop("unmarshall")
        assert hit
        assert hit.id == 1l
        assert hit.value == null

        // but data is still in the index if required
        def s = compass.openSession()
        def tx = s.beginTransaction()

        Resource resource = s.loadResource(Unsupported, 1l)
        String value = resource.getProperty("value").getStringValue()
        assert value == "unmarshall not supported", value

        tx.commit();
        s.close()
    }
}