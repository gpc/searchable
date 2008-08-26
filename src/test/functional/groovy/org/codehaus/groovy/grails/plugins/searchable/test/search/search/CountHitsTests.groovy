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

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase;

/**
 * @author Maurice Nicholson
 */
class CountHitsTests extends SearchableFunctionalTestCase {

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

    void testServiceCountHitsMethod() {
        // without class
        def count = searchableService.countHits("live OR love")
        assert count == 600, count
    }

    void testClassCountHitsMethod() {
        def count = Post.countHits("posty") // should match all, thru post body
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
}
