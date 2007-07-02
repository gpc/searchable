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
import org.compass.core.config.*

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*
import org.jmock.core.constraint.*

/**
 * 
 *
 * @author Maurice Nicholson
 */
class SearchableCompassQueryBuilderSortOptionHelperTests extends GroovyTestCase {
    def helper = new SearchableCompassQueryBuilderSortOptionHelper()
    def compass

    void setUp() {
        compass = getCompass()
    }

    void tearDown() {
        compass.close()
        compass = null
    }

    /*
        [sort: 'modifiedDate'] -- chronological
        [sort: 'modifiedDate', direction: 'auto'] -- chronological
        [sort: 'modifiedDate', direction: 'reverse'] -- reverse chronological
        [sort: 'modifiedDate', order: 'asc'] -- chronological
        [sort: 'modifiedDate', order: 'desc'] -- reverse chronological
        [sort: 'modifiedDate', direction: 'asc'] -- chronological
        [sort: 'modifiedDate', direction: 'desc'] -- reverse chronological
        [sort: 'modifiedDate', order: 'auto'] -- chronological
        [sort: 'modifiedDate', order: 'reverse'] -- reverse chronological

        [sort: 'name'] -- alphabetical
        [sort: 'name', direction: 'auto'] -- alphabetical
        [sort: 'name', direction: 'reverse'] -- reverse alphabetical
        [sort: 'name', order: 'asc'] -- alphabetical
        [sort: 'name', order: 'desc'] -- reverse alphabetical
        [sort: 'name', direction: 'asc'] -- alphabetical
        [sort: 'name', direction: 'desc'] -- reverse alphabetical
        [sort: 'name', order: 'auto'] -- alphabetical
        [sort: 'name', order: 'reverse'] -- reverse alphabetical

        [sort: 'SCORE'] -- decreasing relevance
        [sort: 'SCORE', direction: 'auto'] -- decreasing relevance
        [sort: 'SCORE', direction: 'reverse'] -- increasing relevance
        [sort: 'SCORE', order: 'desc'] -- decreasing relevance
        [sort: 'SCORE', order: 'asc'] -- increasing relevance
        [sort: 'SCORE', direction: 'asc'] -- increasing relevance
        [sort: 'SCORE', direction: 'desc'] -- decreasing relevance
        [sort: 'SCORE', order: 'auto'] -- increasing relevance
        [sort: 'SCORE', order: 'reverse'] -- decreasing relevance
     */

    void testApplySortNoSort() {
        // should not fail: would get NullPointerException if attempted to add sort
        helper.applyOptions(null, null, null, [:])
    }


