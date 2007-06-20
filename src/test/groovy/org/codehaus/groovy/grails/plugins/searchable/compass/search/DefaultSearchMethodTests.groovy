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

import org.codehaus.groovy.grails.plugins.searchable.compass.*
import org.codehaus.groovy.grails.plugins.searchable.compass.test.*
import org.codehaus.groovy.grails.plugins.searchable.test.compass.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.commons.metaclass.*

import org.compass.core.*

/**
 * 
 *
 * @author Maurice Nicholson
 */
class DefaultSearchMethodTests extends AbstractSearchableCompassTests {
    def compass
    def factory

    void setUp() {
        def posts = []
        def comments = []
        for (i in 0..<100) {
            def post = new Post(id: i as Long, title: "I live to post" + (i % 2 == 0 ? " and it's even" : " it's odd"), post: "posty, posty", comments: new HashSet())
            for (j in 0..<5) {
                def comment = new Comment(id: comments.size() as Long, summary: "I love to comment" + (comments.size() % 5 == 0 ? "ah ha ah" : ''), comment: "commenty, commenty", post: post)
                post.comments << comment
                comments << comment
            }
            posts << post
        }
        assert posts.size() == 100
        assert comments.size() == 500
        compass = TestCompassFactory.getCompass([Post, Comment], posts + comments)

        factory = new DefaultSearchableMethodFactory(compass: compass)
    }

    void tearDown() {
        compass.close()
        compass = null
        factory = null
    }

