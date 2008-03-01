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
package org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping;

import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.plugins.searchable.GrailsDomainClassSearchabilityEvaluator;
import org.compass.core.config.CompassConfiguration;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.Map;

/**
 * Configures Compass with a searchable domain class mapping
 *
 * @author Maurice Nicholson
 */
// todo is there any benefit in having GrailsDomainClassSearchabilityEvaluator? maybe just use configurator to configure all relevant classes and ignore others
public interface SearchableGrailsDomainClassMappingConfigurator extends GrailsDomainClassSearchabilityEvaluator, Ordered {

    /**
     * Configure the Mapping in the CompassConfiguration for the given domain class
     *
     * @param compassConfiguration the CompassConfiguration instance
     * @param configurationContext a configuration context, for flexible parameter passing
     * @param searchableGrailsDomainClasses searchable domain classes to map
     */
    void configureMappings(CompassConfiguration compassConfiguration, Map configurationContext, Collection searchableGrailsDomainClasses);

    /**
     * Get this strategy's name
     * @return name
     */
    String getName();
}
