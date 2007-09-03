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
package org.codehaus.groovy.grails.plugins.searchable;

import org.codehaus.groovy.grails.commons.*;
import org.codehaus.groovy.grails.plugins.searchable.compass.CompassGrailsDomainClassSearchabilityEvaluatorFactory;
import org.codehaus.groovy.grails.plugins.searchable.util.PatternUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import java.util.*;
import java.util.regex.Pattern;

/**
 * General purpose utilities for the Grails Searchable Plugin
 *
 * @author Maurice Nicholson
 */
public class SearchableUtils {
    public static final String SEARCHABLE_PROPERTY_NAME = "searchable";
    public static final String ONLY = "only";
    public static final String EXCEPT = "except";

    /**
     * Are there "searchable" domain classes?
     *
     * @param application the GrailsApplication
     * @param resourceLoader
     * @return true if there are "searchable" domain classes
     */
    public static boolean hasSearchableGrailsDomainClasses(GrailsApplication application, ResourceLoader resourceLoader) {
        return getSearchableGrailsDomainClasses(application, resourceLoader).length > 0;
    }

    /**
     * Get the "searchable" Grails domain classes
     *
     * @param application the GrailsApplication
     * @param resourceLoader
     * @return the searchable domain classes, which may be empty
     */
    public static GrailsDomainClass[] getSearchableGrailsDomainClasses(GrailsApplication application, ResourceLoader resourceLoader) {
        Assert.notNull(application, "GrailsApplication cannot be null");
        List domainClasses = new ArrayList();
        for (int i = 0, max = application.getArtefacts(DomainClassArtefactHandler.TYPE).length; i < max; i++) {
            GrailsDomainClass grailsDomainClass = (GrailsDomainClass) application.getArtefacts(DomainClassArtefactHandler.TYPE)[i];
            if (isSearchable(grailsDomainClass, resourceLoader)) {
                domainClasses.add(grailsDomainClass);
            }
        }
        return (GrailsDomainClass[]) domainClasses.toArray(new GrailsDomainClass[domainClasses.size()]);
    }

