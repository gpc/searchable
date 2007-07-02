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

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompassMappingUtils;
import org.codehaus.groovy.grails.plugins.searchable.util.GrailsDomainClassUtils;
import org.compass.core.*;

import java.util.*;

/**
 * Helper dealing with a "class" query builder query option
 *
 * @author Maurice Nicholson
 */
public class SearchableCompassQueryBuilderClassOptionHelper implements SearchableCompassQueryBuilderOptionsHelper {

    public CompassQuery applyOptions(Compass compass, CompassSession compassSession, CompassQuery compassQuery, Map options) {
        Class clazz = (Class) options.get("class");
        if (clazz == null) {
            return compassQuery;
        }

        // TODO add poly=false option?

        GrailsApplication application = ApplicationHolder.getApplication();
        List grailsDomainClasses = Arrays.asList(application.getArtefacts(DomainClassArtefactHandler.TYPE));
        GrailsDomainClass grailsDomainClass = GrailsDomainClassUtils.getGrailsDomainClass(clazz, grailsDomainClasses);
        Set subClasses = grailsDomainClass.getSubClasses();

        setAliases(compass, clazz, compassQuery, subClasses);
        setPolyClassFilter(compassSession, compassQuery, clazz, subClasses);

        return compassQuery;
    }


    /**
     * Set aliases on the query for the given clazz, respecting inheritance in mapping
     */
    public CompassQuery setAliases(Compass compass, Class clazz, CompassQuery compassQuery, Collection subClasses) {
        Set aliases = new HashSet();
        aliases.add(CompassMappingUtils.getMappingAlias(compass, clazz));
        if (!subClasses.isEmpty()) {
            for (Iterator iter = subClasses.iterator(); iter.hasNext(); ) {
                GrailsDomainClass subClass = (GrailsDomainClass) iter.next();
                Class subClazz = subClass.getClazz();
                aliases.add(CompassMappingUtils.getMappingAlias(compass, subClazz));
            }
        }
        compassQuery.setAliases((String[]) aliases.toArray(new String[aliases.size()]));
        return compassQuery;
    }

    /**
     * Set the query filter for poly classes if there are subclasses
     * @param compassSession
     * @param compassQuery
     * @param clazz
     * @param subClasses
     */
    public void setPolyClassFilter(CompassSession compassSession, CompassQuery compassQuery, Class clazz, Set subClasses) {
        if (subClasses.size() > 1) {
            Set clazzes = new HashSet(GrailsDomainClassUtils.getClazzes(subClasses));
            clazzes.add(clazz);
            CompassQuery polyClassQuery = buildPolyClassQuery(compassSession, clazzes);
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
