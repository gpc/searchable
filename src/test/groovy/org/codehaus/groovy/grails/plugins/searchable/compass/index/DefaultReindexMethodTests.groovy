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

import org.compass.gps.*
import org.compass.gps.impl.*

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*

import groovy.mock.interceptor.MockFor
import org.jmock.core.constraint.IsSame
import org.jmock.core.constraint.IsArrayContaining

/**
 * 
 *
 * @author Maurice Nicholson
 */
// todo convert to functional tests as appropriate
class DefaultReindexMethodTests extends AbstractSearchableCompassTestCase {
    def compass
    def methodFactory

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
        methodFactory = new DefaultSearchableMethodFactory(compass: compass)
    }

    void tearDown() {
        compass.close()
        compass = null
        methodFactory = null
    }

/*
   reindexAll()

   service.reindexAll() // all searchable class instances
   service.reindexAll([class: Post]) // all Post instances
   service.reindexAll(1l, 2l, 3l) // ERROR: unknown class
   service.reindexAll(1l, 2l, 3l, [class: Post]) // id'd Post instances
   service.reindexAll(x, y, z) // given instances

   Thing.reindexAll() // all Thing instances
   Thing.reindexAll(1l, 2l, 3l) // id'd Post instances
   Thing.reindexAll(x, y, z) // given instances


*/

    void testReindexAllNoArgs() {
        assert numberIndexed(Post) == 10
        assert numberIndexed(Comment) == 20

        def mockGps = new Mock(CompassGps.class)
        def reindexAll = new DefaultReindexMethod("reindexAll", compass, mockGps.proxy(), methodFactory)

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').withNoArguments().after('isRunning').isVoid()

        reindexAll.invoke()
        mockGps.verify()

        // Doesn't really make sense but we know the right methods were called so it does what it's supposed to
        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 0
    }

    void testReindexAllClassOption() {
        def mockGps = new Mock(CompassGps.class)
        def reindexAll = new DefaultReindexMethod("reindexAll", compass, mockGps.proxy(), methodFactory)

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').with(new IsArrayContaining(new IsSame(Post))).after('isRunning').isVoid()

        reindexAll.invoke([class: Post])
        mockGps.verify()
    }

    void testReindexAllIdArgs() {
        def reindexAll = new DefaultReindexMethod("reindexAll", compass, null, methodFactory)
        shouldFail {
            reindexAll.invoke([1l, 2l])
        }
    }

    void testReindexAllIdsAndClass() {
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 1).post == "posty, posty"

        def reindexAll = new DefaultReindexMethod("reindexAll", compass, null, methodFactory)

        def mockPost = new MockFor(Post.class)
        mockPost.demand.getAll { ids ->
            [
                new Post(id: 1l, title: "Post 1 updated", post: "updated"),
                new Post(id: 5l, title: "Post 5 updated", post: "updated"),
                new Post(id: 9l, title: "Post 9 updated", post: "updated"),
            ]
        }
        mockPost.use {
            reindexAll.invoke(1l, 5l, 9l, [class: Post])
        }

        // Still 10
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 1).post == "updated"
    }

    void testReindexAllObjectArgs() {
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 1).post == "posty, posty"

        def reindexAll = new DefaultReindexMethod("reindexAll", compass, null, methodFactory)
        reindexAll.invoke(
            new Post(id: 1l, title: "Post 1 updated", post: "updated"),
            new Post(id: 5l, title: "Post 5 updated", post: "updated"),
            new Post(id: 9l, title: "Post 9 updated", post: "updated"),
        )

        // Still 10
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 1l).post == "updated"
    }

    void testClassStaticReindexAllNoArgs() {
        def mockGps = new Mock(CompassGps.class)

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').with(new IsArrayContaining(new IsSame(Post))).after('isRunning').isVoid()

        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.reindexAll << { Object[] args ->
            new DefaultReindexMethod("reindexAll", compass, mockGps.proxy(), methodFactory, [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        Post.reindexAll()
        mockGps.verify()
    }

    void testClassStaticReindexAllIdsArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.reindexAll << { Object[] args ->
            new DefaultReindexMethod("reindexAll", compass, null, methodFactory, [class: Post]).invoke(*args)
        }
        // hack for testing
        metaClass.'static'.getAll << { Object[] args ->
            return [
                new Post(id: 1l, title: "First post", post: "I'm a beginner"),
                new Post(id: 2l, title: "Second post", post: "I'm moderate"),
                new Post(id: 8l, title: "Another post", post: "Now an expert")
            ]
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 8l).post == "posty, posty"

        Post.reindexAll(1l, 2l, 8l)

        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 8l).post == "Now an expert"
    }

    void testClassStaticReindexAllInstancesArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.reindexAll << { Object[] args ->
            new DefaultReindexMethod("reindexAll", compass, null, methodFactory, [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 8l).post == "posty, posty"

        Post.reindexAll(
            new Post(id: 1l, title: "First post", post: "I'm a beginner"),
            new Post(id: 2l, title: "Second post", post: "I'm moderate"),
            new Post(id: 8l, title: "Another post", post: "Now an expert")
        )
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 8l).post == "Now an expert"

        Post.reindexAll(
            new Post(id: 10l, title: "At it again", post: "Blah blah whatever"),
        )
        assert numberIndexed(Post) == 11
    }

/*
    reindex()

    Like reindexAll but without no-arg bulk behavoir

    service.reindex() // all searchable instances - same as reindexAll
    service.reindex([class: Post]) // all class instances
    service.reindex(x, y, z) // given object(s)
    service.reindex(1, 2, 3, [class: Post]) // id'd objects

    Thing.reindex() // all Thing instances
    Thing.reindex(1,2,3) // id'd instances
    Thing.reindex(x,y,z) // given instances

    */

    void testReindexNoArgs() {
        def mockGps = new Mock(CompassGps.class)

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').withNoArguments().after('isRunning').isVoid()

        def reindex = new DefaultReindexMethod("reindex", compass, mockGps.proxy(), methodFactory)
        reindex()
        mockGps.verify()
    }

    void testReindexClassOnly() {
        def mockGps = new Mock(CompassGps.class)

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').with(new IsArrayContaining(new IsSame(Post))).after('isRunning').isVoid()

        def reindex = new DefaultReindexMethod("reindex", compass, mockGps.proxy(), methodFactory)

        reindex([class: Post])
        mockGps.verify()
    }

    void testReindexObjectArgs() {
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 1).post == "posty, posty"

        def reindex = new DefaultReindexMethod("reindex", compass, null, methodFactory)
        reindex.invoke(
            new Post(id: 1l, title: "Post 1 updated", post: "updated")
        )

        // Still 10
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 1l).post == "updated"
    }

    void testReindexIdsAndClass() {
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 1).post == "posty, posty"

        def reindex = new DefaultReindexMethod("reindex", compass, null, methodFactory)

        def mockPost = new MockFor(Post.class)
        mockPost.demand.getAll { ids ->
            [
                new Post(id: 1l, title: "Post 1 updated", post: "updated"),
                new Post(id: 5l, title: "Post 5 updated", post: "updated"),
                new Post(id: 9l, title: "Post 9 updated", post: "updated"),
            ]
        }
        mockPost.use {
            reindex.invoke(1l, 5l, 9l, [class: Post])
        }

        // Still 10
        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 1).post == "updated"
    }

    void testClassStaticReindexNoArgs() {
        def mockGps = new Mock(CompassGps.class)

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').with(new IsArrayContaining(new IsSame(Post))).after('isRunning').isVoid()

        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.reindex << { Object[] args ->
            new DefaultReindexMethod("reindex", compass, mockGps.proxy(), methodFactory, [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        Post.reindex()
        mockGps.verify()
    }

    void testClassStaticReindexIdsArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.reindex << { Object[] args ->
            new DefaultReindexMethod("reindex", compass, null, methodFactory, [class: Post]).invoke(*args)
        }
        // hack for testing
        metaClass.'static'.getAll << { Object[] args ->
            return [
                new Post(id: 1l, title: "First post", post: "I'm a beginner"),
                new Post(id: 2l, title: "Second post", post: "I'm moderate"),
                new Post(id: 8l, title: "Another post", post: "Now an expert")
            ]
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 8l).post == "posty, posty"

        Post.reindex(1l, 2l, 8l)

        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 8l).post == "Now an expert"
    }

    void testClassStaticReindexInstancesArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.reindex << { Object[] args ->
            new DefaultReindexMethod("reindex", compass, null, methodFactory, [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 8l).post == "posty, posty"

        Post.reindex(
            new Post(id: 1l, title: "First post", post: "I'm a beginner"),
            new Post(id: 2l, title: "Second post", post: "I'm moderate"),
            new Post(id: 8l, title: "Another post", post: "Now an expert")
        )

        assert numberIndexed(Post) == 10
        assert loadFromCompass(Post, 8l).post == "Now an expert"
    }
}