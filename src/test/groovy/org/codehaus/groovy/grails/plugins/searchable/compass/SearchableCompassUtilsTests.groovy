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
package org.codehaus.groovy.grails.plugins.searchable.compass

import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.codehaus.groovy.grails.plugins.searchable.test.domain.component.*
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

/**
*
*
* @author Maurice Nicholson
*/
class SearchableCompassUtilsTests extends GroovyTestCase {
    def domainClassMap

    void setUp() {
        domainClassMap = [:]
        for (type in [Post, User, Comment, ComponentOwner, SearchableComp]) {
            domainClassMap[type] = new DefaultGrailsDomainClass(type)
        }
    }

    void testGetDefaultAlias() {
        assert SearchableCompassUtils.getDefaultAlias(Post) == 'Post'
        assert SearchableCompassUtils.getDefaultAlias(User) == 'User'
        assert SearchableCompassUtils.getDefaultAlias(Comment) == 'Comment'
    }

    void testIsRoot() {
        assert isRoot(Post, [Post, User, Comment])
        assert isRoot(User, [Post, User, Comment])
        assert isRoot(Comment, [Post, User, Comment])

        assert isRoot(ComponentOwner, [ComponentOwner, SearchableComp])
        assert isRoot(SearchableComp, [ComponentOwner, SearchableComp]) == false
    }

    private isRoot(type, searchableClasses) {
        SearchableCompassUtils.isRoot(domainClassMap[type], searchableClasses.collect { domainClassMap[it] })
    }
}