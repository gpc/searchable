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
package org.codehaus.groovy.grails.plugins.searchable.compass.mapping

import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.plugins.searchable.SearchableUtils
import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils

/**
 * 
 *
 * @author Maurice Nicholson
 */
class ClosureSearchableGrailsDomainClassCompassMappingDescriptionProvider extends AbstractSearchableGrailsDomainClassCompassMappingDescriptionProvider implements SearchableGrailsDomainClassCompassMappingDescriptionProvider {
    static final SEARCHABLE_PROPERTY_OPTIONS = ['accessor', 'analyzer', 'boost', 'converter', 'excludeFromAll', 'format', 'index', 'managedId', 'managedIdIndex', 'propertyConverter', 'reverse', 'store', 'termVector']
    static final SEARCHABLE_REFERENCE_OPTIONS = ['accessor', 'cascade', 'converter', 'refAlias', 'refComponentAlias']
    static final SEARCHABLE_COMPONENT_OPTIONS = ['accessor', 'cascade', 'converter', 'maxDepth', 'override', 'refAlias']
    static final SEARCHABLE_REFERENCE_MAPPING_OPTIONS = SEARCHABLE_REFERENCE_OPTIONS + ["reference", "component"]

    def grailsDomainClass
    def mappedClass
    def mappableProperties
    def searchableGrailsDomainClasses
    def only
    def except
    def mappedProperties

    /**
     * Get the CompassMappingDescription  for the given GrailsDomainClass and "searchable" value
     * @param grailsDomainClass the Grails domain class
     * @param searchableGrailsDomainClasses a collection of searchable GrailsDomainClass instances
     * @param searchableValue the searchable value: Closure in this case
     * @param excludedProperties a List of excluded properties NOT to map; ignored in this impl
     * @return the CompassMappingDescription
     */
    CompassMappingDescription getCompassMappingDescription(GrailsDomainClass grailsDomainClass, Collection searchableGrailsDomainClasses, Object closure, List excludedProperties) {
        // Reset state
        this.grailsDomainClass = grailsDomainClass
        this.mappedClass = grailsDomainClass.clazz
        this.mappableProperties = super.getMappableProperties(grailsDomainClass, true, searchableGrailsDomainClasses, excludedProperties)
        this.searchableGrailsDomainClasses = searchableGrailsDomainClasses
        this.only = null
        this.except = null
        this.mappedProperties = [:]

        // Build user-defined specific mappings
        closure.delegate = this
        closure()

        // Default any remaining mappable properties
        if (only && except) throw new IllegalArgumentException("Both 'only' and 'except' were used in '${mappedClass.getName()}.searchable': provide one or neither but not both")
        def searchableValue = only ? [only: only] : except ? [except: except] : true
        this.mappableProperties = super.getMappableProperties(grailsDomainClass, searchableValue, searchableGrailsDomainClasses, excludedProperties)

        for (property in mappableProperties) {
            if (!mappedProperties[property.name]) {
                mappedProperties.put(property.name, super.getDefaultPropertyMapping(property, grailsDomainClass, searchableGrailsDomainClasses))

            }
        }

        return new CompassMappingDescription(mappedClass: mappedClass, root: SearchableCompassUtils.isRoot(grailsDomainClass, searchableGrailsDomainClasses), properties: mappedProperties)
    }

