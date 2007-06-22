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
package org.codehaus.groovy.grails.plugins.searchable.compass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.plugins.searchable.util.TimeUtils;
import org.compass.gps.CompassGps;
import org.springframework.util.Assert;

/**
 * @author Maurice Nicholson
 */
public class CompassGpsUtils {
    private static final Log LOG = LogFactory.getLog(CompassGpsUtils.class);

    /**
     * Calls CompassGps's index method, starting and stopping it if required
     * @param compassGps aCompassGps instance, cannot be null
     */
    public static void index(CompassGps compassGps) {
        Assert.notNull(compassGps, "compassGps cannot be null");

        long start = System.currentTimeMillis();
        LOG.info("Starting Searchable Plugin bulk index");
        boolean gpsRunning = compassGps.isRunning();
        try {
            if (!gpsRunning) {
                compassGps.start();
            }
            compassGps.index();
        } finally {
            if (!gpsRunning) {
                compassGps.stop();
            }
        }
        LOG.info("Finished Searchable Plugin bulk index, " + TimeUtils.formatMillisAsShortHumanReadablePeriod(System.currentTimeMillis() - start));
    }
}
