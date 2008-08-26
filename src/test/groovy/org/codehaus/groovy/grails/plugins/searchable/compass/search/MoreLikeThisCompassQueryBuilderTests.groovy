/*
 * Copyright 2008 the original author or authors.
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

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.test.compass.TestCompassFactory
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Comment
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.Post
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.Parent
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.SearchableChildOne
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.SearchableChildTwo
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.SearchableGrandChild
import org.compass.core.CompassQuery
import org.compass.core.CompassQueryBuilder
import org.compass.core.CompassSession
import org.jmock.Mock
import org.jmock.core.constraint.IsEqual
import org.jmock.core.matcher.InvokeOnceMatcher
import org.jmock.core.stub.ReturnStub

/**
 * @author Maurice Nicholson
 */
// todo move anything from here that isn't covered by functonal inheritance test there and remove this
class MoreLikeThisCompassQueryBuilderTests extends GroovyTestCase {
    def application
    def compass

    {
        Thread.currentThread().setContextClassLoader(new GroovyClassLoader())
    }

    void setUp() {
        def posts = [new Post(id: 1l, title: "This is my very very very first post", post: "Do you like the very very very first post")]
        def comments = [new Comment(id: 1l, summary: "About your very very very first post", comment: "No it's rubbish. Next time get some content")]
        def family = [new Parent(id: 1l, commonProperty: 'Whatever'), new SearchableChildOne(id: 1l, commonProperty: 'Whatever', childOneProperty: 'child one'), new SearchableChildTwo(id: 1l, commonProperty: 'Whatever', childTwoProperty: 'child two'), new SearchableGrandChild(id: 1l, commonProperty: 'Whatever', childOneProperty: 'inherited', grandChildProperty: 'grand child')]
        application = TestCompassFactory.getGrailsApplication([Post, Comment, Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild])
        compass = TestCompassFactory.getCompass(application, posts + comments + family)
    }

    void tearDown() {
        compass.close()
        compass = null
        application = null
    }

