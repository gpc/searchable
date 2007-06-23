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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.util.ClassUtils;

import java.util.Map;

import groovy.lang.Closure;

/**
 * The default query builder strategy
 *
 * @author Maurice Nicholson
 */
public class DefaultSearchableCompassQueryBuilder implements SearchableCompassQueryBuilder {
    private static final Log LOG = LogFactory.getLog(DefaultSearchableCompassQueryBuilder.class);

    private SearchableCompassQueryBuilder stringQueryBuilder = new DefaultStringQuerySearchableCompassQueryBuilder();
    private Class closureQueryBuilderClass;
    private SearchableCompassQueryBuilderOptionsHelper[] optionHelpers = new SearchableCompassQueryBuilderOptionsHelper[] {
        new SearchableCompassQueryBuilderClassOptionHelper(),
        new SearchableCompassQueryBuilderSortOptionHelper()
    };

    public DefaultSearchableCompassQueryBuilder() {
        String name = "org.codehaus.groovy.grails.plugins.searchable.compass.search.GroovyCompassQueryBuilder";
        try {
            closureQueryBuilderClass = ClassUtils.forName(name);
        } catch (Exception ex) {
            LOG.error("Class not found [" + name + "]", ex);
            throw new IllegalStateException("Class not found [" + name + "]");
        }
    }

    public CompassQuery buildQuery(Compass compass, CompassQueryBuilder compassQueryBuilder, String query, Map options) {
        CompassQuery compassQuery = stringQueryBuilder.buildQuery(compass, compassQueryBuilder, query, options);
        return applyOptions(compass, compassQueryBuilder, compassQuery, options);
    }

    public CompassQuery buildQuery(Compass compass, CompassQueryBuilder compassQueryBuilder, Map options, Closure closure) {
        Object closureQueryBuilder = InvokerHelper.invokeConstructorOf(closureQueryBuilderClass, compassQueryBuilder);
        CompassQuery compassQuery = (CompassQuery) InvokerHelper.invokeMethod(closureQueryBuilder, "buildQuery", closure);
        return applyOptions(compass, compassQueryBuilder, compassQuery, options);
    }

    protected CompassQuery applyOptions(Compass compass, CompassQueryBuilder compassQueryBuilder, CompassQuery compassQuery, Map options) {
        for (int i = 0, max = optionHelpers.length; i < max; i++) {
            compassQuery = optionHelpers[i].applyOptions(compass, compassQueryBuilder, compassQuery, options);
        }
        return compassQuery;
    }
}
