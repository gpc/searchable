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
import org.compass.core.Compass;

import java.util.Map;

import groovy.lang.Closure;

/**
 * A thread-safe Compass query builder
 *
 * @author Maurice Nicholson
 */
public interface SearchableCompassQueryBuilder {

    /**
     * Build and return a CompassQuery
     *
     * @param compass Compass instance
     * @param compassQueryBuilder a query builder
     * @param query the query string
     * @param options query options @return the compass query
     */
    CompassQuery buildQuery(Compass compass, CompassQueryBuilder compassQueryBuilder, String query, Map options);

    /**
     * Build and return a CompassQuery
     *
     * @param compass Compass instance
     * @param compassQueryBuilder a query builder
     * @param options query options
     * @param closure a query-building closure @return the compass query
     */
    CompassQuery buildQuery(Compass compass, CompassQueryBuilder compassQueryBuilder, Map options, Closure closure);
}
