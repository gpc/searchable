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
package grails.plugin.searchable.internal.compass.mapping;

import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.springframework.util.Assert;

import java.util.*;

/**
 * A searchable GrailsDomainClass class mapper for Compass
 *
 * @author Maurice Nicholson
 */
public class CompositeSearchableGrailsDomainClassCompassClassMapper extends AbstractSearchableGrailsDomainClassCompassClassMapper implements SearchableGrailsDomainClassCompassClassMapper {
    private SearchableGrailsDomainClassCompassClassMapper[] classMappers;

    /**
     * Get the property mappings for the given GrailsDomainClass
     * @param grailsDomainClass the Grails domain class
     * @param searchableGrailsDomainClasses a collection of searchable GrailsDomainClass instances
     * @param searchableValue the searchable value: true|false|Map|Closure
     * @param excludedProperties a List of properties NOT to map; may be ignored by impl
     * @return a List of CompassClassPropertyMapping
     */
    public List getCompassClassPropertyMappings(GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses, Object searchableValue, List excludedProperties) {
        SearchableGrailsDomainClassCompassClassMapper classMapper = getCompassClassMapperForSearchableValue(searchableValue);
        Assert.notNull(classMapper, "No class mapper found for class [" + grailsDomainClass.getClazz().getName() + "]. Does the class declare a searchable property?");
        return classMapper.getCompassClassPropertyMappings(grailsDomainClass, searchableGrailsDomainClasses, searchableValue, excludedProperties);
    }

    /**
     * Get the CompassClassMapping  for the given GrailsDomainClass and "searchable" value
     * @param grailsDomainClass the Grails domain class
     * @param searchableGrailsDomainClasses a collection of searchable GrailsDomainClass instances
     * @param searchableValue the searchable value: true|false|Map|Closure
     * @param excludedProperties a List of properties NOT to map; may be ignored by impl
     * @return the CompassClassMapping
     */
    public CompassClassMapping getCompassClassMapping(GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses, Object searchableValue, List excludedProperties) {
        SearchableGrailsDomainClassCompassClassMapper classMapper = getCompassClassMapperForSearchableValue(searchableValue);
        Assert.notNull(classMapper, "No class mapper found for class [" + grailsDomainClass.getClazz().getName() + "]. Does the class declare a searchable property?");
        return classMapper.getCompassClassMapping(grailsDomainClass, searchableGrailsDomainClasses, searchableValue, excludedProperties);
    }

    /**
     * Does the implementation handle a "searchable" value of the given type?
     * @param searchableValue the searchable value
     * @return true if this implementation understands the type
     */
    public boolean handlesSearchableValue(Object searchableValue) {
        return getCompassClassMapperForSearchableValue(searchableValue) != null;
    }

    private SearchableGrailsDomainClassCompassClassMapper getCompassClassMapperForSearchableValue(Object searchableValue) {
        for (int i = 0; i < classMappers.length; i++) {
            if (classMappers[i].handlesSearchableValue(searchableValue)) {
                return classMappers[i];
            }
        }
        return null;
    }

    public SearchableGrailsDomainClassCompassClassMapper[] getSearchableGrailsDomainClassCompassMappingDescriptionProviders() {
        return classMappers;
    }

    public void setSearchableGrailsDomainClassCompassMappingDescriptionProviders(SearchableGrailsDomainClassCompassClassMapper[] classMappers) {
        this.classMappers = classMappers;
    }

}
