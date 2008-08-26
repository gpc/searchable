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
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.searchable.SearchableMethod;
import org.codehaus.groovy.grails.plugins.searchable.SearchableMethodFactory;
import org.codehaus.groovy.grails.plugins.searchable.compass.support.AbstractSearchableMethod;
import org.codehaus.groovy.grails.plugins.searchable.compass.support.SearchableMethodUtils;
import org.compass.core.*;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Collection;
import java.util.HashMap;

/**
 * The default search method implementation
 *
 * @author Maurice Nicholson
 */
public class DefaultSearchMethod extends AbstractSearchableMethod implements SearchableMethod {
    private static Log LOG = LogFactory.getLog(DefaultSearchMethod.class);

    private GrailsApplication grailsApplication;
    private SearchableCompassQueryBuilder compassQueryBuilder;
    private SearchableHitCollector hitCollector;
    private SearchableSearchResultFactory searchResultFactory;

    public DefaultSearchMethod(String methodName, Compass compass, GrailsApplication grailsApplication, SearchableMethodFactory methodFactory, Map defaultOptions) {
        super(methodName, compass, methodFactory, defaultOptions);
        this.grailsApplication = grailsApplication;
    }

    public Object invoke(Object[] args) {
        Assert.notNull(args, "args cannot be null");
        Assert.notEmpty(args, "args cannot be empty");

        SearchableMethod suggestQueryMethod = getMethodFactory().getMethod("suggestQuery");

        SearchCompassCallback searchCallback = new SearchCompassCallback(getCompass(), getDefaultOptions(), args);
        searchCallback.setGrailsApplication(grailsApplication);
        searchCallback.setCompassQueryBuilder(compassQueryBuilder);
        searchCallback.setHitCollector(hitCollector);
        searchCallback.setSearchResultFactory(searchResultFactory);
        searchCallback.setSuggestQueryMethod(suggestQueryMethod);
        return doInCompass(searchCallback);
    }

    public void setCompassQueryBuilder(SearchableCompassQueryBuilder compassQueryBuilder) {
        this.compassQueryBuilder = compassQueryBuilder;
    }

    public void setHitCollector(SearchableHitCollector hitCollector) {
        this.hitCollector = hitCollector;
    }

    public void setSearchResultFactory(SearchableSearchResultFactory searchResultFactory) {
        this.searchResultFactory = searchResultFactory;
    }

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
    }

    public static class SearchCompassCallback implements CompassCallback {
        private Object[] args;
        private Map defaultOptions;
        private GrailsApplication grailsApplication;
        private SearchableCompassQueryBuilder compassQueryBuilder;
        private SearchableHitCollector hitCollector;
        private SearchableSearchResultFactory searchResultFactory;
        private SearchableMethod suggestQueryMethod;

        public SearchCompassCallback(Compass compass, Map defaultOptions, Object[] args) {
            this.args = args;
            this.defaultOptions = defaultOptions;
        }

        public Object doInCompass(CompassSession session) throws CompassException {
            Map options = SearchableMethodUtils.getOptionsArgument(args, defaultOptions);
            CompassQuery compassQuery = compassQueryBuilder.buildQuery(grailsApplication, session, options, args);
            long start = System.currentTimeMillis();
            CompassHits hits = compassQuery.hits();
            if (LOG.isDebugEnabled()) {
                long time = System.currentTimeMillis() - start;
                LOG.debug("query: [" + compassQuery + "], [" + hits.length() + "] hits, took [" + time + "] millis");
            }
            if (hitCollector == null && searchResultFactory == null) {
                Assert.notNull(options.get("result"), "Missing 'result' option for search/query method: this should be provided if hitCollector/searchResultFactory are null to determine the type of result to return");
                String result = (String) options.get("result");
                if (result.equals("top")) {
                    hitCollector = new DefaultSearchableTopHitCollector();
                    searchResultFactory = new SearchableHitsOnlySearchResultFactory();
                } else if (result.equals("every")) {
                    hitCollector = new DefaultSearchableEveryHitCollector();
                    searchResultFactory = new SearchableHitsOnlySearchResultFactory();
                } else if (result.equals("searchResult")) {
                    hitCollector = new DefaultSearchableSubsetHitCollector();
                    searchResultFactory = new SearchableSubsetSearchResultFactory();
                } else if (result.equals("count")) {
                    hitCollector = new CountOnlyHitCollector();
                    searchResultFactory = new SearchableHitsOnlySearchResultFactory();
                } else {
                    throw new IllegalArgumentException("Invalid 'result' option for search/query method [" + result + "]. Supported values are ['searchResult', 'every', 'top']");
                }
            }
            Object collectedHits = hitCollector.collect(hits, options);
            Object searchResult = searchResultFactory.buildSearchResult(hits, collectedHits, options);

            doWithHighlighter(collectedHits, hits, searchResult, options);

            Object suggestOption = options.get("suggestQuery");
            if (searchResult instanceof Map && suggestOption != null) {
                addSuggestedQuery((Map) searchResult, suggestOption);
            }
            return searchResult;
        }

        private void addSuggestedQuery(Map searchResult, Object suggestOption) {
            if (suggestOption instanceof Boolean && suggestOption.equals(Boolean.FALSE)) {
                return;
            }
            Object[] suggestArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Map) {
                    Map searchOptions = (Map) args[i];
                    Map suggestOptions = new HashMap(searchOptions);
                    if (suggestOption instanceof Map) {
                        suggestOptions.putAll((Map) suggestOption);
                    }
                    suggestOptions.remove("suggestQuery"); // remove the option itself
                    suggestArgs[i] = suggestOptions;
                } else {
                    suggestArgs[i] = args[i];
                }
            }
            searchResult.put("suggestedQuery", suggestQueryMethod.invoke(suggestArgs));
        }

        public void doWithHighlighter(Object collectedHits, CompassHits hits, Object searchResult, Map options) {
            if (!(collectedHits instanceof Collection)) {
                return;
            }
            Closure withHighlighter = (Closure) options.get("withHighlighter");
            if (withHighlighter == null) {
                return;
            }
            withHighlighter = (Closure) withHighlighter.clone();
            int offset = org.apache.commons.collections.MapUtils.getIntValue(options, "offset");
            for (int i = 0, length = ((Collection) collectedHits).size(); i < length; i++) {
                withHighlighter.call(new Object[] {
                    hits.highlighter(offset + i), new Integer(i), searchResult
                });
            }
        }

        public void setGrailsApplication(GrailsApplication grailsApplication) {
            this.grailsApplication = grailsApplication;
        }

        public void setCompassQueryBuilder(SearchableCompassQueryBuilder compassQueryBuilder) {
            this.compassQueryBuilder = compassQueryBuilder;
        }

        public void setHitCollector(SearchableHitCollector hitCollector) {
            this.hitCollector = hitCollector;
        }

        public void setSearchResultFactory(SearchableSearchResultFactory searchResultFactory) {
            this.searchResultFactory = searchResultFactory;
        }

        public void setSuggestQueryMethod(SearchableMethod suggestQueryMethod) {
            this.suggestQueryMethod = suggestQueryMethod;
        }
    }
}
