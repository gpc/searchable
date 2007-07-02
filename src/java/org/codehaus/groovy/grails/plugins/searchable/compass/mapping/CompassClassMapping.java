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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a Compass searchable class mapping
 *
 * Note: a runtime Compass Mapping API is coming soon, so I'll replace this with that then
 *
 * @author Maurice Nicholson
 */
public class CompassClassMapping {
    private Class mappedClass;
    private String alias;
    private boolean poly = false;
    private String extend;
    private boolean root = true;
    private List propertyMappings = new ArrayList();
    private List constantMetaData = new ArrayList();

    public Class getMappedClass() {
        return mappedClass;
    }

    public void setMappedClass(Class mappedClass) {
        this.mappedClass = mappedClass;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isPoly() {
        return poly;
    }

    public void setPoly(boolean poly) {
        this.poly = poly;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public List getConstantMetaData() {
        return constantMetaData;
    }

    public void setConstantMetaData(List constantMetaData) {
        this.constantMetaData = constantMetaData;
    }

    public void addConstantMetaData(final String name, final Map attributes, final List values) {
        this.constantMetaData.add(new HashMap() {{
            put("name", name);
            put("attributes", attributes);
            put("values", values);
        }});
    }

    public List getPropertyMappings() {
        return propertyMappings;
    }

    public void setPropertyMappings(List propertyMappings) {
        this.propertyMappings = propertyMappings;
    }

    public void addPropertyMapping(CompassClassPropertyMapping propertyMapping) {
        this.propertyMappings.add(propertyMapping);
    }

    /**
     * Provide a useful String
     * @return String
     */
    public String toString() {
        return "CompassClassMapping: mappedClass=[" + mappedClass + "], root=[" + root + "], poly=[" + poly + "], extend=[" + extend + "], propertyMappings=[" + propertyMappings + "]";
    }
}
