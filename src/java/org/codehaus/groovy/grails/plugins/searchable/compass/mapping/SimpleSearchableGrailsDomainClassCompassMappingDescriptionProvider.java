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

import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @author Maurice Nicholson
 */
public class SimpleSearchableGrailsDomainClassCompassMappingDescriptionProvider extends AbstractSearchableGrailsDomainClassCompassMappingDescriptionProvider implements SearchableGrailsDomainClassCompassMappingDescriptionProvider {

    /**
     * Get the CompassMappingDescription  for the given GrailsDomainClass and "searchable" value
     * @param grailsDomainClass the Grails domain class
     * @param searchableGrailsDomainClasses a collection of searchable GrailsDomainClass instances
     * @param searchableValue the searchable value: true|false|Map in this case
     * @param excludedProperties a List of properties NOT to map; only used for "true" searchable value
     * @return the CompassMappingDescription
     */
    public CompassMappingDescription getCompassMappingDescription(GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses, Object searchableValue, List excludedProperties) {
        Assert.notNull(searchableValue, "searchableValue cannot be null");
        Assert.isTrue(searchableValue instanceof Boolean || searchableValue instanceof Map, "[" + grailsDomainClass.getClazz().getName() + ".searchable] must be either a boolean, Map or closure (not [" + searchableValue.getClass().getName() + "]");

        GrailsDomainClassProperty[] mappableProperties = getMappableProperties(grailsDomainClass, searchableValue, searchableGrailsDomainClasses, excludedProperties);
        if (mappableProperties == null) {
            return null;
        }
        Map map = getProperyMappings(mappableProperties, grailsDomainClass, searchableGrailsDomainClasses);
        if (map == null) {
            return null;
        }
        CompassMappingDescription description = new CompassMappingDescription();
        description.setMappedClass(grailsDomainClass.getClazz());
        description.setProperties(map);
        description.setRoot(SearchableCompassUtils.isRoot(grailsDomainClass, searchableGrailsDomainClasses));
        return description;
    }

    protected Map getProperyMappings(GrailsDomainClassProperty[] mappableProperties, GrailsDomainClass grailsDomainClass, Collection searchableClasses) {
        Map mapping = new HashMap();
        for (int i = 0, max = mappableProperties.length; i < max; i++) {
            mapping.put(mappableProperties[i].getName(), getDefaultPropertyMapping(mappableProperties[i], grailsDomainClass, searchableClasses));
        }
        return mapping;
    }

    /**
     * Does the implementation handle th given "searchable" value type?
     * @param searchableValue a searchable value
     * @return true for Map and Boolean for this class
     */
    public boolean handlesSearchableValue(Object searchableValue) {
        return searchableValue instanceof Map || searchableValue instanceof Boolean;
    }
}
