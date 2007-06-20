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
package org.codehaus.groovy.grails.plugins.searchable.compass.config

import org.compass.core.config.CompassConfiguration
import org.compass.core.config.CompassEnvironment
import org.codehaus.groovy.grails.commons.*

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*
import org.compass.core.config.CompassSettings
import org.jmock.core.constraint.IsEqual

/**
* @author Maurice Nicholson
*/
class EnvironmentSearchableCompassConfiguratorTests extends GroovyTestCase {
    def configurator
    def oldApp

    void setUp() {
        configurator = new EnvironmentSearchableCompassConfigurator()
        oldApp = ApplicationHolder.getApplication()
        def mockApp = new Mock(GrailsApplication.class)
        ApplicationHolder.setApplication(mockApp.proxy())
        def appMetaData = ["app.name": "shinnynewgrailsapp", "grails.env": "dev"]
        mockApp.expects(new InvokeAtLeastOnceMatcher()).method('getMetadata').withNoArguments().will(new ReturnStub(appMetaData))
    }

    void tearDown() {
        ApplicationHolder.setApplication(oldApp)
        oldApp = null
    }

    void testConfigureDefaultConnection() {
        def config = new CompassConfiguration()
        configurator.configure(config, [:])

        assert getConnection(config) == System.properties["user.home"] + File.separator + "shinnynewgrailsapp" + File.separator + ".searchable" + File.separator + "development"
    }

    void testConfigureSpecificConnection() {
        configurator.connection = "ram://myramconnection"
        def config = new CompassConfiguration()
        configurator.configure(config, [:])

        assert getConnection(config) == "ram://myramconnection"

        configurator.connection = "/home/path/to/connection"
        config = new CompassConfiguration()
        configurator.configure(config, [:])

        assert getConnection(config) == "/home/path/to/connection"
    }

    void testConfigureWithCompassSettings() {
        // String values
        configurator.compassSettings = [foo: "FOO", bar: "BAR", baz: "BAZ"]

        def config = new CompassConfiguration()
        configurator.configure(config, [:])

        // Check that the settings have been set
        [foo: "FOO", bar: "BAR", baz: "BAZ"].each { k, v ->
            assert config.settings.getProperties()[k] == v
        }

        // Mixed String and non-String values
        configurator.compassSettings = [string: "String", number: 152634, clazz: URL]

        config = new CompassConfiguration()
        configurator.configure(config, [:])

        // Check that the settings have been set
        [string: "String", number: "152634", clazz: "java.net.URL"].each { k, v ->
            assert config.settings.getProperties()[k] == v
        }
    }

    void testNotConfigureConnectionIfAlreadySet() {
        def config = new CompassConfiguration()
        config.setConnection("ram://index")

        configurator.configure(config, [:])

        assert getConnection(config) == "ram://index"
    }

    private getConnection(config) {
        config.getSettings().getSetting(CompassEnvironment.CONNECTION)
    }
}
