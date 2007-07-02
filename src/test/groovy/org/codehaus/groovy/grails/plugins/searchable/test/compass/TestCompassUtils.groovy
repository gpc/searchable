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
package org.codehaus.groovy.grails.plugins.searchable.test.compass

import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils
import org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompassMappingUtils

/**
*
*
* @author Maurice Nicholson
*/
class TestCompassUtils {

    static withCompassQueryBuilder(compass, closure) {
        def session = compass.openSession()
        def tx = session.beginTransaction()
        def result
        try {
            def queryBuilder = session.queryBuilder()
            result = closure(queryBuilder)
        } finally {
            tx.commit()
            session.close()
        }
        return result
    }

    static withCompassSession(compass, closure) {
        def session = compass.openSession()
        def tx = session.beginTransaction()
        def result
        try {
            result = closure(session)
        } finally {
            tx.commit()
            session.close()
        }
        return result
    }

    static numberIndexed(compass, clazz) {
        withCompassSession(compass) { session ->
            session.queryBuilder().alias(CompassMappingUtils.getMappingAlias(compass, clazz)).hits().length()
        }
    }

    static countHits(compass, closure) {
        withCompassSession(compass) { session ->
            def queryBuilder = session.queryBuilder()
            def query = closure(queryBuilder)
            return query.hits().length()
        }
    }

    static saveToCompass(compass, Object[] objects) {
        withCompassSession(compass) { session ->
            objects.each { object ->
                def collection = object
                if (!(collection instanceof Collection)) collection = [collection]
                collection.each { session.save(it) }
            }
        }
    }

    static loadFromCompass(compass, clazz, id) {
        withCompassSession(compass) { session ->
            session.load(clazz, id)
        }
    }

    static clearIndex(compass) {
        withCompassSession(compass) { session ->
            session.delete(session.queryBuilder().matchAll())
        }
    }
}