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
package org.codehaus.groovy.grails.plugins.searchable.test.search.search

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class SearchTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [Post, Comment]
    }

    void setUp() {
        super.setUp()

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

        (posts + comments).each { it.index() }
    }

    void testServiceSearchMethod() {
        def results = searchableService.search("live OR love", [max: 101])
        assert results.results.size() == 101
        assert results.results*.class.unique() as Set == [Post, Comment] as Set

        // search result has scores
        assert results.scores
        assert results.scores.size() == 101

        // With closure
        results = searchableService.search {
            queryString("live OR love")
        }
        assert results.total == 600

        // With closure and options
        results = searchableService.search(max: 101) { // notice no [] brackets around options Map (it works either way)
            queryString("live OR love")
        }
        assert results.results.size() == 101
        assert results.total == 600
        assert results.results*.class.unique() as Set == [Post, Comment] as Set

        // with properties option
        results = searchableService.search("live OR love", properties: ['title', 'summary'], max: 1000)
        assert results.results.collect { it.class}.containsAll([Comment, Post])

        results = searchableService.search("live OR love", properties: ['post', 'comment'], max: 1000)
        assert results.results.size() == 0

        // prefix query with properties; Compass bug regression test
        results = searchableService.search("l*", properties: ['title', 'summary'], max: 1000)
        assert results.results.collect { it.class}.containsAll([Comment, Post])

        // for 'searchResult' result
        results = searchableService.search("live OR love", result: 'searchResult')
        assert results.results != null

        // for 'top' result
        results = searchableService.search("live OR love", result: 'top')
        assert results.class in [Comment, Post]

        // for 'every' result
        results = searchableService.search("live OR love", result: 'every')
        assert results instanceof Collection
        assert results.collect { it.class }.containsAll([Comment, Post])

        // for 'count' result
        results = searchableService.search("live OR love", result: 'count')
        assert results instanceof Number
    }

    void testClassSearchMethod() {
        def results = Post.search("posty") // should match all, thru post body
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
            queryString("l*") // query is all:l*
        }
        assert results.total == 100
        assert results.results*.class.unique() == [Post]

        // With closure and options
        results = Post.search([max: 101]) { // notice [] brackets around options Map (it works either way)
            queryString("l*") // query is all:l*
        }
        assert results.results.size() == 100
        assert results.total == 100
        assert results.results*.class.unique() == [Post]

        // "withHighlighter" option
        results = Post.search("posty", withHighlighter: { highlighter, index, sr ->
            if (!sr.highlights) {
                sr.highlights = []
            }
            sr.highlights[index] = highlighter.fragment("post")
            assert sr.results[index] instanceof Post
        })
        assert results.highlights
        assert results.highlights.findAll { it }.size() == results.results.size()
        assert results.highlights[0].indexOf("<b>posty</b>") > -1
    }
}
