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

import org.compass.core.*
import org.compass.core.impl.*

/**
 * 
 *
 * @author Maurice Nicholson
 */
// TODO replace usages with Map coerced to CompassHits interface
class TestCompassHits implements CompassHits {
    def data
    def scores
    
    TestCompassHits() {
    }
    Object data(int n) throws CompassException {
        data[n]
    }
    int length() {
        data ? data.size() : 0
    }
    int getLength() {
        length()
    }
    float score(int i) throws CompassException {
        scores ? scores[i] : null
    }

    void close() throws CompassException {}
    CompassDetachedHits detach() throws CompassException {}
    CompassDetachedHits detach(int i, int i1) throws CompassException, IllegalArgumentException {}
    CompassHighlighter highlighter(int i) throws CompassException {}
    Resource resource(int i) throws CompassException {}
    CompassHit hit(int i) throws CompassException {}
    CompassHighlightedText highlightedText(int i) throws CompassException {}
}