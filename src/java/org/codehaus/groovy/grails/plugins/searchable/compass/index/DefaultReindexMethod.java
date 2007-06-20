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
import org.compass.core.Compass;
import org.compass.gps.CompassGps;

import java.util.Map;
import java.util.HashMap;

/*
   reindexAll()

   service.reindexAll() // all searchable class instances
   service.reindexAll([class: Post]) // all Post instances - ERROR: not supported
   service.reindexAll(1l, 2l, 3l) // ERROR: unknown class
   service.reindexAll(1l, 2l, 3l, [class: Post]) // id'd Post instances
   service.reindexAll(x, y, z) // given instances

   Thing.reindexAll() // all Thing instances - ERROR: not supported
   Thing.reindexAll(1l, 2l, 3l) // id'd Post instances
   Thing.reindexAll(x, y, z) // given instances


*/
/*
    reindex()

    Like reindexAll but without no-arg bulk behavoir

    service.reindex() // ERROR: not allowed
    service.reindex([class: Post]) // all class instances - ERROR: not supported
    service.reindex(x, y, z) // given object(s)
    service.reindex(1, 2, 3, [class: Post]) // id'd objects

    Thing.reindex() // all Thing instances - ERROR: not supported
    Thing.reindex(1,2,3) // id'd instances
    Thing.reindex(x,y,z) // given instances

    */

/**
 * @author Maurice Nicholson
 */
public class DefaultReindexMethod extends DefaultIndexMethod implements SearchableMethod {
    private DefaultUnindexMethod unindexMethod;

    public DefaultReindexMethod(String methodName, Compass compass, CompassGps compassGps, boolean bulkAllowed, Map defaultOptions) {
        super(methodName, compass, compassGps, bulkAllowed, defaultOptions);
        unindexMethod = new DefaultUnindexMethod(methodName, compass, bulkAllowed, defaultOptions);
    }

    public DefaultReindexMethod(String methodName, Compass compass, CompassGps compassGps, boolean bulkAllowed) {
        this(methodName, compass, compassGps, bulkAllowed, new HashMap());
    }

    public Object invoke(Object[] args) {
        unindexMethod.invoke(args);
        return super.invoke(args);
    }
}
