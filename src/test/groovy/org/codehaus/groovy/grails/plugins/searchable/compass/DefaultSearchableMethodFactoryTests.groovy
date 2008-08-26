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
package org.codehaus.groovy.grails.plugins.searchable.compass

import org.codehaus.groovy.grails.plugins.searchable.compass.test.*
import org.codehaus.groovy.grails.plugins.searchable.test.compass.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.commons.metaclass.*
import org.codehaus.groovy.grails.plugins.searchable.SearchableMethodFactory
import org.compass.core.Compass
import org.compass.core.CompassTemplate

/**
*
*
* @author Maurice Nicholson
*/
class DefaultSearchableMethodFactoryTests extends GroovyTestCase {
    def searchableMethodFactory

    void setUp() {
        searchableMethodFactory = new DefaultSearchableMethodFactory(
            compass: [:] as Compass
        )
    }

    void tearDown() {
        searchableMethodFactory = null
    }

    void testGetMethod() {
        def methodNames = [
            'indexAll', 'index', 'unindexAll', 'unindex', 'reindexAll', 'reindex',
            'termFreqs', 'moreLikeThis', 'search', 'searchEvery', 'searchTop', 'countHits',
            'suggestQuery'
        ]
        for (name in methodNames) {
            def method = searchableMethodFactory.getMethod(name)
            assert method
        }
    }

    void testSearchDefaults() {
        searchableMethodFactory = new DefaultSearchableMethodFactory(
            compass: [:] as Compass,
            defaultMethodOptions: [search: [max: 50, escape: true]]
        )

        def method = searchableMethodFactory.getMethod("search")
        assert method.defaultOptions.max == 50
        assert method.defaultOptions.escape == true
    }
}