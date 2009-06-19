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
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.springframework.util.Assert;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.compass.core.lucene.util.LuceneHelper;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import org.apache.lucene.search.Filter;
import org.compass.core.CompassQueryFilter;
import org.apache.lucene.search.SortField;


/**
 * Post-processes a query to add a filter
 *
 * @author Phillip Rhodes PCR
 */
public class SearchableCompassQueryBuilderFilterOptionHelper implements SearchableCompassQueryBuilderOptionsHelper {
    public static final String FILTER = "filter";

    public CompassQuery applyOptions(GrailsApplication grailsApplication, Compass compass, CompassSession compassSession, CompassQuery compassQuery, Map options) {
    	System.out.println("SearchableCompassQueryBuilderFilterOptionHelper applyOptions  ");
        return addFilter(compassQuery, options,compassSession);
    }

    public CompassQuery addFilter(CompassQuery compassQuery, Map options,CompassSession compassSession) {
    	
    	if (compassSession == null) {
    		
        	System.out.println("compassSession is null in SearchableCompassQueryBuilderFilterOptionHelper  ");
    	}
    	
    	try {
        Filter filter = (Filter) options.get("filter");
        if (filter == null) {
            return compassQuery;
        }
        CompassQueryFilter compassQueryFilter = LuceneHelper.createCompassQueryFilter(compassSession , filter);

        compassQuery.setFilter(compassQueryFilter);


        
        return compassQuery;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return compassQuery;
    	}
    }

    
}
