package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.propertyconverter.springbean

import org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.propertyconverter.springbean.ValueConverter

beans = {
    // implements Conveter, so automatically registered with its name
    myValueConverter(ValueConverter)
}