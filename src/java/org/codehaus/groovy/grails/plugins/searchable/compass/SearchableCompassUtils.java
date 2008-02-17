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
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.io.File;

/**
 * Utilities for Compass and Grails Searchable Plugin
 *
 * @author Maurice Nicholson
 */
public class SearchableCompassUtils {

    /**
     * Get the default Compass connection (ie, Lucene index dir)
     *
     * @return {user.home}/.grails/projects/{project-name}/searchable-index/{grails.env}
     * @param grailsApplication
     */
    public static String getDefaultConnection(GrailsApplication grailsApplication) {
        return new StringBuffer(System.getProperty("user.home")).
            append(File.separator).
            append(".grails").
            append(File.separator).
            append("projects").
            append(File.separator).
            append((String) grailsApplication.getMetadata().get("app.name")).
            append(File.separator).
            append("searchable-index").
            append(File.separator).
            append(GrailsUtil.getEnvironment()).
            toString();
    }
}
