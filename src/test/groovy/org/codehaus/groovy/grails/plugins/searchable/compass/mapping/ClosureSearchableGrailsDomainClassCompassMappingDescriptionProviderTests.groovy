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
import org.codehaus.groovy.grails.plugins.searchable.test.domain.component.*
import org.codehaus.groovy.grails.plugins.searchable.compass.converter.DefaultCompassConverterLookupHelper

/**
*
*
* @author Maurice Nicholson
*/
class ClosureSearchableGrailsDomainClassCompassMappingDescriptionProviderTests extends GroovyTestCase {
    def provider
    def domainClassMap
//    def currentSearchableValues

    void setUp() {
//        currentSearchableValues = [(Post): Post.searchable, (Comment): Comment.searchable, (User): User.searchable]
        domainClassMap = [:]
        for (clazz in [Post, Comment, User, Comp, SearchableComp, ComponentOwner]) {
            domainClassMap[clazz] = new DefaultGrailsDomainClass(clazz)
        }
        provider = new ClosureSearchableGrailsDomainClassCompassMappingDescriptionProvider(
            domainClassPropertyMappingStrategyFactory: new GrailsDomainClassPropertyMappingStrategyFactory(
                converterLookupHelper: new DefaultCompassConverterLookupHelper(converterLookup: new CompassConfiguration().setConnection("ram://dummy").buildCompass().converterLookup)
            )
        )
    }

    void tearDown() {
//        currentSearchableValues.each { c, v -> c.searchable = v }
//        currentSearchableValues = null
        domainClassMap = null
        provider = null
    }

    void testHandlesSearchableValue() {
        assert provider.handlesSearchableValue({ -> })
        assert provider.handlesSearchableValue(null) == false
        assert provider.handlesSearchableValue([:]) == false
        assert provider.handlesSearchableValue(true) == false
        assert provider.handlesSearchableValue(false) == false
    }

    void testGetCompassMappingDescription() {
        shouldFail(IllegalArgumentException) {
            getMapping(Comment, [Comment, User, Post], {
                only = ["summary", "comment"]
                except = ["createdAt"]
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Comment, [Comment, User, Post], {
                notAProperty(rubbish: true)
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Comment, [Comment, User, Post], {
                comment(reference: true) // not a suitable reference type
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Comment, [Comment, User, Post], {
                createdAt(component: true) // not a suitable component type
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Post, [Comment, User, Post], {
                abbreviatedTitle(cascade: 'create,delete') // derived property defined with "def", so unknown default searchable type
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Post, [Comment, User, Post], {
                comments(nonsense: 'abc')
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Post, [Comment, User, Post], {
                comments(reference: [nonsense: 'abc'])
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Post, [Comment, User, Post], {
                comments(component: [unsupportedOption: 'xyz'])
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Post, [Comment, User, Post], {
                comments(reference: [nonsense: 'abc'], component: [unsupportedOption: 'xyz'])
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(Post, [Comment, User, Post], {
                only = "author"
                author(component: [cascade: 'all', accessor: 'field'], referencet: true) // typo "referencet"
            })
        }

        // Empty closure; same as "searchable = true"
        def mapping = getMapping(User, [Comment, User, Post], { }, ["password"])
        assert mapping.mappedClass == User
        assert mapping.root == true
        assert mapping.properties == [version: [property: true], username: [property: true], email: [property: true], createdAt: [property: true], posts: [reference: [refAlias: 'Post']]]

        // with "only", otherwise defaults
        mapping = getMapping(Comment, [Comment, User, Post], {
            only = ["summary", "comment"]
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.properties == [summary: [property: true], comment: [property: true]]

        // with "except", otherwise defaults
        mapping = getMapping(Post, [Comment, User, Post], {
            except = "createdAt"
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.properties == [title: [property: true], post: [property: true], version: [property: true], comments: [reference: [refAlias: 'Comment']], author: [reference: [refAlias: 'User']]]

        // searchable property options
        mapping = getMapping(Comment, [Comment, User, Post], {
            only = "comment"
            comment(index: 'tokenized', termVector: 'yes', boost: 2.0f)
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.properties == [comment: [property: [index: 'tokenized', termVector: 'yes', boost: 2.0f]]]

        // searchable reference options
        mapping = getMapping(Comment, [Comment, User, Post], {
            only = "comment"
            comment(index: 'tokenized', termVector: 'yes', boost: 2.0f)
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.properties == [comment: [property: [index: 'tokenized', termVector: 'yes', boost: 2.0f]]]

        // searchable component + reference defaults true, true
        mapping = getMapping(Post, [Comment, User, Post], {
            only = "author"
            author(reference: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.properties == [author: [reference: [refAlias: 'User']]]

        mapping = getMapping(Post, [Comment, User, Post], {
            only = "author"
            author(reference: true, component: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.properties == [author: [reference: [refAlias: 'User'], component: [refAlias: 'User']]]

        // Defined specific properties for reference
        mapping = getMapping(Post, [Comment, User, Post], {
            only = "author"
            author(reference: [cascade: 'all', accessor: 'field'])
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.properties == [author: [reference: [cascade: 'all', accessor: 'field', refAlias: 'User']]]

        // Defined specific properties for reference with default component
        mapping = getMapping(Post, [Comment, User, Post], {
            only = "author"
            author(reference: [cascade: 'all', accessor: 'field'], component: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.properties == [author: [reference: [cascade: 'all', accessor: 'field', refAlias: 'User'], component: [refAlias: 'User']]]

        // Defined specific properties for component with default reference
        mapping = getMapping(Post, [Comment, User, Post], {
            only = "author"
            author(component: [cascade: 'all', accessor: 'field'], reference: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.properties == [author: [component: [cascade: 'all', accessor: 'field', refAlias: 'User'], reference: [refAlias: 'User']]]

        // Components
        shouldFail(IllegalArgumentException) {
            getMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                searchableComp(noSuchComponentOption: true)
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                searchableComp(component: true) // components are implicit
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                searchableComp(reference: true) // components are not allowed to be references
            })
        }

        shouldFail(IllegalArgumentException) {
            getMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                comp(maxDepth: 1) // not searchable
            })
        }

        // with empty closure (same as true)
        mapping = getMapping(ComponentOwner, [ComponentOwner, SearchableComp], { })
        assert mapping.mappedClass == ComponentOwner
        assert mapping.root == true
        assert mapping.properties == [version: [property: true], componentOwnerName: [property: true], searchableCompOne: [component: [refAlias: 'SearchableComp']], searchableCompTwo: [component: [refAlias: 'SearchableComp']]]

        // ...other side of the relationship
        mapping = getMapping(SearchableComp, [ComponentOwner, SearchableComp], { })
        assert mapping.mappedClass == SearchableComp
        assert mapping.root == false
        assert mapping.properties == [version: [property: true], searchableCompName: [property: true]]

        // define options for implicit searchable component
        mapping = getMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
            searchableCompOne(maxDepth: 1, cascade: 'create,delete')
        })
        assert mapping.mappedClass == ComponentOwner
        assert mapping.root == true
        assert mapping.properties == [version: [property: true], componentOwnerName: [property: true], searchableCompOne: [component: [refAlias: 'SearchableComp', maxDepth: 1, cascade: 'create,delete']], searchableCompTwo: [component: [refAlias: 'SearchableComp']]]
    }

    def getMapping(clazz, searchableClasses, searchableValue, excludedProperties = []) {
        provider.getCompassMappingDescription(domainClassMap[clazz], searchableClasses.collect { domainClassMap[it] }, searchableValue, excludedProperties)
    }
}