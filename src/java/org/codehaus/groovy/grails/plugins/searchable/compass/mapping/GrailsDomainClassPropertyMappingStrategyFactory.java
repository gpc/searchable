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
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * @author Maurice Nicholson
 */
public class GrailsDomainClassPropertyMappingStrategyFactory {
    private static final String PROPERTY = "property";
    private static final String REFERENCE = "reference";
    private static final String COMPONENT = "component";

    private Map defaultFormats;
    private CompassConverterLookupHelper converterLookupHelper;

    public GrailsDomainClassPropertyMappingStrategy getGrailsDomainClassPropertyMappingStrategy(GrailsDomainClassProperty domainClassProperty, Collection searchableGrailsDomainClasses) {
        Class propertyType = domainClassProperty.getType();
        if (Map.class.isAssignableFrom(propertyType) && !domainClassProperty.isAssociation()) {
            return new StringMapGrailsDomainClassPropertyMappingStrategy();
        }
        if (converterLookupHelper.hasConverter(propertyType)) {
            String format = null;
            if (defaultFormats != null) {
                format = (String) defaultFormats.get(propertyType); // TODO use class distance algorithm
            }
            return new PropertyGrailsDomainClassPropertyMappingStrategy(format);
        }
        propertyType = SearchableUtils.getSearchablePropertyAssociatedClass(domainClassProperty, searchableGrailsDomainClasses);
        if (propertyType == null) {
            return null;
        }
        if (domainClassProperty.isEmbedded()) {
            return new ComponentGrailsDomainClassPropertyMappingStrategy(propertyType);
        }
        if (Map.class.isAssignableFrom(propertyType) && domainClassProperty.isAssociation()) {
            return new ReferenceMapGrailsDomainClassPropertyMappingStrategy(propertyType);
        }
        return new ReferenceGrailsDomainClassPropertyMappingStrategy(propertyType);
    }

    public static abstract class AbstractGrailsDomainClassPropertyMappingStrategy implements GrailsDomainClassPropertyMappingStrategy {
        private String type;

        public AbstractGrailsDomainClassPropertyMappingStrategy(String type) {
            Assert.notNull(type, "type cannot be null");
            this.type = type;
        }

        /**
         * Is the strategy a searchable property?
         * @return true if this is a searchable property strategy
         */
        public boolean isProperty() {
            return type.equals(PROPERTY);
        }

        /**
         * Is the strategy a searchable reference?
         * @return true if this is a searchable reference strategy
         */
        public boolean isReference() {
            return type.equals(REFERENCE);
        }

        /**
         * Is the strategy a searchable component?
         * @return true if this is a searchable component strategy
         */
        public boolean isComponent() {
            return type.equals(COMPONENT);
        }
    }

    public static class PropertyGrailsDomainClassPropertyMappingStrategy extends AbstractGrailsDomainClassPropertyMappingStrategy implements GrailsDomainClassPropertyMappingStrategy {
        private String format;

        public PropertyGrailsDomainClassPropertyMappingStrategy(String format) {
            super(PROPERTY);
            this.format = format;
        }

        public Map getMapping() {
            Map mapping = new HashMap();
            Object propertyValue = Boolean.TRUE;
            if (format != null) {
                propertyValue = new HashMap() {{
                    put("format", format);
                }};
            }
            mapping.put("property", propertyValue);
            return mapping;
        }
    }

    public static class ComponentGrailsDomainClassPropertyMappingStrategy extends AbstractGrailsDomainClassPropertyMappingStrategy implements GrailsDomainClassPropertyMappingStrategy {
        private Class propertyType;

        public ComponentGrailsDomainClassPropertyMappingStrategy(Class propertyType) {
            super(COMPONENT);
            this.propertyType = propertyType;
        }

        public Map getMapping() {
            Map mapping = new HashMap();
            mapping.put("component", new HashMap() {{
                put("refAlias", SearchableCompassUtils.getDefaultAlias(propertyType));
            }});
            return mapping;
        }
    }

    public static class ReferenceGrailsDomainClassPropertyMappingStrategy extends AbstractGrailsDomainClassPropertyMappingStrategy implements GrailsDomainClassPropertyMappingStrategy {
        private Class propertyType;

        public ReferenceGrailsDomainClassPropertyMappingStrategy(Class propertyType) {
            super(REFERENCE);
            this.propertyType = propertyType;
        }

        public Map getMapping() {
            Map mapping = new HashMap();
            mapping.put("reference", new HashMap() {{
                put("refAlias", SearchableCompassUtils.getDefaultAlias(propertyType));
            }});
            return mapping;
        }
    }

    public static class StringMapGrailsDomainClassPropertyMappingStrategy extends AbstractGrailsDomainClassPropertyMappingStrategy implements GrailsDomainClassPropertyMappingStrategy {
        public StringMapGrailsDomainClassPropertyMappingStrategy() {
            super(PROPERTY);
        }

        public Map getMapping() {
            Map mapping = new HashMap();
            mapping.put("property", new HashMap() {{
                put("converter", StringMapConverter.CONVERTER_NAME);
                put("managedId", Boolean.FALSE);
            }});
            return mapping;
        }
    }

    public static class ReferenceMapGrailsDomainClassPropertyMappingStrategy extends ReferenceGrailsDomainClassPropertyMappingStrategy implements GrailsDomainClassPropertyMappingStrategy {

        public ReferenceMapGrailsDomainClassPropertyMappingStrategy(Class propertyType) {
            super(propertyType);
        }

//        public Map getMapping() {
//            Map mapping = super.getMapping();
//            Map options = (Map) mapping.get("reference");
//            return mapping;
//        }
    }

    public void setDefaultFormats(Map defaultFormats) {
        this.defaultFormats = defaultFormats;
    }

    public void setConverterLookupHelper(CompassConverterLookupHelper converterLookupHelper) {
        this.converterLookupHelper = converterLookupHelper;
    }
}
