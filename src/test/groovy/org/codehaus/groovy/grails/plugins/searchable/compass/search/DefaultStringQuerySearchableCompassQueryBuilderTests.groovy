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
package org.codehaus.groovy.grails.plugins.searchable.compass.search

import org.compass.core.*

import org.codehaus.groovy.grails.plugins.searchable.test.compass.*
import org.codehaus.groovy.grails.plugins.searchable.compass.test.*

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*
import org.jmock.core.constraint.*

/**
 * 
 *
 * @author Maurice Nicholson
 */
class DefaultStringQuerySearchableCompassQueryBuilderTests extends AbstractSearchableCompassTests {
    def compass
    def builder = new DefaultStringQuerySearchableCompassQueryBuilder()

    void setUp() {
        compass = TestCompassFactory.getCompass([])
    }

    void tearDown() {
        compass.close()
        compass = null
    }

    void testBasicQueryString() {
        def mockBuilder = new Mock(CompassQueryBuilder.class)
        def mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("chocolate biscuits")).will(
            new ReturnStub(mockStringBuilder.proxy())
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "chocolate biscuits", [:])
        mockBuilder.verify()
        mockStringBuilder.verify()
    }

    void testWithOptions() {
        // First test individual options

        // Analyzer
        def mockBuilder = new Mock(CompassQueryBuilder.class)
        def mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        def stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("banana")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setAnalyzer').'with'(new IsEqual('myFunkyAnalyzer')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "banana", [analyzer: 'myFunkyAnalyzer'])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Query parser ("parser" shorthand option)
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("orange")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setQueryParser').'with'(new IsEqual('myCustomParser')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "orange", [parser: 'myCustomParser'])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Query parser ("queryParser" full name option variant)
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("lemon")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setQueryParser').'with'(new IsEqual('myCustomParser')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "lemon", [queryParser: 'myCustomParser'])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Default search property ("defaultProperty" shorthand option)
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("apple")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setDefaultSearchProperty').'with'(new IsEqual('keywords')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "apple", [defaultProperty: 'keywords'])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Default search property ("defaultSearchProperty" full name variety)
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("apple")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setDefaultSearchProperty').'with'(new IsEqual('description')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "apple", [defaultSearchProperty: 'description'])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Default AND operator ("andDefaultOperator" shorthand)
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("kiwi")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('useAndDefaultOperator').withNoArguments().will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "kiwi", [andDefaultOperator: true])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Default AND operator = false has no effect
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("lime")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "lime", [andDefaultOperator: false])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Default AND operator ("useAndDefaultOperator" full name variant)
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("tomato")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('useAndDefaultOperator').withNoArguments().will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "tomato", [useAndDefaultOperator: true])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Default AND operator = false has no effect
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("pear")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "pear", [useAndDefaultOperator: false])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // All together for basic string query
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("pear")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setAnalyzer').'with'(new IsEqual('myFunkyAnalyzer')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setQueryParser').'with'(new IsEqual('myCustomParser')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setDefaultSearchProperty').'with'(new IsEqual('title')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('useAndDefaultOperator').withNoArguments().will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "pear", [analyzer: 'myFunkyAnalyzer', parser: 'myCustomParser', defaultProperty: 'title', andDefaultOperator: true])

        mockBuilder.verify()
        mockStringBuilder.verify()


        // Multi-property query string - properties
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassMultiPropertyQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('multiPropertyQueryString').'with'(new IsEqual("blah blah")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('add').'with'(new IsEqual('title')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('add').'with'(new IsEqual('description')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "blah blah", [properties: ['title', 'description']])

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Properties and defaultSearchProperty/defaultProperty - not compatible
        shouldFail {
            builder.buildQuery(null, null, "blah blah", [defaultSearchProperty: 'anything', properties: ['title', 'description']])
        }

        shouldFail {
            builder.buildQuery(null, null, "blah blah", [defaultProperty: 'anything', properties: ['title', 'description']])
        }

        // All together for multi-property string query
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassMultiPropertyQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('multiPropertyQueryString').'with'(new IsEqual("chicken and pig")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('add').'with'(new IsEqual('description')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('add').'with'(new IsEqual('title')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setAnalyzer').'with'(new IsEqual('myFunkyAnalyzer')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('setQueryParser').'with'(new IsEqual('myCustomParser')).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('useAndDefaultOperator').withNoArguments().will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        builder.buildQuery(null, mockBuilder.proxy(), "chicken and pig", [analyzer: 'myFunkyAnalyzer', parser: 'myCustomParser', properties: ['title', 'description'], andDefaultOperator: true])

        mockBuilder.verify()
        mockStringBuilder.verify()
    }

    void testWithEscape() {
        withCompassSession { compassSession ->
            // No class, no escape
            def queryBuilder = compassSession.queryBuilder()
            def query = builder.buildQuery(null, queryBuilder, "Hello World", [escape: false])
            assert query.toString() == "all:hello all:world"

            // escape does not affect normal queries
            query = builder.buildQuery(null, queryBuilder, "Hello World", [escape: true])
            assert query.toString() == "all:hello all:world"

            // no escape, bad query
            shouldFail {
                builder.buildQuery(null, queryBuilder, "[this is a bad query}", [escape: false])
            }

            // should not fail with escape
            builder.buildQuery(null, queryBuilder, "[this is a bad query}", [escape: true])
        }
    }
}