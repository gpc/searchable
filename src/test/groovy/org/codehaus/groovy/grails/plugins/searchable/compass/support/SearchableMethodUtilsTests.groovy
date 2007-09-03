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
package org.codehaus.groovy.grails.plugins.searchable.compass.support

/**
 * @author Maurice Nicholson
 */
class SearchableMethodUtilsTests extends GroovyTestCase {

    void testGetOptionsArgument() {
        // Null arg not allowed
        shouldFail {
            SearchableMethodUtils.getOptionsArgument(null, [:])
        }

        // Empty args ok
        def options = SearchableMethodUtils.getOptionsArgument([] as Object[], null)
        assert options == [:]

        def defaultOptions = [:]
        options = SearchableMethodUtils.getOptionsArgument([] as Object[], defaultOptions)
        assert options == [:]
        assert !options.is(defaultOptions)

        // Null defaults
        def userOptions = [a: 1, b: 2]
        options = SearchableMethodUtils.getOptionsArgument([userOptions, "hello"] as Object[], null)
        assert options == [a: 1, b: 2]
        assert !options.is(userOptions) // should NOT use the original to avoid corruption of user variables

        // No defaults
        userOptions = [a: 1, b: 2]
        options = SearchableMethodUtils.getOptionsArgument([userOptions, "hello"] as Object[], defaultOptions)
        assert options == [a: 1, b: 2]
        assert !options.is(userOptions) // should NOT use the original to avoid corruption of user variables
        assert !options.is(defaultOptions) // same

        // With defaults, user option overrides default
        defaultOptions = [a: "a"]
        userOptions = [a: 1, b: 2, c: 3]
        options = SearchableMethodUtils.getOptionsArgument(["hello", userOptions] as Object[], defaultOptions)
        assert options == [a: 1, b: 2, c: 3]
        assert !options.is(userOptions)
        assert !options.is(defaultOptions)

        // with defaults, default takes place of missing user option
        userOptions = [c: 3]
        options = SearchableMethodUtils.getOptionsArgument(["hello", userOptions] as Object[], defaultOptions)
        assert options == [a: "a", c: 3]
        assert !options.is(userOptions)
        assert !options.is(defaultOptions)

        userOptions = [:] // empty map ?
        options = SearchableMethodUtils.getOptionsArgument(["between", userOptions, "hello"] as Object[], defaultOptions)
        assert options == [a: "a"] // just defaults now
        assert !options.is(userOptions)
        assert !options.is(defaultOptions)

        userOptions = null // missing user options arg
        options = SearchableMethodUtils.getOptionsArgument(["hello", userOptions] as Object[], defaultOptions)
        assert options == [a: "a"] // user option overrides default
        assert !options.is(userOptions)
        assert !options.is(defaultOptions)
    }
}