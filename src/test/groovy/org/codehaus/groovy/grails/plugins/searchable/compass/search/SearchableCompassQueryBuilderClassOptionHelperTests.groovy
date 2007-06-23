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
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*
import org.jmock.core.constraint.*
import org.compass.core.mapping.osem.ClassMapping
import org.compass.core.mapping.CompassMapping
import org.compass.core.spi.InternalCompass

/**
*
*
* @author Maurice Nicholson
*/
class SearchableCompassQueryBuilderClassOptionHelperTests extends AbstractSearchableCompassTests {
    def helper = new SearchableCompassQueryBuilderClassOptionHelper()
    def compass

    void setUp() {
        compass = TestCompassFactory.getCompass([Post, Comment])
    }

    void tearDown() {
        compass.close()
        compass = null
    }

    void testQueryWithAndWithoutClass() {
        withCompassSession { compassSession ->
            def queryBuilder = compassSession.queryBuilder()
            def query = queryBuilder.queryString("some typical search term").toQuery()
            assert query.toString() == "all:some all:typical all:search all:term"

            // Without class: no difference
            def queryApplied = helper.applyOptions(null, queryBuilder, query, [:])
            assert queryApplied.toString() == "all:some all:typical all:search all:term"

            // With class
            def mockQuery = new Mock(CompassQuery.class)
            def mockQueryProxy = mockQuery.proxy()
            mockQuery.expects(new InvokeOnceMatcher()).method('setAliases').'with'(new IsEqual(["apost"] as String[])).will(new ReturnStub(mockQueryProxy))

            def classMapping = new ClassMapping(clazz: Post, name: Post.name, alias: "apost")
            def mapping = new CompassMapping()
            mapping.addMapping(classMapping)
            def compass = [
                getMapping: {
                    mapping
                }
            ] as InternalCompass
            helper.applyOptions(compass, null, mockQueryProxy, [class: Post])

            mockQuery.verify()
        }
    }
}
