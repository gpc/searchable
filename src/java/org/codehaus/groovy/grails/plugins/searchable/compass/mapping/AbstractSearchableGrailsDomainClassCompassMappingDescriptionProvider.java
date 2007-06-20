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
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.springframework.util.ClassUtils;

import java.util.*;

/**
 * @author Maurice Nicholson
 */
public abstract class AbstractSearchableGrailsDomainClassCompassMappingDescriptionProvider implements SearchableGrailsDomainClassCompassMappingDescriptionProvider {
    private static final Log LOG = LogFactory.getLog(AbstractSearchableGrailsDomainClassCompassMappingDescriptionProvider.class);

    private GrailsDomainClassPropertyMappingStrategyFactory domainClassPropertyMappingStrategyFactory;

    protected Map getDefaultPropertyMapping(GrailsDomainClassProperty property, GrailsDomainClass grailsDomainClass, Collection searchableClasses) {
        return domainClassPropertyMappingStrategyFactory.getGrailsDomainClassPropertyMappingStrategy(property, searchableClasses).getMapping();
    }

    protected GrailsDomainClassProperty[] getMappableProperties(GrailsDomainClass grailsDomainClass, Object searchableValue, Collection searchableGrailsDomainClasses, final List excludedProperties) {
        boolean defaultExcludes = false;
        if (searchableValue instanceof Boolean) {
            if (searchableValue.equals(Boolean.FALSE)) {
                return null;
            }
            searchableValue = new HashMap() {{
                put("except", excludedProperties);
            }};
            defaultExcludes = true;
        }

        Class mappedClass = grailsDomainClass.getClazz();
        List properties = new ArrayList();
        for (int i = 0, max = grailsDomainClass.getProperties().length; i < max; i++) {
            GrailsDomainClassProperty property = grailsDomainClass.getProperties()[i];
            String propertyName = property.getName();
            if (propertyName.equals("id")) { // TODO refactor with specific id mapping
                continue;
            }
            if (!SearchableUtils.isIncludedProperty(propertyName, searchableValue)) {
                LOG.debug(
                    "Not mapping [" + ClassUtils.getShortName(mappedClass) + "." + propertyName + "] because of " +
                    (defaultExcludes ? "default property exclusions" : "specified only/except rule")
                );
                continue;
            }
            if (domainClassPropertyMappingStrategyFactory.getGrailsDomainClassPropertyMappingStrategy(property, searchableGrailsDomainClasses) == null) {
                continue;
            }
            LOG.debug("Mapping [" + ClassUtils.getShortName(mappedClass) + "." + propertyName + "]");
            properties.add(property);
        }
        return (GrailsDomainClassProperty[]) properties.toArray(new GrailsDomainClassProperty[properties.size()]);
    }

    public GrailsDomainClassPropertyMappingStrategyFactory getDomainClassPropertyMappingStrategyFactory() {
        return domainClassPropertyMappingStrategyFactory;
    }

    public void setDomainClassPropertyMappingStrategyFactory(GrailsDomainClassPropertyMappingStrategyFactory domainClassPropertyMappingStrategyFactory) {
        this.domainClassPropertyMappingStrategyFactory = domainClassPropertyMappingStrategyFactory;
    }
}
