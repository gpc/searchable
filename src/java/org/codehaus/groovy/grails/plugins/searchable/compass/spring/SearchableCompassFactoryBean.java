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
package org.codehaus.groovy.grails.plugins.searchable.compass.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.plugins.searchable.compass.config.SearchableCompassConfigurator;
import org.codehaus.groovy.grails.plugins.searchable.compass.converter.StringMapConverter;
import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassConfigurationFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.DisposableBean;

import java.util.HashMap;

/**
 * A pluggable Spring factory bean for Compass
 *
 * @author Maurice Nicholson
 */
public class SearchableCompassFactoryBean implements FactoryBean, DisposableBean {
    private static final Log LOG = LogFactory.getLog(SearchableCompassFactoryBean.class);

    private SearchableCompassConfigurator searchableCompassConfigurator;
    private Compass compass;

    public Object getObject() throws Exception {
        if (compass == null) {
            compass = buildCompass();
        }
        return compass;
    }

    public Class getObjectType() {
        return Compass.class;
    }

    public boolean isSingleton() {
        return true;
    }

    private Compass buildCompass() {
        LOG.debug("Building new Compass");

        CompassConfiguration configuration = CompassConfigurationFactory.newConfiguration();

        // TODO find a better place for this
        // register custom converters
        configuration.registerConverter(StringMapConverter.CONVERTER_NAME, new StringMapConverter());

        // register analyzers used internally
        configuration.getSettings().setSetting("compass.engine.analyzer.searchableplugin_whitespace.type", "whitespace");
        configuration.getSettings().setSetting("compass.engine.analyzer.searchableplugin_simple.type", "simple");

        searchableCompassConfigurator.configure(configuration, new HashMap());

        Compass compass = configuration.buildCompass();

        LOG.debug("Done building Compass");
        return compass;
    }

    public SearchableCompassConfigurator getSearchableCompassConfigurator() {
        return searchableCompassConfigurator;
    }

    public void setSearchableCompassConfigurator(SearchableCompassConfigurator searchableCompassConfigurator) {
        this.searchableCompassConfigurator = searchableCompassConfigurator;
    }

    /**
     * Destroy the Compass instance (if created), typically called when shutting down the Spring
     * application context.
     *
     * Just calls {@link org.compass.core.Compass#close()} 
     *
     * @throws Exception
     */
    public void destroy() throws Exception {
        if (compass != null) {
            compass.close();
        }
    }
}
