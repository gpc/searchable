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
package org.codehaus.groovy.grails.plugins.searchable.test.config.defaultfile

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase
import org.compass.core.config.CompassEnvironment

/**
 * @author Maurice Nicholson
 */
class DefaultConfigurationFileTests extends SearchableFunctionalTestCase {
    def applicationContext
    def compass

    public getDomainClasses() {
        return []
    }

    // pre-load the default configuration file into the ClassLoader;
    // return null to indicate that the Spring searchableConfig bean should
    // not be created
    protected Map getSearchableConfig(ClassLoader cl) {
        def pluginHome = getPluginHome(cl)
        cl.parseClass(new File(pluginHome, "src/conf/Searchable.groovy"))
        return null
    }

    // This test simply allows the Grails app to load with the pre-loaded Searchable config
    // class (above)
    // Unfortunately this means the test is effectively split over two methods
    void testDefaultConfigFile() {
        // this is what functional tests normally use
        assert super.getSearchableConfig(this.getClass().getClassLoader()).searchable.compassConnection == "ram://functional-test-index"

        // this value comes from the default config file
        assert applicationContext.grailsApplication.config.searchable.compassConnection == "ram://test-index"
        assert compass.config.getSettings().getSetting(CompassEnvironment.CONNECTION) == "ram://test-index"
    }
}