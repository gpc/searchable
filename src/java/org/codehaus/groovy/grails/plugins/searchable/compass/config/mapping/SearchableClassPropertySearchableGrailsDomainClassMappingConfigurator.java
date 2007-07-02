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

import groovy.lang.MissingPropertyException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.plugins.searchable.GrailsDomainClassSearchabilityEvaluator;
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompassClassMapping;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompositeSearchableGrailsDomainClassCompassClassMapper;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.SearchableCompassClassMappingXmlBuilder;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.SearchableGrailsDomainClassCompassClassMapper;
import org.compass.core.config.CompassConfiguration;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Configures Compass with searchable domain classes according to a "searchable" class property value
 *
 * @author Maurice Nicholson
 */
public class SearchableClassPropertySearchableGrailsDomainClassMappingConfigurator implements SearchableGrailsDomainClassMappingConfigurator, GrailsDomainClassSearchabilityEvaluator {
    private static final Log LOG = LogFactory.getLog(SearchableClassPropertySearchableGrailsDomainClassMappingConfigurator.class);

    private SearchableGrailsDomainClassCompassClassMapper classMapper;
    private SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder;

    /**
     * Does this strategy handle the given domain class (and it's respective mapping type)
     *
     * @param grailsDomainClass the Grails domain class
     * @return true if the mapping of the class can be handled by this strategy
     */
    public boolean isSearchable(GrailsDomainClass grailsDomainClass) {
        Assert.notNull(grailsDomainClass, "grailsDomainClass cannot be null");
        try {
            Object value = SearchableUtils.getSearchablePropertyValue(grailsDomainClass);
            return value != null && !((value instanceof Boolean) && value.equals(Boolean.FALSE));
        } catch (MissingPropertyException ex) {
            return false;
        }
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
        Assert.notNull(classMapper, "classMapper cannot be null");
        Assert.notNull(compassClassMappingXmlBuilder, "compassClassMappingXmlBuilder cannot be null");
        Assert.notNull(grailsDomainClass, "grailsDomainClass cannot be null");

        CompassClassMapping classMapping = classMapper.getCompassClassMapping(grailsDomainClass, searchableGrailsDomainClasses);
        InputStream inputStream = compassClassMappingXmlBuilder.buildClassMappingXml(classMapping);
        LOG.debug("Adding [" + classMapping.getMappedClass().getName() + "] mapping to CompassConfiguration");
        compassConfiguration.addInputStream(inputStream, classMapping.getMappedClass().getName().replaceAll("\\.", "/") + ".cpm.xml");
    }

    /**
     * Get this strategy's name
     *
     * @return name
     */
    public String getName() {
        return "searchable class property";
    }

    public void setMappingDescriptionProviderManager(CompositeSearchableGrailsDomainClassCompassClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    public void setCompassClassMappingXmlBuilder(SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder) {
        this.compassClassMappingXmlBuilder = compassClassMappingXmlBuilder;
    }
}
