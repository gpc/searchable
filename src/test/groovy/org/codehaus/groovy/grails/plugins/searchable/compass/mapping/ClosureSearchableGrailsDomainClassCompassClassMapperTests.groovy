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
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.*

/**
*
*
* @author Maurice Nicholson
*/
class ClosureSearchableGrailsDomainClassCompassClassMapperTests extends GroovyTestCase {
    def classMapper

    void setUp() {
        def parent = SearchableGrailsDomainClassCompassClassMapperFactory.getDefaultSearchableGrailsDomainClassCompassClassMapper([], [:])
        classMapper = parent.classMappers.find { it instanceof ClosureSearchableGrailsDomainClassCompassClassMapper }
//            domainClassPropertyMappingStrategyFactory: new SearchableGrailsDomainClassPropertyMappingFactory(
//                converterLookupHelper: new DefaultCompassConverterLookupHelper(converterLookup: new CompassConfiguration().setConnection("ram://dummy").buildCompass().converterLookup)
//            )
//        )
    }

    void tearDown() {
        classMapper = null
    }

    void testHandlesSearchableValue() {
        assert classMapper.handlesSearchableValue({ -> })
        assert classMapper.handlesSearchableValue(null) == false
        assert classMapper.handlesSearchableValue([:]) == false
        assert classMapper.handlesSearchableValue(true) == false
        assert classMapper.handlesSearchableValue(false) == false
    }

