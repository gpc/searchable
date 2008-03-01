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
package org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping

import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.Resource
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.test.domain.blog.*
import org.compass.core.config.CompassConfiguration
import org.compass.core.config.ConfigurationException
import org.codehaus.groovy.grails.plugins.searchable.compass.config.CompassXmlConfigurationSearchableCompassConfigurator

/**
*
*
* @author Maurice Nicholson
*/
class CompassMappingXmlSearchableGrailsDomainClassMappingConfiguratorTests extends GroovyTestCase {
    SearchableGrailsDomainClassMappingConfigurator strategy

    void setUp() {
        strategy = new CompassMappingXmlSearchableGrailsDomainClassMappingConfigurator()
    }

    void tearDown() {
        strategy = null
    }

    void testIsSearchableWhenMappingXmlAvailable() {
        strategy.resourceLoader = getResourceLoader(User)
        assert strategy.isSearchable(new DefaultGrailsDomainClass(User))
    }

    void testIsSearchableWhenMappingXmlNotAvailable() {
        strategy.resourceLoader = getResourceLoader()
        assert strategy.isSearchable(new DefaultGrailsDomainClass(User)) == false
    }

    void testConfigureMappingWithoutCompassXml() {
        strategy.resourceLoader = getResourceLoader(User)

        def config = new MyCompassConfiguration2()
        strategy.configureMappings(config, [:], [new DefaultGrailsDomainClass(User)])
        assert config.url == new URL("file:/path/to" + getMappingResourceName(User))
    }

    void testConfigureMappingWithCompassXml() {
        strategy.resourceLoader = getResourceLoader(User)

        def config = new MyCompassConfiguration2()
        strategy.configureMappings(config, [(CompassXmlConfigurationSearchableCompassConfigurator.CONFIGURED): true], [new DefaultGrailsDomainClass(User)])
        assert config.url == null
    }

    private getResourceLoader(clazz) {
        def resourceName = getMappingResourceName(clazz)
        [getResource: { name ->
            if (clazz) {
                assert name == "classpath:" + resourceName
            }
            [
                exists: { clazz ? true : false },
                getURL: { new URL("file:/path/to" + resourceName) }
            ] as Resource
        }] as ResourceLoader
    }

    private getMappingResourceName(clazz) {
        clazz ? "/" + clazz.name.replaceAll("\\.", "/") + ".cpm.xml" : null
    }
}

class MyCompassConfiguration2 extends CompassConfiguration {
    def url
    public CompassConfiguration addURL(URL url) throws ConfigurationException {
        assert this.url == null // shouldn't be called more than once for these tests
        this.url = url
        this
    }
}