    void testSearch() {
        // A key feature is re-use of the same "method" instance as implicitly tested here

        // without class
        def anyClassSearch = factory.getMethod("search")
        def results = anyClassSearch("live love", [max: 101])
        assert results.results.size() == 101
        assert results.results*.class.unique() as Set == [Post, Comment] as Set

        // search result has scores (sanity check only: more detailed tests elsewhere)
        assert results.scores
        assert results.scores.size() == 101

        // With closure
        results = anyClassSearch {
            bool {
                addShould(term("all", "live"))
                addShould(term("all", "love"))
            }
        }
        assert results.total == 600

        // With closure and options
        results = anyClassSearch(max: 101) { // notice no [] brackets around options Map (it works either way)
            bool {
                addShould(term("all", "live"))
                addShould(term("all", "love"))
            }
        }
        assert results.results.size() == 101
        assert results.total == 600
        assert results.results*.class.unique() as Set == [Post, Comment] as Set

        // single class
        def postSearch = factory.getMethod(Post, "search")
        results = postSearch("posty") // should match all, thru post body
        assert results.results.size() == 10
        assert results.results*.class.unique() == [Post]
        assert results.max == 10
        assert results.offset == 0
        assert results.total == 100

        // offset + max override
        results = postSearch("even", [offset: 20, max: 30]) // should match half, thru title
        assert results.results.size() == 30
        assert results.max == 30
        assert results.offset == 20
        assert results.total == 50

        def commentSearch = factory.getMethod(Comment, "search")
        results = commentSearch("commenty") // should match all
        assert results.results.size() == 10
        assert results.results*.class.unique() == [Comment]
        assert results.max == 10
        assert results.offset == 0
        assert results.total == 500

        // offset + max is beyond the last result
        results = commentSearch("ah", [offset: 99, max: 50]) // should match 1/5
        assert results.results.size() == 1
        assert results.results*.class.unique() == [Comment]
        assert results.max == 50
        assert results.offset == 99
        assert results.total == 100

        // default escape off
        shouldFail {
            commentSearch("[this is a bad query]")
        }

        // escape bad query: should not fail
        commentSearch("[this is a bad query]", [escape: true])

        // turn default escape on; then override
        def defaultOptions = new HashMap(commentSearch.defaultOptions) // not recommended! clone to avoid corrupting global settings
        defaultOptions.escape = true
        commentSearch.defaultOptions = defaultOptions

        shouldFail {
            commentSearch("[this is a bad query]", [escape: false])
        }

        // but now bad queries should not fail
        commentSearch("[this is a bad query]")

        // In situ
        def metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.'static'.search << { Object[] args ->
            factory.getMethod(Comment, "search").invoke(*args)
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.search << { Object[] args ->
            factory.getMethod(Post, "search").invoke(*args)
        }
        metaClass.initialize()

        results = Post.search("posty") // should match all, thru post body
        assert results.results.size() == 10
        assert results.results*.class.unique() == [Post]
        assert results.max == 10
        assert results.offset == 0
        assert results.total == 100

        // offset + max
        results = Post.search("even", [offset: 15, max: 30]) // should match half, thru title
        assert results.results.size() == 30
        assert results.max == 30
        assert results.offset == 15
        assert results.total == 50

        results = Comment.search("commenty") // should match all
        assert results.results.size() == 10
        assert results.results*.class.unique() == [Comment]
        assert results.max == 10
        assert results.offset == 0
        assert results.total == 500

        // offset + max is beyond the last result
        results = Comment.search("ah", [offset: 99, max: 50]) // should match 1/5
        assert results.results.size() == 1
        assert results.results*.class.unique() == [Comment]
        assert results.max == 50
        assert results.offset == 99
        assert results.total == 100

        shouldFail {
            results = Post.search("[this is a bad query]")
        }

        // should not fail
        results = Post.search("[this is a bad query]", [escape: true])

        // With closure
        results = Post.search {
            prefix("all", "l") // query is all:l*
        }
        assert results.total == 100
        assert results.results*.class.unique() == [Post]

        // With closure and options
        results = Post.search([max: 101]) { // notice [] brackets around options Map (it works either way)
            prefix("all", "l") // query is all:l*
        }
        assert results.results.size() == 100
        assert results.total == 100
        assert results.results*.class.unique() == [Post]
    }

    void testSearchTop() {
        // A key feature is re-use of the same "method" instance as implicitly tested here

        // without class
        def anyClassSearch = factory.getMethod("searchTop")
        def results = anyClassSearch.invoke("love")
        assert results.class == Comment
        results = anyClassSearch.invoke("live")
        assert results.class == Post

        def postSearchTop = factory.getMethod(Post, "searchTop")
        results = postSearchTop("posty") // should match all, thru post body
        assert results instanceof Post

        results = postSearchTop("even") // should match half, thru title
        assert results instanceof Post

        results = postSearchTop("odd") // should match half, thru title
        assert results instanceof Post

        def commentSearchTop = factory.getMethod(Comment, "searchTop")
        results = commentSearchTop("commenty") // should match all
        assert results instanceof Comment

        results = commentSearchTop("ah") // should match 1/5
        assert results instanceof Comment

        // default escape off; with override
        shouldFail {
            postSearchTop("[this is a bad query]")
        }

        // should not fail
        postSearchTop("[this is a bad query]", [escape: true])

        // default escape on; with override
        def defaultOptions = new HashMap(postSearchTop.defaultOptions) // not recommended! clone to avoid corrupting global settings
        defaultOptions.escape = true
        postSearchTop.defaultOptions = defaultOptions

        shouldFail {
            postSearchTop("[this is a bad query]", [escape: false])
        }

        // should not fail
        postSearchTop("[this is a bad query]")

        // In situ
        def metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.'static'.searchTop << { Object[] args ->
            factory.getMethod(Comment, "searchTop").invoke(*args)
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.searchTop << { Object[] args ->
            factory.getMethod(Post, "searchTop").invoke(*args)
        }
        metaClass.initialize()

        results = Post.searchTop("posty") // should match all, thru post body
        assert results instanceof Post

        results = Post.searchTop("even") // should match half, thru title
        assert results instanceof Post

        results = Post.searchTop("odd") // should match half, thru title
        assert results instanceof Post

        results = Comment.searchTop("commenty") // should match all
        assert results instanceof Comment

        results = Comment.searchTop("ah") // should match 1/5
        assert results instanceof Comment

        shouldFail {
            results = Post.searchTop("[this is a bad query]")
        }

        // should not fail
        results = Post.searchTop("[this is a bad query]", [escape: true])
    }

    void testSearchEvery() {
        // A key feature is re-use of the same "method" instance as implicitly tested here

        // without class
        def anyClassSearch = factory.getMethod("searchEvery")
        def results = anyClassSearch.invoke("live love")
        assert results.size() == 600
        assert results*.class.unique() == [Post, Comment]

        def postSearchEvery = factory.getMethod(Post, "searchEvery")
        results = postSearchEvery("posty") // should match all, thru post body
        assert results.size() == 100
        assert results*.class.unique() == [Post]

        results = postSearchEvery("even") // should match half, thru title
        assert results.size() == 50
        assert results*.class.unique() == [Post]

        results = postSearchEvery("odd") // should match half, thru title
        assert results.size() == 50
        assert results*.class.unique() == [Post]

        def commentSearchEvery = factory.getMethod(Comment, "searchEvery")
        results = commentSearchEvery("commenty") // should match all
        assert results.size() == 500
        assert results*.class.unique() == [Comment]

        results = commentSearchEvery("ah") // should match 1/5
        assert results.size() == 100
        assert results*.class.unique() == [Comment]

        // default escape off; with override
        shouldFail {
            postSearchEvery("[this is a bad query]")
        }

        // should not fail
        postSearchEvery("[this is a bad query]", [escape: true])

        // default escape on; with override
        def defaultOptions = new HashMap(postSearchEvery.defaultOptions) // not recommended! clone to avoid corrupting global settings
        defaultOptions.escape = true
        postSearchEvery.defaultOptions = defaultOptions

        shouldFail {
            postSearchEvery("[this is a bad query]", [escape: false])
        }

        // should not fail
        postSearchEvery("[this is a bad query]")

        // In situ
        def metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.'static'.searchEvery << { Object[] args ->
            factory.getMethod(Comment, "searchEvery").invoke(*args)
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.searchEvery << { Object[] args ->
            factory.getMethod(Post, "searchEvery").invoke(*args)
        }
        metaClass.initialize()

        results = Post.searchEvery("posty") // should match all, thru post body
        assert results.size() == 100
        assert results*.class.unique() == [Post]

        results = Post.searchEvery("even") // should match half, thru title
        assert results.size() == 50
        assert results*.class.unique() == [Post]

        results = Post.searchEvery("odd") // should match half, thru title
        assert results.size() == 50
        assert results*.class.unique() == [Post]

        results = Comment.searchEvery("commenty") // should match all
        assert results.size() == 500
        assert results*.class.unique() == [Comment]

        results = Comment.searchEvery("ah") // should match 1/5
        assert results.size() == 100
        assert results*.class.unique() == [Comment]

        shouldFail {
            results = Post.searchEvery("[this is a bad query]")
        }

        // should not fail
        results = Post.searchEvery("[this is a bad query]", [escape: true])
    }

    void testCountSearchHits() {
        // A key feature is re-use of the same "method" instance as implicitly tested here

        // without class
        def anyClassCountHits = factory.getMethod("countHits")
        def count = anyClassCountHits.invoke("live love")
        assert count == 600

        def postCountHits = factory.getMethod(Post, "countHits")
        count = postCountHits("posty") // should match all, thru post body
        assert count == 100

        count = postCountHits("even") // should match half, thru title
        assert count == 50

        count = postCountHits("odd") // should match half, thru title
        assert count == 50

        def commentCountHits = factory.getMethod(Comment, "countHits")
        count = commentCountHits("commenty") // should match all
        assert count == 500

        count = commentCountHits("ah") // should match 1/5
        assert count == 100

        // default escape off; with override
        shouldFail {
            postCountHits("[this is a bad query]")
        }

        // should not fail
        postCountHits("[this is a bad query]", [escape: true])

        // default escape on; with override
        def defaultOptions = new HashMap(postCountHits.defaultOptions) // not recommended! clone to avoid corrupting global settings
        defaultOptions.escape = true
        postCountHits.defaultOptions = defaultOptions

        shouldFail {
            postCountHits("[this is a bad query]", [escape: false])
        }

        // should not fail
        postCountHits("[this is a bad query]")

        // In situ
        def metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.'static'.countHits << { Object[] args ->
            factory.getMethod(Comment, "countHits").invoke(*args)
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.countHits << { Object[] args ->
            factory.getMethod(Post, "countHits").invoke(*args)
        }
        metaClass.initialize()

        count = Post.countHits("posty") // should match all, thru post body
        assert count == 100

        count = Post.countHits("even") // should match half, thru title
        assert count == 50

        count = Post.countHits("odd") // should match half, thru title
        assert count == 50

        count = Comment.countHits("commenty") // should match all
        assert count == 500

        count = Comment.countHits("ah") // should match 1/5
        assert count == 100

        shouldFail {
            count = Post.countHits("[this is a bad query]")
        }

        // should not fail
        count = Post.countHits("[this is a bad query]", [escape: true])
    }

    void testSearchWithSortOptions() {
        clearIndex()
        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 0

        def posts = [
            new Post(id: 1l, title: "New York City food and drink", post: "food and drink", createdAt: new Date() - 30),
            new Post(id: 10l, title: "New York, NY events", post: "events", createdAt: new Date() + 10),
            new Post(id: 100l, title: "York, Yorkshire people and places", post: "people and laces", createdAt: new Date()),
        ]
        saveToCompass(posts)
        assert numberIndexed(Post) == 3

        def searchEvery = factory.getMethod("searchEvery")

        // Note this isn't demonstrating *every* sort + dir/order combo; just a sanity check to show sort is being applied
        def hits = searchEvery("new york city") // implicit sort by score
        assert hits*.id == [1l, 10l, 100l]

        hits = searchEvery("new york city", [sort: 'SCORE'])
        assert hits*.id == [1l, 10l, 100l]

        hits = searchEvery("new york city", [sort: 'SCORE', direction: 'reverse'])
        assert hits*.id == [100l, 10l, 1l]

        hits = searchEvery("new york city", [sort: 'post', order: 'asc'])
        assert hits*.id == [10l, 1l, 100l]

        hits = searchEvery("new york city", [sort: 'createdAt'])
        assert hits*.id == [1l, 100l, 10l]

        hits = searchEvery("new york city", [sort: 'createdAt', order: 'desc'])
        assert hits*.id == [10l, 100l, 1l]

        // sort option can be used with closure built queries...
        // but sort option is added after sorts added in closure so have a lower priority
        // as shown by the next three examples
        hits = searchEvery {
            queryString("new york city")
            addSort("createdAt", CompassQuery.SortDirection.AUTO)
        }
        assert hits*.id == [1l, 100l, 10l]

        hits = searchEvery(sort: 'post', order: 'asc') { // notice no [] brackets around options Map (it works either way)
            queryString("new york city")
            addSort("createdAt", CompassQuery.SortDirection.AUTO)
        }
        assert hits*.id == [1l, 100l, 10l] // same order as above since the sort in the buider takes precedence

        hits = searchEvery([sort: 'post', order: 'asc']) { // notice [] brackets around options Map (it works either way)
            queryString("new york city")
            // no sort in here this time to illustrate the effect of the sort option
        }
        assert hits*.id == [10l, 1l, 100l]
    }
}