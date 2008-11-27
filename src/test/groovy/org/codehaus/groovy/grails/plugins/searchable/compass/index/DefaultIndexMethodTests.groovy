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
import org.jmock.core.constraint.IsEqual
import org.jmock.core.constraint.IsSame
import org.jmock.core.constraint.IsArrayContaining

/**
 * 
 *
 * @author Maurice Nicholson
 */
// todo delete when indexAll is removed
class DefaultIndexMethodTests extends AbstractSearchableCompassTestCase {
    def compass

    void setUp() {
        compass = TestCompassFactory.getCompass([Post, Comment])
    }

    void tearDown() {
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        metaClassRegistry.removeMetaClass(Post)
        metaClassRegistry.removeMetaClass(Comment) 
        compass.close()
        compass = null
    }

/*
   indexAll()

   service.indexAll() // all searchable class instances
   service.indexAll([class: Post]) // all Post instances
   service.indexAll(1l, 2l, 3l) // ERROR: unknown class
   service.indexAll(1l, 2l, 3l, [class: Post]) // id'd Post instances
   service.indexAll(x, y, z) // given instances

   Thing.indexAll() // all Thing instances
   Thing.indexAll(1l, 2l, 3l) // id'd Thing instances
   Thing.indexAll(x, y, z) // given instances


*/

    void testIndexAllNoArgsNoClass() {
        // No class, no args
        def mockGps = new Mock(CompassGps.class)
        def indexAll = new DefaultIndexMethod("indexAll", compass, mockGps.proxy())

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').withNoArguments().after('isRunning').isVoid()

        indexAll.invoke()
        mockGps.verify()
    }

    void testIndexAllNoArgsClassOption() {
        // With class, no args
        def mockGps = new Mock(CompassGps.class)
        def indexAll = new DefaultIndexMethod("indexAll", compass, mockGps.proxy())

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').with(new IsArrayContaining(new IsSame(Post.class))).after('isRunning').isVoid()

        indexAll.invoke(class: Post.class)
        mockGps.verify()
    }

    void testIndexAllIdArgsNoClass() {
        // Id args, no class
        def indexAll = new DefaultIndexMethod("indexAll", compass, null)
        shouldFail {
            indexAll.invoke(301l, 60l)
        }
    }

    void testIndexAllIdArgsWithClass() {
        assert numberIndexed(Post) == 0

        // Id args and class
        def indexAll = new DefaultIndexMethod("indexAll", compass, null)

        def mockPost = new MockFor(Post.class)
        mockPost.demand.getAll { ids ->
            [
                new Post(id: 301l, title: "First post", post: "I'm a beginner"),
                new Post(id: 60l, title: "Second post", post: "I'm moderate"),
                new Post(id: 25l, title: "Another post", post: "Now an expert")
            ]
        }
        mockPost.use {
            indexAll.invoke(301l, 60l, 25l, class: Post)
        }

        assert numberIndexed(Post) == 3
    }

    void testIndexAllObjectArgs() {
        assert numberIndexed(Post) == 0

        def indexAll = new DefaultIndexMethod("indexAll", compass, null)

        indexAll.invoke([
            new Post(id: 301l, title: "First post", post: "I'm a beginner"),
            new Post(id: 60l, title: "Second post", post: "I'm moderate"),
            new Post(id: 25l, title: "Another post", post: "Now an expert")
        ])

        assert numberIndexed(Post) == 3
    }

    void testClassStaticIndexAllNoArgs() {
        // On class
        def mockGps = new Mock(CompassGps.class)

        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').with(new IsArrayContaining(new IsSame(Post.class))).after('isRunning').isVoid()

        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.indexAll << { Object[] args ->
            new DefaultIndexMethod("indexAll", compass, mockGps.proxy(), [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        Post.indexAll()
        mockGps.verify()
    }


    void testClassStaticIndexAllIdsArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.indexAll << { Object[] args ->
            new DefaultIndexMethod("indexAll", compass, null, [class: Post]).invoke(*args)
        }
        // hack for testing
        metaClass.'static'.getAll << { Object[] args ->
            return [
                new Post(id: 301l, title: "First post", post: "I'm a beginner"),
                new Post(id: 60l, title: "Second post", post: "I'm moderate"),
                new Post(id: 25l, title: "Another post", post: "Now an expert")
            ]
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 0

        Post.indexAll(301l, 60l, 25l)

        assert numberIndexed(Post) == 3
    }

    void testClassStaticIndexAllInstancesArgs() {
        // On class
        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.'static'.indexAll << { Object[] args ->
            new DefaultIndexMethod("indexAll", compass, null, [class: Post]).invoke(*args)
        }
        metaClass.initialize()

        assert numberIndexed(Post) == 0

        Post.indexAll(
            new Post(id: 301l, title: "First post", post: "I'm a beginner"),
            new Post(id: 60l, title: "Second post", post: "I'm moderate"),
            new Post(id: 25l, title: "Another post", post: "Now an expert")
        )

        assert numberIndexed(Post) == 3
    }
}