    void testGetCompassClassMapping() {
        shouldFail(IllegalArgumentException) {
            getClassMapping(Comment, [Comment, User, Post], {
                only = ["summary", "comment"]
                except = ["createdAt"]
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Comment, [Comment, User, Post], {
                notAProperty(rubbish: true)
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Comment, [Comment, User, Post], {
                comment(reference: true) // not a suitable reference type
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Comment, [Comment, User, Post], {
                createdAt(component: true) // not a suitable component type
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                abbreviatedTitle(cascade: 'create,delete') // derived property defined with "def", so unknown default searchable type
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                comments(nonsense: 'abc')
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                comments(reference: [nonsense: 'abc'])
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                comments(component: [unsupportedOption: 'xyz'])
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                comments(reference: [nonsense: 'abc'], component: [unsupportedOption: 'xyz'])
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                only = "author"
                author(component: [cascade: 'all', accessor: 'field'], referencet: true) // typo "referencet"
            })
        }

        // Empty closure; same as "searchable = true"
        def mapping = getClassMapping(User, [Comment, User, Post], { }, ["password"])
        assert mapping.mappedClass == User
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 5
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'username', 'email', 'createdAt'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.find { it.propertyName == 'posts' }.every { it.reference && it.propertyType == Post }

        // alias
        mapping = getClassMapping(User, [Comment, User, Post], {
            alias "my_user_alias"
        })
        assert mapping.mappedClass == User
        assert mapping.alias == "my_user_alias"
        assert mapping.root == true

        // constant
        mapping = getClassMapping(User, [Comment, User, Post], {
            constant drink: "beer"
            constant eat: ["pie", "chips"]
        })
        assert mapping.mappedClass == User
        assert mapping.root == true
        assert mapping.constantMetaData.size() == 2
        def constant = mapping.constantMetaData.find {it.name == "drink"}
        assert constant.values == ["beer"] && constant.attributes == [:]
        constant = mapping.constantMetaData.find { it.name == "eat" }
        assert constant.values == ["pie", "chips"] && constant.attributes == [:]

        // constant expressed another way
        mapping = getClassMapping(User, [Comment, User, Post], {
            constants drink: "wine", eat: ["cheese", "biscuits"]
        })
        assert mapping.mappedClass == User
        assert mapping.root == true
        assert mapping.constantMetaData.size() == 2
        constant = mapping.constantMetaData.find { it.name == "drink" }
        assert constant.values == ["wine"] && constant.attributes == [:]
        constant = mapping.constantMetaData.find { it.name == "eat" }
        assert constant.values == ["cheese", "biscuits"] && constant.attributes == [:]

        // todo convert only/except to method-setting style
        // todo move special properties into "mapping closure?

        /*
        searchable = {
            mapping {
                alias "myclass"
                except ["password", "secretQuestion"]
            }
            username(...)
            otherProperty(...)
        }

        need to look at GORM ORM DSL and also Compass stuff
        */

        // with "only", otherwise defaults
        mapping = getClassMapping(Comment, [Comment, User, Post], {
            only = ["summary", "comment"]
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 2
        assert mapping.propertyMappings.findAll { it.propertyName in ['summary', 'comment'] }.every { it.property && it.attributes.size() == 0 }

        // with "except", otherwise defaults
        mapping = getClassMapping(Post, [Comment, User, Post], {
            except = "createdAt"
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 5
        assert mapping.propertyMappings.findAll { it.propertyName in ['title', 'post', 'version'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.find { it.propertyName == 'comments' }.every { it.reference && it.propertyType == Comment }
        assert mapping.propertyMappings.find { it.propertyName == 'author' }.every { it.reference && it.propertyType == User }

        // searchable property options
        mapping = getClassMapping(Comment, [Comment, User, Post], {
            only = "comment"
            comment(index: 'tokenized', termVector: 'yes', boost: 2.0f)
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 1
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [index: 'tokenized', termVector: 'yes', boost: 2.0f] }

        // same as above but with BigDecimal instead of float
        mapping = getClassMapping(Comment, [Comment, User, Post], {
            only = "comment"
            comment(index: 'tokenized', termVector: 'yes', boost: 2.0) // <!-- BigDecimal
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 1
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [index: 'tokenized', termVector: 'yes', boost: 2.0] }

        // searchable reference options
        mapping = getClassMapping(Comment, [Comment, User, Post], {
            only = "comment"
            comment(index: 'tokenized', termVector: 'yes', boost: 2.0f)
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 1
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [index: 'tokenized', termVector: 'yes', boost: 2.0f] }

        // searchable component + reference defaults true, true
        mapping = getClassMapping(Post, [Comment, User, Post], {
            only = "author"
            author(reference: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 1
        assert mapping.propertyMappings.find { it.propertyName == 'author' }.every { it.reference && it.propertyType == User }

        mapping = getClassMapping(Post, [Comment, User, Post], {
            only = "author"
            author(reference: true, component: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 2
        assert mapping.propertyMappings.find { it.propertyName == 'author' && it.reference }.every { it.propertyType == User }
        assert mapping.propertyMappings.find { it.propertyName == 'author' && it.component }.every { it.propertyType == User }

        // Defined specific properties for reference
        mapping = getClassMapping(Post, [Comment, User, Post], {
            only = "author"
            author(reference: [cascade: 'all', accessor: 'field'])
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 1
        def pm = mapping.propertyMappings[0]
        assert pm.propertyName == 'author'
        assert pm.reference
        assert pm.propertyType == User
        assert pm.attributes == [cascade: 'all', accessor: 'field']

        // Defined specific properties for reference with default component
        mapping = getClassMapping(Post, [Comment, User, Post], {
            only = "author"
            author(reference: [cascade: 'all', accessor: 'field'], component: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 2
        assert mapping.propertyMappings.find { it.propertyName == 'author' && it.reference }.every { it.propertyType == User && it.attributes == [cascade: 'all', accessor: 'field'] }
        assert mapping.propertyMappings.find { it.propertyName == 'author' && it.component }.every { it.propertyType == User }

        // Defined specific properties for component with default reference
        mapping = getClassMapping(Post, [Comment, User, Post], {
            only = "author"
            author(component: [cascade: 'all', accessor: 'field'], reference: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 2
        assert mapping.propertyMappings.find { it.propertyName == 'author' && it.component }.every { it.propertyType == User && it.attributes == [cascade: 'all', accessor: 'field'] }
        assert mapping.propertyMappings.find { it.propertyName == 'author' && it.reference }.every { it.propertyType == User }

        // Components
        shouldFail(IllegalArgumentException) {
            getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                searchableComp(noSuchComponentOption: true)
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                searchableComp(component: true) // components are implicit
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                searchableComp(reference: true) // components are not allowed to be references
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                comp(maxDepth: 1) // not searchable
            })
        }

        // with empty closure (same as true)
        mapping = getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], { })
        assert mapping.mappedClass == ComponentOwner
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 4
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'componentOwnerName'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.findAll { it.propertyName in ['searchableCompOne', 'searchableCompTwo'] }.every { it.component && it.propertyType == SearchableComp }

        // ...other side of the relationship
        mapping = getClassMapping(SearchableComp, [ComponentOwner, SearchableComp], { })
        assert mapping.mappedClass == SearchableComp
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 2
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'searchableCompName'] }.every { it.property && it.attributes.size() == 0 }

        // define options for implicit searchable component
        mapping = getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
            searchableCompOne(maxDepth: 1, cascade: 'create,delete')
        })
        assert mapping.mappedClass == ComponentOwner
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 4
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'componentOwnerName'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.find { it.propertyName == 'searchableCompOne' }.every { it.component && it.propertyType == SearchableComp && it.attributes == [maxDepth: 1, cascade: 'create,delete'] }
        assert mapping.propertyMappings.find { it.propertyName == 'searchableCompTwo' }.every { it.component && it.propertyType == SearchableComp }
//        assert mapping.properties == [version: [property: true], componentOwnerName: [property: true], searchableCompOne: [component: ], searchableCompTwo: [component: [refAlias: CompassMappingUtils.getDefaultAlias(SearchableComp)]]]
    }

    void testGetClassMappingWithInheritedMappings() {
        def sv = [:]
        [Parent, SearchableChildOne, SearchableChildTwo].each { c ->
            sv[c] = c.searchable
        }

        try {
            CompassClassMapping mapping

            Parent.searchable = {
                commonProperty(boost: 1.5) // common definition unless overriden by sub-class
            }
            mapping = getClassMapping(Parent, [Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild])
            assert mapping.getPropertyMappings().find { it.propertyName == 'commonProperty' }.every { it.property && it.attributes == [boost: 1.5] }

            mapping = getClassMapping(SearchableChildOne, [Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild])
            assert mapping.getPropertyMappings().find { it.propertyName == 'commonProperty' }.every { it.property && it.attributes == [boost: 1.5] }
            assert mapping.getPropertyMappings().find { it.propertyName == 'childOneProperty' }.every { it.property && it.attributes.size() == 0 }

            // SearchableChildTwo inherits Parent's "commonProperty(boost: 1.5)"
            SearchableChildTwo.searchable = {
                childTwoProperty(index: 'un_tokenized')
            }
            mapping = getClassMapping(SearchableChildTwo, [Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild])
            assert mapping.getPropertyMappings().find { it.propertyName == 'commonProperty' }.every { it.property && it.attributes == [boost: 1.5] }
            assert mapping.getPropertyMappings().find { it.propertyName == 'childTwoProperty' }.every { it.property && it.attributes == [index: 'un_tokenized'] }

            // SearchbaleChildTwo overrides parent def, other properties are mapped with defaults
            SearchableChildTwo.searchable = {
                commonProperty() // overrides super-class definition; uses default searchable property mapping
            }
            mapping = getClassMapping(SearchableChildTwo, [Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild])
            assert mapping.getPropertyMappings().find { it.propertyName == 'childTwoProperty' }.every { it.property && it.attributes.size() == 0 }
            assert mapping.getPropertyMappings().find { it.propertyName == 'commonProperty' }.every { it.property && it.attributes.size() == 0 }
        } finally {
            sv.each { c, v ->
                c.searchable = v
            }
        }
    }

    def getClassMapping(clazz, searchableClasses, searchableValue) {
        getClassMapping(clazz, searchableClasses, searchableValue, [])
    }

    CompassClassMapping getClassMapping(clazz, searchableClazzes, searchableValue, excludedProperties = []) {
        def domainClasses = getDomainClasses(searchableClazzes)
        def gdc = domainClasses.find { it.clazz == clazz}
        classMapper.getCompassClassMapping(gdc, domainClasses, searchableValue, excludedProperties)
    }

    CompassClassMapping getClassMapping(clazz, searchableClazzes) {
        def domainClasses = getDomainClasses(searchableClazzes)
        def gdc = domainClasses.find { it.clazz == clazz}
        classMapper.getCompassClassMapping(gdc, domainClasses)
    }

    def getDomainClasses(clazzes) {
        def domainClasses = []
        for (clazz in clazzes) {
            def DefaultGrailsDomainClass domainClass = new DefaultGrailsDomainClass(clazz)
            domainClasses << domainClass
        }
        configureDomainClassRelationships(domainClasses)
        domainClasses
    }

    def configureDomainClassRelationships(domainClasses) {
        def domainClassMap = getDomainClassMap(domainClasses)
        GrailsDomainConfigurationUtil.configureDomainClassRelationships(domainClasses as GrailsClass[], domainClassMap)
    }

    def getDomainClassMap(domainClasses) {
        def domainClassMap = [:]
        for (dc in domainClasses) {
            domainClassMap[dc.clazz.name] = dc
        }
        domainClassMap
    }
}