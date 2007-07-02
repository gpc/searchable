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
package org.codehaus.groovy.grails.plugins.searchable.compass.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping.SearchableGrailsDomainClassMappingConfigurator;
import org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping.SearchableGrailsDomainClassMappingConfiguratorFactory;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.SearchableCompassClassMappingXmlBuilder;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.Map;

/**
 * @author Maurice Nicholson
 */
public abstract class SearchableCompassConfiguratorFactory {
    private static final Log LOG = LogFactory.getLog(SearchableCompassConfiguratorFactory.class);

    public static EnvironmentSearchableCompassConfigurator getEnvironmentConfigurator(String compassConnection, Map compassSettings) {
        EnvironmentSearchableCompassConfigurator environment = new EnvironmentSearchableCompassConfigurator();
        environment.setConnection(compassConnection);
        environment.setCompassSettings(compassSettings);
        return environment;
    }

    public static CompassXmlConfigurationSearchableCompassConfigurator getCompassXmlConfigurator(ResourceLoader resourceLoader) {
        CompassXmlConfigurationSearchableCompassConfigurator compassXml = new CompassXmlConfigurationSearchableCompassConfigurator();
        compassXml.setResourceLoader(resourceLoader);
        return compassXml;
    }

    public static DefaultGrailsDomainClassMappingSearchableCompassConfigurator getDomainClassMappingConfigurator(GrailsApplication grailsApplication, ResourceLoader resourceLoader, Map defaultFormats, List defaultExcludedProperties, SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder) {
        SearchableGrailsDomainClassMappingConfigurator[] classMappingConfigurators = SearchableGrailsDomainClassMappingConfiguratorFactory.getSearchableGrailsDomainClassMappingConfigurators(resourceLoader, defaultFormats, defaultExcludedProperties, compassClassMappingXmlBuilder);

        return getDomainClassMappingConfigurator(grailsApplication, classMappingConfigurators);
    }

    private static DefaultGrailsDomainClassMappingSearchableCompassConfigurator getDomainClassMappingConfigurator(GrailsApplication grailsApplication, SearchableGrailsDomainClassMappingConfigurator[] classMappingConfigurators) {
        DefaultGrailsDomainClassMappingSearchableCompassConfigurator mappings = new DefaultGrailsDomainClassMappingSearchableCompassConfigurator();
        mappings.setGrailsApplication(grailsApplication);
        mappings.setClassMappingStrategies(classMappingConfigurators);
        return mappings;
    }

}
