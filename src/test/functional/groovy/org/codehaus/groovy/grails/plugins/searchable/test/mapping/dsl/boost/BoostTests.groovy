/*
 * Copyright 2008 the original author or authors.
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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.boost

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class BoostTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [XxxBoosted, AaaUnBoosted]
    }

    void testBoost() {
        for (i in 0..9) {
            def b = new XxxBoosted(id: i as Long, value: "boost test")
            b.index()
            def u = new AaaUnBoosted(id: i as Long, value: "boost test")
            u.index()
        }

        def every = searchableService.searchEvery("boost test")
        assert every.size() == 20

        assert every[0..9]*.class.unique() == [XxxBoosted]
        assert every[10..19]*.class.unique() == [AaaUnBoosted]
    }

    void testBoostAlternateIndexInsertionorder() {
        for (i in 0..9) {
            def b = new AaaUnBoosted(id: i as Long, value: "boost test")
            b.index()
            def u = new XxxBoosted(id: i as Long, value: "boost test")
            u.index()
        }

        def every = searchableService.searchEvery("boost test")
        assert every.size() == 20

        assert every[0..9]*.class.unique() == [XxxBoosted]
        assert every[10..19]*.class.unique() == [AaaUnBoosted]
    }
}