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
package org.codehaus.groovy.grails.plugins.searchable.compass;

import org.codehaus.groovy.grails.plugins.searchable.GrailsDomainClassSearchabilityEvaluator;
import org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping.SearchableGrailsDomainClassMappingConfiguratorFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Maurice Nicholson
 */
public class CompassGrailsDomainClassSearchabilityEvaluatorFactory {

    public static GrailsDomainClassSearchabilityEvaluator[] getGrailsDomainClassSearchabilityEvaluators(ResourceLoader resourceLoader) {
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader();
        }
        return SearchableGrailsDomainClassMappingConfiguratorFactory.getSearchableGrailsDomainClassMappingConfigurators(resourceLoader, null, null, null);
    }
}
