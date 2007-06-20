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
package org.codehaus.groovy.grails.plugins.searchable.compass.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.springframework.util.Assert;

import java.util.*;

/**
 * A provider of CompassMappingDescriptions for "searchable" Grails domain classes
 *
 * @author Maurice Nicholson
 */
public class SearchableGrailsDomainClassCompassMappingDescriptionProviderManager {
    private static final Log LOG = LogFactory.getLog(SearchableGrailsDomainClassCompassMappingDescriptionProviderManager.class);
    private static final String[] DEFAULT_EXCLUDED_PROPERTIES = {
        "password"
    };

    private List defaultExcludedProperties;
    private SearchableGrailsDomainClassCompassMappingDescriptionProvider[] searchableGrailsDomainClassCompassMappingDescriptionProviders;

    /**
     * Is there an appropriate mapping description provider for the given domain class
     * (and its searchable value)
     */
    public boolean handles(GrailsDomainClass grailsDomainClass) {
        return getCompassMappingDescriptionProvider(SearchableUtils.getSearchablePropertyValue(grailsDomainClass)) != null;
    }

    /**
     * Get CompassMappingDescriptions for searchable domain classes
     * @param grailsApplication
     * @return mapping descriptions
     */
    public CompassMappingDescription[] getCompassMappingDescriptions(GrailsApplication grailsApplication) {
        Map searchableDomainClassesMap = SearchableUtils.getSearchableGrailsDomainClassesMap(grailsApplication);
        return getCompassMappingDescriptions(searchableDomainClassesMap);
    }

    /**
     * Gets Compass mapping descriptions for the given searchable domain classes map
     *
     * @param searchableDomainClassesMap a Map from searchable GrailsDomainClass to searchable property value
     * @return mapping descriptions
     */
    public CompassMappingDescription[] getCompassMappingDescriptions(Map searchableDomainClassesMap) {
        Set searchableGrailsDomainClasses = new HashSet();
        for (Iterator iter = searchableDomainClassesMap.keySet().iterator(); iter.hasNext(); ) {
            searchableGrailsDomainClasses.add(iter.next());
        }
        LOG.debug("Searchable domain classes: " + searchableGrailsDomainClasses);

        List descriptions = new ArrayList();
        for (Iterator iter = searchableDomainClassesMap.keySet().iterator(); iter.hasNext(); ) {
            GrailsDomainClass grailsDomainClass = (GrailsDomainClass) iter.next();
            Object searchableValue = searchableDomainClassesMap.get(grailsDomainClass);

            CompassMappingDescription description = getCompassMappingDescription(grailsDomainClass, searchableGrailsDomainClasses, searchableValue);
            descriptions.add(description);
        }

        return (CompassMappingDescription[]) descriptions.toArray(new CompassMappingDescription[descriptions.size()]);
    }

    /**
     * Gets the CompassMappingDescription for the given domain class
     * @param grailsDomainClass
     * @param searchableClasses
     * @return
     */
    public CompassMappingDescription getCompassMappingDescription(GrailsDomainClass grailsDomainClass, Collection searchableClasses) {
        return getCompassMappingDescription(grailsDomainClass, searchableClasses, SearchableUtils.getSearchablePropertyValue(grailsDomainClass));
    }

    /**
     * Gets the CompassMappingDescription for the given domain class
     * @param grailsDomainClass
     * @param searchableClasses
     * @param searchableValue
     * @return
     */
    public CompassMappingDescription getCompassMappingDescription(GrailsDomainClass grailsDomainClass, Collection searchableClasses, Object searchableValue) {
        SearchableGrailsDomainClassCompassMappingDescriptionProvider mappingDescriptionProvider = getCompassMappingDescriptionProvider(searchableValue);
        Assert.notNull(mappingDescriptionProvider, "No mapping description provider for searchable value [" + searchableValue + "] it should be boolean, Map or Closure");

        return mappingDescriptionProvider.getCompassMappingDescription(grailsDomainClass, searchableClasses, searchableValue, getExcludedProperties());
    }

    private SearchableGrailsDomainClassCompassMappingDescriptionProvider getCompassMappingDescriptionProvider(Object searchableValue) {
        SearchableGrailsDomainClassCompassMappingDescriptionProvider mappingDescriptionProvider = null;
        for (int i = 0; i < searchableGrailsDomainClassCompassMappingDescriptionProviders.length; i++) {
            if (searchableGrailsDomainClassCompassMappingDescriptionProviders[i].handlesSearchableValue(searchableValue)) {
                mappingDescriptionProvider = searchableGrailsDomainClassCompassMappingDescriptionProviders[i];
                break;
            }
        }
        return mappingDescriptionProvider;
    }

    protected List getExcludedProperties() {
        if (defaultExcludedProperties != null) {
            return defaultExcludedProperties;
        }
        return Arrays.asList(DEFAULT_EXCLUDED_PROPERTIES);
    }

    public void setDefaultExcludedProperties(List defaultExcludedProperties) {
        this.defaultExcludedProperties = defaultExcludedProperties;
    }

    public SearchableGrailsDomainClassCompassMappingDescriptionProvider[] getSearchableGrailsDomainClassCompassMappingDescriptionProviders() {
        return searchableGrailsDomainClassCompassMappingDescriptionProviders;
    }

    public void setSearchableGrailsDomainClassCompassMappingDescriptionProviders(SearchableGrailsDomainClassCompassMappingDescriptionProvider[] searchableGrailsDomainClassCompassMappingDescriptionProviders) {
        this.searchableGrailsDomainClassCompassMappingDescriptionProviders = searchableGrailsDomainClassCompassMappingDescriptionProviders;
    }
}
