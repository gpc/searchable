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
package org.codehaus.groovy.grails.plugins.searchable.compass.config

import org.compass.core.config.CompassConfiguration
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.plugins.searchable.compass.test.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.property.SearchablePropertyTypes
import org.codehaus.groovy.grails.plugins.searchable.test.domain.reference.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.component.*
import org.codehaus.groovy.grails.plugins.searchable.test.compass.*
import org.compass.core.util.ClassUtils
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils

/**
* TODO refactor the bulk of this test code to integration style test, independent from the actual confiuration strategy used

* @author Maurice Nicholson
*/
class DomainClassXmlMappingBuilderSearchableCompassConfiguratorTests extends GroovyTestCase {
    def compass

    void tearDown() {
        compass?.close()
        compass = null
    }

    void testConfigure() {
        compass = buildCompass([Post, Comment])

        // Store and search some instances
        def post = new Post(id: 1L, title: "Hello world", post: "Hello world", comments: new HashSet(), createdAt: new Date())
        def comment1 = new Comment(id: 1L, post: post, comment: "Hello Hello World")
        def comment2 = new Comment(id: 2L, post: post, comment: "Hello Hello Hello World")
        post.comments.add(comment1)
        post.comments.add(comment2)
        assert post.comments.size() == 2

        TestCompassUtils.saveToCompass(compass, post, comment1, comment2)

        def fromIndex = TestCompassUtils.loadFromCompass(compass, Post, 1L)

        assert fromIndex.id == post.id
        assert fromIndex.title == post.title
        assert fromIndex.post == post.post
        assert fromIndex.createdAt
        assert fromIndex.comments.size() == 2
    }

    void testSearchableComponentTypes() {
        compass = buildCompass([ComponentOwner, SearchableComp])

        def comp1 = new Comp(compName: "comp one")
        def searchableCompOne1 = new SearchableComp(searchableCompName: "searchableCompOne one")
        def searchableCompTwo1 = new SearchableComp(searchableCompName: "searchableCompTwo one")
        def owner1 = new ComponentOwner(id: 1l, componentOwnerName: "owner one", searchableCompOne: searchableCompOne1, searchableCompTwo: searchableCompTwo1, comp: comp1)

        def comp2 = new Comp(compName: "comp two")
        def searchableCompOne2 = new SearchableComp(searchableCompName: "searchableCompOne two")
        def searchableCompTwo2 = new SearchableComp(searchableCompName: "searchableCompTwo two")
        def owner2 = new ComponentOwner(id: 2l, componentOwnerName: "owner two", searchableCompOne: searchableCompOne2, searchableCompTwo: searchableCompTwo2, comp: comp2)

        TestCompassUtils.saveToCompass(compass, owner1, owner2)

        assert TestCompassUtils.countHits(compass, { it.matchAll() }) == 2
        assert TestCompassUtils.numberIndexed(compass, ComponentOwner) == 2

        // Search on owner and component data
        assert TestCompassUtils.countHits(compass, { it.queryString("+owner +one").toQuery() }) == 1
        assert TestCompassUtils.countHits(compass, { it.queryString("+searchableCompTwo +one").toQuery() }) == 1
        assert TestCompassUtils.countHits(compass, { it.queryString("+owner +two").toQuery() }) == 1
        assert TestCompassUtils.countHits(compass, { it.queryString("+searchableCompOne +two").toQuery() }) == 1
        assert TestCompassUtils.countHits(compass, { it.queryString("owner").toQuery() }) == 2
        assert TestCompassUtils.countHits(compass, { it.queryString("searchableCompOne").toQuery() }) == 2

        // TODO maybe override normal Compass componnent converter so that properties are prefixed, eg, "searchableComp.searchableCompName:xyz"
    }

