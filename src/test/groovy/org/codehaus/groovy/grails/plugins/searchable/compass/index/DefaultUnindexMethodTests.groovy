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
package org.codehaus.groovy.grails.plugins.searchable.compass.index

import org.codehaus.groovy.grails.plugins.searchable.test.compass.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.commons.metaclass.*
import org.codehaus.groovy.grails.plugins.searchable.compass.*
import org.codehaus.groovy.grails.plugins.searchable.compass.test.*

/**
 * 
 *
 * @author Maurice Nicholson
 */
// delete when unindexAll is removed
class DefaultUnindexMethodTests extends AbstractSearchableCompassTestCase {
    def compass

    void setUp() {
        def posts = []
        def comments = []
        for (i in 0..<10) {
            def post = new Post(id: i as Long, title: "I live to post" + (i % 2 == 0 ? " and it's even" : " it's odd"), post: "posty, posty", comments: new HashSet())
            for (j in 0..<2) {
                def comment = new Comment(id: comments.size() as Long, summary: "I love to comment" + (comments.size() % 5 == 0 ? "ah ha ah" : ''), comment: "commenty, commenty", post: post)
                post.comments << comment
                comments << comment
            }
            posts << post
        }
        assert posts.size() == 10
        assert comments.size() == 20
        compass = TestCompassFactory.getCompass([Post, Comment], posts + comments)
    }

    void tearDown() {
        compass.close()
        compass = null
    }

/*
    unindexAll()

    service.unindexAll() // everything
    service.unindexAll([class: Post]) // all class instances
    service.unindexAll(1, 2, 3) // ERROR: unknown class
    service.unindexAll(1, 2, 3, [class: Post]) // id'd objects
    service.unindexAll(x, y, z) // given objects

    Thing.unindexAll() // all class instances
    Thing.unindexAll(1,2,3) // id'd instances
    Thing.unindexAll(x,y,z) // given instances

    */

    void testUnindexAllNoArgsNoOptions() {
        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        // No class, no args
        def unindexAll = new DefaultUnindexMethod("unindexAll", compass)
        unindexAll.invoke()

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 0
    }

    void testUnindexAllNoArgsWithClassOption() {
        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        // With class
        def unindexAll = new DefaultUnindexMethod("unindexAll", compass)
        unindexAll.invoke([class: Post])

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 20

        unindexAll.invoke([class: Comment])

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 0
    }

    void testUnindexAllObjectsArgs() {
        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        def objects = []
        withCompassSession { session ->
            [1l, 3l, 5l, 7l, 9l].each { objects << session.get(Post, it) }
            [2l, 4l, 6l, 8l, 10l, 12l, 14l].each { objects << session.get(Comment, it) }
        }
        def unindexAll = new DefaultUnindexMethod("unindexAll", compass)
        unindexAll.invoke(objects)

        assert numberIndexed(Post) == 5
        assert numberIndexed(Comment) == 13
    }

    void testUnindexAllIdsNoClass() {
        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        def unindexAll = new DefaultUnindexMethod("unindexAll", compass)

        shouldFail {
            unindexAll.invoke(1l, 2l, 3l)
        }
        shouldFail {
            unindexAll.invoke([1l, 2l, 3l])
        }
        shouldFail {
            unindexAll.invoke([7l, 8l, 9l] as Set)
        }

        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20
    }

    void testUnindexAllIdsAndClass() {
        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        def unindexAll = new DefaultUnindexMethod("unindexAll", compass)
        unindexAll.invoke(1l, 2l, 3l, [class: Post]) // ids

        assert numberIndexed(Post) == 7
        assert numberIndexed(Comment) == 20

        unindexAll.invoke([4l, 5l, 6l], [class: Post]) // id List

        assert numberIndexed(Post) == 4
        assert numberIndexed(Comment) == 20

        unindexAll.invoke([7l, 8l, 9l] as Set, [class: Post]) // id Set

        assert numberIndexed(Post) == 1
        assert numberIndexed(Comment) == 20
    }

    void testUnindexAllOnClassNoArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.'static'.unindexAll << { Object[] args ->
            new DefaultUnindexMethod("unindexAll", compass, [class: Comment]).invoke(*args)
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.unindexAll << { Object[] args ->
            new DefaultUnindexMethod("unindexAll", compass, [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        Post.unindexAll()

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 20

        Comment.unindexAll()

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 0
    }

    void testUnindexAllOnClassIdsArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.'static'.unindexAll << { Object[] args ->
            new DefaultUnindexMethod("unindexAll", compass, [class: Comment]).invoke(*args)
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.unindexAll << { Object[] args ->
            new DefaultUnindexMethod("unindexAll", compass, [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        Post.unindexAll([1l, 5l, 0l])

        assert numberIndexed(Post) == 7
        assert numberIndexed(Comment) == 20

        Comment.unindexAll(10l, 11l, 12l)

        assert numberIndexed(Post) == 7
        assert numberIndexed(Comment) == 17
    }

    void testUnindexAllOnClassInstancesArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.'static'.unindexAll << { Object[] args ->
            new DefaultUnindexMethod("unindexAll", compass, [class: Comment]).invoke(*args)
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.unindexAll << { Object[] args ->
            new DefaultUnindexMethod("unindexAll", compass, [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        def objects = []
        withCompassSession { session ->
            [1l, 2l, 3l].each { objects << session.get(Post, it) }
        }

        Post.unindexAll(objects)

        assert numberIndexed(Post) == 7
        assert numberIndexed(Comment) == 20

        objects = new HashSet()
        withCompassSession { session ->
            [1l, 2l, 3l].each { objects.add(session.get(Comment, it)) }
        }
        Comment.unindexAll(objects)

        assert numberIndexed(Post) == 7
        assert numberIndexed(Comment) == 17

        // handles nulls
        objects = []
        withCompassSession { session ->
            [4l, -10l, 6l, -9l].each { objects.add(session.get(Comment, it)) }
        }
        assert objects.findAll { it == null }.size() == 2
        Comment.unindexAll(objects)

        assert numberIndexed(Post) == 7
        assert numberIndexed(Comment) == 15
    }
}