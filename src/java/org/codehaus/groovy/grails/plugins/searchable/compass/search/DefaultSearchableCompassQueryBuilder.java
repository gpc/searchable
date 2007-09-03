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

import groovy.lang.Closure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.compass.core.Compass;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * The default query builder strategy
 *
 * @author Maurice Nicholson
 */
public class DefaultSearchableCompassQueryBuilder extends AbstractSearchableCompassQueryBuilder implements SearchableCompassQueryBuilder {
    private static final Log LOG = LogFactory.getLog(DefaultSearchableCompassQueryBuilder.class);

    private SearchableCompassQueryBuilder stringQueryBuilder;
    private Class closureQueryBuilderClass;
    private SearchableCompassQueryBuilderOptionsHelper[] optionHelpers = new SearchableCompassQueryBuilderOptionsHelper[] {
        new SearchableCompassQueryBuilderClassOptionHelper(),
        new SearchableCompassQueryBuilderSortOptionHelper()
    };

    public DefaultSearchableCompassQueryBuilder(Compass compass) {
        super(compass);
        stringQueryBuilder = new DefaultStringQuerySearchableCompassQueryBuilder(getCompass());
        String name = "org.codehaus.groovy.grails.plugins.searchable.compass.search.GroovyCompassQueryBuilder";
        try {
            closureQueryBuilderClass = ClassUtils.forName(name);
        } catch (Exception ex) {
            LOG.error("Class not found [" + name + "]", ex);
            throw new IllegalStateException("Class not found [" + name + "]");
        }
    }

    public CompassQuery buildQuery(GrailsApplication grailsApplication, CompassSession compassSession, Map options, Object query) {
        Assert.notNull(query, "query cannot be null");
        CompassQuery compassQuery;
        if (query instanceof String) {
            compassQuery = stringQueryBuilder.buildQuery(grailsApplication, compassSession, options, query);
        } else {
            Assert.isInstanceOf(Closure.class, query, "query is neither String nor Closure: must be one of these but is [" + query.getClass().getName() + "]");
            Object closureQueryBuilder = InvokerHelper.invokeConstructorOf(closureQueryBuilderClass, compassSession.queryBuilder());
            compassQuery = (CompassQuery) InvokerHelper.invokeMethod(closureQueryBuilder, "buildQuery", query);
        }
        return applyOptions(grailsApplication, getCompass(), compassSession, compassQuery, options);
    }

    protected CompassQuery applyOptions(GrailsApplication grailsApplication, Compass compass, CompassSession compassSession, CompassQuery compassQuery, Map options) {
        for (int i = 0, max = optionHelpers.length; i < max; i++) {
            compassQuery = optionHelpers[i].applyOptions(grailsApplication, compass, compassSession, compassQuery, options);
        }
        return compassQuery;
    }
}
