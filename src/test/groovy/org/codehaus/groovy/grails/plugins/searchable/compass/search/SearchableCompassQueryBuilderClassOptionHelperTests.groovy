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
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.*

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*
import org.jmock.core.constraint.*
import org.compass.core.mapping.osem.ClassMapping
import org.compass.core.mapping.CompassMapping
import org.compass.core.spi.InternalCompass
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompassMappingUtils
import org.compass.core.CompassQueryBuilder.*
import org.compass.core.mapping.internal.DefaultCompassMapping

/**
*
*
* @author Maurice Nicholson
*/
class SearchableCompassQueryBuilderClassOptionHelperTests extends AbstractSearchableCompassTestCase {
    def helper
    def compass
    def grailsApplication

    void setUp() {
        grailsApplication = TestCompassFactory.getGrailsApplication([Post, Comment, Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild, NonSearchableChild])
        compass = TestCompassFactory.getCompass(grailsApplication)
        helper = new SearchableCompassQueryBuilderClassOptionHelper()
    }

    void tearDown() {
        compass.close()
        compass = null
        helper = null
        grailsApplication = null
    }

    void testQueryWithAndWithoutClass() {
        withCompassSession { compassSession ->
            def queryBuilder = compassSession.queryBuilder()
            def query = queryBuilder.queryString("some typical search term").toQuery()
            assert query.toString() == "+some +typical +search +term"

            // Without class: no difference
            def queryApplied = helper.applyOptions(null, null, compassSession, query, [:])
            assert queryApplied.toString() == "+some +typical +search +term"

            // With class
            def mockQuery = new Mock(CompassQuery.class)
            def mockQueryProxy = mockQuery.proxy()
            mockQuery.expects(new InvokeOnceMatcher()).method('setAliases').'with'(new IsEqual(["apost"] as String[])).will(new ReturnStub(mockQueryProxy))

            def classMapping = new ClassMapping(clazz: Post, name: Post.name, alias: "apost")
            def mapping = new DefaultCompassMapping()
            mapping.addMapping(classMapping)
            def compass = [
                getMapping: {
                    mapping
                }
            ] as InternalCompass
            helper.applyOptions(grailsApplication, compass, null, mockQueryProxy, [class: Post])

            mockQuery.verify()
        }
    }

    void testBuildPolyClassQuery() {
        withCompassSession { compassSession ->
            def query = helper.buildPolyClassQuery(compassSession, [Parent, SearchableChildOne, SearchableChildOne, SearchableGrandChild])
            assert query.toString() == '$/poly/class:org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.Parent $/poly/class:org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.SearchableChildOne $/poly/class:org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.SearchableChildOne $/poly/class:org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.SearchableGrandChild'
        }
    }
}
