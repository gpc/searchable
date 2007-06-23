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
package org.codehaus.groovy.grails.plugins.searchable.compass.mapping

import org.compass.core.config.*
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils

/**
*
*
* @author Maurice Nicholson
*/
class SearchableGrailsDomainClassCompassMappingDescriptionProviderManagerTests extends GroovyTestCase {
    def provider
    def postDc
    def commentDc
    def userDc

    void setUp() {
        provider = getProvider()
        postDc = new DefaultGrailsDomainClass(Post)
        commentDc = new DefaultGrailsDomainClass(Comment)
        userDc = new DefaultGrailsDomainClass(User)
    }

    void tearDown() {
        provider = null
        postDc = null
        commentDc = null
        userDc = null
    }

    private getProvider(defaulExcludes = ["password"], defaultFormats = [:]) {
        SearchableGrailsDomainClassMappingStrategyFactory.getMappingDescriptionProviderManager(defaulExcludes, defaultFormats)
    }

    //  public CompassMappingDescription[] getCompassMappingDescriptions(Map searchableDomainClassesMap) {
    void testGetCompassMappingDescriptions() {
        def searchableMap
        def descs
        def desc

        // When "searchable = true" across all domain objects
        searchableMap = [(postDc): true, (commentDc): true, (userDc): true]
        descs = provider.getCompassMappingDescriptions(searchableMap)
        assert descs.size() == 3

        desc = descs.find { it.mappedClass == Post }
        assert desc.properties == [version: [property: true], title: [property: true], post: [property: true], comments: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(Comment)]], createdAt: [property: true], author: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(User)]]]

        // NOT mapping password!
        desc = descs.find { it.mappedClass == User }
        assert desc.properties == [version: [property: true], username: [property: true], email: [property: true], createdAt: [property: true], posts: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(Post)]]]

        // Possible to override this behavoir with other default property excludes
        def currentDefaultExcludedProperties = provider.defaultExcludedProperties
        provider.defaultExcludedProperties = ["createdAt", "version"] as String[]
        descs = provider.getCompassMappingDescriptions(searchableMap)
        desc = descs.find { it.mappedClass == User }
        assert desc.properties == [username: [property: true], password: [property: true], email: [property: true], posts: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(Post)]]]
        provider.defaultExcludedProperties = currentDefaultExcludedProperties // reset

        // default format
        provider = getProvider([], [(Date): 'yyyy-MMM-dd, HH:mm', (Long): '0000'])
        descs = provider.getCompassMappingDescriptions(searchableMap)
        desc = descs.find { it.mappedClass == Comment }
        assert desc.properties == [version: [property: [format: '0000']], summary: [property: true], comment: [property: true], createdAt: [property: [format: 'yyyy-MMM-dd, HH:mm']], post: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(Post)]]]
        provider = getProvider() // restore defaults
        
        // When "searchable = true" for *some* but not all domain classes
        searchableMap = [(postDc): true, (commentDc): true]
        descs = provider.getCompassMappingDescriptions(searchableMap)
        assert descs.size() == 2

        desc = descs.find { it.mappedClass == Post }
        assert desc.properties.keySet() == ["version", "title", "post", "comments", "createdAt"] as Set

        desc = descs.find { it.mappedClass == Comment }
        assert desc.properties.keySet() == ["version", "summary", "comment", "post", "createdAt"] as Set

        // When "searchable = [except: ...]/[only: ...]"
        searchableMap = [(postDc): [only: ["title", "post"]], (commentDc): [except: "post"]]
        descs = provider.getCompassMappingDescriptions(searchableMap)
        assert descs.size() == 2

        desc = descs.find { it.mappedClass == Post }
        assert desc.properties.keySet() == ["title", "post"] as Set

        desc = descs.find { it.mappedClass == Comment }
        assert desc.properties.keySet() == ["version", "summary", "comment", "createdAt"] as Set

        // When searchable is closure
        searchableMap = [
            (postDc): {
                // all properties
                version(index: 'un_tokenized', excludeFromAll: true) // assumed to be property
                title(boost: 2.0f, analyzer: "myAnalyzer") // assumed to be property
                post(termVector: 'yes') // property
                comments(reference: true, component: true) // reference & component defaults
            },
            (commentDc): {
                only = ["comment", "summary"]
                summary(boost: 1.5f)
            },
            (userDc): {
                except = ["password", "email"]
                username(index: 'un_tokenized')
            }]
        descs = provider.getCompassMappingDescriptions(searchableMap)
        assert descs.size() == 3

        desc = descs.find { it.mappedClass == Post }
        assert desc.properties == [version: [property: [index: 'un_tokenized', excludeFromAll: true]], title: [property: [boost: 2.0f, analyzer: 'myAnalyzer']], post: [property: [termVector: 'yes']], comments: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(Comment)], component: [refAlias: SearchableCompassUtils.getDefaultAlias(Comment)]], createdAt: [property: true], author: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(User)]]]

        desc = descs.find { it.mappedClass == Comment }
        assert desc.properties == [comment: [property: true], summary: [property: [boost: 1.5f]]]

        desc = descs.find { it.mappedClass == User }
        assert desc.properties == [version: [property: true], createdAt: [property: true], username: [property: [index: 'un_tokenized']], posts: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(Post)]]]

        // Mix of "searchable" as Closure, boolean, Map
        searchableMap = [
            (postDc): {
                // all properties
                version(index: 'un_tokenized', excludeFromAll: true) // assumed to be property
                title(boost: 2.0f, analyzer: "myAnalyzer") // assumed to be property
                post(termVector: 'yes') // property
                comments(reference: true, component: true) // reference & component defaults
            },
            (commentDc): [only: ["comment", "summary"]],
            (userDc): true
        ]
        descs = provider.getCompassMappingDescriptions(searchableMap)
        assert descs.size() == 3

        desc = descs.find { it.mappedClass == Post }
        assert desc.properties == [version: [property: [index: 'un_tokenized', excludeFromAll: true]], title: [property: [boost: 2.0f, analyzer: 'myAnalyzer']], post: [property: [termVector: 'yes']], comments: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(Comment)], component: [refAlias: SearchableCompassUtils.getDefaultAlias(Comment)]], createdAt: [property: true], author: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(User)]]]

        desc = descs.find { it.mappedClass == Comment }
        assert desc.properties == [comment: [property: true], summary: [property: true]]

        desc = descs.find { it.mappedClass == User }
        assert desc.properties == [version: [property: true], createdAt: [property: true], username: [property: true], email: [property: true],posts: [reference: [refAlias: SearchableCompassUtils.getDefaultAlias(Post)]]]
    }
}