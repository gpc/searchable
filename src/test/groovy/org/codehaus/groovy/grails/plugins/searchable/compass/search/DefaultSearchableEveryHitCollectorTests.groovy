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
package org.codehaus.groovy.grails.plugins.searchable.compass.search

import org.codehaus.groovy.grails.plugins.searchable.test.*
import org.compass.core.CompassHits

/**
 *
 * @author Maurice Nicholson
 */
class DefaultSearchableEveryHitCollectorTests extends GroovyTestCase {

    void testCollect() {
        def hitCollector = new DefaultSearchableEveryHitCollector()

        // without reload
        def collectedHits = hitCollector.collect([length: {1000}, data: {i -> new DataObject(i, "This is hit object #${i}")}] as CompassHits, [reload: false])
        assert collectedHits.size() == 1000
        for (i in 0..<1000) assert collectedHits[i].data == "This is hit object #${i}"

        // with reload
        collectedHits = hitCollector.collect([length: {1000}, data: {i -> new DataObject(i, "This is hit object #${i}")}] as CompassHits, [reload: true])
        assert collectedHits.size() == 1000
        assert collectedHits.collect { it.data }.containsAll((0..<1000).collect { "This is hit object #${it} -- RELOADED" })

        // Handles no hits (no exception)
        collectedHits = hitCollector.collect([length: {0}] as CompassHits, [reload: false])
        assert collectedHits.size() == 0
    }
}
