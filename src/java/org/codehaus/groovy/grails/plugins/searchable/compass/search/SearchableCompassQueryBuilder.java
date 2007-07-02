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
import org.compass.core.CompassSession;

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
     * @param compassSession the current Compass session
     * @param options query options
     * @param query the query object: either a String or Closure @return the compass query
     */
    CompassQuery buildQuery(CompassSession compassSession, Map options, Object query);
}
