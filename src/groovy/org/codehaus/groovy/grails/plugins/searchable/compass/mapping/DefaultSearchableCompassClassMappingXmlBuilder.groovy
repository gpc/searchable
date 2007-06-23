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
    InputStream buildClassMappingXml(CompassMappingDescription description) {
        def writer = new StringWriter()
        def mkp = new groovy.xml.MarkupBuilder(writer)

        def className = description.mappedClass.name
        LOG.debug("Building Compass mapping XML for [${className}] from description [${description}]")
        def r = mkp."compass-core-mapping" {
            "class"(name: className, alias: SearchableCompassUtils.getDefaultAlias(description.mappedClass), root: description.root) {
                id(name: "id") // TODO support other "id" properties?
                for (entry in description.properties) {
                    def propertyName = entry.key
                    def mapping = entry.value
                    LOG.debug("Mapping '${className}.${propertyName}' as '${mapping}'")

                    def attrs = [name: propertyName]
                    if (mapping.reference) {
                        def refAttrs = new HashMap(attrs)
                        refAttrs.putAll(transformAttrNames(mapping.reference))
                        def invalidAttrs = refAttrs.keySet() - REFERENCE_ATTR_NAMES
                        if (invalidAttrs) {
                            throw new IllegalArgumentException("Invalid attribute(s) for reference element: ${invalidAttrs}. Valid attributes are ${REFERENCE_ATTR_NAMES}. Given attributes are ${refAttrs}")
                        }
                        reference(refAttrs)
                    }
                    if (mapping.component) {
                        def compAttrs = new HashMap(attrs)
                        compAttrs.putAll(transformAttrNames(mapping.component))
                        def invalidAttrs = compAttrs.keySet() - COMPONENT_ATTR_NAMES
                        if (invalidAttrs) {
                            throw new IllegalArgumentException("Invalid attribute(s) for reference element: ${invalidAttrs}. Valid attributes are ${COMPONENT_ATTR_NAMES}. Given attributes are ${compAttrs}")
                        }
                        component(compAttrs)
                    }
                    if (mapping.property) {
                        def metaDataAttrs = [:]
                        def tmp = transformAttrNames(mapping.property)
                        def invalidAttrs = tmp.keySet() - (PROPERTY_ATTR_NAMES + META_DATA_ATTR_NAMES)
                        if (invalidAttrs) {
                            throw new IllegalArgumentException("Invalid attribute(s) for property element: ${invalidAttrs}. Valid attributes are ${(PROPERTY_ATTR_NAMES + META_DATA_ATTR_NAMES).unique().sort()}. Given attributes are ${tmp}")
                        }
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