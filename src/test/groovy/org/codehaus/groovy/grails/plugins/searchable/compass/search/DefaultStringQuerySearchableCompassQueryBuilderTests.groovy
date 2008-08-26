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
class DefaultStringQuerySearchableCompassQueryBuilderTests extends AbstractSearchableCompassTestCase {
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

        builder.buildQuery(null, [queryBuilder : {mockBuilder.proxy()}] as CompassSession, [:], "chocolate biscuits")

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

        def mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [analyzer: 'myFunkyAnalyzer'], "banana")

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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [parser: 'myCustomParser'], "orange")

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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [queryParser: 'myCustomParser'], "lemon")

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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [defaultProperty: 'keywords'], "apple")

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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [defaultSearchProperty: 'description'], "apple")

        mockBuilder.verify()
        mockStringBuilder.verify()

        // @deprecated todo emit deprecation warning on usage
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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [andDefaultOperator: true], "kiwi")

        mockBuilder.verify()
        mockStringBuilder.verify()

        // @deprecated todo emit deprecation warning on usage
        // Default AND operator = false; means use OR
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("lime")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('useOrDefaultOperator').withNoArguments().will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [andDefaultOperator: false], "lime")

        mockBuilder.verify()
        mockStringBuilder.verify()

        // @deprecated todo emit deprecation warning on usage
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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [useAndDefaultOperator: true], "tomato")

        mockBuilder.verify()
        mockStringBuilder.verify()

        // @deprecated todo emit deprecation warning on usage
        // Default AND operator = false; means use OR
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("pear")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('useOrDefaultOperator').withNoArguments().will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [useAndDefaultOperator: false], "pear")

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Default operator = 'and'
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("pear")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('useAndDefaultOperator').withNoArguments().will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [defaultOperator: 'and'], "pear")

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Default operator = 'or'
        mockBuilder = new Mock(CompassQueryBuilder.class)
        mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)

        stringBuilderProxy = mockStringBuilder.proxy()
        mockBuilder.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("pear")).will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('useOrDefaultOperator').withNoArguments().will(
            new ReturnStub(stringBuilderProxy)
        )
        mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments()

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [defaultOperator: 'or'], "pear")

        mockBuilder.verify()
        mockStringBuilder.verify()

        // invalid value
        shouldFail(IllegalArgumentException) {
            builder.buildQuery(null, [queryBuilder: {[queryString: {[:] as CompassQueryBuilder.CompassQueryStringBuilder}] as CompassQueryBuilder}] as CompassSession, [defaultOperator: 'um'], "pear")
        }

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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [analyzer: 'myFunkyAnalyzer', parser: 'myCustomParser', defaultProperty: 'title', andDefaultOperator: true], "pear")

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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [properties: ['title', 'description']], "blah blah")

        mockBuilder.verify()
        mockStringBuilder.verify()

        // Properties and defaultSearchProperty/defaultProperty - not compatible
        shouldFail(IllegalArgumentException) {
            builder.buildQuery(null, null, [defaultSearchProperty: 'anything', properties: ['title', 'description']], "blah blah")
        }

        shouldFail(IllegalArgumentException) {
            builder.buildQuery(null, null, [defaultProperty: 'anything', properties: ['title', 'description']], "blah blah")
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

        mockSession = [
            queryBuilder: { mockBuilder.proxy() }
        ] as CompassSession
        builder.buildQuery(null, mockSession, [analyzer: 'myFunkyAnalyzer', parser: 'myCustomParser', properties: ['title', 'description'], andDefaultOperator: true], "chicken and pig")

        mockBuilder.verify()
        mockStringBuilder.verify()
    }

    void testWithEscape() {
        withCompassSession { compassSession ->
            // No class, no escape
            def query = builder.buildQuery(null, compassSession, [escape: false], "Hello World")
            assert query.toString() == "+hello +world"

            // escape does not affect normal queries
            query = builder.buildQuery(null, compassSession, [escape: true], "Hello World")
            assert query.toString() == "+hello +world"

            // no escape, bad query
            shouldFail {
                builder.buildQuery(null, compassSession, [escape: false], "[this is a bad query}")
            }

            // should not fail with escape
            query = builder.buildQuery(null, compassSession, [escape: true], "[this is a bad query}")
            assert query.toString() == "+bad +query"
        }
    }
}
