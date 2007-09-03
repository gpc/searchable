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
package org.codehaus.groovy.grails.plugins.searchable.compass.support;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Maurice Nicholson
 */
public abstract class SearchableMethodUtils {

    /**
     * Get the options Map from the given argument array
     * @param args the given array of arguments, may not be null, may be empty
     * @param defaultOptions the default options, to be merged with user options, may be null
     * @return a Map of options, never null
     */
    public static Map getOptionsArgument(Object[] args, Map defaultOptions) {
        Assert.notNull(args, "args cannot be null");
        Map options = null;
        for (int i = 0, max = args.length; i < max; i++) {
            if (args[i] instanceof Map) {
                options = (Map) args[i];
                break;
            }
        }
        Map merged = new HashMap();
        if (defaultOptions != null) {
            merged.putAll(defaultOptions);
        }
        if (options != null) {
            merged.putAll(options);
        }
        return merged;
    }

}
