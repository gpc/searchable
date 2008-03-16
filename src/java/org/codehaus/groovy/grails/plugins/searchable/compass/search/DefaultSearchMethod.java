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
import org.codehaus.groovy.grails.plugins.searchable.compass.support.AbstractSearchableMethod;
import org.codehaus.groovy.grails.plugins.searchable.compass.support.SearchableMethodUtils;
import org.compass.core.*;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Collection;

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
    
    public DefaultSearchMethod(String methodName, Compass compass, GrailsApplication grailsApplication, Map defaultOptions) {
        super(methodName, compass, defaultOptions);
        this.grailsApplication = grailsApplication;
    }

    public Object invoke(Object[] args) {
        Assert.notNull(args, "args cannot be null");
        Assert.notEmpty(args, "args cannot be empty");

        final Object query = getQuery(args);
        Assert.notNull(query, "No query String or Closure argument given to " + getMethodName() + "(): you must supply one");
        final Map options = SearchableMethodUtils.getOptionsArgument(args, getDefaultOptions());

        return doInCompass(new CompassCallback() {
            public Object doInCompass(CompassSession session) throws CompassException {
                CompassQuery compassQuery = compassQueryBuilder.buildQuery(grailsApplication, session, options, query);
                long start = System.currentTimeMillis();
                CompassHits hits = compassQuery.hits();
                if (LOG.isDebugEnabled()) {
                    long time = System.currentTimeMillis() - start;
                    LOG.debug("query: [" + compassQuery + "], [" + hits.length() + "] hits, took [" + time + "] millis");
                }
//                long time = System.currentTimeMillis() - start;
//                System.out.println("query: [" + compassQuery + "], [" + hits.length() + "] hits, took [" + time + "] millis");
                Object collectedHits = hitCollector.collect(hits, options);
                Object searchResult = searchResultFactory.buildSearchResult(hits, collectedHits, options);

                if (collectedHits instanceof Collection) {
                    Closure withHighlighter = (Closure) options.get("withHighlighter");
                    if (withHighlighter != null) {
                        withHighlighter = (Closure) withHighlighter.clone();
                        for (int i = 0, length = ((Collection) collectedHits).size(); i < length; i++) {
                            withHighlighter.call(new Object[] {
                                hits.highlighter(i), new Integer(i), searchResult
                            });
                        }
                    }
                }

                return searchResult;
            }
        });
    }

    private Object getQuery(Object[] args) {
        for (int i = 0, max = args.length; i < max; i++) {
            if (args[i] instanceof Closure || args[i] instanceof String) {
                return args[i];
            }
        }
        return null;
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
}