    Object invokeMethod(String name, Object args) {
        def property = grailsDomainClass.getProperties().find { it.name == name }
        if (!property) throw new IllegalArgumentException("Unable to find property '${name}' used in '${mappedClass.getName()}.searchable'. The names of method invocations in the closure must match class properties (like GORM's 'constraints' closure)")

        if (!mappableProperties.any { it == property }) {
            throw new IllegalArgumentException("Unable to map '${mappedClass.getName()}.${property.name}'. It does not appear to a suitable 'searchable property' (normally simple types like Strings, Dates, Numbers, etc), 'searchable reference' (normally another domain class) or 'searchable component' (normally another domain class defined as a component, using the 'embedded' declaration). Is it a derived property (a getter method with no equivalent field) defined with 'def'? Try defining it with a more specific return type")
        }

        args = args[0]
        Class propertyType = property.getType()
        if (domainClassPropertyMappingStrategyFactory.getGrailsDomainClassPropertyMappingStrategy(property, searchableGrailsDomainClasses).property) {
            def invalidOptions = args.keySet() - SEARCHABLE_PROPERTY_OPTIONS 
            if (invalidOptions) {
                throw new IllegalArgumentException("One or more invalid options were defined in '${mappedClass.getName()}.searchable' for property '${name}'. " +
                    "'${mappedClass.getName()}.${name}' is assumed to be a 'searchable property', meaning you can only define the options allowed " +
                    "for searchable properties. The invalid options are: [${invalidOptions.join(', ')}]. Supported options for 'searchable properties' are [${SEARCHABLE_PROPERTY_OPTIONS.join(', ')}]")
            }
            mappedProperties[name] = [property: args]
            return
        }

        // Get default mapping
        def defaultMapping = super.getDefaultPropertyMapping(property, grailsDomainClass, searchableGrailsDomainClasses)
        assert defaultMapping, "Expected property ${property} to be mappable using default rules"
        def defaultTypeReference = defaultMapping.reference != null

        // Arg check
        def validOptions = defaultTypeReference ? SEARCHABLE_REFERENCE_MAPPING_OPTIONS : SEARCHABLE_COMPONENT_OPTIONS
        def invalidOptions = args.keySet() - validOptions
        if (invalidOptions) {
            throw new IllegalArgumentException("One or more invalid options were defined in '${mappedClass.getName()}.searchable' for property '${name}'. " +
                (defaultTypeReference ? "'${mappedClass.getName()}.${name}' can either be implicitly defined as a 'searchable reference', in which case you can use the options for searchable reference directly " +
                    "- eg, 'user(cascade: \"create,delete\")' - or you can defined it as a 'searchable reference' and/or 'searchable component' explicity and use their options within nested maps " +
                    " - eg, 'user(reference: [cascade: \"create,delete\"], component: [maxDepth : 3])'. Supported options are [${SEARCHABLE_REFERENCE_MAPPING_OPTIONS.join(', ')}]. " :
                    "'${mappedClass.getName()}.${name}' is implicity a 'searchable component'. Supported options are [${SEARCHABLE_COMPONENT_OPTIONS.join(', ')}]. ") +
                "The invalid options are: [${invalidOptions.join(', ')}]."
            )
        }

        def referenceOptions
        def componentOptions
        boolean implicitReference = true
        boolean implicitComponent = false
        if (defaultTypeReference) {
            assert defaultMapping.reference
            if (!args.reference && !args.component) {
                // eg "comments(cascade: true)" - assumed to be reference
                referenceOptions = new HashMap(defaultMapping.reference)
                referenceOptions.putAll(args)
            }
            if (args.reference) {
                // eg "comments(reference: true)" - explicit reference type or
                // eg "comments(reference: [cascase: 'all'])" - explicit reference options
                referenceOptions = new HashMap(defaultMapping.reference)
                if (args.reference != true) {
                    referenceOptions.putAll(args.reference)
                }
                implicitReference = false
            }
            if (args.component) {
                // eg "comments(component: true)" - explicit component type or
                // eg "comments(component: [cascase: 'all'])" - explicit component options
                componentOptions = new HashMap(defaultMapping.reference)
                if (args.component != true) {
                    componentOptions.putAll(args.component)
                }
            }
        } else {
            implicitComponent = true
            assert defaultMapping.component
            componentOptions = new HashMap(defaultMapping.component)
            componentOptions.putAll(args)
        }

        if (!referenceOptions && !componentOptions) {
            return
        }
        mappedProperties[name] = [:]

        // Check for invalid options
        if (componentOptions) {
            invalidOptions = componentOptions.keySet() - SEARCHABLE_COMPONENT_OPTIONS
            if (invalidOptions) {
                throw new IllegalArgumentException("One or more invalid options were defined in '${mappedClass.getName()}.searchable' for property '${name}'. " +
                    "'${mappedClass.getName()}.${name}' is ${implicitComponent ? 'implicitly' : 'defined to be'} a 'searchable component', meaning you can only define the options allowed " +
                    "for searchable references. The invalid options are: [${invalidOptions.join(', ')}]. Supported options for 'searchable properties' are [${SEARCHABLE_COMPONENT_OPTIONS.join(', ')}]")
            }
            mappedProperties[name].component = componentOptions
        }
        if (referenceOptions) {
            invalidOptions = referenceOptions.keySet() - SEARCHABLE_REFERENCE_OPTIONS
            if (invalidOptions) {
                throw new IllegalArgumentException("One or more invalid options were defined in '${mappedClass.getName()}.searchable' for property '${name}'. " +
                    "'${mappedClass.getName()}.${name}' is ${implicitReference ? 'implicitly' : 'declared to be'} a 'searchable reference', meaning you can only define the options allowed " +
                    "for searchable references. The invalid options are: [${invalidOptions.join(', ')}]. Supported options for 'searchable properties' are [${SEARCHABLE_REFERENCE_OPTIONS.join(', ')}]")
            }
            mappedProperties[name].reference = referenceOptions
        }
    }

    /**
     * Does the implementation handle th given "searchable" value type?
     * @param searchableValue a searchable value
     * @return true for Closure for this class
     */
    public boolean handlesSearchableValue(Object searchableValue) {
        searchableValue instanceof Closure
    }
}