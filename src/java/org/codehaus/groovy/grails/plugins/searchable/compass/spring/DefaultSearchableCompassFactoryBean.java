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
package org.codehaus.groovy.grails.plugins.searchable.compass.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.searchable.compass.config.*;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * A default Compass configuration strategy
 *
 * @author Maurice Nicholson
 */
public class DefaultSearchableCompassFactoryBean extends SearchableCompassFactoryBean implements InitializingBean, ResourceLoaderAware {
    private static final Log LOG = LogFactory.getLog(DefaultSearchableCompassFactoryBean.class);

    private GrailsApplication grailsApplication;
    private SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder;
    private String compassConnection;
    private Map compassSettings;
    private List defaultExcludedProperties;
    private Map defaultFormats;
    private ResourceLoader resourceLoader;

    /**
     * Build the superclass; the "real" factory bean
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(grailsApplication, getClass().getName() + ".grailsApplication cannot be null");
        Assert.notNull(compassClassMappingXmlBuilder, getClass().getName() + ".compassClassMappingXmlBuilder cannot be null");
        Assert.notNull(resourceLoader, getClass().getName() + ".resourceLoader cannot be null");

        LOG.debug("Building SearchableCompassFactoryBean with grailsApplication [" + grailsApplication + "] and compassClassMappingXmlBuilder [" + compassClassMappingXmlBuilder + "]");

        EnvironmentSearchableCompassConfigurator environment = SearchableCompassConfiguratorFactory.getEnvironmentConfigurator(compassConnection, compassSettings);
        CompassXmlConfigurationSearchableCompassConfigurator compassXml = SearchableCompassConfiguratorFactory.getCompassXmlConfigurator(resourceLoader);
        DefaultGrailsDomainClassMappingSearchableCompassConfigurator mappings = SearchableCompassConfiguratorFactory.getDomainClassMappingConfigurator(grailsApplication, resourceLoader, defaultFormats, defaultExcludedProperties, compassClassMappingXmlBuilder);

        CompositeSearchableCompassConfigurator configurator = new CompositeSearchableCompassConfigurator();
        configurator.setSearchableCompassConfigurators(new SearchableCompassConfigurator[] {
            compassXml, environment, mappings
        });

        super.setSearchableCompassConfigurator(configurator);
    }

    public GrailsApplication getGrailsApplication() {
        return grailsApplication;
    }

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
    }

    public SearchableCompassClassMappingXmlBuilder getCompassClassMappingXmlBuilder() {
        return compassClassMappingXmlBuilder;
    }

    public void setCompassClassMappingXmlBuilder(SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder) {
        this.compassClassMappingXmlBuilder = compassClassMappingXmlBuilder;
    }

    public String getCompassConnection() {
        return compassConnection;
    }

    public void setCompassConnection(String compassConnection) {
        this.compassConnection = compassConnection;
    }

    public Map getCompassSettings() {
        return compassSettings;
    }

    public void setCompassSettings(Map compassSettings) {
        this.compassSettings = compassSettings;
    }


    public void setDefaultExcludedProperties(List defaultExcludedProperties) {
        this.defaultExcludedProperties = defaultExcludedProperties;
    }

    public Map getDefaultFormats() {
        return defaultFormats;
    }

    public void setDefaultFormats(Map defaultFormats) {
        this.defaultFormats = defaultFormats;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
