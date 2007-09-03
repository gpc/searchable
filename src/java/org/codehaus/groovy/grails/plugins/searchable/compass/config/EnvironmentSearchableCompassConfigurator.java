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
package org.codehaus.groovy.grails.plugins.searchable.compass.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.util.Assert;

import java.beans.PropertyEditor;
import java.util.Iterator;
import java.util.Map;

/**
 * Responsible for setting the Compass environment, most importantly the "connection" (ie, Lucene index dir),
 * as well as any other general properties
 *
 * @author Maurice Nicholson
 */
public class EnvironmentSearchableCompassConfigurator implements SearchableCompassConfigurator {
    private static final Log LOG = LogFactory.getLog(EnvironmentSearchableCompassConfigurator.class);

    private String connection;
    private Map compassSettings;
    private GrailsApplication grailsApplication;

    /**
     * Configure the Compass environment
     *
     * @param compassConfiguration the runtime config instance
     * @param configurationContext a context allowing flexible parameter passing
     */
    public void configure(CompassConfiguration compassConfiguration, Map configurationContext) {
        // Configure connection?
        if (compassConfiguration.getSettings().getSetting(CompassEnvironment.CONNECTION) == null) {
            String conn = connection;
            if (conn == null) {
                LOG.debug("No connection specified, using default");
                conn = SearchableCompassUtils.getDefaultConnection(grailsApplication);
            }
            LOG.info("Setting Compass connection to [" + conn + "]");
            compassConfiguration.setConnection(conn);
        }

        if (compassSettings != null) {
            ToStringConverterHelper helper = new ToStringConverterHelper();
            for (Iterator iter = compassSettings.keySet().iterator(); iter.hasNext(); ) {
                String name = iter.next().toString();
                String value = helper.convertToStringIfNecessary(compassSettings.get(name));
                LOG.debug("Setting Compass setting [" + name + "] = [" + value + "]");
                compassConfiguration.setSetting(name, value);
            }
        }
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public Map getCompassSettings() {
        return compassSettings;
    }

    public void setCompassSettings(Map compassSettings) {
        this.compassSettings = compassSettings;
    }

    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication;
    }

    /**
     * Helper for converting objects to Strings
     */
    private static class ToStringConverterHelper extends PropertyEditorRegistrySupport {
        public ToStringConverterHelper() {
            registerDefaultEditors();
        }

        public String convertToStringIfNecessary(Object value) {
            Assert.notNull(value, "value cannot be null");
            if (value instanceof String) {
                return (String) value;
            }
            PropertyEditor propertyEditor = getDefaultEditor(value.getClass());
            propertyEditor.setValue(value);
            return propertyEditor.getAsText();
        }
    }
}
