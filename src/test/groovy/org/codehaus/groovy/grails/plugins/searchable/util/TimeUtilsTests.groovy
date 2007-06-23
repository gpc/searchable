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
package org.codehaus.groovy.grails.plugins.searchable.util

/**
 *
 *
 * @author Maurice Nicholson
 */
class TimeUtilsTests extends GroovyTestCase {

    void testFormatMillisAsHumanReadableDHMSMNotNull() {
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(126) == "126 ms"
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(126, false) == "126 ms"
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(28881) == "28 secs, 881 ms"
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(334556) == "5 mins, 34 secs" // 556 ms
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(334556, false) == "5 mins, 34 secs" // 556 ms
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(45344021) == "12 hrs, 35 mins, 44 secs" // 21 ms
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(45344021, false) == "12 hrs, 35 mins, 44 secs" // 21 ms
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(3022100309) == "34 days, 23 hrs, 28 mins" // 20 secs, 309 ms
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(3022100309, false) == "34 days, 23 hrs, 28 mins" // 20 secs, 309 ms
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(3022080309) == "34 days, 23 hrs, 28 mins" // 0 secs, 309 ms
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(3022080309, false) == "34 days, 23 hrs, 28 mins" // 0 secs, 309 ms
    }

    void testFormatMillisAsHumanReadableDHMSMFull() {
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(126, true) == "126 ms"
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(28881, true) == "28 secs, 881 ms"
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(334556, true) == "5 mins, 34 secs, 556 ms"
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(45344021, true) == "12 hrs, 35 mins, 44 secs, 21 ms"
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(3022100309, true) == "34 days, 23 hrs, 28 mins, 20 secs, 309 ms"
        assert TimeUtils.formatMillisAsShortHumanReadablePeriod(3022080309, true) == "34 days, 23 hrs, 28 mins, 0 secs, 309 ms"
    }
}