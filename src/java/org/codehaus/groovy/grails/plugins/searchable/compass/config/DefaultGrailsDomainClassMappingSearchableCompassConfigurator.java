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
import org.codehaus.groovy.grails.plugins.searchable.compass.config.mapping.SearchableGrailsDomainClassMappingConfigurator;
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.springframework.util.Assert;
import org.springframework.core.OrderComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * A Compass configurator that configures Compass with the Grails domain class mappings.
 *
 * An appropriate mapping strategy is identified for each searchable domain class from the
 * {@link #classMappingConfigurators}
 *
 * @author Maurice Nicholson
 */
public class DefaultGrailsDomainClassMappingSearchableCompassConfigurator implements SearchableCompassConfigurator {
    private static final Log LOG = LogFactory.getLog(DefaultGrailsDomainClassMappingSearchableCompassConfigurator.class);

    private GrailsApplication grailsApplication;
    private SearchableGrailsDomainClassMappingConfigurator[] classMappingConfigurators;

    /**
     * Configure Compass ready for it to be built
     *
     * @param compassConfiguration runtime configuration instance
     * @param configurationContext a context allowing flexible parameter passing
     */
    public void configure(CompassConfiguration compassConfiguration, Map configurationContext) {
        Assert.notNull(grailsApplication, "grailsApplication cannot be null");
        Assert.notNull(classMappingConfigurators, "classMappingConfigurators cannot be null");

        // determine which classes are mapped by which strategy
        Map classesByStrategy = new HashMap();
        Collection grailsDomainClasses = SearchableUtils.getGrailsDomainClasses(grailsApplication);
        Collection mappableClasses = new HashSet();
        Set notMapped = new HashSet(grailsDomainClasses);
        for (int i = 0; i < classMappingConfigurators.length; i++) {
            SearchableGrailsDomainClassMappingConfigurator configurator = classMappingConfigurators[i];
            Collection classes = configurator.getMappedBy(notMapped);
            if (classes != null) {
                notMapped.removeAll(classes);
                if (LOG.isDebugEnabled()) {
                    for (Iterator iter = classes.iterator(); iter.hasNext(); ) {
                        GrailsDomainClass grailsDomainClass = (GrailsDomainClass) iter.next();
                        LOG.debug("Mapping class [" + grailsDomainClass.getClazz().getName() + "] with strategy [" + configurator.getName() + "]");
                    }
                }
                classesByStrategy.put(classMappingConfigurators[i], classes);
                mappableClasses.addAll(classes);
            }
        }

        if (LOG.isDebugEnabled() && !notMapped.isEmpty()) {
            for (Iterator iter = notMapped.iterator(); iter.hasNext(); ) {
                GrailsDomainClass grailsDomainClass = (GrailsDomainClass) iter.next();
                LOG.debug("No mapping strategy found for class [" + grailsDomainClass.getClazz() + "]: assuming this class is not searchable");

            }
        }

        // map classes in the order defined by the classMappingConfigurators
        for (int i = 0; i < classMappingConfigurators.length; i++) {
            SearchableGrailsDomainClassMappingConfigurator classMappingConfigurator = classMappingConfigurators[i];
            Collection classes = (Collection) classesByStrategy.get(classMappingConfigurator);
            if (classes != null && !classes.isEmpty()) {
                classMappingConfigurator.configureMappings(compassConfiguration, configurationContext, classes, mappableClasses);
            }
        }
    }

    public GrailsApplication getGrailsApplication() {
        return grailsApplication;
    }

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
    }

    public void setClassMappingStrategies(SearchableGrailsDomainClassMappingConfigurator[] classMappingConfigurators) {
        Arrays.sort(classMappingConfigurators, new OrderComparator());
        this.classMappingConfigurators = classMappingConfigurators;
    }
}
