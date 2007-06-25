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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.plugins.searchable.SearchableMethod;
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils;
import org.codehaus.groovy.grails.plugins.searchable.compass.CompassGpsUtils;
import org.codehaus.groovy.grails.plugins.searchable.util.TimeUtils;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.CompassGps;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
   indexAll()

   service.indexAll() // all searchable class instances
   service.indexAll([class: Post]) // all Post instances - ERROR: not supported
   service.indexAll(1l, 2l, 3l) // ERROR: unknown class
   service.indexAll(1l, 2l, 3l, [class: Post]) // id'd Post instances
   service.indexAll(x, y, z) // given instances

   Thing.indexAll() // all Thing instances - ERROR: not supported
   Thing.indexAll(1l, 2l, 3l) // id'd Thing instances
   Thing.indexAll(x, y, z) // given instances


*/
/*
    index()

    Same as indexAll

    service.index() // ERROR: not allowed
    service.index([class: Post]) // all Post instances - ERROR: not supported
    service.index(x, y, z) // given object(s)
    service.index(1, 2, 3, [class: Post]) // id'd objects

    Thing.index() // ERROR: not allowed
    Thing.index(1,2,3) // id'd instances
    Thing.index(x,y,z) // given instances

    */

/**
 * @author Maurice Nicholson
 */
public class DefaultIndexMethod extends AbstractDefaultIndexMethod implements SearchableMethod {
    private static Log LOG = LogFactory.getLog(DefaultIndexMethod.class);

    private CompassGps compassGps;

    public DefaultIndexMethod(String methodName, Compass compass, CompassGps compassGps, boolean bulkAllowed, Map defaultOptions) {
        super(methodName, compass, defaultOptions, bulkAllowed);
        this.compassGps = compassGps;
    }

    public DefaultIndexMethod(String methodName, Compass compass, CompassGps compassGps, boolean bulkAllowed) {
        this(methodName, compass, compassGps, bulkAllowed, new HashMap());
    }

    public Object invoke(Object[] args) {
        Map options = getOptions(args);
        final Class clazz = (Class) options.get("class");
        final List ids = getIds(args);
        final List objects = getObjects(args);

        validateArguments(args, clazz, ids, objects, options);

        if (isBulkAllowed() && args.length == 0 && clazz == null) {
            CompassGpsUtils.index(compassGps);
            return null;
        }

        return doInCompass(new CompassCallback() {
            public Object doInCompass(CompassSession session) throws CompassException {
                List objectsToSave = objects;
                if (clazz != null && !ids.isEmpty()) {
                    Assert.isTrue(objects.isEmpty(), "Either provide ids or objects, not both");
                    objectsToSave = (List) InvokerHelper.invokeStaticMethod(clazz, "getAll", ids);
                }
                Assert.notEmpty(objectsToSave);
                for (Iterator iter = objectsToSave.iterator(); iter.hasNext(); ) {
                    Object o = iter.next();
                    if (o != null) {
                        session.save(o);
                    }
                }
                return null;
            }
        });
    }

    protected void validateArguments(Object[] args, Class clazz, List ids, List objects, Map options) {
        super.validateArguments(args, clazz, ids, objects, options);
        if (objects.isEmpty() && ids.isEmpty() && clazz != null) {
            throw new IllegalArgumentException(
                "You called " + getMethodName() + "() for a class, but did not provide ids. " +
                "Unfortunately this isn't supported due to performance concerns"
            );
        }
    }

    public CompassGps getCompassGps() {
        return compassGps;
    }

    public void setCompassGps(CompassGps compassGps) {
        this.compassGps = compassGps;
    }
}
