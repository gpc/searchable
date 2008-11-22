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

package org.codehaus.groovy.grails.plugins.searchable.test;

import org.springframework.core.OverridingClassLoader;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Maurice Nicholson
 */
public class SearchableFunctionalTestCaseClassLoader extends OverridingClassLoader {
    private Map preloadedClasses = new HashMap();

    public SearchableFunctionalTestCaseClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    protected boolean isEligibleForOverriding(String className) {
        return preloadedClasses.containsKey(className) || className.startsWith("org.codehaus.groovy.grails.plugins.searchable.test.");
//        return className.startsWith("org.codehaus.groovy.grails.plugins.searchable.test.");
    }

    protected Class loadClassForOverriding(String name) throws ClassNotFoundException {
        if (preloadedClasses.containsKey(name)) {
            return (Class) preloadedClasses.get(name);
        }
        return super.loadClassForOverriding(name);
    }

    public void addPreloadedClass(String name, Class clazz) {
        preloadedClasses.put(name, clazz);
    }
}
