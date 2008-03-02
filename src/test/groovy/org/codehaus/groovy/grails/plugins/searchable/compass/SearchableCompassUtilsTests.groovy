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

import org.codehaus.groovy.grails.commons.GrailsApplication

/**
*
*
* @author Maurice Nicholson
*/
class SearchableCompassUtilsTests extends GroovyTestCase {

    void testGetDefaultConnection() {
        // with grails application
        def grailsApplication = [getMetadata: {-> ['app.name': "blah"] }] as GrailsApplication
        String expected = [System.properties['user.home'], '.grails', 'projects', 'blah', 'searchable-index', 'development'].join(File.separator)
        String actual = SearchableCompassUtils.getDefaultConnection(grailsApplication)
        assert actual == expected

        // without grails application; just make sure there is no error
        actual = SearchableCompassUtils.getDefaultConnection()
        assert actual
    }
}
