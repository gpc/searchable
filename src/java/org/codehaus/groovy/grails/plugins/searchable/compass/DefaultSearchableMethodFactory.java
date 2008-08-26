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
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.*;

/**
 * Default implementation of creating SearchableMethod instances
 *
 * @author Maurice Nicholson
 */
public class DefaultSearchableMethodFactory implements SearchableMethodFactory {
    private static final Log LOG = LogFactory.getLog(DefaultSearchableMethodFactory.class);
    private static final Map DEFAULT_METHOD_OPTIONS = new HashMap() {{
        put("search", new HashMap() {{
            put("escape", Boolean.FALSE);
            put("offset", new Integer(0));
            put("max", new Integer(10));
            put("reload", Boolean.FALSE);
        }});
        put("termFreqs", new HashMap() {{
            put("properties", new String[] {"zzz-all"}); // todo make it respect the configured name
        }});
        put("suggestQuery", new HashMap() {{
            put("userFriendly", Boolean.TRUE);
            put("emulateCapitalisation", Boolean.TRUE);
        }});
    }};

    private Map defaultMethodOptions = DEFAULT_METHOD_OPTIONS;
    private Compass compass;
    private CompassGps compassGps;
    private GrailsApplication grailsApplication;

    public SearchableMethod getMethod(final Class clazz, String methodName) {
        AbstractSearchableMethod method = (AbstractSearchableMethod) getMethod(methodName);
        method.setDefaultOptions(new HashMap(method.getDefaultOptions()) {{ // clone to avoid corrupting original
            put("match", clazz);
        }});
        return method;
    }

    public SearchableMethod getMethod(String methodName) {
        // TODO refactor to (injected) lookup map
        if (methodName.equals("indexAll")) {
            System.out.println("The Searchable Plugin 'indexAll' method is deprecated and will be removed in the next version: please use 'index' instead");
            LOG.warn("The Searchable Plugin 'indexAll' method is deprecated and will be removed in the next version: please use 'index' instead");
            return new DefaultIndexMethod(methodName, compass, compassGps);
        }
        if (methodName.equals("index")) {
            return new DefaultIndexMethod(methodName, compass, compassGps);
        }
        if (methodName.equals("unindexAll")) {
            System.out.println("The Searchable Plugin 'unindexAll' method is deprecated and will be removed in the next version: please use 'unindex' instead");
            LOG.warn("The Searchable Plugin 'unindexAll' method is deprecated and will be removed in the next version: please use 'unindex' instead");
            return new DefaultUnindexMethod(methodName, compass);
        }
        if (methodName.equals("unindex")) {
            return new DefaultUnindexMethod(methodName, compass);
        }
        if (methodName.equals("reindexAll")) {
            System.out.println("The Searchable Plugin 'reindexAll' method is deprecated and will be removed in the next version: please use 'reindex' instead");
            LOG.warn("The Searchable Plugin 'reindexAll' method is deprecated and will be removed in the next version: please use 'reindex' instead");
            return new DefaultReindexMethod(methodName, compass, compassGps, this);
        }
        if (methodName.equals("reindex")) {
            return new DefaultReindexMethod(methodName, compass, compassGps, this);
        }

        if (methodName.equals("termFreqs")) {
            return new DefaultTermFreqsMethod(methodName, compass, grailsApplication, getDefaultOptions(methodName));
        }

        if (methodName.equals("search")) {
            DefaultSearchMethod searchMethod = new DefaultSearchMethod(methodName, compass, grailsApplication, this, getDefaultOptions(methodName));
            searchMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            searchMethod.getDefaultOptions().put("result", "searchResult");
            return searchMethod;
        }
        if (methodName.equals("moreLikeThis")) {
            DefaultSearchMethod searchMethod = new DefaultSearchMethod(methodName, compass, grailsApplication, this, getDefaultOptions(methodName));
            searchMethod.setCompassQueryBuilder(new MoreLikeThisCompassQueryBuilder(compass));
            searchMethod.getDefaultOptions().put("result", "searchResult");
            return searchMethod;
        }
        if (methodName.equals("countHits")) {
            DefaultSearchMethod searchMethod = new DefaultSearchMethod(methodName, compass, grailsApplication, this, getDefaultOptions(methodName));
            searchMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            searchMethod.setHitCollector(new CountOnlyHitCollector());
            searchMethod.setSearchResultFactory(new SearchableHitsOnlySearchResultFactory());
            return searchMethod;
        }
        if (methodName.equals("searchTop")) {
//            System.out.println("The Searchable Plugin 'searchTop' method is deprecated and will be removed in the next version: please use 'search(result: 'top', ...)' instead");
//            LOG.warn("The Searchable Plugin 'searchTop' method is deprecated and will be removed in the next version: please use 'search(result: 'top', ...)' instead");
            DefaultSearchMethod searchMethod = new DefaultSearchMethod(methodName, compass, grailsApplication, this, getDefaultOptions(methodName));
            searchMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            searchMethod.setHitCollector(new DefaultSearchableTopHitCollector());
            searchMethod.setSearchResultFactory(new SearchableHitsOnlySearchResultFactory());
            return searchMethod;
        }
        if (methodName.equals("searchEvery")) {
//            System.out.println("The Searchable Plugin 'searchEvery' method is deprecated and will be removed in the next version: please use 'search(result: 'every', ...)' instead");
//            LOG.warn("The Searchable Plugin 'searchEvery' method is deprecated and will be removed in the next version: please use 'search(result: 'every', ...)' instead");
            DefaultSearchMethod searchMethod = new DefaultSearchMethod(methodName, compass, grailsApplication, this, getDefaultOptions(methodName));
            searchMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            searchMethod.setHitCollector(new DefaultSearchableEveryHitCollector());
            searchMethod.setSearchResultFactory(new SearchableHitsOnlySearchResultFactory());
            return searchMethod;
        }
        if (methodName.equals("suggestQuery")) {
            DefaultSuggestQueryMethod suggestQueryMethod = new DefaultSuggestQueryMethod(methodName, compass, grailsApplication, getDefaultOptions(methodName));
            suggestQueryMethod.setCompassQueryBuilder(new DefaultSearchableCompassQueryBuilder(compass));
            return suggestQueryMethod;
        }
        throw new IllegalArgumentException("Searchable Method not found for name [" + methodName + "]");
    }

    private Map getDefaultOptions(String methodName) {
        if (defaultMethodOptions.containsKey(methodName)) {
            return (Map) defaultMethodOptions.get(methodName);
        }
        final Collection searchMethodsNames = Arrays.asList(new String[] {"search", "searchTop", "searchEvery", "moreLikeThis", "countHits"});
        if (searchMethodsNames.contains(methodName)) {
            return (Map) defaultMethodOptions.get("search");
        }
        return null;
    }

    public Map getDefaultMethodOptions() {
        return defaultMethodOptions;
    }

    /**
     * Merges the user provided method default options with the defaults in {@link #DEFAULT_METHOD_OPTIONS}
     * @param defaultMethodOptions
     */
    public void setDefaultMethodOptions(Map defaultMethodOptions) {
        Map tmp = new HashMap();
        tmp.putAll(DEFAULT_METHOD_OPTIONS);
        tmp.putAll(defaultMethodOptions);
        this.defaultMethodOptions = tmp;
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

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
    }
}
