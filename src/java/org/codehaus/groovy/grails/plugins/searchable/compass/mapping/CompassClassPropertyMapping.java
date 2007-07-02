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

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes a searchable class property mapping in Compass
 * 
 * @author Maurice Nicholson
 */
public class CompassClassPropertyMapping {
    public static final String PROPERTY = "property";
    public static final String REFERENCE = "reference";
    public static final String COMPONENT = "component";

    /**
     * The type of this mapping
     */
    private String type;

    /**
     * The property mapping attributes
     */
    private Map attributes = new HashMap();

    /**
     * The name of the class property
     */
    private String propertyName;

    /**
     * No arg constructor
     */
    public CompassClassPropertyMapping() {
        super();
    }

    /**
     * Constructor taking type constant
     * @param type REFERENCE, COMPONENT or PROPERTY
     * @param propertyName the property name
     */
    protected CompassClassPropertyMapping(String type, String propertyName) {
        Assert.notNull(type, "type cannot be null");
        Assert.notNull(propertyName, "propertyName cannot be null");
        this.type = type;
        this.propertyName = propertyName;
    }

    protected CompassClassPropertyMapping(String type, String propertyName, Map attributes) {
        this(type, propertyName);
        Assert.notNull(attributes, "attributes cannot be null");
        this.attributes = attributes;
    }

    /**
     * Factory-style constructor for type-safe property type
     * @param propertyName the name of the mapped property
     * @return a new CompassClassPropertyMapping instance
     */
    public static CompassClassPropertyMapping getPropertyInstance(String propertyName) {
        return new CompassClassPropertyMapping(PROPERTY, propertyName);
    }

    /**
     * Factory-style constructor for type-safe property type
     * @param propertyName the name of the mapped property
     * @param attributes mapping attributes
     * @return a new CompassClassPropertyMapping instance
     */
    public static CompassClassPropertyMapping getPropertyInstance(String propertyName, Map attributes) {
        return new CompassClassPropertyMapping(PROPERTY, propertyName, attributes);
    }

    /**
     * Factory-style constructor for type-safe reference type
     * @param propertyName the name of the mapped property
     * @return a new CompassClassPropertyMapping instance
     */
    public static CompassClassPropertyMapping getReferenceInstance(String propertyName) {
        return new CompassClassPropertyMapping(REFERENCE, propertyName);
    }

    /**
     * Factory-style constructor for type-safe reference type
     * @param propertyName the name of the mapped property
     * @param attributes mapping attributes
     * @return a new CompassClassPropertyMapping instance
     */
    public static CompassClassPropertyMapping getReferenceInstance(String propertyName, Map attributes) {
        return new CompassClassPropertyMapping(REFERENCE, propertyName, attributes);
    }

    /**
     * Factory-style constructor for type-safe compoonent type
     * @param propertyName the name of the mapped property
     * @return a new CompassClassPropertyMapping instance
     */
    public static CompassClassPropertyMapping getComponentInstance(String propertyName) {
        return new CompassClassPropertyMapping(COMPONENT, propertyName);
    }

    /**
     * Factory-style constructor for type-safe compoonent type
     * @param propertyName the name of the mapped property
     * @param attributes mapping attributes
     * @return a new CompassClassPropertyMapping instance
     */
    public static CompassClassPropertyMapping getComponentInstance(String propertyName, Map attributes) {
        return new CompassClassPropertyMapping(COMPONENT, propertyName, attributes);
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

    public Map getAttributes() {
        return attributes;
    }

    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Provide a useful String
     * @return String
     */
    public String toString() {
        return "CompassClassPropertyMapping: type=[" + type + "], propertyName=[" + propertyName + "], attributes=[" + attributes + "]";
    }
}
