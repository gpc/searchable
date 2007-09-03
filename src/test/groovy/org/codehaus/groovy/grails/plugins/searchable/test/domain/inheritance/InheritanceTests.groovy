package org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance

import org.codehaus.groovy.grails.plugins.searchable.compass.spring.DefaultSearchableCompassFactoryBean
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.springframework.util.ClassUtils
import org.springframework.core.io.DefaultResourceLoader
import org.codehaus.groovy.grails.plugins.searchable.compass.test.AbstractSearchableCompassTests
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompassMappingUtils
import org.codehaus.groovy.grails.plugins.searchable.compass.DefaultSearchableMethodFactory

/**
* @author Maurice Nicholson
*/
class InheritanceTests extends AbstractSearchableCompassTests {
    def compass
    def grailsApplication
    def methodFactory

    void setUp() {
        def cl = new GroovyClassLoader() //Thread.currentThread().getContextClassLoader())
        Thread.currentThread().setContextClassLoader(cl)
        compass = buildCompass([Parent, SearchableChildOne, SearchableChildTwo, NonSearchableChild, Associate, SearchableGrandChild], cl)
        methodFactory = new DefaultSearchableMethodFactory(compass: compass, grailsApplication: grailsApplication)
    }

    void tearDown() {
        compass = null
        grailsApplication = null
    }

    void testMappingWithSaveAndLoad() {
        def p = new Parent(id: 1l, version: 0l, commonProperty: "I am a parent")
        saveToCompass(p)
        p = loadFromCompass(Parent, 1l)
        assert p
        assert p.id == 1l
        assert p.commonProperty == 'I am a parent'

        def c1 = new SearchableChildOne(id: 10l, version: 0l, commonProperty: 'I am a searchable child one', childOneProperty: 'Child One')
        def c2 = new SearchableChildTwo(id: 11l, version: 0l, commonProperty: 'I am a searchable child two', childTwoProperty: 'Child Two')
        saveToCompass(c1, c2)
        c1 = loadFromCompass(SearchableChildOne, 10l)
        c2 = loadFromCompass(SearchableChildTwo, 11l)
        assert c1.commonProperty == 'I am a searchable child one'
        assert c1.childOneProperty == 'Child One'
        assert c2.commonProperty == 'I am a searchable child two'
        assert c2.childTwoProperty == 'Child Two'

        // TODO this works !? sort of. How to exclude non-searchable child from index... hmm
        def n1 = new NonSearchableChild(id: 20l, version: 0l, commonProperty: 'I am a non searchable child', nonSearchableChildProperty: 'Non Searchable')
        saveToCompass(n1)
        n1 = loadFromCompass(NonSearchableChild, 20l)
        assert n1

        def poly1 = new SearchableChildTwo(id: 10l, version: 0l, commonProperty: 'I the poly instance', childTwoProperty: 'number two')
        c1 = new SearchableChildOne(id: 11l, version: 0l, commonProperty: 'I am a specific class instance', childOneProperty: 'number one')
        def a = new Associate(id: 50l, name: 'Bill', polyInstance: poly1, specificInstance: c1)
        saveToCompass(poly1, c1, a)
        a = loadFromCompass(Associate, 50l)
        assert a.name == 'Bill'
        assert a.specificInstance instanceof SearchableChildOne
        assert a.specificInstance.childOneProperty == 'number one'
        assert a.polyInstance instanceof SearchableChildTwo
        assert a.polyInstance.childTwoProperty == 'number two'
    }

    void testInheritanceMetaData() {
        // Tests that additional meta data is added to the index to allow for
        // psuedo-polymorphic queries
        def p1 = new Parent(id: 10l, commonProperty: "I am the searchable parent")
        def c1 = new SearchableChildOne(id: 10l, version: 0l, commonProperty: 'I am a searchable child one', childOneProperty: 'Child One')
        def c2 = new SearchableChildTwo(id: 11l, version: 0l, commonProperty: 'I am a searchable child two', childTwoProperty: 'Child Two')
        def g1 = new SearchableGrandChild(id: 11l, version: 0l, commonProperty: 'I am a searchable grandchild one', grandChildProperty: 'youngest')
        saveToCompass(p1, c1, c2, g1)

        def hits = withCompassQueryBuilder { queryBuilder ->
            queryBuilder.queryBuilder.queryString("searchable").toQuery().setAliases([CompassMappingUtils.getDefaultAlias(Parent)] as String[]).hits()
        }
        assert hits.length == 1

        hits = withCompassQueryBuilder { queryBuilder ->
            queryBuilder.queryBuilder.queryString("searchable").toQuery().hits()
        }
        assert hits.length == 4

        hits = withCompassQueryBuilder { queryBuilder ->
            queryBuilder.queryBuilder.term('$/poly/class', Parent.class.name).hits()
        }
        assert hits.length == 1

        hits = withCompassQueryBuilder { queryBuilder ->
            queryBuilder.queryBuilder.term('$/poly/class', SearchableChildOne.class.name).hits()
        }
        assert hits.length == 1

        hits = withCompassQueryBuilder { queryBuilder ->
            queryBuilder.queryBuilder.term('$/poly/class', SearchableChildTwo.class.name).hits()
        }
        assert hits.length == 1

        hits = withCompassQueryBuilder { queryBuilder ->
            queryBuilder.queryBuilder.term('$/poly/class', SearchableGrandChild.class.name).hits()
        }
        assert hits.length == 1
    }

    void testPolymorphicQuery() {
        def p1 = new Parent(id: 10l, commonProperty: "I am the searchable parent")
        def c1 = new SearchableChildOne(id: 10l, version: 0l, commonProperty: 'I am a searchable child one', childOneProperty: 'Child One')
        def c2 = new SearchableChildTwo(id: 11l, version: 0l, commonProperty: 'I am a searchable child two', childTwoProperty: 'Child Two')
        def g1 = new SearchableGrandChild(id: 11l, version: 0l, commonProperty: 'I am a searchable grandchild one', grandChildProperty: 'youngest')
        saveToCompass(p1, c1, c2, g1)

        def searchEvery = methodFactory.getMethod("searchEvery")

        def objects = searchEvery.invoke("searchable")
        assert objects.size() == 4

        objects = searchEvery.invoke("searchable", [class: Parent])
        assert objects.size() == 4

        objects = searchEvery.invoke("searchable", [class: SearchableChildOne])
        assert objects.size() == 2

        objects = searchEvery.invoke("searchable", [class: SearchableChildTwo])
        assert objects.size() == 1

        objects = searchEvery.invoke("searchable", [class: SearchableGrandChild])
        assert objects.size() == 1
    }

    private buildCompass(classes, cl) {
        // TODO this is nasty - remove dependency on GrailsApplication!
        grailsApplication = new DefaultGrailsApplication(classes as Class[], cl)
        grailsApplication.initialise()

        def fb = new DefaultSearchableCompassFactoryBean()
        fb.resourceLoader = new DefaultResourceLoader()
        fb.grailsApplication = grailsApplication
        fb.compassClassMappingXmlBuilder = ClassUtils.forName("org.codehaus.groovy.grails.plugins.searchable.compass.mapping.DefaultSearchableCompassClassMappingXmlBuilder").newInstance()
        fb.compassConnection = "ram://testindex"
        fb.afterPropertiesSet()
        fb.getObject()
    }
}