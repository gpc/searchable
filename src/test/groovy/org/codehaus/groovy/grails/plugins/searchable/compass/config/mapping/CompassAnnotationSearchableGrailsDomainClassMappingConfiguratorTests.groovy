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

import org.springframework.core.JdkVersion

import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.compass.config.CompassXmlConfigurationSearchableCompassConfigurator

import org.compass.core.config.*

/**
 * @author Maurice Nicholson
 */
class CompassAnnotationSearchableGrailsDomainClassMappingConfiguratorTests extends GroovyTestCase {
    SearchableGrailsDomainClassMappingConfigurator strategy
    Class compassAnnotated

    void setUp() {
        def gcl = new GroovyClassLoader()
        Thread.currentThread().setContextClassLoader(gcl)
        if (JdkVersion.isAtLeastJava15()) {
            def cl = Thread.currentThread().getContextClassLoader()
            assert cl instanceof GroovyClassLoader
            compassAnnotated = cl.parseClass("""
package org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping

import org.compass.annotations.*

@Searchable
private class CompassAnnotated {

}
""")
        }
        strategy = new CompassAnnotationSearchableGrailsDomainClassMappingConfigurator()
    }

    void tearDown() {
        strategy = null
    }

    void testIsMappedByWhenNotAnnotated() {
        assert strategy.isMappedBy([
            getClazz: { Object.class } // not important
        ] as GrailsDomainClass) == false
    }

    void testIsMappedByWhenAnnotated() {
        if (!JdkVersion.isAtLeastJava15()) {
            return
        }
        assert strategy.isMappedBy([
            getClazz: { return compassAnnotated }
        ] as GrailsDomainClass) == true
    }

    void testConfigureWithoutCompassXml() {
        if (!JdkVersion.isAtLeastJava15()) {
            return
        }

        def compassConfiguration = new MockCompassConfiguration()
        strategy.configureMappings(compassConfiguration, [:], [[getClazz: { compassAnnotated }] as GrailsDomainClass], null)
        assert compassConfiguration.addedClass == compassAnnotated
    }

    void testConfigureWithCompassXml() {
        if (!JdkVersion.isAtLeastJava15()) {
            return
        }

        def compassConfiguration = new MockCompassConfiguration()
        strategy.configureMappings(compassConfiguration, [(CompassXmlConfigurationSearchableCompassConfigurator.CONFIGURED): true], [[getClazz: { compassAnnotated }] as GrailsDomainClass], null)
        assert compassConfiguration.addedClass == null
    }
}

class MockCompassConfiguration extends CompassConfiguration {
    def addedClass
    CompassConfiguration addClass(Class clazz) {
        addedClass = clazz
        return this
    }
}