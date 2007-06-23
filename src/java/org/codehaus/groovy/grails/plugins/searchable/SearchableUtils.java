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

import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.plugins.searchable.compass.CompassGrailsDomainClassSearchabilityEvaluatorFactory;
import org.codehaus.groovy.runtime.InvokerHelper;
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
        return grailsDomainClass.getPropertyValue(SEARCHABLE_PROPERTY_NAME);
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
            if (!hasWildcards(v)) {
                if (v.equals(thing)) {
                    return true;
                }
            } else {
                Pattern pattern = makePattern(v);
                if (pattern.matcher(thing).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Pattern makePattern(String string) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case '?':
                    buf.append(".");
                    break;
                case '*':
                    buf.append(".*");
                    break;
                default:
                    buf.append(c);
            }
        }
        return Pattern.compile(buf.toString());
    }

    private static boolean hasWildcards(String string) {
        return string.indexOf('*') > -1 || string.indexOf('?') > -1;
    }

    /**
     * Get the named boolean option from the map
     * @param name
     * @param options
     * @return
     * @throws Exception if no option
     */
    public static boolean getBooleanOption(String name, Map options) {
        Assert.notNull(name, "name cannot be null");
        Assert.notNull(options, "options name cannot be null");
        Assert.isTrue(options.containsKey(name), "options must contain an entry for key [" + name + "]");
        return getBooleanOption(name, options, null).booleanValue();
    }

    /**
     * Get a named boolean option or default
     * @param name
     * @param options
     * @param defaultValue
     * @return
     */
    public static Boolean getBooleanOption(String name, Map options, Boolean defaultValue) {
        return toBoolean(getOption(name, options, defaultValue));
    }

    /**
     * Get a mandatory integer option
     * @param name
     * @param options
     * @return
     */
    public static int getIntegerOption(String name, Map options) {
        Assert.notNull(name, "name cannot be null");
        Assert.notNull(options, "options name cannot be null");
        Assert.isTrue(options.containsKey(name), "options must contain an entry for key [" + name + "]");
        return getIntegerOption(name, options, null).intValue();
    }

    /**
     * Get a named integer option or default
     * @param name
     * @param options
     * @param defaultValue
     * @return
     */
    public static Integer getIntegerOption(String name, Map options, Integer defaultValue) {
        return toInteger(getOption(name, options, defaultValue));
    }

    /**
     * Get a named option or default
     * @param name
     * @param options
     * @param defaultValue
     * @return
     */
    public static Object getOption(String name, Map options, Object defaultValue) {
        if (options != null) {
            if (options.containsKey(name)) {
                return options.get(name);
            }
        }
        return defaultValue;
    }

    public static Boolean getBooleanOption(String name, Object[] args, Boolean defaultValue) {
        return toBoolean(getOption(name, args, defaultValue));
    }

    public static Integer getIntegerOption(String name, Object[] args, Integer defaultValue) {
        return toInteger(getOption(name, args, defaultValue));
    }

    public static Object getOption(String name, Object[] args, Object defaultValue) {
        if (args == null) return defaultValue;
        for (int i = 0, max = args.length; i < max; i++) {
            if (args[i] instanceof Map) {
                return getOption(name, (Map) args[i], defaultValue);
            }
        }
        return defaultValue;

    }

    private static Integer toInteger(Object value) {
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        }
        return (Integer) value;
    }

    private static Boolean toBoolean(Object value) {
        if (value instanceof String) {
            return Boolean.valueOf((String) value);
        }
        return (Boolean) value;
    }

    /**
     * Take a map of options which may include a page number and page size and
     * convert to start and size parameters
     *
     * @param options the options from the UI: page and size
     * @param defaultMax a max if not provided
     * @return an equivalent map of start and size options
     */
    public static Map convertPageAndSizeToOffsetAndMax(Map options, Integer defaultMax) {
        Assert.notNull(options, "options cannot be null");
        Assert.notNull(defaultMax, "defaultMax cannot be null");

        // Get/default options
        final Integer size = getIntegerOption("size", options, getIntegerOption("max", options, defaultMax));
        final Integer page = getIntegerOption("page", options, new Integer(1));

        // Set start and size
        return new HashMap() {{
            put("offset", new Integer(Math.max(0, (page.intValue() - 1) * size.intValue())));
            put("max", size);
        }};
    }

    /**
     * Gets the current 1-based page number for the given search args
     * @param args a Map like [total: 100, offset: 0, max: 10]
     * @return the 1-based page number
     */
    public static int getCurrentPage(Map args) {
        Assert.notNull(args, "args cannot be null");
        Integer offset = getIntegerOption("offset", args, new Integer(0));
        Integer max = getIntegerOption("max", args, new Integer(0));
        return (offset.intValue() / max.intValue()) + 1;
    }

    /**
     * Gets the total pages, rounded up, for the given search args
     * @param args a Map like [total: 100, offset: 0, max: 10]
     * @return the total pages
     */
    public static int getTotalPages(Map args) {
        Assert.notNull(args, "args cannot be null");
        Integer max = getIntegerOption("max", args, new Integer(0));
        Integer total = getIntegerOption("total", args, new Integer(0));
        return (int) Math.ceil(total.doubleValue() / max.doubleValue());
    }

    /**
     * Get the required 0-based offset to fetch a given page number and search args
     * @param page a page number
     * @param args a Map like [total: 100, offset: 0, max: 10]
     * @return the 0-based offset
     */
    public static int getOffsetForPage(final Integer page, Map args) {
        Map options = new HashMap() {{
            put("page", page);
        }};
        return ((Integer) convertPageAndSizeToOffsetAndMax(options, getIntegerOption("max", args, null)).get("offset")).intValue();
    }
}
