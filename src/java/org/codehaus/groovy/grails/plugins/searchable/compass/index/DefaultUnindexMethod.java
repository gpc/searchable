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
package org.codehaus.groovy.grails.plugins.searchable.compass.index;

import org.codehaus.groovy.grails.plugins.searchable.SearchableMethod;
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils;
import org.compass.core.*;

import java.util.*;

/*
    unindexAll()

    service.unindexAll() // everything
    service.unindexAll([class: Post]) // all class instances
    service.unindexAll(1, 2, 3) // ERROR: unknown class
    service.unindexAll(1, 2, 3, [class: Post]) // id'd objects
    service.unindexAll(x, y, z) // given objects

    Thing.unindexAll() // all class instances
    Thing.unindexAll(1,2,3) // id'd instances
    Thing.unindexAll(x,y,z) // given instances

    */
/*
    unindex()

    Like unindexAll but without dangerous no-arg bulk behavoir

    service.unindex() // ERROR: not allowed
    service.unindex([class: Post]) // all class instances
    service.unindex(x, y, z) // given object(s)
    service.unindex(1, 2, 3, [class: Post]) // id'd objects

    Thing.unindex() // ERROR: not allowed
    Thing.unindex(1,2,3) // id'd instances
    Thing.unindex(x,y,z) // given instances

    */


/**
 * @author Maurice Nicholson
 */
public class DefaultUnindexMethod extends AbstractDefaultIndexMethod implements SearchableMethod  {

    public DefaultUnindexMethod(String methodName, Compass compass, boolean bulkAllowed, Map defaultOptions) {
        super(methodName, compass, defaultOptions, bulkAllowed);
    }

    public DefaultUnindexMethod(String methodName, Compass compass, boolean bulkAllowed) {
        this(methodName, compass, bulkAllowed, new HashMap());
    }

    public Object invoke(final Object[] args) {
        Map options = getOptions(args);
        final Class clazz = (Class) SearchableUtils.getOption("class", options, null);
        final List ids = getIds(args);
        final List objects = getObjects(args);

        validateArguments(args, clazz, ids, objects, options);

        return doInCompass(new CompassCallback() {
            public Object doInCompass(CompassSession session) throws CompassException {
                if (!objects.isEmpty()) {
                    for (Iterator iter = objects.iterator(); iter.hasNext(); ) {
                        session.delete(iter.next());
                    }
                    return null;
                }
                CompassQuery query = null;
                CompassQueryBuilder queryBuilder = session.queryBuilder();
                if (args.length == 0) {
                    query = queryBuilder.matchAll();
                }
                if (clazz != null && ids.isEmpty()) {
                    query = queryBuilder.alias(SearchableCompassUtils.getDefaultAlias(clazz));
                }
                if (query != null) {
                    session.delete(query);
                    return null;
                }
                for (Iterator iter = ids.iterator(); iter.hasNext(); ) {
                    session.delete(clazz, iter.next());
                }
                return null;
            }
        });
    }
}
