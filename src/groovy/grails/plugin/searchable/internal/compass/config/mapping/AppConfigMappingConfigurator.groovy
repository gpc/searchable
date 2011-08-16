/*
 * Copyright 2011 the original author or authors.
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
package grails.plugin.searchable.internal.compass.config.mapping

import java.util.Collection;

import grails.plugin.searchable.internal.SearchableUtils
import grails.plugin.searchable.internal.compass.mapping.*

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.compass.core.config.CompassConfiguration
import org.springframework.util.Assert
import org.springframework.core.Ordered

/**
 * Configures Compass with searchable domain classes according to
 * "searchable.domain.*" application configuration settings.
 *
 * @author Peter Ledbrook
 */
class AppConfigMappingConfigurator implements SearchableGrailsDomainClassMappingConfigurator, Ordered {
    private static final Log LOG = LogFactory.getLog(this)

    private SearchableGrailsDomainClassCompassClassMapper classMapper
    private SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder
    private config
    
    public AppConfigMappingConfigurator(config) {
        this.config = config;
    }

    /**
     * Returns the collection of GrailsDomainClasses that are mapped by this instance
     * @param grailsDomainClasses ALL domain classes
     * @return the collection of domain classes mapped by this instance
     */
    Collection getMappedBy(Collection grailsDomainClasses) {
        Set mappedBy = []
        def searchableDomainConfig = config.searchable.domain
        
        if (!searchableDomainConfig) return mappedBy
        
        for (gdc in grailsDomainClasses) {
            if (searchableDomainConfig."${gdc.logicalPropertyName}") mappedBy << gdc
        }
        
        return mappedBy
    }
    
    Collection getUnmapped(Collection grailsDomainClasses) {
        Set unmapped = []
        def searchableDomainConfig = config.searchable.domain
        
        if (!searchableDomainConfig) return unmapped
        
        for (gdc in grailsDomainClasses) {
            def dcConfig = searchableDomainConfig."${gdc.logicalPropertyName}"
            if (dcConfig instanceof Boolean && !dcConfig) unmapped << gdc
        }
        
        return unmapped
    }

    /**
     * Configure the Mapping in the CompassConfiguration for the given domain class
     *
     * @param compassConfiguration          the CompassConfiguration instance
     * @param configurationContext          a configuration context, for flexible parameter passing
     * @param searchableGrailsDomainClasses all searchable domain classes, whether configured here or elsewhere
     */
    public void configureMappings(
            CompassConfiguration compassConfiguration,
            Map configurationContext,
            Collection searchableClasses,
            Collection allSearchableClasses) {
        Assert.notNull(classMapper, "classMapper cannot be null");
        Assert.notNull(compassClassMappingXmlBuilder, "compassClassMappingXmlBuilder cannot be null");

        // map all classes
        def classMappings = searchableClasses.collect { gdc -> classMapper.getCompassClassMapping(gdc, allSearchableClasses) }

        // resolve aliases
        CompassMappingUtils.resolveAliases(classMappings, allSearchableClasses, compassConfiguration);

        // add completed mappings to compass
        for (Iterator iter = classMappings.iterator(); iter.hasNext(); ) {
            CompassClassMapping classMapping = (CompassClassMapping) iter.next();
            InputStream inputStream = compassClassMappingXmlBuilder.buildClassMappingXml(classMapping);
            LOG.debug("Adding [" + classMapping.getMappedClass().getName() + "] mapping to CompassConfiguration");
            compassConfiguration.removeMappingByClass(classMapping.getMappedClass());
            compassConfiguration.addInputStream(inputStream, classMapping.getMappedClass().getName().replaceAll("\\.", "/") + ".cpm.xml");
        }
    }

    /**
     * Get this strategy's name
     *
     * @return name
     */
    public String getName() {
        return "searchable class property";
    }

    public void setMappingDescriptionProviderManager(SearchableGrailsDomainClassCompassClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    public void setCompassClassMappingXmlBuilder(SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder) {
        this.compassClassMappingXmlBuilder = compassClassMappingXmlBuilder;
    }

    /**
     * Determines the order of this mapping configurator in relation to others
     * @return the order
     */
    public int getOrder() {
        return 3;
    }
}
