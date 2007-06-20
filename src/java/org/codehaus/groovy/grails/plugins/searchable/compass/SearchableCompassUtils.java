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
     * Get the Compass alias for the given Class
     *
     * @param clazz the class
     * @return the Compass alias
     */
    public static String getDefaultAlias(Class clazz) {
        Assert.notNull(clazz, "clazz cannot be null");
        String alias = clazz.getName();
        if (alias.indexOf(".") != -1) {
            alias = alias.substring(alias.lastIndexOf(".") + 1, alias.length());
        }
        return alias;
    }

    /**
     * Is the given GrailsDomainClass a root class in the Compass mapping?
     * @param grailsDomainClass the domain class to check for
     * @param searchableGrailsDomainClasses a collection of searchable GrailsDomainClass instances
     * @return true if it's an embedded class in another domain class
     */
    public static boolean isRoot(GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses) {
        // TODO log warning when used as both component and non-component
        for (Iterator iter = searchableGrailsDomainClasses.iterator(); iter.hasNext(); ) {
            GrailsDomainClass otherDomainClass = (GrailsDomainClass) iter.next();
            if (grailsDomainClass.equals(otherDomainClass)) {
                continue;
            }
            for (int i = 0; i < otherDomainClass.getProperties().length; i++) {
                GrailsDomainClassProperty property = otherDomainClass.getProperties()[i];
                if (property.getType() != null && property.getType().equals(grailsDomainClass.getClazz()) && property.isEmbedded()) {
                    return false;
                }
            }
        }
        return true;
    }

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
