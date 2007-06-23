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
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils;

import java.util.Map;

/**
 * Helper dealing with a "class" query builder query option
 *
 * @author Maurice Nicholson
 */
public class SearchableCompassQueryBuilderClassOptionHelper implements SearchableCompassQueryBuilderOptionsHelper {

    public CompassQuery applyOptions(Compass compass, CompassQueryBuilder compassQueryBuilder, CompassQuery compassQuery, Map options) {
        Class clazz = (Class) SearchableUtils.getOption("class", options, null);
        if (clazz != null) {
            compassQuery.setAliases(new String[] {SearchableCompassUtils.getMappingAlias(compass, clazz)});
        }
        return compassQuery;
    }
}
