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
import org.springframework.util.Assert;

import java.util.*;

/**
 * @author Maurice Nicholson
 */
public class SimpleSearchableGrailsDomainClassCompassClassMapper extends AbstractSearchableGrailsDomainClassCompassClassMapper implements SearchableGrailsDomainClassCompassClassMapper {

    /**
     * Get the property mappings for the given GrailsDomainClass
     * @param grailsDomainClass the Grails domain class
     * @param searchableGrailsDomainClasses a collection of searchable GrailsDomainClass instances
     * @param searchableValue the searchable value: true|false|Map|Closure
     * @param excludedProperties a List of properties NOT to map; may be ignored by impl
     * @return a List of CompassClassPropertyMapping
     */
    public List getCompassClassPropertyMappings(GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses, Object searchableValue, List excludedProperties) {
        Assert.notNull(searchableValue, "searchableValue cannot be null");
        Assert.isTrue(searchableValue instanceof Boolean || searchableValue instanceof Map, "[" + grailsDomainClass.getClazz().getName() + ".searchable] must be either a boolean, Map or closure (not [" + searchableValue.getClass().getName() + "]");

        GrailsDomainClassProperty[] mappableProperties = SearchableGrailsDomainClassCompassMappingUtils.getMappableProperties(grailsDomainClass, searchableValue, searchableGrailsDomainClasses, excludedProperties, getDomainClassPropertyMappingStrategyFactory());
        if (mappableProperties == null) {
            return null;
        }
        return getProperyMappings(mappableProperties, searchableGrailsDomainClasses);
    }

    private List getProperyMappings(GrailsDomainClassProperty[] mappableProperties, Collection searchableClasses) {
        List propertyMappings = new ArrayList();
        for (int i = 0, max = mappableProperties.length; i < max; i++) {
            propertyMappings.add(getDefaultPropertyMapping(mappableProperties[i], searchableClasses));
        }
        return propertyMappings;
    }

    /**
     * Get the CompassClassMapping  for the given GrailsDomainClass and "searchable" value
     * @param grailsDomainClass the Grails domain class
     * @param searchableGrailsDomainClasses a collection of searchable GrailsDomainClass instances
     * @param searchableValue the searchable value: true|false|Map in this case
     * @param excludedProperties a List of properties NOT to map; only used for "true" searchable value
     * @return the CompassClassMapping
     */
    public CompassClassMapping getCompassClassMapping(GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses, Object searchableValue, List excludedProperties) {
        List propertyMappings = getCompassClassPropertyMappings(grailsDomainClass, searchableGrailsDomainClasses, searchableValue, excludedProperties);
        if (propertyMappings == null) {
            return null;
        }
        return SearchableGrailsDomainClassCompassMappingUtils.buildCompassClassMapping(grailsDomainClass, searchableGrailsDomainClasses, propertyMappings, null);
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
