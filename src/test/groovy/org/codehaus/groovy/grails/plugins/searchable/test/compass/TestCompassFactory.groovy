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
package org.codehaus.groovy.grails.plugins.searchable.test.compass

import org.compass.core.*
import org.compass.core.config.CompassConfiguration
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.plugins.searchable.compass.config.*
import org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping.*
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.*
import org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping.SearchableGrailsDomainClassMappingConfigurator

/**
 *
 * @author Maurice Nicholson
 */
class TestCompassFactory {
    static getCompass(classes, instances = null) {
        def grailsApplication = new DefaultGrailsApplication(classes as Class[], new GroovyClassLoader())
        ApplicationHolder.setApplication(grailsApplication)
        def configurator = SearchableCompassConfiguratorFactory.getDomainClassMappingConfigurator(
            grailsApplication,
            [SearchableGrailsDomainClassMappingConfiguratorFactory.getSearchableClassPropertyMappingConfigurator([:], [], new DefaultSearchableCompassClassMappingXmlBuilder())] as SearchableGrailsDomainClassMappingConfigurator[]
        )
        def config = new CompassConfiguration()
        config.setConnection("ram://testindex")
        configurator.configure(config, [:])
        def compass = config.buildCompass()

        if (instances) {
            CompassTemplate template = new CompassTemplate(compass)
            template.execute(new SaveInstancesCompassCallback(instances))
        }

        compass
    }
}

// Save all object instances to compass
class SaveInstancesCompassCallback implements CompassCallback {
    def instances

    SaveInstancesCompassCallback(instances) {
        this.instances = instances
    }

    public Object doInCompass(CompassSession session) throws CompassException {
        instances.each { session.save(it) }
    }
}