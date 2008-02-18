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

import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.*
import org.compass.core.Compass
import org.compass.core.CompassTermFreqsBuilder;
import org.codehaus.groovy.grails.plugins.searchable.compass.search.DefaultTermFreqsMethod.TermFreqsCompassCallback
import org.codehaus.groovy.grails.plugins.searchable.compass.search.DefaultTermFreqsMethod.TermFreqsArgs
import org.compass.core.CompassSession
import static org.easymock.EasyMock.*
import org.easymock.EasyMock
import org.compass.core.CompassTransaction
import org.easymock.IMocksControl
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.osem.ClassMapping
import org.compass.core.spi.InternalCompass
import org.compass.core.config.CompassSettings;

/**
*
* @author Maurice Nicholson
*/
// TODO default options: size 100, normalize ?
class DefaultTermFreqsMethodTests extends GroovyTestCase {

    void setUp() {

    }

    void tearDown() {

    }

    void testTermFreqsArgsParse() {
        TermFreqsArgs tfa

        // without default properties
        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name']] as Object[], [:])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs([[size: 100, properties: ['title', 'desc']]] as Object[], [:])
        assert tfa.properties == ['title', 'desc'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs(['title', 'desc'] as Object[], [:])
        assert tfa.properties == ['title', 'desc'] as String[]
        assert tfa.size == null
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs(['name', [sort: 'freq']] as Object[], [:])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == null
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == CompassTermFreqsBuilder.Sort.FREQ

        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name', class: Post.class]] as Object[], [:])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null
        assert tfa.clazz == Post.class

        // with default properties
        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name']] as Object[], [property: 'all'])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs([[size: 100, properties: ['title', 'desc']]] as Object[], [property: 'all'])
        assert tfa.properties == ['title', 'desc'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs(['title', 'desc'] as Object[], [property: 'all'])
        assert tfa.properties == ['title', 'desc'] as String[]
        assert tfa.size == null
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null

        tfa = TermFreqsArgs.parseArgs(['name', [sort: 'freq']] as Object[], [property: 'all'])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == null
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == CompassTermFreqsBuilder.Sort.FREQ

        tfa = TermFreqsArgs.parseArgs([[size: 100, property: 'name']] as Object[], [property: 'all', class: Comment.class])
        assert tfa.properties == ['name'] as String[]
        assert tfa.size == 100
        assert tfa.normalizeRange == null
        assert tfa.aliases == null
        assert tfa.sort == null
        assert tfa.clazz == Comment.class
    }

    void testTermFreqsMethod() {
        def mockTestBuilder = new EasyMockTestBuilder()
        Map defaultOptions = [property: 'all']
        def result
        def gcl = new GroovyClassLoader()
        GrailsApplication application = new DefaultGrailsApplication([Post, User, Comment, Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild, NonSearchableChild] as Class[], gcl)
        application.initialise()

        // no options (defaults)
        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb ->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
            String[] properties = ['all'] as String[]
            expects {
                inOrder = true
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
//                inOrder = false
                expect(session.termFreqsBuilder(aryEq(properties))).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
//                inOrder = true
                tx.commit()
                session.close()
            }

            result = method.invoke()
        }

        // with all options
        mockTestBuilder.verify { InternalCompass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
            String[] properties = ['all'] as String[]
            CompassMapping compassMapping = new CompassMapping();
            compassMapping.addMapping(new ClassMapping(clazz: Post.class, name: Post.class.name, alias: "P"))
            expects {
//                ordered {
                expect(compass.getMapping()).andReturn(compassMapping)
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
//                }
                // unordered {
                expect(tfb.setSize(50)).andReturn(tfb)
                expect(tfb.normalize(0, 1)).andReturn(tfb)
                expect(tfb.setAliases(aryEq(['P'] as String[]))).andReturn(tfb)
                expect(tfb.setSort(CompassTermFreqsBuilder.Sort.TERM)).andReturn(tfb)
                // }
//                ordered {
                expect(session.termFreqsBuilder(aryEq(properties))).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
                // }
            }

            result = method.invoke(normalize: 0..1, size: 50, sort: 'term', class: Post.class)
        }

        // with non-poly class options (and other options)
/*        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, defaultOptions)
            String[] properties = ['name'] as String[]
            expects {
//                inOrder = true
                expect(compass.openSession()).andReturn(session)
                expect(session.beginTransaction(null)).andReturn(tx)
//                inOrder = false
                expect(session.termFreqsBuilder(aryEq(properties))).andReturn(tfb)
                expect(tfb.setSize(50)).andReturn(tfb)
                expect(tfb.normalize(0, 1)).andReturn(tfb)
                // TODO
//                expect(tfb.setAliases(['Post'] as String[])).andReturn(tfb)
                expect(tfb.setSort(CompassTermFreqsBuilder.Sort.TERM)).andReturn(tfb)
//                inOrder = true
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
            }

            result = method.invoke(class: Post.class, normalize: 0..1, size: 50, sort: 'term')
        }
*/
        // Now individual options in detail

        // "property" single String
        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
            String[] properties = ['title'] as String[]
            expects {
//                inOrder = true
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
                expect(session.termFreqsBuilder(aryEq(properties))).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
            }

            result = method.invoke(property: 'title')
        }

        // "properties" List value
        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
            String[] properties = ['title', 'description'] as String[]
            expects {
//                inOrder = true
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
                expect(session.termFreqsBuilder(aryEq(properties))).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
            }

            result = method.invoke(properties: ['title', 'description'])
        }

