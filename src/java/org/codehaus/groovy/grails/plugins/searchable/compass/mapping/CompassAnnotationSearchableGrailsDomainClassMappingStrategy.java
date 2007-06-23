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
import org.codehaus.groovy.grails.plugins.searchable.compass.config.CompassXmlConfigurationSearchableCompassConfigurator;
import org.compass.core.config.CompassConfiguration;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;
import java.lang.reflect.Method;

/**
 * Maps searchable domain classes that are annotated with Compass's native @Searchable annotations.
 *
 * @author Maurice Nicholson
 */
public class CompassAnnotationSearchableGrailsDomainClassMappingStrategy implements SearchableGrailsDomainClassMappingStrategy {
    private static final String SEARCHABLE_ANNOTATION_CLASS_NAME = "org.compass.annotations.Searchable";
    private static final String ANNOTATIONS_CONFIGURATION_CLASS_NAME = "org.compass.annotations.config.CompassAnnotationsConfiguration";
    private static final Log LOG = LogFactory.getLog(CompassAnnotationSearchableGrailsDomainClassMappingStrategy.class);
    private static boolean annotationsAvailable = getSearchableAnnotationClass() != null;

    /**
     * Does this strategy handle the given domain class (and it's respective mapping type)
     *
     * @param grailsDomainClass the Grails domain class
     * @return true if the mapping of the class can be handled by this strategy
     */
    public boolean isSearchable(GrailsDomainClass grailsDomainClass) {
        if (!annotationsAvailable) {
            return false;
        }
        Class clazz = grailsDomainClass.getClazz();
        Method getter = findGetAnnotationMethod();
        if (getter == null) {
            return false;
        }
        return ReflectionUtils.invokeMethod(getter, clazz, new Class[] { getSearchableAnnotationClass() }) != null;
    }

    /**
     * Configure the Mapping in the CompassConfiguration for the given domain class
     *
     * @param compassConfiguration          the CompassConfiguration instance
     * @param configurationContext          a configuration context, for flexible parameter passing
     * @param grailsDomainClass             the Grails domain class to map
     * @param searchableGrailsDomainClasses all searchable domain classes
     */
    public void configureMapping(CompassConfiguration compassConfiguration, Map configurationContext, GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses) {
        Assert.isTrue(annotationsAvailable, "Annotations must be available");
        Assert.notNull(compassConfiguration, "compassConfiguration cannot be null");
        Assert.notNull(configurationContext, "configurationContext cannot be null");

        if (!configurationContext.containsKey(CompassXmlConfigurationSearchableCompassConfigurator.CONFIGURED)) {
            Class compassAnnotationsConfigurationClass = getCompassAnnotationConfigurationClass();
            Assert.isTrue(compassAnnotationsConfigurationClass.isAssignableFrom(compassConfiguration.getClass()), "compassConfiguration must be an instance of CompassAnnotationsConfiguration");
            compassConfiguration.addClass(grailsDomainClass.getClazz());
        }
    }

    /**
     * Get this strategy's name
     *
     * @return name
     */
    public String getName() {
        return "Compass annotations";
    }

    private Method findGetAnnotationMethod() {
        return ReflectionUtils.findMethod(Class.class, "getAnnotation", new Class[] { Class.class });
    }

    private static Class getSearchableAnnotationClass() {
        try {
            return ClassUtils.forName(SEARCHABLE_ANNOTATION_CLASS_NAME);
        } catch (Throwable ex) {
            LOG.debug("Annotations unavailable");
        }
        return null;
    }

    public Class getCompassAnnotationConfigurationClass() {
        try {
            return ClassUtils.forName(ANNOTATIONS_CONFIGURATION_CLASS_NAME);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Cannot find Compass annotation class [" + ANNOTATIONS_CONFIGURATION_CLASS_NAME + "]: " + ex);
        }
    }
}
