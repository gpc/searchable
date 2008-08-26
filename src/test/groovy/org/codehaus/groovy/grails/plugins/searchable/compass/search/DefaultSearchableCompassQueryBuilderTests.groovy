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
import org.compass.core.engine.SearchEngineQueryParseException
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompassMappingUtils

/**
*
* @author Maurice Nicholson
*/
class DefaultSearchableCompassQueryBuilderTests extends AbstractSearchableCompassTestCase {
    def builder = new DefaultSearchableCompassQueryBuilder()
    def compass
    def grailsApplication

    void setUp() {
//        Thread.currentThread().setContextClassLoader(new GroovyClassLoader())
        grailsApplication = TestCompassFactory.getGrailsApplication([Post, Comment])
        compass = TestCompassFactory.getCompass(grailsApplication)
    }

    void tearDown() {
        compass.close()
        compass = null
        grailsApplication = null
    }

    void testBuildQueryWithString() {
        withCompassSession { compassSession ->
            // No class, no escape
            def query = builder.buildQuery(grailsApplication, compassSession, [escape: false], "Hello World")
            assert query.toString() == "+hello +world", query.toString()

            // escape does not affect normal queries
            query = builder.buildQuery(grailsApplication, compassSession, [escape: true], "Hello OR World")
            assert query.toString() == "hello world", query.toString()

            // no escape, bad query
            shouldFail {
                builder.buildQuery(grailsApplication, compassSession, [escape: false], "[this is a bad query}")
            }

            // should not fail with escape
            builder.buildQuery(grailsApplication, compassSession, [escape: true], "[this is a bad query}")

            // bad query, no escape, with class
            shouldFail(SearchEngineQueryParseException) {
                query = builder.buildQuery(grailsApplication, compassSession, [class: Post, escape: false], "[special characters sometimes need to be escaped to avoid a runtime parse exception]")
            }

            // Without escape, with class
            def mockQuery = new Mock(CompassQuery.class)
            def queryProxy = mockQuery.proxy()
            mockQuery.expects(new InvokeOnceMatcher()).method('setAliases').'with'(new IsEqual([CompassMappingUtils.getMappingAlias(compass, Post)] as String[])).will(new ReturnStub(queryProxy))
            def mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)
            mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments().will(new ReturnStub(queryProxy))
            def mockCqb = new Mock(CompassQueryBuilder.class)
            mockCqb.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual("some typical search term")).will(new ReturnStub(mockStringBuilder.proxy()))

            def mockSession = [
                queryBuilder: { mockCqb.proxy() }
            ] as CompassSession
            builder.compass = compass
            builder.buildQuery(grailsApplication, mockSession, [escape: false, class: Post], "some typical search term")

            mockCqb.verify()
            mockStringBuilder.verify()
            mockQuery.verify()

            // With class and escape combo
            mockQuery = new Mock(CompassQuery.class)
            queryProxy = mockQuery.proxy()
            mockQuery.expects(new InvokeOnceMatcher()).method('setAliases').'with'(new IsEqual([CompassMappingUtils.getMappingAlias(compass, Comment)] as String[])).will(new ReturnStub(queryProxy))
            mockStringBuilder = new Mock(CompassQueryBuilder.CompassQueryStringBuilder.class)
            mockStringBuilder.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments().will(new ReturnStub(queryProxy))
            mockCqb = new Mock(CompassQueryBuilder.class)
            mockCqb.expects(new InvokeOnceMatcher()).method('queryString').'with'(new IsEqual(/\[special characters sometimes need to be escaped to avoid a runtime parse exception\]/)).will(new ReturnStub(mockStringBuilder.proxy()))

            mockSession = [
                queryBuilder: { mockCqb.proxy() }
            ] as CompassSession
            builder.buildQuery(grailsApplication, mockSession, [class: Comment, escape: true], "[special characters sometimes need to be escaped to avoid a runtime parse exception]")

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
            def query = builder.buildQuery(grailsApplication, compassSession, [:]) {
                queryString("Hello OR WORLD")
            }
            assert query.toString() == "hello world", query.toString()

            // escape *does not affect string queries* when using a builder closure
            // this is more of an assertion of "not yet implemented" rather than a purposeful feature
            shouldFail {
                query = builder.buildQuery(grailsApplication, compassSession, [escape: true]) {
                    queryString("[this is a bad query]")
                }
            }

            // we can create complex queries quickly
            query = builder.buildQuery(grailsApplication, compassSession, [:]) {
                multiPropertyQueryString("Hello Searchable Plugin") {
                    add("title")
                    add("summary")
                }
            }
            assert query.toString() == "+(title:hello summary:hello) +(title:searchable summary:searchable) +(title:plugin summary:plugin)", query.toString()

            // class *does have an effect* using the builder
            def mockQuery = new Mock(CompassQuery.class)
            def queryProxy = mockQuery.proxy()
            mockQuery.expects(new InvokeOnceMatcher()).method('setAliases').'with'(new IsEqual([CompassMappingUtils.getMappingAlias(compass, Post)] as String[])).will(new ReturnStub(queryProxy))
            def mockCqb = new Mock(CompassQueryBuilder.class)
            def cqbProxy = mockCqb.proxy()
            mockCqb.expects(new InvokeOnceMatcher()).method('term').'with'(new IsEqual("keywords"), new IsEqual('groovy')).will(new ReturnStub(queryProxy))
//            mockCqb.expects(new InvokeOnceMatcher()).method('toQuery')//.'with'(new IsEqual("keywords"), new IsEqual('groovy')).will(new ReturnStub(cqbProxy))

            def mockSession = [
                queryBuilder: {
                    cqbProxy
                }
            ] as CompassSession
            builder.compass = compass
            query = builder.buildQuery(grailsApplication, mockSession, [class: Post]) {
                term("keywords", "groovy")
            }

            mockCqb.verify()
            mockQuery.verify()
        }
    }
}
