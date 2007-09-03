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
import org.compass.core.Compass;
import org.compass.core.spi.InternalCompass;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Maurice Nicholson
 */
public class CompassMappingUtils {

    /**
     * Get the Compass alias for the given Class
     *
     * @param clazz the class
     * @return the Compass alias
     */
    public static String getDefaultAlias(Class clazz) {
        Assert.notNull(clazz, "clazz cannot be null");
        String alias = clazz.getName();
        if (alias.indexOf(".") != -1) {
            alias = alias.substring(alias.lastIndexOf(".") + 1, alias.length());
        }
        return "ALIAS" + alias + "ALIAS";
    }

    /**
     * Get the alias used to map the class in Compass
     * @param compass Compass
     * @param clazz the class whose alias to look up
     * @return the alias
     */
    public static String getMappingAlias(Compass compass, Class clazz) {
        return ((InternalCompass) compass).getMapping().findRootMappingByClass(clazz).getAlias();
    }

    /**
     * Get the mapping aliases for the given user-defined domain classes any
     * @param compass Compass instance
     * @param clazzes the user-defined domain classes
     * @return the Compass aliases for the hierarchy
     */
    public static String[] getMappingAliases(Compass compass, Collection clazzes) {
        Set aliases = new HashSet();
        for (Iterator iter = clazzes.iterator(); iter.hasNext(); ) {
            Class clazz = (Class) iter.next();
            aliases.add(getMappingAlias(compass, clazz));
        }
        return (String[]) aliases.toArray(new String[aliases.size()]);
    }
}
