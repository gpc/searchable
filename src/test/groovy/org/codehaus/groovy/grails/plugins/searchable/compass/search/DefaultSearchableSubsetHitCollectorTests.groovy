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
 *
 * @author Maurice Nicholson
 */
class DefaultSearchableSubsetHitCollectorTests extends GroovyTestCase {

    void testCollect() {
        def collector = new DefaultSearchableSubsetHitCollector()

        // Handles zero hits
        def collectedHits = collector.collect([length: {0}] as CompassHits, [offset: 0, max: 10, reload: false])
        assert collectedHits == []

        // Handles max < hits
        collectedHits = collector.collect([length: {1000}, data: {i -> new DataObject(i, "Object#${i}")}] as CompassHits, [offset: 0, max: 10, reload: false])
        assert collectedHits.size() == 10
        def ids = (0..9).collect { it }
        assert collectedHits.id == ids

        // Handles max > hits
        collectedHits = collector.collect([length: {1000}, data: {i -> new DataObject(i, "Object#${i}")}] as CompassHits, [offset: 0, max: 5000, reload: false])
        assert collectedHits.size() == 1000
        ids = (0..<1000).collect { it }
        assert collectedHits.id == ids

        // Pages: other offsets and max
        collectedHits = collector.collect([length: {1000}, data: {i -> new DataObject(i, "Object#${i}")}] as CompassHits, [offset: 10, max: 25, reload: false])
        assert collectedHits.size() == 25
        ids = (10..<35).collect { it }
        assert collectedHits.id == ids

        collectedHits = collector.collect([length: {1000}, data: {i -> new DataObject(i, "Object#${i}")}] as CompassHits, [offset: 39, max: 1, reload: false])
        assert collectedHits.size() == 1
        assert collectedHits.id == [39]

        // Handles offset > hits
        collectedHits = collector.collect([length: {25}, data: {i -> new DataObject(i, "Object#${i}")}] as CompassHits, [offset: 50, max: 30, reload: false])
        assert collectedHits == []

        // Borderline: offset + max > hits
        collectedHits = collector.collect([length: {100}, data: {i -> new DataObject(i, "Object#${i}")}] as CompassHits, [offset: 90, max: 20, reload: false])
        assert collectedHits.size() == 10
        ids = (90..<100).collect { it }
        assert collectedHits.id == ids

        // with reload
        collectedHits = collector.collect([length: {100}, data: {i -> new DataObject(i, "Object#${i}")}] as CompassHits, [offset: 0, max: 20, reload: true])
        assert collectedHits.size() == 20
        def datas = (0..<20).collect { "Object#${it} -- RELOADED" }
        assert collectedHits.data.size() == datas.size(), collectedHits.data.size()
        assert collectedHits.data.containsAll(datas)

        // without reload
        collectedHits = collector.collect([length: {100}, data: {i -> new DataObject(i, "Object#${i}")}] as CompassHits, [offset: 50, max: 25, reload: false])
        assert collectedHits.size() == 25
        datas = (50..<75).collect { "Object#${it}" } // original ("from index") hit data
        assert collectedHits.data == datas
    }
}
