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
package org.codehaus.groovy.grails.plugins.searchable.compass.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.plugins.searchable.compass.converter.DefaultCompassConverterLookupHelper;
import org.compass.core.config.CompassConfigurationFactory;
import org.compass.core.impl.DefaultCompass;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * @author Maurice Nicholson
 */
public abstract class SearchableGrailsDomainClassMappingStrategyFactory {
    private static final Log LOG = LogFactory.getLog(SearchableGrailsDomainClassMappingStrategyFactory.class);

    public static SearchableGrailsDomainClassMappingStrategy[] getSearchableGrailsDomainClassMappingStrategies(ResourceLoader resourceLoader, Map defaultFormats, List defaultExcludedProperties, SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder) {
        List strategies = new ArrayList();
        strategies.add(getXmlMappingStrategy(resourceLoader));
        strategies.add(getSearchableClassPropertyMappingStrategy(defaultFormats, defaultExcludedProperties, compassClassMappingXmlBuilder));
        SearchableGrailsDomainClassMappingStrategy annotationsMappingStrategy = getAnnotationsMappingStrategy();
        if (annotationsMappingStrategy != null) {
            strategies.add(annotationsMappingStrategy);
        }

        return (SearchableGrailsDomainClassMappingStrategy[]) strategies.toArray(new SearchableGrailsDomainClassMappingStrategy[strategies.size()]);
    }

    public static SearchableGrailsDomainClassMappingStrategy getAnnotationsMappingStrategy() {
        try {
            return (SearchableGrailsDomainClassMappingStrategy) ClassUtils.forName("org.codehaus.groovy.grails.plugins.searchable.compass.mapping.CompassAnnotationSearchableGrailsDomainClassMappingStrategy").newInstance();
        } catch (Exception ex) {
            LOG.warn("CompassAnnotationSearchableGrailsDomainClassMappingStrategy is unavailable");
        }
        return null;
    }

    public static CompassMappingXmlSearchableGrailsDomainClassMappingStrategy getXmlMappingStrategy(ResourceLoader resourceLoader) {
        CompassMappingXmlSearchableGrailsDomainClassMappingStrategy mappingXmlMappingStrategy = new CompassMappingXmlSearchableGrailsDomainClassMappingStrategy();
        mappingXmlMappingStrategy.setResourceLoader(resourceLoader);
        return mappingXmlMappingStrategy;
    }

    public static SearchableClassPropertySearchableGrailsDomainClassMappingStrategy getSearchableClassPropertyMappingStrategy(Map defaultFormats, List defaultExcludedProperties, SearchableCompassClassMappingXmlBuilder compassClassMappingXmlBuilder) {
        SearchableGrailsDomainClassCompassMappingDescriptionProviderManager mappingDescriptionProvider = getMappingDescriptionProviderManager(defaultExcludedProperties, defaultFormats);

        SearchableClassPropertySearchableGrailsDomainClassMappingStrategy searchableClassPropertyMappingStrategy = new SearchableClassPropertySearchableGrailsDomainClassMappingStrategy();
        searchableClassPropertyMappingStrategy.setCompassClassMappingXmlBuilder(compassClassMappingXmlBuilder);
        searchableClassPropertyMappingStrategy.setMappingDescriptionProviderManager(mappingDescriptionProvider);
        return searchableClassPropertyMappingStrategy;
    }

    public static SearchableGrailsDomainClassCompassMappingDescriptionProviderManager getMappingDescriptionProviderManager(List defaultExcludedProperties, Map defaultFormats) {
        DefaultCompassConverterLookupHelper converterLookupHelper = new DefaultCompassConverterLookupHelper();
        converterLookupHelper.setConverterLookup(((DefaultCompass) CompassConfigurationFactory.newConfiguration().setConnection("ram://dummy").buildCompass()).getConverterLookup());

        GrailsDomainClassPropertyMappingStrategyFactory mappingStrategyFactory = new GrailsDomainClassPropertyMappingStrategyFactory();
        mappingStrategyFactory.setDefaultFormats(defaultFormats);
        mappingStrategyFactory.setConverterLookupHelper(converterLookupHelper);

        SearchableGrailsDomainClassCompassMappingDescriptionProviderManager mappingDescriptionProvider = new SearchableGrailsDomainClassCompassMappingDescriptionProviderManager();
        mappingDescriptionProvider.setDefaultExcludedProperties(defaultExcludedProperties);
        SearchableGrailsDomainClassCompassMappingDescriptionProvider[] searchableGrailsDomainClassCompassMappingDescriptionProviders;
        try {
            SimpleSearchableGrailsDomainClassCompassMappingDescriptionProvider simpleMappingProvider = new SimpleSearchableGrailsDomainClassCompassMappingDescriptionProvider();
            simpleMappingProvider.setDomainClassPropertyMappingStrategyFactory(mappingStrategyFactory);
            AbstractSearchableGrailsDomainClassCompassMappingDescriptionProvider closureMappingProvider = (AbstractSearchableGrailsDomainClassCompassMappingDescriptionProvider) ClassUtils.forName("org.codehaus.groovy.grails.plugins.searchable.compass.mapping.ClosureSearchableGrailsDomainClassCompassMappingDescriptionProvider").newInstance();
            closureMappingProvider.setDomainClassPropertyMappingStrategyFactory(mappingStrategyFactory);
            searchableGrailsDomainClassCompassMappingDescriptionProviders = new SearchableGrailsDomainClassCompassMappingDescriptionProvider[] {
                simpleMappingProvider, closureMappingProvider
            };
        } catch (Exception ex) {
            // Log and throw runtime exception
            LOG.error("Failed to find or create closure mapping provider class instance", ex);
            throw new IllegalStateException("Failed to find or create closure mapping provider class instance: " + ex);
        }
        mappingDescriptionProvider.setSearchableGrailsDomainClassCompassMappingDescriptionProviders(searchableGrailsDomainClassCompassMappingDescriptionProviders);
        return mappingDescriptionProvider;
    }
}
