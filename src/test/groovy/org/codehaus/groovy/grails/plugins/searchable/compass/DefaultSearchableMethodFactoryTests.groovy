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

/**
 * 
 *
 * @author Maurice Nicholson
 */
class DefaultSearchableMethodFactoryTests extends AbstractSearchableCompassTests {
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

    void testIndexAll() {
        // Sanity check: thorough tests in DefaultIndexMethodTests
        assert numberIndexed(Post) == 100

        def indexAll = factory.getMethod("indexAll")
        indexAll(
            new Post(id: 5000l, title: "New post", post: "Something to say"),
            new Post(id: 5001l, title: "Another new post", post: "Something to say")
        )

        assert numberIndexed(Post) == 102
    }

    void testIndex() {
        // Sanity check: thorough tests in DefaultIndexMethodTests
        assert numberIndexed(Post) == 100

        def index = factory.getMethod("index")
        shouldFail {
            index()
        }
        index(
            new Post(id: 5000l, title: "New post", post: "Something to say")
        )

        assert numberIndexed(Post) == 101
    }

    void testUnindexAll() {
        // Sanity check: more tests in DefaultUnindexMethodTests
        assert numberIndexed(Post) == 100
        assert numberIndexed(Comment) == 500

        // No class, no args
        def unindexAll = factory.getMethod("unindexAll")
        unindexAll.invoke()

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 0
    }

    void testUnindex() {
        // Sanity check: more tests in DefaultUnindexMethodTests
        assert numberIndexed(Post) == 100
        assert numberIndexed(Comment) == 500

        // No class, no args; should fail
        def unindex = factory.getMethod("unindex")
        shouldFail {
            unindex.invoke()
        }

        assert numberIndexed(Post) == 100
        assert numberIndexed(Comment) == 500

        unindex.invoke(1l, 2l, 3l, [class: Post])

        assert numberIndexed(Post) == 97
        assert numberIndexed(Comment) == 500
    }

    void testReindexAll() {
        // Sanity check: thorough tests in DefaultReindexMethodTests
        assert numberIndexed(Post) == 100
        assert loadFromCompass(Post, 1).post == "posty, posty"

        def reindexAll = factory.getMethod("reindexAll")
        reindexAll.invoke(
            new Post(id: 1l, title: "Updated post 1", post: "update"),
            new Post(id: 2l, title: "Updated post 2", post: "update")
        )

        assert numberIndexed(Post) == 100
        assert loadFromCompass(Post, 1).post == "update"
    }

    void testReindex() {
        // Sanity check: thorough tests in DefaultReindexMethodTests
        assert numberIndexed(Post) == 100
        assert loadFromCompass(Post, 2).post == "posty, posty"

        def reindexAll = factory.getMethod("reindexAll")
        reindexAll.invoke(
            new Post(id: 2l, title: "Updated post 2", post: "update")
        )

        assert numberIndexed(Post) == 100
        assert loadFromCompass(Post, 2).post == "update"
    }

}