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

import org.compass.core.config.CompassConfiguration;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.SearchableGrailsDomainClassMappingStrategy;
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.springframework.util.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * A Compass configurator that configures Compass with the Grails domain class mappings.
 *
 * An appropriate mapping strategy is identified for each searchable domain class from the
 * {@link #classMappingStrategies}
 *
 * @author Maurice Nicholson
 */
public class DefaultGrailsDomainClassMappingSearchableCompassConfigurator implements SearchableCompassConfigurator {
    private static final Log LOG = LogFactory.getLog(DefaultGrailsDomainClassMappingSearchableCompassConfigurator.class);

    private GrailsApplication grailsApplication;
    private SearchableGrailsDomainClassMappingStrategy[] classMappingStrategies;

    /**
     * Configure Compass ready for it to be built
     *
     * @param compassConfiguration runtime configuration instance
     * @param configurationContext a context allowing flexible parameter passing
     */
    public void configure(CompassConfiguration compassConfiguration, Map configurationContext) {
        Assert.notNull(grailsApplication, "grailsApplication cannot be null");
        Map strategyBySearchableDomainClass = new HashMap();
        for (Iterator iter = SearchableUtils.getGrailsDomainClasses(grailsApplication).iterator(); iter.hasNext(); ) {
            GrailsDomainClass grailsDomainClass = (GrailsDomainClass) iter.next();
            for (int i = 0; i < classMappingStrategies.length; i++) {
                if (classMappingStrategies[i].isSearchable(grailsDomainClass)) {
                    strategyBySearchableDomainClass.put(grailsDomainClass, classMappingStrategies[i]);
                    break;
                }
            }
            if (LOG.isDebugEnabled() && strategyBySearchableDomainClass.get(grailsDomainClass) == null) {
                LOG.debug("No mapping strategy found for class [" + grailsDomainClass.getClazz() + "]: assuming this class is not searchable");
            }
        }

        Collection searchableGrailsDomainClasses = strategyBySearchableDomainClass.keySet();
        for (Iterator iter = searchableGrailsDomainClasses.iterator(); iter.hasNext(); ) {
            GrailsDomainClass grailsDomainClass = (GrailsDomainClass) iter.next();
            SearchableGrailsDomainClassMappingStrategy mappingStrategy = (SearchableGrailsDomainClassMappingStrategy) strategyBySearchableDomainClass.get(grailsDomainClass);
            LOG.debug("Mapping class [" + grailsDomainClass.getClazz().getName() + "] with strategy [" + mappingStrategy.getName() + "]");
            mappingStrategy.configureMapping(compassConfiguration, configurationContext, grailsDomainClass, searchableGrailsDomainClasses);
        }
    }

    public GrailsApplication getGrailsApplication() {
        return grailsApplication;
    }

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
    }

    public void setClassMappingStrategies(SearchableGrailsDomainClassMappingStrategy[] classMappingStrategies) {
        this.classMappingStrategies = classMappingStrategies;
    }
}
