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

import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.Resource
import org.compass.core.config.CompassConfiguration

/**
*
*
* @author Maurice Nicholson
*/
class CompassXmlConfigurationSearchableCompassConfiguratorTests extends GroovyTestCase {
    def configurator

    void setUp() {
        configurator = new CompassXmlConfigurationSearchableCompassConfigurator()
    }

    void tearDown() {
        configurator = null
    }

    void testConfigureWhenCompassConfigAvailable() {
        def resourceLoader = getResourceLoader(true)
        configurator.resourceLoader = resourceLoader
        def context = [:]
        def conf = new MyCompassConfiguration()
        configurator.configure(conf, context)
        assert conf.configureCalled
        assert context[CompassXmlConfigurationSearchableCompassConfigurator.CONFIGURED]
    }

    void testConfigureWhenCompassConfigNotAvailable() {
        def resourceLoader = getResourceLoader(false)
        configurator.resourceLoader = resourceLoader
        def context = [:]
        def conf = new MyCompassConfiguration()
        configurator.configure(conf, context)
        assert conf.configureCalled == false
        assert !context[CompassXmlConfigurationSearchableCompassConfigurator.CONFIGURED]
    }

    private getResourceLoader(exists) {
        [ getResource: { location ->
            assert location == "classpath:/compass.cfg.xml"
            [
                exists: { exists },
                getURL: {
                    new URL("file://path-to-compass-cfg")
                }
            ] as Resource
            }
        ] as ResourceLoader
    }
}

class MyCompassConfiguration extends CompassConfiguration {
    def configureCalled = false
    URL configUrl
    CompassConfiguration configure(URL url) {
        configureCalled = true
        assert url.toString() == "file://path-to-compass-cfg"
//        configUrl = url
    }
}