        // "properties" String[] value
        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
            String[] properties = ['title', 'description'] as String[]
            expects {
//                inOrder = true
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
                expect(session.termFreqsBuilder(aryEq(properties))).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
            }

            result = method.invoke(properties: ['title', 'description'] as String[])
        }

        // "normalise" British English spelling ;-)
        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
            String[] properties = ['all'] as String[]
            expects {
//                inOrder = true
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
//                inOrder = false
                expect(tfb.normalize(0, 10)).andReturn(tfb)
//                expect(tfb.setAliases(aryEq(['Book'] as String[]))).andReturn(tfb)
//                inOrder = true
                expect(session.termFreqsBuilder(aryEq(properties))).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
            }

            result = method.invoke(normalise: 0..10)
        }

        // "normalise" British English spelling ;-)
        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
            String[] properties = ['all'] as String[]
            expects {
//                inOrder = true
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
//                inOrder = false
                expect(tfb.setSort(CompassTermFreqsBuilder.Sort.FREQ)).andReturn(tfb)
//                expect(tfb.setAliases(aryEq(['Book'] as String[]))).andReturn(tfb)
//                inOrder = true
                expect(session.termFreqsBuilder(aryEq(properties))).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
            }

            result = method.invoke(sort: 'freq')
        }

        // (unamed) property name arg
        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
