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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.boost

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class BoostTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [XxxBoosted, AaaUnBoosted]
    }

    void testBoost() {
        new XxxBoosted(id: 1l, value: "value").index()
        new AaaUnBoosted(id: 1l, value: "value").index()

        def hits = searchableService.searchEvery("value")
        assert hits[0] instanceof XxxBoosted
        assert hits[1] instanceof AaaUnBoosted
    }
    
    void testBoostAlternateInsertionOrder() {
        new AaaUnBoosted(id: 1l, value: "value").index()
        new XxxBoosted(id: 1l, value: "value").index()

        def hits = searchableService.searchEvery("value")
        assert hits[0] instanceof XxxBoosted
        assert hits[1] instanceof AaaUnBoosted
    }
}
