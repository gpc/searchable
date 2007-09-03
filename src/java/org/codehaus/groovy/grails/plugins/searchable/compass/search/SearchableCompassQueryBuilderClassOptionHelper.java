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

import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.SearchableGrailsDomainClassCompassMappingUtils;
import org.codehaus.groovy.grails.plugins.searchable.util.GrailsDomainClassUtils;
import org.compass.core.*;

import java.util.*;

/**
 * Helper dealing with a "class" query builder query option
 *
 * @author Maurice Nicholson
 */
public class SearchableCompassQueryBuilderClassOptionHelper implements SearchableCompassQueryBuilderOptionsHelper {

    public CompassQuery applyOptions(GrailsApplication grailsApplication, Compass compass, CompassSession compassSession, CompassQuery compassQuery, Map options) {
        Class clazz = (Class) options.get("class");
        if (clazz == null) {
            return compassQuery;
        }

        // TODO add poly=false option?

        setAliases(compass, clazz, compassQuery, grailsApplication);

        List grailsDomainClasses = Arrays.asList(grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE));
        GrailsDomainClass grailsDomainClass = GrailsDomainClassUtils.getGrailsDomainClass(clazz, grailsDomainClasses);
        Set subClasses = grailsDomainClass.getSubClasses();
        setPolyClassFilter(this, compassSession, compassQuery, clazz, subClasses);

        return compassQuery;
    }

    /**
     * Set aliases on the query for the given clazz, respecting inheritance in mapping
     */
    private static CompassQuery setAliases(Compass compass, Class clazz, CompassQuery compassQuery, GrailsApplication application) {
        String[] aliasesArr = SearchableGrailsDomainClassCompassMappingUtils.getPolyMappingAliases(compass, clazz, application);
        compassQuery.setAliases(aliasesArr);
        return compassQuery;
    }

    /**
     * Set the query filter for poly classes if there are subclasses
     * @param searchableCompassQueryBuilderClassOptionHelper
     * @param compassSession
     * @param compassQuery
     * @param clazz
     * @param subClasses
     */
    private static void setPolyClassFilter(SearchableCompassQueryBuilderClassOptionHelper searchableCompassQueryBuilderClassOptionHelper, CompassSession compassSession, CompassQuery compassQuery, Class clazz, Set subClasses) {
        if (subClasses.size() > 1) {
            Set clazzes = new HashSet(GrailsDomainClassUtils.getClazzes(subClasses));
            clazzes.add(clazz);
            CompassQuery polyClassQuery = searchableCompassQueryBuilderClassOptionHelper.buildPolyClassQuery(compassSession, clazzes);
            CompassQueryFilter instanceFilter = compassSession.queryFilterBuilder().query(polyClassQuery);
            compassQuery.setFilter(instanceFilter);
        }
    }

    /**
     * Builds the polymorphic class-instance query part
     * @param compassSession
     * @param clazzes
     * @return
     */
    public CompassQuery buildPolyClassQuery(CompassSession compassSession, Collection clazzes) {
        CompassQueryBuilder queryBuilder = compassSession.queryBuilder();
        CompassQueryBuilder.CompassBooleanQueryBuilder instanceBoolBuilder = queryBuilder.bool();
        for (Iterator iter = clazzes.iterator(); iter.hasNext(); ) {
            Class clz = (Class) iter.next();
            instanceBoolBuilder.addShould(
                queryBuilder.term("$/poly/class", clz.getName())
            );
        }
        return instanceBoolBuilder.toQuery();
    }
}