//            String[] properties = ['all'] as String[]
            expects {
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
                expect(session.termFreqsBuilder(aryEq(['name'] as String[]))).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
            }

            result = method.invoke('name')
        }

        // (unamed) property names and other options
        mockTestBuilder.verify { Compass compass, CompassSession session, CompassTransaction tx, CompassTermFreqsBuilder tfb->
            def method = new DefaultTermFreqsMethod("termFreqs", compass, application, defaultOptions)
//            String[] properties = ['all'] as String[]
            expects {
                expect(compass.openSession()).andReturn(session)
                expect(session.getSettings()).andReturn(new CompassSettings())
                expect(session.beginTransaction(null)).andReturn(tx)
                expect(session.termFreqsBuilder(aryEq(['title', 'desc'] as String[]))).andReturn(tfb)
                expect(tfb.setSort(CompassTermFreqsBuilder.Sort.TERM)).andReturn(tfb)
                expect(tfb.setSize(10)).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
                tx.commit()
                session.close()
            }

            result = method.invoke('title', 'desc', sort: 'term', size: 10)
        }
    }

    void testTermFreqsBuilderCompassCallback() {
        // Test everything set on the builds is passed on to the actual Compass builder
        TermFreqsCompassCallback builder
        def result

/*
        CompassTermFreqsBuilder tfb = createMock(CompassTermFreqsBuilder.class)
        CompassSession session = createMock(CompassSession.class)
        String[] properties = ['title', 'description'] as String[]
        expect(session.termFreqsBuilder(properties)).andReturn(tfb)
        expect(tfb.toTermFreqs()).andReturn(null)
        replay(tfb)
        replay(session)
        builder = new TermFreqsBuilderCompassCallback(properties)
        result = builder.doInCompass(session)
        verify(session)
        verify(tfb)
*/
        // Just minimum
        def mockTestBuilder = new EasyMockTestBuilder()
        mockTestBuilder.verify { CompassTermFreqsBuilder tfb, CompassSession session ->
            String[] properties = ['title', 'description'] as String[]
            expects {
                expect(session.termFreqsBuilder(properties)).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
            }

            builder = new TermFreqsCompassCallback(new TermFreqsArgs(properties: properties))
            result = builder.doInCompass(session)
        }

        // With normalize range
        mockTestBuilder.verify { CompassTermFreqsBuilder tfb, CompassSession session ->
            String[] properties = ['tag'] as String[]
            expects {
                expect(session.termFreqsBuilder(properties)).andReturn(tfb)
                expect(tfb.normalize(0, 1)).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
            }

            builder = new TermFreqsCompassCallback(new TermFreqsArgs(properties: properties, normalizeRange: 0..1))
            result = builder.doInCompass(session)
        }

        // With size
        mockTestBuilder.verify { CompassTermFreqsBuilder tfb, CompassSession session ->
            String[] properties = ['name', 'address'] as String[]
            expects {
                expect(session.termFreqsBuilder(properties)).andReturn(tfb)
                expect(tfb.setSize(100)).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
            }

            builder = new TermFreqsCompassCallback(new TermFreqsArgs(properties: properties, size: 100))
            result = builder.doInCompass(session)
        }

        // With sort
        mockTestBuilder.verify { CompassTermFreqsBuilder tfb, CompassSession session ->
            String[] properties = ['name', 'address'] as String[]
            expects {
                expect(session.termFreqsBuilder(properties)).andReturn(tfb)
                expect(tfb.setSort(CompassTermFreqsBuilder.Sort.TERM)).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
            }

            builder = new TermFreqsCompassCallback(new TermFreqsArgs(properties: properties, sort: CompassTermFreqsBuilder.Sort.TERM))
            result = builder.doInCompass(session)
        }

        // With aliases
        mockTestBuilder.verify { CompassTermFreqsBuilder tfb, CompassSession session ->
            String[] properties = ['name', 'address'] as String[]
            String[] aliases = ['Bookmark', 'Tag'] as String[]
            expects {
                expect(session.termFreqsBuilder(properties)).andReturn(tfb)
                expect(tfb.setAliases(aliases)).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
            }

            builder = new TermFreqsCompassCallback(new TermFreqsArgs(properties: properties, aliases: aliases))
            result = builder.doInCompass(session)
        }

        // Combos
        mockTestBuilder.verify { CompassTermFreqsBuilder tfb, CompassSession session ->
            String[] properties = ['text', 'preface'] as String[]
            String[] aliases = ['Book'] as String[]
            expects {
                expect(session.termFreqsBuilder(properties)).andReturn(tfb)
                expect(tfb.setSize(3000)).andReturn(tfb)
                expect(tfb.normalize(1, 100)).andReturn(tfb)
                expect(tfb.setAliases(aliases)).andReturn(tfb)
                expect(tfb.setSort(CompassTermFreqsBuilder.Sort.FREQ)).andReturn(tfb)
                expect(tfb.toTermFreqs()).andReturn(null)
            }

            builder = new TermFreqsCompassCallback(new TermFreqsArgs(properties: properties, aliases: aliases, size: 3000, normalizeRange: 1..100, sort: CompassTermFreqsBuilder.Sort.FREQ))
            result = builder.doInCompass(session)
        }
    }
}

class EasyMockTestBuilder {
    boolean inExpects = false
    IMocksControl control

    def verify(Closure closure) {
        control = EasyMock.createControl(); // start over

        // make mocks
        List mocks = []
        for (type in closure.getParameterTypes()) {
            mocks << control.createMock(type)
        }
        closure = closure.clone()
        closure.delegate = this
        closure.call(mocks)

        control.verify()
    }

    void expects(Closure closure) {
        inExpects = true
        closure.call()
        control.replay()
        inExpects = false
    }

    void setProperty(String name, Object value) {
        if (name == "inOrder") {
            control.checkOrder(value as boolean)
            return
        }
        throw new MissingPropertyException()
    }
}