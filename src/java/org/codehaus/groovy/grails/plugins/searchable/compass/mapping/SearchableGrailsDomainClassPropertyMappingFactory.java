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

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils;
import org.codehaus.groovy.grails.plugins.searchable.compass.converter.CompassConverterLookupHelper;
import org.codehaus.groovy.grails.plugins.searchable.compass.converter.StringMapConverter;
import org.codehaus.groovy.grails.plugins.searchable.util.GrailsDomainClassUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.*;

/**
 * Builds Compass mapping for searchable class properties
 *
 * @author Maurice Nicholson
 */
public class SearchableGrailsDomainClassPropertyMappingFactory {
    private Map defaultFormats;
    private CompassConverterLookupHelper converterLookupHelper;

    public CompassClassPropertyMapping getGrailsDomainClassPropertyMapping(GrailsDomainClassProperty domainClassProperty, Collection searchableGrailsDomainClasses) {
        Class propertyType = domainClassProperty.getType();
        String propertyName = domainClassProperty.getName();

        if (Map.class.isAssignableFrom(propertyType) && !domainClassProperty.isAssociation()) {
            return stringMapCompassClassPropertyMapping(propertyName);
        }

        if (converterLookupHelper.hasConverter(propertyType)) {
            String format = null;
            if (defaultFormats != null) {
                format = (String) defaultFormats.get(propertyType); // TODO use class distance algorithm
            }
            return propertyCompassClassPropertyMapping(propertyName, format);
        }

        propertyType = SearchableUtils.getSearchablePropertyAssociatedClass(domainClassProperty, searchableGrailsDomainClasses);
        if (propertyType == null) {
            return null;
        }

        if (domainClassProperty.isEmbedded()) {
            return componentCompassClassPropertyMapping(propertyName, propertyType);
        }
        if (Map.class.isAssignableFrom(propertyType) && domainClassProperty.isAssociation()) {
            return referenceMapCompassClassPropertyMapping(propertyName, propertyType);
        }

        // TODO refactor this into main referenceCompassClassPropertyMapping() method
        // If it has sub-classes it may be polymorphic, so supply all ref-aliases
        Set hierarchyTypes = new HashSet();
        hierarchyTypes.add(propertyType);
        hierarchyTypes.addAll(GrailsDomainClassUtils.getClazzes(domainClassProperty.getDomainClass().getSubClasses()));
        List refAliases = new ArrayList();
        for (Iterator iter = hierarchyTypes.iterator(); iter.hasNext(); ) {
            Class type = (Class) iter.next();
            refAliases.add(CompassMappingUtils.getDefaultAlias(type));
        }
        return referenceCompassClassPropertyMapping(propertyName, DefaultGroovyMethods.join(refAliases, ", "));
    }

    public CompassClassPropertyMapping propertyCompassClassPropertyMapping(String propertyName, String format) {
        CompassClassPropertyMapping propertyMapping = CompassClassPropertyMapping.getPropertyInstance(propertyName);
        Map attributes = propertyMapping.getAttributes();
        if (format != null) {
            attributes.put("format", format);
        }
        return propertyMapping;
    }

    public CompassClassPropertyMapping componentCompassClassPropertyMapping(String propertyName, final Class propertyType) {
        CompassClassPropertyMapping propertyMapping = CompassClassPropertyMapping.getComponentInstance(propertyName);
        Map attributes = propertyMapping.getAttributes();
        attributes.put("refAlias", CompassMappingUtils.getDefaultAlias(propertyType));
        return propertyMapping;
    }

    public CompassClassPropertyMapping referenceCompassClassPropertyMapping(String propertyName, final String refAlias) {
        CompassClassPropertyMapping propertyMapping = CompassClassPropertyMapping.getReferenceInstance(propertyName);
        Map attributes = propertyMapping.getAttributes();
        attributes.put("refAlias", refAlias);
        return propertyMapping;
    }

    public CompassClassPropertyMapping stringMapCompassClassPropertyMapping(String propertyName) {
        CompassClassPropertyMapping propertyMapping = CompassClassPropertyMapping.getPropertyInstance(propertyName);
        Map attributes = propertyMapping.getAttributes();
        attributes.put("converter", StringMapConverter.CONVERTER_NAME);
        attributes.put("managedId", Boolean.FALSE);
        return propertyMapping;
    }

    public CompassClassPropertyMapping referenceMapCompassClassPropertyMapping(String propertyName, Class propertyType) {
        return null;
    }

    public void setDefaultFormats(Map defaultFormats) {
        this.defaultFormats = defaultFormats;
    }

    public void setConverterLookupHelper(CompassConverterLookupHelper converterLookupHelper) {
        this.converterLookupHelper = converterLookupHelper;
    }
}
