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

import java.util.Map;

/**
 * Describes a class mapped in Compass
 *
 * Note: a runtime Compass Mapping API is coming soon, so I'll replace this with that then
 *
 * @author Maurice Nicholson
 */
public class CompassMappingDescription {
    private Class mappedClass;
    private boolean root = true;
    private Map properties;

    public Class getMappedClass() {
        return mappedClass;
    }

    public void setMappedClass(Class mappedClass) {
        this.mappedClass = mappedClass;
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public String toString() {
        return "CompassMappingDescription: mappedClass=[" + mappedClass + "], root=[" + root + "], properties=[" + properties + "]";
    }
}
