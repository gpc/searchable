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

import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Builds the Compass class mapping XML. Done with Groovy cos it's so easy compared with Java
 *
 * @author Maurice Nicholson
 */
class DefaultSearchableCompassClassMappingXmlBuilder implements SearchableCompassClassMappingXmlBuilder {
    private static final Log LOG = LogFactory.getLog(DefaultSearchableCompassClassMappingXmlBuilder.class)

    /** Legal attribute names for known XML elements */
    static final PROPERTY_ATTR_NAMES = ['accessor', 'analyzer', 'boost', 'class', 'converter', 'exclude-from-all', 'managed-id', 'managed-id-index', 'managed-id-converter', 'name', 'override']
    static final META_DATA_ATTR_NAMES = ['analyzer', 'boost', 'converter', 'exclude-from-all', 'format', 'index', 'reverse', 'store', 'term-vector']
    static final REFERENCE_ATTR_NAMES = ['accessor', 'cascade', 'converter', 'name', 'ref-alias', 'ref-comp-alias']
    static final COMPONENT_ATTR_NAMES = ['accessor', 'cascade', 'converter', 'max-depth', 'name', 'override', 'ref-alias']

    /** Mapping from input option names to output XML attribute names */
    static final OPTION_ATTR_MAP = [type: 'class', propertyConverter: 'converter', refComponentAlias: 'ref-comp-alias']

    /**
     * Returns an InputStream for the given mapping description
     *
     * @param description describes the class mapping
     * @return an InputStream for the Compass class mapping XML
     */
    InputStream buildClassMappingXml(CompassClassMapping description) {
        def writer = new StringWriter()
        def mkp = new groovy.xml.MarkupBuilder(writer)

        def className = description.mappedClass.name
        LOG.debug("Building Compass mapping XML for [${className}] from description [${description}]")
        def r = mkp."compass-core-mapping" {
            def classAttrs = [name: className, alias: description.alias, root: description.root]
            if (description.poly) {
                classAttrs.poly = true
            }
            if (description.extend) {
                classAttrs['extends'] = description.extend
            }
            "class"(classAttrs) {
                id(name: "id") // TODO support other "id" properties?

                for (constantMetaData in description.constantMetaData) {
                    def metaData = new HashMap(constantMetaData) // clone to avoid corruption
                    def name = metaData.name
                    def attributes = transformAttrNames(metaData.attributes)
                    validateAttributes("meta-data", attributes, META_DATA_ATTR_NAMES)
                    constant {
                        'meta-data'(attributes, name)
                        for (value in metaData.values) {
                            'meta-data-value'(value)
                        }
                    }
                }

                for (propertyMapping in description.propertyMappings) {
                    def propertyName = propertyMapping.propertyName
                    def attributes = propertyMapping.attributes
                    LOG.debug("Mapping '${className}.${propertyName}' with attributes ${attributes}")

                    def attrs = [name: propertyName]
                    if (propertyMapping.reference) {
                        def refAttrs = new HashMap(attrs)
                        refAttrs.putAll(transformAttrNames(attributes))
                        validateAttributes("reference", refAttrs, REFERENCE_ATTR_NAMES)
                        reference(refAttrs)
                    }
                    if (propertyMapping.component) {
                        def compAttrs = new HashMap(attrs)
                        compAttrs.putAll(transformAttrNames(attributes))
                        validateAttributes("component", compAttrs, COMPONENT_ATTR_NAMES)
                        component(compAttrs)
                    }
                    if (propertyMapping.property) {
                        def metaDataAttrs = [:]
                        def tmp = transformAttrNames(attributes)
                        validateAttributes("property", tmp, PROPERTY_ATTR_NAMES + META_DATA_ATTR_NAMES)
                        tmp.each { k, v ->
                            if (META_DATA_ATTR_NAMES.contains(k)) {
                                metaDataAttrs[k] = v
                            } else {
                                assert PROPERTY_ATTR_NAMES.contains(k)
                                attrs[k] = v
                            }
                        }
                        property(attrs) {
                            "meta-data"(metaDataAttrs, propertyName)
                        }
                    }
                }
           }
       }

       def xml = """<?xml version="1.0"?>
<!DOCTYPE compass-core-mapping PUBLIC
   "-//Compass/Compass Core Mapping DTD 1.0//EN"
   "http://www.opensymphony.com/compass/dtd/compass-core-mapping.dtd">
""" + writer.toString()

//       System.out.println("${className} xml [${xml}]")
       LOG.debug("${className} xml [${xml}]")
       return new ByteArrayInputStream(xml.getBytes())
    }

    private validateAttributes(elementName, attributeMap, validAttrNames) {
        def invalidAttrs = attributeMap.keySet() - validAttrNames
        if (invalidAttrs) {
            throw new IllegalArgumentException("Invalid attribute(s) for $elementName element: ${invalidAttrs}. Valid attributes are ${validAttrNames.unique().sort()}. Given attributes are ${attributeMap}")
        }
    }

    private transformAttrNames(value) {
        if (value == true) {
            return [:]
        }
        assert value instanceof Map, "attrs should be value of Map"
        def attrs = [:]
        value.each { k, v ->
            if (OPTION_ATTR_MAP[k]) {
                k = OPTION_ATTR_MAP[k]
            } else {
                k = convertCamelCaseToLowerCaseDashed(k)
            }
            attrs[k] = v
        }
        return attrs
    }

    // TODO extract to utils class if needed elsewhere
    public convertCamelCaseToLowerCaseDashed(String string) {
        def buf = new StringBuffer()
        for (i in 0..<string.size()) {
            def ch = string[i]
            if (Character.isUpperCase(ch as char)) {
                if (i != 0) {
                    buf.append("-")
                }
                ch = ch.toLowerCase()
            }
            buf.append(ch)
        }
        return buf.toString()
    }
}