    void testSearchablePropertyTypes() {
        compass = buildCompass([SearchablePropertyTypes])

        def allProperties = new SearchablePropertyTypes(
            id: 1l, version: 0l,
            aLong: 8787878l,
            aBoolean: false, aBooleanObj: Boolean.TRUE,
            aByte: 0x7e, aByteObj: 0x12,
            aChar: 'x', aCharacter: 'M',
            aShort: 42, aShortObj: 16,
            anInt: 99, anInteger: 100,
            aFloat: 99.999f, aFloatObj: 78.78f,
            aDouble: 8686868.1, aDoubleObj: 76767.9090,

            string: "XYZ ABC 123",
            bigDecimal: 7.3,
            bigInteger: 15,
            locale: Locale.UK,
            url: new URL("http://grails.org"),
            date: new Date(676767676l),
            calendar: Calendar.getInstance(TimeZone.getTimeZone(TimeZone.availableIDs[0])), // TODO improve to definitely not use default
            //    File file
            sqlDate: new java.sql.Date(2007, 10, 19),
            sqlTime: new java.sql.Time(06, 12, 31),
            sqlTimestamp: new java.sql.Timestamp(1515151515l),
            buf: new StringBuffer("this string is not quite ready yet"),

            longArray: [1l, 2l, 4000l] as long[], longObjArray: [5l, 90l] as Long[],
            booleanArray: [false, true] as boolean[], booleanObjArray: [Boolean.TRUE, Boolean.FALSE] as Boolean[],
            byteArray: [0x10, 0x11] as byte[], byteObjArray: [0x1, 0x2] as Byte[],
            charArray: ['a', 'b', 'c'] as char[], characterArray: ['X', 'Y', 'Z'] as Character[],
            shortArray: [10, 9] as short[], shortObjArray: [3, 4] as Short[],
            intArray: [99, 100, 101] as int[], integerArray: [49, 50, 51] as Integer[],
            floatArray: [76.5f, 13.3f, 33.333f] as float[], floatObjArray: [66.66f, 56565.0f] as Float[],
            doubleArray: [8.0, 1.0, 23.6] as double[], doubleObjArray: [9.0, 56.11112] as Double[],

            stringArray: ["Hello", "World"] as String[],
            bigDecimalArray: [3.0, 5.0, 7.0] as BigDecimal[],
            bigIntegerArray: [1, 2, 9, 10] as BigInteger[],
            //localeArray: Locale[]
            urlArray: [new URL("http://grails.org"), new URL("http://groovy.codehaus.org")] as URL[],
            dateArray: [new Date(444444l), new Date(55555l)] as Date[],
            calendarArray: [Calendar.getInstance(TimeZone.getTimeZone(TimeZone.availableIDs[0])), Calendar.getInstance(TimeZone.getTimeZone(TimeZone.availableIDs[1]))] as Calendar[],
            //    File[] fileArray
            sqlDateArray: [new java.sql.Date(1995, 1, 20), new java.sql.Date(1964, 11, 1)] as java.sql.Date[],
            sqlTimeArray: [new java.sql.Time(01, 05, 12), new java.sql.Time(3, 3, 16)] as java.sql.Time[],
            sqlTimestampArray: [new java.sql.Timestamp(777777l), new java.sql.Timestamp(666666l)] as java.sql.Timestamp[],

            stringMap: [banana: "yellow", apple: "red", lime: "green", orange: "orange", plum: "purple"]
        )
        TestCompassUtils.saveToCompass(compass, allProperties)
        assert TestCompassUtils.countHits(compass) { builder -> builder.matchAll() } == 1

        def fromIndex = TestCompassUtils.loadFromCompass(compass, SearchablePropertyTypes, 1L)
        assert fromIndex.id == 1l
        assert fromIndex.version == 0l
        assert fromIndex.aLong == 8787878l
        assert fromIndex.aBoolean == false
        assert fromIndex.aBooleanObj == Boolean.TRUE
        assert fromIndex.aByte == 0x7E
        assert fromIndex.aByteObj == 0x12
        assert fromIndex.aChar == 'x'
        assert fromIndex.aCharacter == 'M'
        assert fromIndex.aShort == 42
        assert fromIndex.aShortObj == 16
        assert fromIndex.anInt == 99
        assert fromIndex.anInteger == 100
        assert fromIndex.aFloat == 99.999f
        assert fromIndex.aFloatObj == 78.78f
        assert fromIndex.aDouble == 8686868.1
        assert fromIndex.aDoubleObj == 76767.9090

        assert fromIndex.string == "XYZ ABC 123"
        assert fromIndex.bigDecimal ==  7.3
        assert fromIndex.bigInteger == 15
        // TODO when saving en_GB, from index it is en_gb
        // http://forums.opensymphony.com/thread.jspa?threadID=82127
//        assert fromIndex.locale == Locale.UK
        assert fromIndex.url == new URL("http://grails.org")
        assert fromIndex.date == new Date(676767676l)
        // TODO not working
        // http://forums.opensymphony.com/thread.jspa?threadID=82127
//        println "fromIndex.calendar ${fromIndex.calendar}\nallProperties.calendar ${allProperties.calendar}\n"
//        assert fromIndex.calendar.timeZone == allProperties.calendar.timeZone
        //    File file
        assert fromIndex.sqlDate == new java.sql.Date(2007, 10, 19)
        assert fromIndex.sqlTime == new java.sql.Time(06, 12, 31)
        assert fromIndex.sqlTimestamp == new java.sql.Timestamp(1515151515)
        assert fromIndex.buf instanceof StringBuffer && fromIndex.buf.toString() == "this string is not quite ready yet"

        assert fromIndex.longArray as List == [1l, 2l, 4000l]
        assert fromIndex.longObjArray as List == [5l, 90l]
        assert fromIndex.booleanArray as List == [false, true]
        assert fromIndex.booleanObjArray as List  == [Boolean.TRUE, Boolean.FALSE]
        assert fromIndex.byteArray as List  == [0x10, 0x11]
        assert fromIndex.byteObjArray as List  == [0x1, 0x2]
        assert fromIndex.charArray.size() == 3
        ['a', 'b', 'c'].eachWithIndex { c, i -> assert fromIndex.charArray[i] == c as char }
        assert fromIndex.charArray.size() == 3
        ['X', 'Y', 'Z'].eachWithIndex { c, i -> assert fromIndex.characterArray[i] == c as Character }
        assert fromIndex.shortArray as List  == [10, 9]
        assert fromIndex.shortObjArray as List  == [3, 4]
        assert fromIndex.intArray as List  == [99, 100, 101]
        assert fromIndex.integerArray as List  == [49, 50, 51]
        assert fromIndex.floatArray as List  == [76.5f, 13.3f, 33.333f]
        assert fromIndex.floatObjArray as List  == [66.66f, 56565.0f]
        assert fromIndex.doubleArray as List  == [8.0, 1.0, 23.6]
        assert fromIndex.doubleObjArray as List  == [9.0, 56.11112]

        assert fromIndex.stringArray as List == ["Hello", "World"]
        assert fromIndex.bigDecimalArray == [3.0, 5.0, 7.0] as BigDecimal[]
        assert fromIndex.bigIntegerArray == [1, 2, 9, 10] as BigInteger[]
        // TODO
        // http://forums.opensymphony.com/thread.jspa?threadID=82127
        //localeArray: Locale[]
        assert fromIndex.urlArray == [new URL("http://grails.org"), new URL("http://groovy.codehaus.org")] as URL[]
        assert fromIndex.dateArray == [new Date(444444l), new Date(55555l)] as Date[]
        // TODO
        // Comes back from index as default time zone
        // http://forums.opensymphony.com/thread.jspa?threadID=82127
//        println "fromIndex.calendarArray[0] is ${fromIndex.calendarArray[0].timeZone}\n was ${allProperties.calendarArray[0].timeZone}\n"
//        assert fromIndex.calendarArray[0].timeZone.iD == allProperties.calendarArray[0].timeZone.iD
//        assert fromIndex.calendarArray[1].timeZone == allProperties.calendarArray[1].timeZone
            //    File[] fileArray
        assert fromIndex.sqlDateArray == [new java.sql.Date(1995, 1, 20), new java.sql.Date(1964, 11, 1)] as java.sql.Date[]
        assert fromIndex.sqlTimeArray == [new java.sql.Time(01, 05, 12), new java.sql.Time(3, 3, 16)] as java.sql.Time[]
        assert fromIndex.sqlTimestampArray == [new java.sql.Timestamp(777777l), new java.sql.Timestamp(666666l)] as java.sql.Timestamp[]

        assert fromIndex.stringMap == [banana: "yellow", apple: "red", lime: "green", orange: "orange", plum: "purple"]

        // Search with specific Map key as index field
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.queryString('banana:yellow').toQuery()
        }
        // Search in ALL property
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.queryString('yellow').toQuery()
        }

        // Search by derived property, since they only exist in the index or in the original object
        // as there is no corresponding setter for when they are unmarshalled
        def alias = SearchableCompassUtils.getMappingAlias(compass, SearchablePropertyTypes)
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.bool().addMust(builder.alias(alias)).addMust(builder.term('derivedInt', 6)).toQuery()
        }
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.bool().addMust(builder.alias(alias)).addMust(builder.term('derivedIntArray', 900)).addMust(builder.term('derivedIntArray', 300)).toQuery()
        }
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.bool().addMust(builder.alias(alias)).addMust(builder.term('derivedInteger', 60)).toQuery()
        }
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.bool().addMust(builder.alias(alias)).addMust(builder.term('derivedIntegerArray', 9000)).addMust(builder.term('derivedIntegerArray', 3000)).toQuery()
        }
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.bool().addMust(builder.alias(alias)).addMust(builder.term('derivedBigDecimal', 7230.12)).toQuery()
        }
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.bool().addMust(builder.alias(alias)).addMust(builder.term('derivedBigDecimalArray', 1.1)).addMust(builder.term('derivedBigDecimalArray', 99.8999999999)).toQuery()
        }

        // Same for explicit transient property
        assert 1 == TestCompassUtils.countHits(compass) { builder ->
            builder.bool().addMust(builder.alias(alias)).addMust(builder.term('transientProperty', 'minute')).toQuery()
        }
    }

    void testSearchableReferenceTypes() {
        compass = buildCompass([SearchableReferenceTypes, Referee])

        def orig = new SearchableReferenceTypes(
            id: 1l, version: 0l,

            ref: new Referee(999l, "single ref"),
            refSet: [new Referee(1l, "set 1"), new Referee(2l, "set 2"), new Referee(3l, "set 3")] as Set,
            refList: [new Referee(10l, "list 1"), new Referee(20l, "list 2"), new Referee(30l, "list 3")] //,
            // TODO implement reference maps
//            refMap: ["map 1": new Referee(100l, "map 1"), "map 2": new Referee(200l, "map 2"), "map 3": new Referee(300l, "map 2")]
        )
//        (orig.refSet + orig.refList + orig.refMap.values() + orig.ref + orig).each { TestCompassUtils.saveToCompass(compass, it) }
        (orig.refSet + orig.refList + orig.ref + orig).each { TestCompassUtils.saveToCompass(compass, it) }
        def fromIndex = TestCompassUtils.loadFromCompass(compass, SearchableReferenceTypes, 1L)
        assert fromIndex.ref == new Referee(999l, "single ref")
        def refSetAsList = fromIndex.refSet.sort { it.id } as List
        assert refSetAsList[0] == new Referee(1l, "set 1")
        assert refSetAsList[1] == new Referee(2l, "set 2")
        assert refSetAsList[2] == new Referee(3l, "set 3")
        assert fromIndex.refList[0] == new Referee(10l, "list 1")
        assert fromIndex.refList[1] == new Referee(20l, "list 2")
        assert fromIndex.refList[2] == new Referee(30l, "list 3")
        //println "fromIndex.refMap " + fromIndex.refMap
//        assert fromIndex.refMap["map 1"] == new Referee(100l, "map 1")
    }

    private buildCompass(classes) {
        def configurator = new DomainClassXmlMappingBuilderSearchableCompassConfigurator()
        configurator.grailsApplication = new DefaultGrailsApplication(classes as Class[], new GroovyClassLoader(Thread.currentThread().getContextClassLoader()))
        // TODO using "new org.codehaus.groovy.grails.plugins.searchable.compass.mapping.DefaultSearchableCompassClassMappingXmlBuilder()" causes a StackOverflowError - why!? 
        configurator.compassClassMappingXmlBuilder = ClassUtils.forName("org.codehaus.groovy.grails.plugins.searchable.compass.mapping.DefaultSearchableCompassClassMappingXmlBuilder").newInstance()

        def config = new CompassConfiguration()
        config.setConnection("ram://compasstest")
        configurator.configure(config, [:])
        compass = config.buildCompass()
    }
}
