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
package org.codehaus.groovy.grails.plugins.searchable.test.search.search.sort

import org.compass.core.CompassQuery
import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class SearchSortOptionsTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [Article]
    }

    void testSearchWithSortOptions() {
        def articles = [
            new Article(id: 1l, name: "New York City food and drink", body: "food and drink", createdAt: new Date() - 30),
            new Article(id: 10l, name: "New York, NY events", body: "events", createdAt: new Date() + 10),
            new Article(id: 100l, name: "York, Yorkshire people and places", body: "people and laces", createdAt: new Date()),
        ]
        articles.each { it.index() }
        assert searchableService.searchEvery("alias:Article").size() == 3, searchableService.searchEvery("alias:Article").size()

        // Note this isn't demonstrating *every* sort + dir/order combo; just a sanity check to show sort is being applied
        def hits = searchableService.searchEvery("new york city", defaultOperator: 'or') // implicit sort by score
        assert hits*.id == [1l, 10l, 100l]

        hits = searchableService.searchEvery("new york city", [sort: 'SCORE', defaultOperator: 'or'])
        assert hits*.id == [1l, 10l, 100l]

        hits = searchableService.searchEvery("new york city", [sort: 'SCORE', direction: 'reverse', defaultOperator: 'or'])
        assert hits*.id == [100l, 10l, 1l]

        /*
        the following throws a Lucene error - why!?

java.lang.RuntimeException: there are more terms than documents in field "body", but it's impossible to sort on tokenized fields
	at org.apache.lucene.search.FieldCacheImpl$10.createValue(FieldCacheImpl.java:379)
	at org.apache.lucene.search.FieldCacheImpl$Cache.get(FieldCacheImpl.java:72)
	at org.apache.lucene.search.FieldCacheImpl.getStringIndex(FieldCacheImpl.java:352)
        */
//        hits = searchableService.searchEvery("new york city", [sort: 'body', order: 'asc', defaultOperator: 'or'])
//        assert hits*.id == [10l, 1l, 100l]

        hits = searchableService.searchEvery("new york city", [sort: 'createdAt', defaultOperator: 'or'])
        assert hits*.id == [1l, 100l, 10l]

        hits = searchableService.searchEvery("new york city", [sort: 'createdAt', order: 'asc', defaultOperator: 'or'])
        assert hits*.id == [1l, 100l, 10l]

        hits = searchableService.searchEvery("new york city", [sort: 'createdAt', order: 'desc', defaultOperator: 'or'])
        assert hits*.id == [10l, 100l, 1l]

        // sort option can be used with closure built queries...
        // but sort option is added after sorts added in closure so have a lower priority
        // as shown by the next three examples
        hits = searchableService.searchEvery {
            queryString("new york city", defaultOperator: 'or')
            addSort("createdAt", CompassQuery.SortDirection.AUTO)
        }
        assert hits*.id == [1l, 100l, 10l]

        hits = searchableService.searchEvery(sort: 'SCORE', order: 'asc') { // notice no [] brackets around options Map (it works either way)
            queryString("new york city", defaultOperator: 'or')
            addSort("createdAt", CompassQuery.SortDirection.AUTO)
        }
        assert hits*.id == [1l, 100l, 10l] // same order as above since the sort in the buider takes precedence

        hits = searchableService.searchEvery([sort: 'createdAt', order: 'desc']) { // notice [] brackets around options Map (it works either way)
            queryString("new york city", defaultOperator: 'or')
//             no sort in here this time to illustrate the effect of the sort option
        }
        assert hits*.id == [10l, 100l, 1l]
    }
}