    void testBuildQuery() {
        def builder = new MoreLikeThisCompassQueryBuilder(compass)

        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.ident << { Object[] args ->
            return delegate.id
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.ident << { Object[] args ->
            return delegate.id
        }
        metaClass.initialize()

        // todo what about CompassQuery options, eg, alias, sort?
        // todo if user defines alias, then assume expert and use that otherwise build aliases from class
//        fail()


        def session = compass.openSession()
        def tx = session.beginTransaction()

        /*
        post.moreLikeThis() -- [class: Post], [id: 10l] => more Posts like 10

        Post.moreLikeThis(10l) -- [class: Post], [id: 10l] => more Posts like 10

        post.moreLikeThis(class: 'any') -- [class: Post], [id: 10l, class: 'any'] => more anything like 10

        Post.moreLikeThis(10l) -- [class: Post], [id: 10l] => more Posts like 10

        Post.moreLikeThis(10l, class: 'any') -- [class: Post], [id: 10l, class: 'any'] => more anything like 10

        searchableService.moreLikeThis(Post, 10l) => more anything like Post #10

        searchableService.moreLikeThis(post) => more anything like Post #10

        searchableService.moreLikeThis(post, class: Post) => more Posts like Post #10

todo what about named id/class options, eg

        Post.moreLikeThis(id: 10l, class: Comment) -- [class: Comment], [class: Post, id: 10l] => more Comments like Post #10

        searchableService.moreLikeThis(class: Post, id: 10l, match: Comment) => !!!! more Comments like Post #10
         */

        // Class + id
        def query = builder.buildQuery(application, session, [:], [Post, 1l] as Object[])
        assert query.toString() == '+() -$/uid:Post#1#', query.toString()

        // "match" option + id - this is how the Post.moreLikeThis(id) work
        query = builder.buildQuery(application, session, [match: Post], [1l] as Object[])
        assert query.toString() == '+(+() -$/uid:Post#1#) +(alias:Post)', query.toString()

        // Instance
        def post = session.get(Post, 1l)
        query = builder.buildQuery(application, session, [:], [post] as Object[])
        assert query.toString() == '+() -$/uid:Post#1#', query.toString()

        // class option and id (named args)
        query = builder.buildQuery(application, session, [class: Post, id: 1l], [] as Object[])
        assert query.toString() == '+() -$/uid:Post#1#', query.toString()

        // class option and instance; instance overrides class option
        def comment = session.get(Comment, 1l)
        query = builder.buildQuery(application, session, [match: Post], [comment] as Object[])
        assert query.toString() == '+(+() -$/uid:Comment#1#) +(alias:Post)', query.toString()

        // match option and class and id
        query = builder.buildQuery(application, session, [match: Post], [Comment, 1l] as Object[])
        assert query.toString() == '+(+() -$/uid:Comment#1#) +(alias:Post)', query.toString()

        // expert: "alias" option
        query = builder.buildQuery(application, session, [alias: 'Post', id: 1l], [] as Object[])
        assert query.toString() == '+() -$/uid:Post#1#', query.toString()

        query = builder.buildQuery(application, session, [match: Comment, alias: 'Post', id: 1l], [] as Object[])
        assert query.toString() == '+(+() -$/uid:Post#1#) +(alias:Comment)', query.toString()

        // neither class/alias or instance
        shouldFail {
            builder.buildQuery(application, session, [], [209l] as Object[])
        }

        shouldFail {
            builder.buildQuery(application, session, [id: 209l], [] as Object[])
        }

        // no instance/id
        shouldFail {
            builder.buildQuery(application, session, [match: Post], [] as Object[])
        }

        shouldFail {
            builder.buildQuery(application, session, [], [Post] as Object[])
        }

        tx.commit()
        session.close()

        GroovySystem.metaClassRegistry.removeMetaClass(Post)
        GroovySystem.metaClassRegistry.removeMetaClass(Comment)
    }


    void testPolymorphicQuery() {
        def builder = new MoreLikeThisCompassQueryBuilder(compass)

        for (cls in [Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild]) {
            def metaClass = new ExpandoMetaClass(cls, true)
            metaClass.ident << { Object[] args ->
                return delegate.id
            }
            metaClass.initialize()
        }
//        metaClass = new ExpandoMetaClass(Comment, true)
//        metaClass.ident << { Object[] args ->
//            return delegate.id
//        }
//        metaClass.initialize()

        // todo what about CompassQuery options, eg, alias, sort?
        // todo if user defines alias, then assume expert and use that otherwise build aliases from class
//        fail()


        def session = compass.openSession()
        def tx = session.beginTransaction()

        def parent = session.get(Parent, 1)
        def query = builder.buildQuery(application, session, [match: Parent], [parent] as Object[])
        assert (query.toString() in [
            '+(+() -$/uid:Parent#1#) +(alias:Parent alias:SearchableChildOne alias:SearchableGrandChild alias:SearchableChildTwo)',
            '+(+() -$/uid:Parent#1#) +(alias:Parent alias:SearchableGrandChild alias:SearchableChildOne alias:SearchableChildTwo)',
            '+(+() -$/uid:Parent#1#) +(alias:SearchableGrandChild alias:Parent alias:SearchableChildOne alias:SearchableChildTwo)']), query.toString()

        query = builder.buildQuery(application, session, [match: SearchableChildOne], [parent] as Object[])
        assert (query.toString() in ['+(+() -$/uid:Parent#1#) +(alias:SearchableChildOne alias:SearchableGrandChild)',
                                     '+(+() -$/uid:Parent#1#) +(alias:SearchableGrandChild alias:SearchableChildOne)']), query.toString()

        query = builder.buildQuery(application, session, [match: SearchableGrandChild], [parent] as Object[])
        assert query.toString() == '+(+() -$/uid:Parent#1#) +(alias:SearchableGrandChild)', query.toString()

        tx.commit()
        session.close()

        [Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild].each { GroovySystem.metaClassRegistry.removeMetaClass(it) }
    }

    void testBuildQueryWithBuilderOptions() {
        def builder = new MoreLikeThisCompassQueryBuilder(compass)

        def recorded

        def metaClass = new ExpandoMetaClass(Post, true)
        metaClass.ident << { Object[] args ->
            return delegate.id
        }
        metaClass.initialize()
        metaClass = new ExpandoMetaClass(Comment, true)
        metaClass.ident << { Object[] args ->
            return delegate.id
        }
        metaClass.initialize()

        def application = [getArtefacts: {
            return [
            [getClazz: {
                return Comment.class
            }] as GrailsDomainClass,
            [getClazz: {
                return Post.class
            }] as GrailsDomainClass] as GrailsClass[]
        }] as GrailsApplication

        def mockQuery = new Mock(CompassQuery.class)
        def query = mockQuery.proxy()
        def mockMltq  = new Mock(CompassQueryBuilder.CompassMoreLikeThisQuery.class)
        def moreLikeThis = mockMltq.proxy()
        mockMltq.expects(new InvokeOnceMatcher()).method('setProperties').with(new IsEqual(['post', 'title'] as String[])).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setAliases').with(new IsEqual(['post', 'comment'] as String[])).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setAnalyzer').with(new IsEqual('myAnalyzer')).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setBoost').with(new IsEqual(true)).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setMaxNumTokensParsed').with(new IsEqual(15)).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setMaxQueryTerms').with(new IsEqual(60)).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setMaxWordLen').with(new IsEqual(8)).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setMinResourceFreq').with(new IsEqual(3)).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setMinTermFreq').with(new IsEqual(3)).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setMinWordLen').with(new IsEqual(3)).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setStopWords').with(new IsEqual(['and', 'to', 'or'] as String[])).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('setSubIndexes').with(new IsEqual(['index-a', 'index-m'] as String[])).will(new ReturnStub(moreLikeThis))
        mockMltq.expects(new InvokeOnceMatcher()).method('toQuery').withNoArguments().will(new ReturnStub(query))

        def session = [queryBuilder: {
            return [moreLikeThis: { String alias, Serializable id ->
                recorded = "MORE LIKE ${alias}#${id}"
                return moreLikeThis
            }] as CompassQueryBuilder
        }] as CompassSession

        // with all options - String[] are defined as List
        builder.buildQuery(application, session,
                [   aliases: ['post', 'comment'], analyzer: 'myAnalyzer', boost: true, maxNumTokensParsed: 15,
                    maxQueryTerms: 60, maxWordLen: 8, minResourceFreq: 3, minTermFreq: 3, minWordLen: 3,
                    properties: ['post', 'title'], stopWords: ['and', 'to', 'or'], subIndexes: ['index-a', 'index-m']
                ],
                [new Post(id: 10l)] as Object[])
        assert recorded == "MORE LIKE Post#10"
        mockMltq.verify()

        GroovySystem.metaClassRegistry.removeMetaClass(Post)
        GroovySystem.metaClassRegistry.removeMetaClass(Comment)
    }
}
