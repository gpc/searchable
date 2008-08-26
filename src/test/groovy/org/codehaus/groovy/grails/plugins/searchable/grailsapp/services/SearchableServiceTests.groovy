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
package org.codehaus.groovy.grails.plugins.searchable.grailsapp.services

import org.codehaus.groovy.grails.plugins.searchable.compass.*
import org.codehaus.groovy.grails.plugins.searchable.compass.search.*
import org.codehaus.groovy.grails.plugins.searchable.test.compass.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.compass.test.*

import org.springframework.core.io.DefaultResourceLoader

import org.compass.core.Compass
import org.compass.gps.*

import groovy.mock.interceptor.MockFor

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*

/**
 * 
 *
 * @author Maurice Nicholson
 */
class SearchableServiceTests extends AbstractSearchableCompassTestCase {
    def compass
    def application
    def service

    void setUp() {
        // todo this is naughty - need better test isolation
        Post.searchable = {
            all termVector: "yes" // required for more-like-this
        }
        Comment.searchable = {
            all termVector: "yes" // required for more-like-this
        }

        def posts = []
        def comments = []
        for (i in 0..<100) {
            def post = new Post(id: i as Long, title: "I live to post" + (i % 2 == 0 ? " and it's even" : " it's odd"), post: "posty, posty", comments: new HashSet())
            for (j in 0..<5) {
                def comment = new Comment(id: comments.size() as Long, summary: "I love to comment " + (comments.size() % 5 == 0 ? "ah ha ah" : ''), comment: "commenty, commenty", post: post)
                post.comments << comment
                comments << comment
            }
            posts << post
        }
        assert posts.size() == 100
        assert comments.size() == 500
        application = TestCompassFactory.getGrailsApplication([Post, Comment])
        compass = TestCompassFactory.getCompass(application, posts + comments)
        service = getSearchableService(compass)
    }

    void tearDown() {
        compass.close()
        compass = null
        application = null
        service = null

        // todo this is naughty - need better test isolation
        Post.searchable = true
        Comment.searchable = true
    }

    void testSearch() {
        def results = service.search("live OR love", [max: 101])
        assert results.results.size() == 101
        assert results.results*.class.unique() as Set == [Post, Comment] as Set
    }

    void testSearchTop() {
        def post = service.searchTop("live OR odd")
        assert post instanceof Post
        assert post.id % 2 == 1

        def comment = service.searchTop("love OR ha")
        assert comment instanceof Comment
        assert comment.id % 5 == 0
    }

    void testSearchEvery() {
        def objects = service.searchEvery("+even +post")
        assert objects.size() == 50
        assert objects*.class.unique() == [Post]

        objects = service.searchEvery("even OR comment")
        assert objects.size() == 550
        assert objects*.class.unique() as Set == [Post, Comment] as Set
    }

    void testMoreLikeThis() {
        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.ident << { Object[] args ->
            return delegate.id
        }
        metaClass.initialize()

        def s = compass.openSession()
        def tx = s.beginTransaction()

        def post = s.get(Post, 1l)

        tx.commit()
        s.close()

        def result = service.moreLikeThis(post, max: 101, match: Post, minResourceFreq: 1, minTermFreq: 1)
        assert result.results.size() == 99, result.results.size()
    }

    void testCountHits() {
        def count = service.countHits("+even +post")
        assert count == 50

        count = service.countHits("even OR comment")
        assert count == 550

        count = service.countHits("+live +comment")
        assert count == 0
    }

    void testIndexAll() {
        // Sanity check: thorough tests in DefaultIndexMethodTests
        assert numberIndexed(Post) == 100

        service.indexAll(
            new Post(id: 5000l, title: "New post", post: "Something to say"),
            new Post(id: 5001l, title: "Another new post", post: "Something to say")
        )

        assert numberIndexed(Post) == 102
    }

    void testIndex() {
        // Sanity check: thorough tests in DefaultIndexMethodTests
        assert numberIndexed(Post) == 100

        service.index(
            new Post(id: 5000l, title: "New post", post: "Something to say")
        )

        assert numberIndexed(Post) == 101
    }

    void testUnindexAll() {
        // Sanity check: thorough tests in DefaultUnindexMethodTests
        assert numberIndexed(Post) == 100
        assert numberIndexed(Comment) == 500

        service.unindexAll([class: Post])

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 500

        def objects = []
        withCompassSession { session ->
            [2l, 4l, 6l, 8l, 10l, 12l, 14l].each { objects << session.get(Comment, it) }
        }
        service.unindexAll(objects)

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 493
    }

    void testUnindex() {
        // Sanity check: thorough tests in DefaultUnindexMethodTests
        assert numberIndexed(Post) == 100
        assert numberIndexed(Comment) == 500

        service.unindex([class: Post])

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 500

        def object = withCompassSession { session ->
            session.get(Comment, 2l)
        }
        service.unindex(object)

        assert numberIndexed(Post) == 0
        assert numberIndexed(Comment) == 499
    }

    void testReindexAll() {
        // Sanity check: thorough tests in DefaultReindexMethodTests
        assert numberIndexed(Post) == 100
        assert loadFromCompass(Post, 1).post == "posty, posty"

        service.reindexAll(
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

        service.reindex(
            new Post(id: 2l, title: "Updated post 2", post: "update")
        )

        assert numberIndexed(Post) == 100
        assert loadFromCompass(Post, 2).post == "update"
    }

    void testStartMirroring() {
        def mockGps = new Mock(CompassGps.class)
        service.compassGps = mockGps.proxy()

        mockGps.expects(new InvokeOnceMatcher()).method('start').withNoArguments().isVoid()

        service.startMirroring()
        mockGps.verify()
    }

    void testStopMirroring() {
        def mockGps = new Mock(CompassGps.class)
        service.compassGps = mockGps.proxy()

        mockGps.expects(new InvokeOnceMatcher()).method('stop').withNoArguments().isVoid()

        service.stopMirroring()
        mockGps.verify()
    }

    private getSearchableService(compass) {
//        DefaultResourceLoader resourceLoader = new DefaultResourceLoader()
//        def resourceName = SearchableServiceTests.class.getName().toString()
//        resourceName = "/" + resourceName.replaceAll("\\.", "/") + ".class"
//        def resource = resourceLoader.getResource("classpath:" + resourceName)
//        assert resource.exists()
        def currentDir = new File(".")//resource.file.parentFile
        def appHome
        while (true) {
            assert currentDir
            if (new File(currentDir, "grails-app/services").exists()) {
                appHome = currentDir
                break
            }
            currentDir = currentDir.parentFile
        }

        GroovyClassLoader gcl = new GroovyClassLoader()
        def clazz = gcl.parseClass(new File(appHome, "grails-app/services/SearchableService.groovy"))

        def service = clazz.newInstance()
        service.searchableMethodFactory = new DefaultSearchableMethodFactory(compass: compass, grailsApplication: application)
        service
    }
}
