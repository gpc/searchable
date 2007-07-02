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
import org.codehaus.groovy.grails.plugins.searchable.compass.config.CompassXmlConfigurationSearchableCompassConfigurator;
import org.codehaus.groovy.grails.plugins.searchable.GrailsDomainClassSearchabilityEvaluator;
import org.compass.core.config.CompassConfiguration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.util.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Map;
import java.io.IOException;

/**
 * Configures Compass with searchable domain classes that have a corresponding Compass mapping XML classpath resource.
 *
 * @author Maurice Nicholson
 */
public class CompassMappingXmlSearchableGrailsDomainClassMappingConfigurator implements SearchableGrailsDomainClassMappingConfigurator, GrailsDomainClassSearchabilityEvaluator, ResourceLoaderAware {
    private static final Log LOG = LogFactory.getLog(CompassMappingXmlSearchableGrailsDomainClassMappingConfigurator.class);

    private ResourceLoader resourceLoader;

    /**
     * Does this strategy handle the given domain class (and it's respective mapping type)
     *
     * @param grailsDomainClass the Grails domain class
     * @return true if the mapping of the class can be handled by this strategy
     */
    public boolean isSearchable(GrailsDomainClass grailsDomainClass) {
        Assert.notNull(resourceLoader, "resourceLoader cannot be null");
        Resource resource = getMappingResource(grailsDomainClass);
        return resource.exists();
    }

    /**
     * Configure the Mapping in the CompassConfiguration for the given domain class
     *
     * @param compassConfiguration          the CompassConfiguration instance
     * @param configurationContext          a configuration context, for flexible parameter passing
     * @param grailsDomainClass             the Grails domain class to map
     * @param searchableGrailsDomainClasses all searchable domain classes
     */
    public void configureMapping(CompassConfiguration compassConfiguration, Map configurationContext, GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses) {
        Assert.notNull(resourceLoader, "resourceLoader cannot be null");
        if (!configurationContext.containsKey(CompassXmlConfigurationSearchableCompassConfigurator.CONFIGURED)) {
            Resource resource = getMappingResource(grailsDomainClass);
            Assert.isTrue(resource.exists(), "mapping resource must exist: did isSearchable() not return false?");
            try {
                compassConfiguration.addURL(resource.getURL());
            } catch (IOException ex) {
                String message = "Failed to configure Compass with mapping resource for class [" + grailsDomainClass.getClazz().getName() + "] and resource [" + getMappingResourceName(grailsDomainClass) + "]";
                LOG.error(message, ex);
                throw new IllegalStateException(message + ": " + ex);
            }
        }
    }

    /**
     * Get this strategy's name
     *
     * @return name
     */
    public String getName() {
        return "Compass Mapping XML";
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private Resource getMappingResource(GrailsDomainClass grailsDomainClass) {
        return resourceLoader.getResource(getMappingResourceName(grailsDomainClass));
    }

    private String getMappingResourceName(GrailsDomainClass grailsDomainClass) {
        String className = grailsDomainClass.getClazz().getName();
        Assert.notNull(grailsDomainClass, "grailsDomainClass cannot be null");
        return "classpath:/" + className.replaceAll("\\.", "/") + ".cpm.xml";
    }

}
