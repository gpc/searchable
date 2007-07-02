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

import grails.util.GrailsUtil;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.compass.core.Compass;
import org.compass.core.spi.InternalCompass;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * Utilities for Compass and Grails Searchable Plugin
 *
 * @author Maurice Nicholson
 */
public class SearchableCompassUtils {

    /**
     * Get the default Compass connection (ie, Lucene index dir)
     *
     * @return {user.home}/{project-name}/.searchable/{grails.env}
     */
    public static String getDefaultConnection() {
        return System.getProperty("user.home") +
                File.separator + getApplicationName() +
                File.separator + ".searchable" +
                File.separator + GrailsUtil.getEnvironment();
    }

    public static String getApplicationName() {
        GrailsApplication app = ApplicationHolder.getApplication();
        return (String) app.getMetadata().get("app.name");
    }
}
