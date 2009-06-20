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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.termvector

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class TermVectorTests extends SearchableFunctionalTestCase {
    def compass

    Collection<Class<?>> getDomainClasses() {
        return [Yes, No]
    }

    void testTermVectorYes() {
        new Yes(id: 1l, value: "term term term term term term term term term term term").index()
        def tf = Yes.termFreqs("value")
        assert tf[0].term == "term"
    }

    void testTermVectorNo() {
        new No(id: 1l, value: "term term term term term term term term term term term").index()
        def tf = No.termFreqs("value")
        assert tf[0].term == "term" // no sure why this still works!? todo inspect index with Luke

        def s = compass.openSession()
        def tx = s.beginTransaction()

        def r = s.loadResource(No, 1l)
        assert !r.getProperty("value").isTermVectorStored()

        tx.commit()
        s.close()
    }
}