    /**
     * Is the given GrailsDomainClass "searchable"?
     *
     * @param grailsDomainClass domain class
     * @param resourceLoader
     * @return true if it declares a "searchable" property not equal to false
     */
    public static boolean isSearchable(GrailsDomainClass grailsDomainClass, ResourceLoader resourceLoader) {
        GrailsDomainClassSearchabilityEvaluator[] searchabilityEvaluators = CompassGrailsDomainClassSearchabilityEvaluatorFactory.getGrailsDomainClassSearchabilityEvaluators(resourceLoader);
        for (int i = 0; i < searchabilityEvaluators.length; i++) {
            if (searchabilityEvaluators[i].isSearchable(grailsDomainClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the "searchable" Grails domain classes and their searchable property value
     *
     * @param application the GrailsApplication
     * @return a Map from GrailsDomainClass instance to "searchable" property value
     */
    public static Map getSearchableGrailsDomainClassesMap(GrailsApplication application) {
        Assert.notNull(application, "GrailsApplication cannot be null");
        Map searchables = new HashMap();
        for (Iterator iter = getGrailsDomainClasses(application).iterator(); iter.hasNext(); ) {
            GrailsDomainClass grailsDomainClass = (GrailsDomainClass) iter.next();
            Object searchable = getSearchablePropertyValue(grailsDomainClass);
            if (searchable != null) {
                searchables.put(grailsDomainClass, searchable);
            }
        }
        return searchables;
    }

    /**
     * Get the given domain class's searchable property value, if any
     *
     * @param grailsDomainClass the Grails domain class
     * @return the searchable property value, or null
     */
    public static Object getSearchablePropertyValue(GrailsDomainClass grailsDomainClass) {
        return GrailsClassUtils.getStaticPropertyValue(grailsDomainClass.getClazz(), SEARCHABLE_PROPERTY_NAME);
    }

    /**
     * Gets the GrailsDomainClass artefacts from the aplication
     * @param application the Grails app
     * @return the Lit of domain classes
     */
    public static Collection getGrailsDomainClasses(GrailsApplication application) {
        Assert.notNull(application, "GrailsApplication cannot be null");
        Set domainClasses = new HashSet();
        for (int i = 0, max = application.getArtefacts(DomainClassArtefactHandler.TYPE).length; i < max; i++) {
            GrailsDomainClass grailsDomainClass = (GrailsDomainClass) application.getArtefacts(DomainClassArtefactHandler.TYPE)[i];
            domainClasses.add(grailsDomainClass);
        }
        return domainClasses;
    }

    /**
     * Returns the class type of the searchable property
     * @param grailsDomainClass
     * @param propertyName
     * @param searchableGrailsDomainClasses
     * @return
     */
    public static Class getSearchablePropertyAssociatedClass(GrailsDomainClass grailsDomainClass, String propertyName, Collection searchableGrailsDomainClasses) {
        Assert.notNull(grailsDomainClass, "grailsDomainClass cannot be null");
        Assert.notNull(propertyName, "propertyName cannot be null");
        return getSearchablePropertyAssociatedClass(grailsDomainClass.getPropertyByName(propertyName), searchableGrailsDomainClasses);
    }

    /**
     * Returns the class type of the searchable property
     * @param property
     * @param searchableGrailsDomainClasses
     * @return
     */
    public static Class getSearchablePropertyAssociatedClass(GrailsDomainClassProperty property, Collection searchableGrailsDomainClasses) {
        Assert.notNull(property, "property cannot be null");
        Assert.notNull(property.getDomainClass(), "grailsDomainClass cannot be null");
        Class propertyType = property.getType();
        Collection classes = getClasses(searchableGrailsDomainClasses);
        if (classes.contains(propertyType)) {
            return propertyType;
        }
        propertyType = property.getDomainClass().getRelatedClassType(property.getName());
        if (propertyType != null && classes.contains(propertyType)) {
            return propertyType;
        }
        return null;
    }

    /**
     * Returns a collection of user classes for the given GrailsDomainClass instances
     * @param grailsDomainClasses a collection of GrailsDomainClass instances
     * @return a collection of user classes
     */
    public static Collection getClasses(Collection grailsDomainClasses) {
        Assert.notNull(grailsDomainClasses, "grailsDomainClasses cannot be null");
        Set classes = new HashSet();
        for (Iterator iter = grailsDomainClasses.iterator(); iter.hasNext(); ) {
            classes.add(((GrailsDomainClass) iter.next()).getClazz());
        }
        return classes;
    }

    /**
     * Should the named property be included in the mapping, according to the value of "searchable"?
     * @param propertyName
     * @param searchable
     * @return true if included
     */
    public static boolean isIncludedProperty(String propertyName, Object searchable) {
        if (searchable instanceof Boolean && searchable.equals(Boolean.TRUE)) {
            return true;
        }
        if (!(searchable instanceof Map)) {
            return false;
        }
        Object only = ((Map) searchable).get(ONLY);
        if (only != null) {
            return isOrContains(propertyName, only);
        }
        return !isOrContains(propertyName, ((Map) searchable).get(EXCEPT));
    }

    private static boolean isOrContains(String thing, final Object value) {
        Collection values = null;
        if (value instanceof Collection) {
            values = (Collection) value;
        } else {
            values = new HashSet() {{
                add(value);
            }};
        }
        for (Iterator iter = values.iterator(); iter.hasNext(); ) {
            String v = (String) iter.next();
            if (!PatternUtils.hasWildcards(v)) {
                if (v.equals(thing)) {
                    return true;
                }
            } else {
                Pattern pattern = PatternUtils.makePatternFromWilcardString(v);
                if (pattern.matcher(thing).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
}
