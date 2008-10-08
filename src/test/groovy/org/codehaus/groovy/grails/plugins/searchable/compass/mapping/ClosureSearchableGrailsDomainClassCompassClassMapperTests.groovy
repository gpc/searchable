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
import org.compass.core.engine.subindex.ConstantSubIndexHash

/**
*
*
* @author Maurice Nicholson
*/
// todo extract functional testing to dedicated functional tests
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

    void testGetCompassClassMappingSpotsInvalidOptions() {
        shouldFail(IllegalArgumentException) {
            getClassMapping(Comment, [Comment, User, Post], {
                only = ["summary", "comment"]  // only and except not allowed together
                except = ["createdAt"]
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Comment, [Comment, User, Post], {
                notAProperty(rubbish: true) // not a domain class property
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
                comments(nonsense: 'abc') // invalid option
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                comments(reference: [nonsense: 'abc']) // invalid option
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                comments(component: [unsupportedOption: 'xyz']) // invalid option
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                comments(reference: [nonsense: 'abc'], component: [unsupportedOption: 'xyz']) // invalid options
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                only = "author"
                author(component: [cascade: 'all', accessor: 'field'], referencet: true) // typo "referencet"
            })
        }

        shouldFail(IllegalArgumentException) {
            getClassMapping(Post, [Comment, User, Post], {
                author(component: true, reference: true) // reference + component disallowed
            })
        }
    }
    
    void testGetCompassClassMappingWithEmptyClosure() {
        // Empty closure; same as "searchable = true"
        def mapping = getClassMapping(User, [Comment, User, Post], { }, ["password"])
        assert mapping.mappedClass == User
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 6, mapping.propertyMappings.size()
        assert mapping.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'username', 'email', 'createdAt'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.find { it.propertyName == 'posts' }.every { it.reference && it.propertyType == Post }
    }

    void testGetCompassClassMappingWithOnly() {
        // with "only", otherwise defaults
        def mapping = getClassMapping(Comment, [Comment, User, Post], {
            only = ["summary", "comment"]
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 3
        assert mapping.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert mapping.propertyMappings.findAll { it.propertyName in ['summary', 'comment'] }.every { it.property && it.attributes.size() == 0 }
    }

    void testGetCompassClassMappingWithExcept() {
        // with "except", otherwise defaults
        def mapping = getClassMapping(Post, [Comment, User, Post], {
            except = "createdAt"
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 6
        assert mapping.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert mapping.propertyMappings.findAll { it.propertyName in ['title', 'post', 'version'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.find { it.propertyName == 'comments' }.every { it.reference && it.propertyType == Comment }
        assert mapping.propertyMappings.find { it.propertyName == 'author' }.every { it.reference && it.propertyType == User }
    }

    void testGetCompassClassMappingForSearchableId() {
        def cls = new GroovyClassLoader().parseClass("""
        class IdMapping {
            Object id
            Long version
            String value
        }
        """)

        def cm = getClassMapping(cls, [cls], {
            id converter: "my_id_converter", accessor: "property"
        })
        assert cm.propertyMappings.size() == 3
        assert cm.propertyMappings.find { it.propertyName == "id" && it.id && it.attributes.converter == 'my_id_converter' }

        cm = getClassMapping(cls, [cls], {
            id name: "the_id"
        })
        assert cm.propertyMappings.size() == 3
        assert cm.propertyMappings.find { it.propertyName == "id" && it.id && it.attributes.name == 'the_id' }
    }

    void testGetCompassClassMappingForSearchableProperty() {
        // searchable property options
        def mapping = getClassMapping(Comment, [Comment, User, Post], {
            comment(index: 'tokenized', termVector: 'yes', boost: 2.0f)
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [index: 'tokenized', termVector: 'yes', boost: 2.0f] }

        // same as above but with BigDecimal instead of float
        mapping = getClassMapping(Comment, [Comment, User, Post], {
            comment(index: 'tokenized', termVector: 'yes', boost: 2.0) // <!-- BigDecimal
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [index: 'tokenized', termVector: 'yes', boost: 2.0] }

        // converter and propertyConverter are equivalent
        mapping = getClassMapping(Comment, [Comment, User, Post], {
            comment(converter: 'customConverter')
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [converter: 'customConverter'] }

        mapping = getClassMapping(Comment, [Comment, User, Post], {
            comment(propertyConverter: 'customConverter')
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [converter: 'customConverter'] }

        mapping = getClassMapping(Comment, [Comment, User, Post], {
            comment(nullValue: 'nullnullnull')
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [nullValue: 'nullnullnull'] }

        mapping = getClassMapping(Comment, [Comment, User, Post], {
            comment(spellCheck: 'include')
        })
        assert mapping.mappedClass == Comment
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'comment' }.every { it.property && it.attributes == [spellCheck: 'include'] }
    }

    void testGetCompassClassMappingForSearchableReference() {
        // searchable reference options

        // todo converter, accessor ...

        // searchable reference on a collection
        def mapping = getClassMapping(Post, [Comment, User, Post], {
            comments reference: true
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'comments' }.every { it.reference && it.propertyType == Comment }

        // searchable component + reference defaults true, true
        mapping = getClassMapping(Post, [Comment, User, Post], {
            author(reference: true)
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'author' }.every { it.reference && it.propertyType == User }

        // Defined specific properties for reference
        mapping = getClassMapping(Post, [Comment, User, Post], {
            author(reference: [cascade: 'all', accessor: 'field'])
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        def pm = mapping.propertyMappings[0]
        assert pm.propertyName == 'author'
        assert pm.reference
        assert pm.propertyType == User
        assert pm.attributes == [cascade: 'all', accessor: 'field']
    }

    void testGetCompassClassMappingForSearchableComponent() {
        // searchable component on a collection
        def mapping = getClassMapping(Post, [Comment, User, Post], {
            comments component: true
        })
        assert mapping.mappedClass == Post
        assert mapping.root == true
        assert mapping.propertyMappings.find { it.propertyName == 'comments' }.every { it.component && it.propertyType == Comment }
    }

    void testgetClassMappingForImplicitSearchableComponent() {
        // Invalid options
        shouldFail(IllegalArgumentException) {
            getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
                searchableComp(noSuchComponentOption: true)
            })
        }

        // todo not sure this is an error any more
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
        def mapping = getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], { })
        assert mapping.mappedClass == ComponentOwner
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 5
        assert mapping.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'componentOwnerName'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.findAll { it.propertyName in ['searchableCompOne', 'searchableCompTwo'] }.every { it.component && it.propertyType == SearchableComp }

        // ...other side of the relationship
        mapping = getClassMapping(SearchableComp, [ComponentOwner, SearchableComp], { })
        assert mapping.mappedClass == SearchableComp
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 3
        assert mapping.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'searchableCompName'] }.every { it.property && it.attributes.size() == 0 }

        // define options for implicit searchable component
        mapping = getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
            searchableCompOne(maxDepth: 1, cascade: 'create,delete')
        })
        assert mapping.mappedClass == ComponentOwner
        assert mapping.root == true
        assert mapping.propertyMappings.size() == 5
        assert mapping.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'componentOwnerName'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.find { it.propertyName == 'searchableCompOne' }.every { it.component && it.propertyType == SearchableComp && it.attributes == [maxDepth: 1, cascade: 'create,delete'] }
        assert mapping.propertyMappings.find { it.propertyName == 'searchableCompTwo' }.every { it.component && it.propertyType == SearchableComp }
//        assert mapping.properties == [version: [property: true], componentOwnerName: [property: true], searchableCompOne: [component: ], searchableCompTwo: [component: [refAlias: CompassMappingUtils.getDefaultAlias(SearchableComp)]]]

        // convert an implicit component into a reference
        mapping = getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
            searchableCompOne(reference: true) // implies component: false
        })
        assert mapping.propertyMappings.size() == 5
        assert mapping.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'componentOwnerName'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.find { it.propertyName == 'searchableCompOne' }.every { it.reference && it.propertyType == SearchableComp && it.attributes == [:] }
        assert mapping.propertyMappings.find { it.propertyName == 'searchableCompTwo' }.every { it.component && it.propertyType == SearchableComp }

        mapping = getClassMapping(ComponentOwner, [ComponentOwner, SearchableComp], {
            searchableCompOne(reference: [cascade: 'true']) // implies component: false
        })
        assert mapping.propertyMappings.size() == 5
        assert mapping.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert mapping.propertyMappings.findAll { it.propertyName in ['version', 'componentOwnerName'] }.every { it.property && it.attributes.size() == 0 }
        assert mapping.propertyMappings.find { it.propertyName == 'searchableCompOne' }.every { it.reference && it.propertyType == SearchableComp && it.attributes == [cascade: 'true'] }
        assert mapping.propertyMappings.find { it.propertyName == 'searchableCompTwo' }.every { it.component && it.propertyType == SearchableComp }
    }

    void testGetClassMappingForClassMappingAttributes() {
        // todo convert only/except to method-setting style

        def cm

        // alias
        cm = getClassMapping(User, [Comment, User, Post], {
            alias "my_user_alias"
        })
        assert cm.mappedClass == User
        assert cm.alias == "my_user_alias"
        assert cm.root == true

        // sub-index
        cm = getClassMapping(User, [Comment, User, Post], {
            subIndex "my_specific_sub_index"
        })
        assert cm.mappedClass == User
        assert cm.subIndex == "my_specific_sub_index"
        assert cm.root == true

        // constant
        cm = getClassMapping(User, [Comment, User, Post], {
            constant name: "drink", value: "beer"
            constant name: "eat", values: ["pie", "chips"], index: 'un_tokenized', excludeFromAll: true
        })
        assert cm.mappedClass == User
        assert cm.root == true
        assert cm.constantMetaData.size() == 2
        def cmd = cm.constantMetaData.find {it.name == "drink"}
        assert cmd.values == ["beer"] && cmd.attributes == [:]
        cmd = cm.constantMetaData.find { it.name == "eat" }
        assert cmd.values == ["pie", "chips"] && cmd.attributes == [index: 'un_tokenized', excludeFromAll: true]

        // analyzer
        cm = getClassMapping(User, [Comment, User, Post], {
            analyzer "funkyanalyzer"
        })
        assert cm.mappedClass == User
        assert cm.analyzer == "funkyanalyzer"
        assert cm.root == true

        // boost
        cm = getClassMapping(User, [Comment, User, Post], {
            boost 5.0
        })
        assert cm.mappedClass == User
        assert cm.boost == 5.0
        assert cm.root == true

        // converter
        cm = getClassMapping(User, [Comment, User, Post], {
            converter "myclassconverter"
        })
        assert cm.mappedClass == User
        assert cm.converter == "myclassconverter"
        assert cm.root == true

        // enableAll
        cm = getClassMapping(User, [Comment, User, Post], {
            enableAll true
        })
        assert cm.mappedClass == User
        assert cm.enableAll == true
        assert cm.root == true

        cm = getClassMapping(User, [Comment, User, Post], {
            enableAll false
        })
        assert cm.mappedClass == User
        assert cm.enableAll == false
        assert cm.root == true

        // managedId
        cm = getClassMapping(User, [Comment, User, Post], {
            managedId "false"
        })
        assert cm.mappedClass == User
        assert cm.managedId == "false"
        assert cm.root == true

        // root
        cm = getClassMapping(User, [Comment, User, Post], {
            root true
        })
        assert cm.mappedClass == User
        assert cm.root == true

        cm = getClassMapping(User, [Comment, User, Post], {
            root false
        })
        assert cm.mappedClass == User
        assert cm.root == false

        // supportUnmarshall
        cm = getClassMapping(User, [Comment, User, Post], {
            supportUnmarshall "false"
        })
        assert cm.mappedClass == User
        assert cm.supportUnmarshall == false, cm.supportUnmarshall

        // supportUnmarshall (boolean)
        cm = getClassMapping(User, [Comment, User, Post], {
            supportUnmarshall true
        })
        assert cm.mappedClass == User
        assert cm.supportUnmarshall == true

        // supportUnmarshall (boolean)
        cm = getClassMapping(User, [Comment, User, Post], {
            supportUnmarshall false
        })
        assert cm.mappedClass == User
        assert cm.supportUnmarshall == false
    }

    void testGetClassMappingForSearchableAllPropertyOptions() {
        // all metadata options
        def cm = getClassMapping(User, [Comment, User, Post], {
            all true // all and enableAll are interchangeable
        })
        assert cm.mappedClass == User
        assert cm.enableAll == true
        assert !cm.allName
        assert !cm.allAnalyzer
        assert !cm.allTermVector

        // using mapping
        cm = getClassMapping(User, [Comment, User, Post], {
            mapping all: true
        })
        assert cm.mappedClass == User
        assert cm.enableAll == true
        assert !cm.allName
        assert !cm.allAnalyzer
        assert !cm.allTermVector

        // all metadata options
        cm = getClassMapping(User, [Comment, User, Post], {
            all true // all and enableAll are interchangeable
            allName "theallproperty"
            allAnalyzer "theallanalyzer"
            allTermVector "offsets"
        })
        assert cm.mappedClass == User
        assert cm.enableAll == true
        assert cm.allName == "theallproperty"
        assert cm.allAnalyzer == "theallanalyzer"
        assert cm.allTermVector == "offsets"

        // using mapping
        cm = getClassMapping(User, [Comment, User, Post], {
            mapping all: true, allName: "myall", allAnalyzer: "allanalyzer", allTermVector: "positions_offsets"
        })
        assert cm.mappedClass == User
        assert cm.enableAll == true
        assert cm.allName == "myall"
        assert cm.allAnalyzer == "allanalyzer"
        assert cm.allTermVector == "positions_offsets"

        // all
        cm = getClassMapping(User, [Comment, User, Post], {
            all name: "all", analyzer: "specificanalyzer", termVector: "offsets"
        })
        assert cm.mappedClass == User
//        assert cm.enableAll == true
        assert cm.allName == "all"
        assert cm.allAnalyzer == "specificanalyzer"
        assert cm.allTermVector == "offsets"

        // using mapping
        cm = getClassMapping(User, [Comment, User, Post], {
            mapping all: [name: "xyz", analyzer: "coolanalyzer", termVector: "positions_offsets"]
        })
        assert cm.mappedClass == User
//        assert cm.enableAll == true
        assert cm.allName == "xyz"
        assert cm.allAnalyzer == "coolanalyzer"
        assert cm.allTermVector == "positions_offsets"

        // disabling all
        cm = getClassMapping(User, [Comment, User, Post], {
            all false
        })
        assert cm.mappedClass == User
        assert cm.enableAll == false
        assert !cm.allName
        assert !cm.allAnalyzer
        assert !cm.allTermVector

        // using mapping
        cm = getClassMapping(User, [Comment, User, Post], {
            mapping all: false
        })
        assert cm.mappedClass == User
        assert cm.enableAll == false
        assert !cm.allName
        assert !cm.allAnalyzer
        assert !cm.allTermVector
    }

    void testGetClassMappingAllowsReservedWordsUsingMappingOption() {
        // demonstrates that properties by the name of certain "reserved" words can be used as searchable
        // properties as long as a mapping section is provided
        def cls = new GroovyClassLoader().parseClass("""
        class ThisClassHasCompassClassMappingOptionsAsProperties {
            Long id
            Long version
            String alias
            String subIndex
            String constant
        }
        """)

        def cm = getClassMapping(cls, [cls], {
            mapping alias: "rocky", subIndex: "tomahawk"
            alias store: 'compress'
            subIndex store: 'compress'
            constant store: 'compress'
        })
        assert cm.alias == "rocky"
        assert cm.subIndex == "tomahawk"
        assert cm.propertyMappings.size() == 5
        assert cm.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert cm.propertyMappings.find { it.propertyName == "alias" && it.attributes.store == 'compress'}
        assert cm.propertyMappings.find { it.propertyName == "subIndex" && it.attributes.store == 'compress' }
        assert cm.propertyMappings.find { it.propertyName == "constant" && it.attributes.store == 'compress' }

        // define options using "mapping" option
        cm = getClassMapping(cls, [cls], {
            mapping {
                alias "super"
                subIndex "duper"
                constant name: "wild", value: "bill"
            }
            alias store: 'compress'
            subIndex store: 'compress'
            constant store: 'compress'
        })
        assert cm.alias == "super"
        assert cm.subIndex == "duper"
        assert cm.constantMetaData.size() == 1
        assert cm.constantMetaData.find { it.name == "wild" }.values == ["bill"]
        assert cm.propertyMappings.size() == 5
        assert cm.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert cm.propertyMappings.find { it.propertyName == "alias" && it.attributes.store == 'compress'}
        assert cm.propertyMappings.find { it.propertyName == "subIndex" && it.attributes.store == 'compress' }
        assert cm.propertyMappings.find { it.propertyName == "constant" && it.attributes.store == 'compress' }

        // define options using "mapping" option with mixture of Map and Closure 
        cm = getClassMapping(cls, [cls], {
            mapping alias: "rocky", subIndex: "tomahawk", {
                constant name: "material", value: "wood"
                constant name: "finish", values: ["polished", "laquered"]
            }
            alias store: 'compress'
            subIndex store: 'compress'
            constant store: 'compress'
        })
        assert cm.alias == "rocky"
        assert cm.subIndex == "tomahawk"
        assert cm.constantMetaData.size() == 2
        assert cm.constantMetaData.find { it.name == "material" }.values == ["wood"]
        assert cm.constantMetaData.find { it.name == "finish" }.values == ["polished", "laquered"]
        assert cm.propertyMappings.size() == 5
        assert cm.propertyMappings.find { it.propertyName == 'id' }.every { it.id }
        assert cm.propertyMappings.find { it.propertyName == "alias" && it.attributes.store == 'compress'}
        assert cm.propertyMappings.find { it.propertyName == "subIndex" && it.attributes.store == 'compress' }
        assert cm.propertyMappings.find { it.propertyName == "constant" && it.attributes.store == 'compress' }
    }

    void testGetClassMappingWithSubIndexHash() {
        // must implement SubIndexHash
        shouldFail {
            getClassMapping(Post, [Post], {
                subIndexHash Object.class
            })
        }

        def cm = getClassMapping(Post, [Post], {
            subIndexHash ConstantSubIndexHash.class
        })
        assert cm.subIndexHash.type == ConstantSubIndexHash.class
        assert cm.subIndexHash.settings == null

        cm = getClassMapping(Post, [Post], {
            subIndexHash type: ConstantSubIndexHash.class
        })
        assert cm.subIndexHash.type == ConstantSubIndexHash.class
        assert cm.subIndexHash.settings == null

        cm = getClassMapping(Post, [Post], {
            subIndexHash ConstantSubIndexHash.class, settings: [foo: 'FOO', bar: 'BAR']
        })
        assert cm.subIndexHash.type == ConstantSubIndexHash.class
        assert cm.subIndexHash.settings == [foo: 'FOO', bar: 'BAR']

        cm = getClassMapping(Post, [Post], {
            subIndexHash type: ConstantSubIndexHash.class, settings: [foo: 'FOO', bar: 'BAR']
        })
        assert cm.subIndexHash.type == ConstantSubIndexHash.class
        assert cm.subIndexHash.settings == [foo: 'FOO', bar: 'BAR']

        // must implement SubIndexHash
        shouldFail {
            getClassMapping(Post, [Post], {
                mapping subIndexHash: Object.class
            })
        }

        cm = getClassMapping(Post, [Post], {
            mapping subIndexHash: ConstantSubIndexHash.class
        })
        assert cm.subIndexHash.type == ConstantSubIndexHash.class
        assert cm.subIndexHash.settings == null

        cm = getClassMapping(Post, [Post], {
            mapping subIndexHash: [type: ConstantSubIndexHash.class, settings: ['blah': 'cuckoo']]
        })
        assert cm.subIndexHash.type == ConstantSubIndexHash.class
        assert cm.subIndexHash.settings == [blah: 'cuckoo']
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
