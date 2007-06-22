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
package org.codehaus.groovy.grails.plugins.searchable.compass

import org.jmock.*
import org.jmock.core.stub.*
import org.jmock.core.matcher.*
import org.compass.gps.CompassGps

/**
*
*
* @author Maurice Nicholson
*/
class CompassGpsUtilsTests extends GroovyTestCase {

    void testIndexWhenCompassGpsRunning() {
//        assert false // does not seem to test the sequence!
        def mockGps  = new Mock(CompassGps.class)
        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(true))
        mockGps.expects(new InvokeOnceMatcher()).method('index').withNoArguments().after('isRunning').isVoid()

        CompassGpsUtils.index(mockGps.proxy())

        mockGps.verify()
    }

    void testIndexWhenCompassGpsNotRunning() {
        def mockGps  = new Mock(CompassGps.class)
        mockGps.expects(new InvokeOnceMatcher()).method('isRunning').withNoArguments().will(new ReturnStub(false))
        mockGps.expects(new InvokeOnceMatcher()).method('start').withNoArguments().after('isRunning').isVoid()
        mockGps.expects(new InvokeOnceMatcher()).method('index').withNoArguments().after('start').isVoid()
        mockGps.expects(new InvokeOnceMatcher()).method('stop').withNoArguments().after('index').isVoid()

        CompassGpsUtils.index(mockGps.proxy())

        mockGps.verify()
    }
}