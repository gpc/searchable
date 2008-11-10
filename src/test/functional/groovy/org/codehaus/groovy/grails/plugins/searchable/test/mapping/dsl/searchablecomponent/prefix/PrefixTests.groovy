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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.searchablecomponent.prefix

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class PrefixTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [Owner, Component]
    }

    void testPrefix() {
        def prefixed = new Component(id: 1l, value: "prefixed")
        def unPrefixed = new Component(id: 2l, value: "unprefixed")
        def owner = new Owner(id: 1l, prefixed: prefixed, unPrefixed: unPrefixed)
        owner.index()

        owner = Owner.search("unprefixed", result: 'top')
        assert owner instanceof Owner

        owner = Owner.search("prefixed", result: 'top')
        assert owner instanceof Owner

        owner = Owner.search("value:unprefixed", result: 'top')
        assert owner instanceof Owner

        owner = Owner.search('prefixed$value:prefixed', result: 'top')
        assert owner instanceof Owner
    }
}