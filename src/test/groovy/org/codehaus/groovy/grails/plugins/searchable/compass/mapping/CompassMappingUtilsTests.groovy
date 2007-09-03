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

    private getClassMapping(Class clazz) {
        def classMapping = new ClassMapping()
        classMapping.clazz = clazz
        classMapping.alias = clazz.simpleName + "alias"
        classMapping.name = clazz.name
        classMapping
    }
}