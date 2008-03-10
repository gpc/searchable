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

import org.compass.core.mapping.osem.ClassMapping
import org.compass.core.mapping.CompassMapping
import org.compass.core.spi.InternalCompass
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance.*
import org.codehaus.groovy.grails.commons.*

/**
* @author Maurice Nicholson
*/
class CompassMappingUtilsTests extends GroovyTestCase {

    void testGetDefaultAlias() {
        assert CompassMappingUtils.getDefaultAlias(Post) == 'ALIASPostALIAS'
        assert CompassMappingUtils.getDefaultAlias(User) == 'ALIASUserALIAS'
        assert CompassMappingUtils.getDefaultAlias(Comment) == 'ALIASCommentALIAS'
    }

    void testGetMappingAlias() {
        def mapping = new CompassMapping()
        mapping.addMapping(getClassMapping(Post.class))
        def compass = [
            getMapping: {
                mapping
            }
        ] as InternalCompass
        assert CompassMappingUtils.getMappingAlias(compass, Post) == "Postalias"
    }

    //    public static String[] getMappingAliases(Compass compass, Collection clazzes) {
    void testGetMappingAliases() {
        def mapping = new CompassMapping()
        mapping.addMapping(getClassMapping(Post.class))
        mapping.addMapping(getClassMapping(User.class))
        mapping.addMapping(getClassMapping(Comment.class))
        def compass = [
            getMapping: {
                mapping
            }
        ] as InternalCompass
        assert CompassMappingUtils.getMappingAliases(compass, [Post]) == ["Postalias"]
        assert (CompassMappingUtils.getMappingAliases(compass, [Post, User, Comment]) as List).sort() == ["Commentalias", "Postalias", "Useralias"]
    }

//    public static void resolveAliases(List classMappings) {
    void testResolveAliases() {
        // no poly classes
        def classMappings = []
        def pm = new CompassClassMapping(mappedClass: Post, alias: "what_i_call_posts")
        pm.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance("comments", Comment))
        pm.addPropertyMapping(CompassClassPropertyMapping.getComponentInstance("author", User))
        classMappings << pm

        def cm = new CompassClassMapping(mappedClass: Comment) // alias: default
        cm.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance("post", Post))
        classMappings << cm

        def um  = new CompassClassMapping(mappedClass: User, alias: "users")
        um.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance("posts", Post))
        classMappings << um

        CompassMappingUtils.resolveAliases(classMappings, getDomainClasses(Post, Comment, User))

        assert cm.alias != null
        assert pm.getPropertyMappings().find { it.propertyName == "comments" }.attributes.refAlias == cm.alias
        assert pm.getPropertyMappings().find { it.propertyName == "author" }.attributes.refAlias == "users"
        assert cm.getPropertyMappings().find { it.propertyName == "post" }.attributes.refAlias == "what_i_call_posts"
        assert um.getPropertyMappings().find { it.propertyName == "posts" }.attributes.refAlias == "what_i_call_posts"

        // poly classes
        classMappings = []
        cm = new CompassClassMapping(mappedClass: Associate, alias: "assoc")
        cm.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance("polyInstance", Parent))
        cm.addPropertyMapping(CompassClassPropertyMapping.getReferenceInstance("specificInstance", SearchableChildOne))
        classMappings << cm

        classMappings << new CompassClassMapping(mappedClass: Parent, alias: "parent")
        classMappings << new CompassClassMapping(mappedClass: SearchableChildOne, alias: "sco", mappedClassSuperClass: Parent)
        classMappings << new CompassClassMapping(mappedClass: SearchableChildTwo, alias: "sct", mappedClassSuperClass: Parent)
        classMappings << new CompassClassMapping(mappedClass: SearchableGrandChild, alias: "sgc", mappedClassSuperClass: SearchableChildOne)

        // note the collection of domain classes contains one that is not mapped (because it is not searchable)
        CompassMappingUtils.resolveAliases(classMappings, getDomainClasses(Associate, Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild, NonSearchableChild))

        def aliases = cm.getPropertyMappings().find {it.propertyName == "polyInstance"}.attributes.refAlias.split(", ") as List
        assert aliases.size() == 4
        assert aliases.containsAll(["parent", "sco", "sct", "sgc"])
        aliases = cm.getPropertyMappings().find {it.propertyName == "specificInstance"}.attributes.refAlias.split(", ") as List
        assert aliases.size() == 2
        assert aliases.containsAll(["sco", "sgc"])
        assert classMappings.find { it.mappedClass == Parent }.extend == null
        assert classMappings.find { it.mappedClass == SearchableChildOne }.extend == "parent"
        assert classMappings.find { it.mappedClass == SearchableChildTwo }.extend == "parent" 
        assert classMappings.find { it.mappedClass == SearchableGrandChild }.extend == "sco"

        // user-defined aliases are not inherited; new ones are provided if required
        // note that this test simulates the case where a user has defined a custom alias in a parent
        // clas but not done anything specifically in a child class
        classMappings = []
        classMappings << new CompassClassMapping(mappedClass: Parent, alias: "parent")
        classMappings << new CompassClassMapping(mappedClass: SearchableChildOne, mappedClassSuperClass: Parent, alias: "parent")
        classMappings << new CompassClassMapping(mappedClass: SearchableChildTwo, mappedClassSuperClass: Parent, alias: "sct")
        classMappings << new CompassClassMapping(mappedClass: SearchableGrandChild, mappedClassSuperClass: SearchableChildOne, alias: "parent")

        // note the collection of domain classes contains one that is not mapped (because it is not searchable)
        CompassMappingUtils.resolveAliases(classMappings, getDomainClasses(Associate, Parent, SearchableChildOne, SearchableChildTwo, SearchableGrandChild, NonSearchableChild))

        assert classMappings.find { it.mappedClass == Parent }.every { it.alias == "parent" && it.extend == null }
        assert classMappings.find { it.mappedClass == SearchableChildOne }.every { it.alias == CompassMappingUtils.getDefaultAlias(SearchableChildOne) && it.extend == "parent" }
        assert classMappings.find { it.mappedClass == SearchableChildTwo }.every { it.alias == "sct" && it.extend == "parent" }
        assert classMappings.find { it.mappedClass == SearchableGrandChild }.every { it.alias == CompassMappingUtils.getDefaultAlias(SearchableGrandChild) && it.extend == CompassMappingUtils.getDefaultAlias(SearchableChildOne) }
    }

    private getClassMapping(Class clazz) {
        def classMapping = new ClassMapping()
        classMapping.clazz = clazz
        classMapping.alias = clazz.simpleName + "alias"
        classMapping.name = clazz.name
        classMapping
    }

    def getDomainClasses(Class... clazzes) {
        def domainClasses = []
        for (clazz in clazzes) {
            DefaultGrailsDomainClass domainClass = new DefaultGrailsDomainClass(clazz)
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