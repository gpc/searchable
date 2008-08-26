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
package org.codehaus.groovy.grails.plugins.searchable.compass.converter

import org.codehaus.groovy.grails.plugins.searchable.test.compass.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.stringmap.*
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.compass.core.config.CompassConfiguration
import org.compass.core.util.ClassUtils
import org.compass.core.mapping.ResourcePropertyMapping
import org.compass.core.marshall.MarshallingContext
import org.compass.core.engine.SearchEngine
import org.compass.core.Property
import org.compass.core.Property.TermVector
import org.compass.core.Property.Store
import org.compass.core.Property.Index
import org.compass.core.Resource
import org.compass.core.engine.naming.*
import org.compass.core.CompassSession
import org.compass.core.engine.SearchEngineFactory
import org.compass.core.Compass
import org.compass.core.spi.InternalCompassSession
import org.compass.core.spi.InternalCompass
import org.compass.core.ResourceFactory

/**
*
*
* @author Maurice Nicholson
*/
class StringMapConverterTests extends GroovyTestCase {
    static EMPTY_MAP = [:]
    static MY_STRING_MAP = [hello: 'world', plugin: 'searchable', withComma: 'a comma, could be bad', 'with': 'is a groovy keyword', thisValue: 'has { certain special ] symbols: that could be "problematic"']
    static NUMBERS_MAP = [one: '1', two: '2', three: "3"]

    static EMPTY_MAP_AS_RESOURCE = ['$/TheClass/emptyMap/stringmap': "[:]"]
    static MY_STRING_MAP_AS_RESOURCE = ['hello': "world", 'plugin': "searchable", 'withComma': "a comma, could be bad", 'with': "is a groovy keyword", 'thisValue': "has { certain special ] symbols: that could be \"problematic\"", '$/TheClass/myStringMap/stringmap': '[\"hello\":\"world\", \"plugin\":\"searchable\", \"withComma\":\"a comma, could be bad\", \"with\":\"is a groovy keyword\", \"thisValue\":\"has { certain special ] symbols: that could be \\"problematic\\"\"]']
    static NUMBERS_MAP_AS_RESOURCE = [one: "1", two: "2", three: "3", '$/TheClass/numbers/stringmap': '[\"one\":\"1\", \"two\":\"2\", \"three\":\"3\"]']

    def converter

    void setUp() {
        converter = new StringMapConverter()
    }

    void tearDown() {
        converter = null
    }

   // TODO test supportUnmarshall = false 

    void testRoundtrip() {
        for (map in [EMPTY_MAP, MY_STRING_MAP, NUMBERS_MAP]) {
            def resource = marshallToResourceMap(map, "theMap", true)
            assert unmarshallFromResourceMap(resource, "theMap", true) == map
        }
    }

    void testMarshall() {
        assert marshallToResourceMap(null, 'nullMap', true) == [:]

        assert marshallToResourceMap(null, 'nullMap', false) == [:]

        assert marshallToResourceMap(EMPTY_MAP, 'emptyMap', false) == EMPTY_MAP_AS_RESOURCE

        assert marshallToResourceMap(MY_STRING_MAP, 'myStringMap', false) == MY_STRING_MAP_AS_RESOURCE

        assert marshallToResourceMap(NUMBERS_MAP, 'numbers', false) == NUMBERS_MAP_AS_RESOURCE
    }

    void testUnmarshall() {
        assert unmarshallFromResourceMap([:], 'nullMap', true) == null

        assert unmarshallFromResourceMap([:], 'nullMap', false) == null

        assert unmarshallFromResourceMap(EMPTY_MAP_AS_RESOURCE, 'emptyMap', false) == EMPTY_MAP

        assert unmarshallFromResourceMap(MY_STRING_MAP_AS_RESOURCE, 'myStringMap', false) == MY_STRING_MAP

        assert unmarshallFromResourceMap(NUMBERS_MAP_AS_RESOURCE, 'numbers', false) == NUMBERS_MAP
    }

    private marshallToResourceMap(map, propertyName, handleNulls = false) {
        def mapping = getResourcePropertyMapping(propertyName)
        def context = getContext(handleNulls)
        def resourceProperties = [:]
        def resource = [
            addProperty: { property ->
                print "${(resourceProperties[property.getName()] = property.getStringValue()) ? '' : ''}"
//                { -> resourceProperties[property.getName()] = property.getStringValue()}()
//                resourceProperties.add(property)
            }] as Resource
        converter.marshall(resource, map, mapping, context)
        return resourceProperties
    }

    private unmarshallFromResourceMap(resourceMap, propertyName, handleNulls = false) {
        def resource = [getValue: {key ->
            resourceMap[key]
        }] as Resource
        def mapping = getResourcePropertyMapping(propertyName)
        def context = getContext(handleNulls)

        return converter.unmarshall(resource, mapping, context)
    }

    private getContext(handleNulls) {
        def resourceFactory = [
            createProperty: { Object[] args -> //String name, String value, Property.Store store, Property.Index index, Property.TermVector termVector = null ->
                [getName: {args[0]}, getOjectValue: {args[1]}, getStringValue: {args[1]}, setBoost: {}, getBoost: {1.0f}] as Property
            }] as ResourceFactory
        def searchEngineFactory = [
            getPropertyNamingStrategy: {
                new DefaultPropertyNamingStrategy()
            }] as SearchEngineFactory
        def compass = [ getSearchEngineFactory: { searchEngineFactory } ] as InternalCompass
        def session = [ getCompass: { compass } ] as InternalCompassSession
        def context = [
            getResourceFactory: { resourceFactory },
            getSession: { session },
            handleNulls: { handleNulls }
        ] as MarshallingContext
        return context
    }

    private getResourcePropertyMapping(propertyName) {
        return [getAnalyzer: {null}, getBoost: {1.0f}, getIndex: {null}, getStore: {null}, getTermVector: {null}, getName: { propertyName }, getPath: { new StaticPropertyPath('$/TheClass/' + propertyName) }] as ResourcePropertyMapping
    }
}