    void testValidAndInvalidSortOrderAndDirections() {
        shouldFail(IllegalArgumentException) {
            helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [order: 'asc', direction: 'desc'])
        }
        shouldFail(IllegalArgumentException) {
            helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [order: 'nonsense'])
        }
        shouldFail(IllegalArgumentException) {
            helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [direction: 'rubbish'])
        }

        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [:]) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [order: 'desc']) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [order: 'asc']) == CompassQuery.SortDirection.REVERSE
        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [order: 'auto']) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [order: 'reverse']) == CompassQuery.SortDirection.REVERSE
        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [direction: 'desc']) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [direction: 'asc']) == CompassQuery.SortDirection.REVERSE
        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [direction: 'auto']) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection(CompassQuery.SortImplicitType.SCORE, [direction: 'reverse']) == CompassQuery.SortDirection.REVERSE

        assert helper.getSortDirection('name', [order: 'desc']) == CompassQuery.SortDirection.REVERSE
        assert helper.getSortDirection('name', [order: 'asc']) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection('name', [order: 'auto']) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection('name', [order: 'reverse']) == CompassQuery.SortDirection.REVERSE
        assert helper.getSortDirection('name', [direction: 'desc']) == CompassQuery.SortDirection.REVERSE
        assert helper.getSortDirection('name', [direction: 'asc']) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection('name', [direction: 'auto']) == CompassQuery.SortDirection.AUTO
        assert helper.getSortDirection('name', [direction: 'reverse']) == CompassQuery.SortDirection.REVERSE
    }

    void testQueryWithSortByProperty() {
        testSortByPropery('modifiedDate', [sort: 'modifiedDate'], CompassQuery.SortDirection.AUTO)
        testSortByPropery('modifiedDate', [sort: 'modifiedDate', direction: 'auto'], CompassQuery.SortDirection.AUTO)
        testSortByPropery('modifiedDate', [sort: 'modifiedDate', direction: 'reverse'], CompassQuery.SortDirection.REVERSE)
        testSortByPropery('modifiedDate', [sort: 'modifiedDate', order: 'asc'], CompassQuery.SortDirection.AUTO)
        testSortByPropery('modifiedDate', [sort: 'modifiedDate', order: 'desc'], CompassQuery.SortDirection.REVERSE)
        testSortByPropery('modifiedDate', [sort: 'modifiedDate', direction: 'asc'], CompassQuery.SortDirection.AUTO)
        testSortByPropery('modifiedDate', [sort: 'modifiedDate', direction: 'desc'], CompassQuery.SortDirection.REVERSE)
        testSortByPropery('modifiedDate', [sort: 'modifiedDate', order: 'auto'], CompassQuery.SortDirection.AUTO)
        testSortByPropery('modifiedDate', [sort: 'modifiedDate', order: 'reverse'], CompassQuery.SortDirection.REVERSE)
    }

    private void testSortByPropery(propertyName, options, sortDirection) {
        def mockQueryBuilder = new Mock(CompassQueryBuilder.class)
        def mockQuery = new Mock(CompassQuery.class)

        def proxy = mockQuery.proxy()
        mockQuery.expects(new InvokeOnceMatcher()).method('addSort').'with'(new IsEqual(propertyName), new IsEqual(sortDirection)).will(new ReturnStub(proxy))

        helper.applyOptions(null, [queryBuilder: {mockQueryBuilder.proxy()}] as CompassSession, proxy, options)

        mockQuery.verify()
    }

    void testQueryWithSortByScore() {
        testSortByScore([sort: 'SCORE'], CompassQuery.SortDirection.AUTO)
        testSortByScore([sort: 'SCORE', direction: 'auto'], CompassQuery.SortDirection.AUTO)
        testSortByScore([sort: 'SCORE', direction: 'reverse'], CompassQuery.SortDirection.REVERSE)
        testSortByScore([sort: 'SCORE', order: 'desc'], CompassQuery.SortDirection.AUTO)
        testSortByScore([sort: 'SCORE', order: 'asc'], CompassQuery.SortDirection.REVERSE)
        testSortByScore([sort: 'SCORE', direction: 'asc'], CompassQuery.SortDirection.REVERSE)
        testSortByScore([sort: 'SCORE', direction: 'desc'], CompassQuery.SortDirection.AUTO)
        testSortByScore([sort: 'SCORE', order: 'auto'], CompassQuery.SortDirection.AUTO)
        testSortByScore([sort: 'SCORE', order: 'reverse'], CompassQuery.SortDirection.REVERSE)
    }

    private void testSortByScore(options, sortDirection) {
        def mockQueryBuilder = new Mock(CompassQueryBuilder.class)
        def mockQuery = new Mock(CompassQuery.class)

        def proxy = mockQuery.proxy()
        mockQuery.expects(new InvokeOnceMatcher()).method('addSort').'with'(new IsEqual(CompassQuery.SortImplicitType.SCORE), new IsEqual(sortDirection)).will(new ReturnStub(proxy))

        helper.applyOptions(null, [queryBuilder: {mockQueryBuilder.proxy()}] as CompassSession, proxy, options)

        mockQuery.verify()
    }

    def getCompass() {
        def conf = new CompassConfiguration()
        conf.connection = "ram://testindex"
        return conf.buildCompass()
    }

    def withCompassSession(closure) {
        def session = compass.openSession()
        def trans = session.beginTransaction()
        def result
        try {
            result = closure(session)
        } finally {
            trans.commit()
            session.close()
        }
        return result
    }
}