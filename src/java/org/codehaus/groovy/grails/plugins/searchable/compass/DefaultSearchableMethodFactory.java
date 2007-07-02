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
package org.codehaus.groovy.grails.plugins.searchable.compass;

import org.compass.core.Compass;
import org.compass.gps.CompassGps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.plugins.searchable.SearchableMethod;
import org.codehaus.groovy.grails.plugins.searchable.compass.support.AbstractSearchableMethod;
import org.codehaus.groovy.grails.plugins.searchable.SearchableMethodFactory;
import org.codehaus.groovy.grails.plugins.searchable.compass.search.*;
import org.codehaus.groovy.grails.plugins.searchable.compass.index.DefaultUnindexMethod;
import org.codehaus.groovy.grails.plugins.searchable.compass.index.DefaultIndexMethod;
import org.codehaus.groovy.grails.plugins.searchable.compass.index.DefaultReindexMethod;

import java.util.Map;
import java.util.HashMap;

/**
 * Default implementation of creating SearchableMethod instances
 *
 * @author Maurice Nicholson
 */
public class DefaultSearchableMethodFactory implements SearchableMethodFactory {
    private static final Log LOG = LogFactory.getLog(DefaultSearchableMethodFactory.class);
    private static final Map DEFAULT_SEARCH_DEFAULTS = new HashMap() {{
        put("escape", Boolean.FALSE);
        put("offset", new Integer(0));
        put("max", new Integer(10));
        put("reload", Boolean.FALSE);
    }};

    private Map searchDefaults;
    private Compass compass;
    private CompassGps compassGps;

    public SearchableMethod getMethod(final Class clazz, String methodName) {
        AbstractSearchableMethod method = (AbstractSearchableMethod) getMethod(methodName);
        method.setDefaultOptions(new HashMap(method.getDefaultOptions()) {{ // clone to avoid corrupting original
            put("class", clazz);
        }});
        return method;
    }

    public SearchableMethod getMethod(String methodName) {
        if (methodName.equals("indexAll")) {
            return new DefaultIndexMethod(methodName, compass, compassGps, true);
        }
        if (methodName.equals("index")) {
            return new DefaultIndexMethod(methodName, compass, compassGps, false);
        }
        if (methodName.equals("unindexAll")) {
            return new DefaultUnindexMethod(methodName, compass, true);
        }
        if (methodName.equals("unindex")) {
            return new DefaultUnindexMethod(methodName, compass, false);
        }
        if (methodName.equals("reindexAll")) {
            return new DefaultReindexMethod(methodName, compass, compassGps, true);
        }
        if (methodName.equals("reindex")) {
            return new DefaultReindexMethod(methodName, compass, compassGps, false);
        }

        DefaultSearchMethod searchMethod = new DefaultSearchMethod(methodName, compass, buildSearchDefaults());
        if (methodName.equals("search")) {
            searchMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            searchMethod.setHitCollector(new DefaultSearchableSubsetHitCollector());
            searchMethod.setSearchResultFactory(new SearchableSubsetSearchResultFactory());
        }
        if (methodName.equals("searchTop")) {
            searchMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            searchMethod.setHitCollector(new DefaultSearchableTopHitCollector());
            searchMethod.setSearchResultFactory(new SearchableHitsOnlySearchResultFactory());
        }
        if (methodName.equals("searchEvery")) {
            searchMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            searchMethod.setHitCollector(new DefaultSearchableEveryHitCollector());
            searchMethod.setSearchResultFactory(new SearchableHitsOnlySearchResultFactory());
        }
        if (methodName.equals("countHits")) {
            searchMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            searchMethod.setHitCollector(new CountOnlyHitCollector());
            searchMethod.setSearchResultFactory(new SearchableHitsOnlySearchResultFactory());
        }
        return searchMethod;
    }

    private Map buildSearchDefaults() {
        Map m = new HashMap(DEFAULT_SEARCH_DEFAULTS);
        if (searchDefaults != null) {
            m.putAll(searchDefaults);
        }
        LOG.debug("search defaults: " + m);
        return m;
    }

    public Map getSearchDefaults() {
        return searchDefaults;
    }

    public void setSearchDefaults(Map searchDefaults) {
        this.searchDefaults = searchDefaults;
    }

    public Compass getCompass() {
        return compass;
    }

    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    public CompassGps getCompassGps() {
        return compassGps;
    }

    public void setCompassGps(CompassGps compassGps) {
        this.compassGps = compassGps;
    }
}
