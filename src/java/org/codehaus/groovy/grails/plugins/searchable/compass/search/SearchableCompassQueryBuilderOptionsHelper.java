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
package org.codehaus.groovy.grails.plugins.searchable.compass.search;

import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryBuilder;

import java.util.Map;

/**
 * A helper for applying query options
 *
 * @author Maurice Nicholson
 */
public interface SearchableCompassQueryBuilderOptionsHelper {

    /**
     * Apply the options and return the (possibly new) query
     *
     * @param compassQueryBuilder a CompassQueryBuilder
     * @param compassQuery the query to apply options to
     * @param options the options to apply, if any @return a (maybe new) query with options applied
     */
    CompassQuery applyOptions(CompassQueryBuilder compassQueryBuilder, CompassQuery compassQuery, Map options);
}
