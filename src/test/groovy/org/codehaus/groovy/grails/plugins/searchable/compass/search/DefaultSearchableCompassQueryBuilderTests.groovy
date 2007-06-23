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

import org.apache.lucene.queryParser.*
import org.compass.core.*

import org.codehaus.groovy.grails.plugins.searchable.test.compass.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.compass.test.*

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*
import org.jmock.core.constraint.*
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils
import org.compass.core.mapping.osem.ClassMapping
import org.compass.core.mapping.CompassMapping
import org.compass.core.spi.InternalCompass

/**
*
* @author Maurice Nicholson
*/
class DefaultSearchableCompassQueryBuilderTests extends AbstractSearchableCompassTests {
    def builder = new DefaultSearchableCompassQueryBuilder()
    def compass

    void setUp() {
//        Thread.currentThread().setContextClassLoader(new GroovyClassLoader())
        compass = TestCompassFactory.getCompass([Post, Comment])
    }

    void tearDown() {
        compass.close()
        compass = null
    }

    void testBuildQueryWithString() {
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

            // bad query, no escape, with class
            shouldFail(ParseException) {
                query = builder.buildQuery(null, queryBuilder, "[special characters sometimes need to be escaped to avoid a runtime parse exception]", [class: Post, escape: false])
            }

            // Without escape, with class
            def mockQuery = new Mock(CompassQuery.class)
            def queryProxy = mockQuery.proxy()
            mockQuery.expects(new InvokeOnceMatcher()).method('setAliases').'with'(new IsEqual([SearchableCompassUtils.getMappingAlias(compass, Post)] as String[])).will(new ReturnStub(queryProxy))
            def mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)
            mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments().will(new ReturnStub(queryProxy))
            def mockCqb = new Mock(CompassQueryBuilder.class)
            mockCqb.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("some typical search term")).will(new ReturnStub(mockStringBuilder.proxy()))

            builder.buildQuery(compass, mockCqb.proxy(), "some typical search term", [escape: false, class: Post])

            mockCqb.verify()
            mockStringBuilder.verify()
            mockQuery.verify()

            // With class and escape combo
            mockQuery = new Mock(CompassQuery.class)
            queryProxy = mockQuery.proxy()
            mockQuery.expects(new InvokeOnceMatcher()).method('setAliases').'with'(new IsEqual([SearchableCompassUtils.getMappingAlias(compass, Comment)] as String[])).will(new ReturnStub(queryProxy))
            mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)
            mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments().will(new ReturnStub(queryProxy))
            mockCqb = new Mock(CompassQueryBuilder.class)
            mockCqb.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual(/\[special characters sometimes need to be escaped to avoid a runtime parse exception\]/)).will(new ReturnStub(mockStringBuilder.proxy()))

            builder.buildQuery(compass, mockCqb.proxy(), "[special characters sometimes need to be escaped to avoid a runtime parse exception]", [class: Comment, escape: true])

            mockCqb.verify()
            mockStringBuilder.verify()
            mockQuery.verify()
        }
    }

    void testBuildQueryWithClosure() {
        // test that the DefaultSearchableCompassQueryBuilder handles query building with a Closure
        // more thorough tests in GroovyCompassQueryBuilderTests
        withCompassSession { compassSession ->
            // Normal string queries are as expected
            def queryBuilder = compassSession.queryBuilder()
            def query = builder.buildQuery(null, queryBuilder, [:]) {
                queryString("Hello WORLD")
            }
            assert query.toString() == "all:hello all:world"

            // escape *does not affect string queries* when using a builder closure
            // this is more of an assertion of "not yet implemented" rather than a purposeful feature
            shouldFail {
                query = builder.buildQuery(null, queryBuilder, [escape: true]) {
                    queryString("[this is a bad query]")
                }
            }

            // we can create complex queries quickly
            query = builder.buildQuery(null, queryBuilder, [:]) {
                multiPropertyQueryString("Hello Searchable Plugin") {
                    add("title")
                    add("summary")
                }
            }
            assert query.toString() == "(title:hello summary:hello) (title:searchable summary:searchable) (title:plugin summary:plugin)" // internally Compass probably creates nested booleans

            // class *does have an effect* using the builder
            def mockQuery = new Mock(CompassQuery.class)
            def queryProxy = mockQuery.proxy()
            mockQuery.expects(new InvokeOnceMatcher()).method('setAliases').'with'(new IsEqual([SearchableCompassUtils.getMappingAlias(compass, Post)] as String[])).will(new ReturnStub(queryProxy))
            def mockCqb = new Mock(CompassQueryBuilder.class)
            def cqbProxy = mockCqb.proxy()
            mockCqb.expects(new InvokeOnceMatcher()).method('term').'with'(new IsEqual("keywords"), new IsEqual('groovy')).will(new ReturnStub(queryProxy))
//            mockCqb.expects(new InvokeOnceMatcher()).method('toQuery')//.'with'(new IsEqual("keywords"), new IsEqual('groovy')).will(new ReturnStub(cqbProxy))

            query = builder.buildQuery(compass, cqbProxy, [class: Post]) {
                term("keywords", "groovy")
            }

            mockCqb.verify()
            mockQuery.verify()
        }
    }
}