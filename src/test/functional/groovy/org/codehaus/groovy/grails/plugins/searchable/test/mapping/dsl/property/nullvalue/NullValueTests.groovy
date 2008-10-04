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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.nullvalue

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class NullValueTests extends SearchableFunctionalTestCase {
    def compass

    public getDomainClasses() {
        return [Default, Custom]
    }

    void testDefault() {
        new Default(id: 1l).index()

        def s = compass.openSession()
        def tx = s.beginTransaction()
        def r = s.loadResource(Default, 1l)
        assert r.getProperty("value") == null
        tx.commit()
        s.close()
    }

    void testCustom() {
        new Custom(id: 1l).index()
        assert Custom.searchTop("is_null") // note null value is tokenized!
        assert Custom.searchTop("IS_NULL") // uppercase search only works because query is tokenized too
        assert null == Custom.searchTop { // searching for the un-tokenized term returns nothing!
            term("value", "IS_NULL")
        }
    }
}

