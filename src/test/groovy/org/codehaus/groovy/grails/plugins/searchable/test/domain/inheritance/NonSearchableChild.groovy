package org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance

class NonSearchableChild extends Parent {
    // Note overrides parent searchable definition 
    static searchable = false

    String nonSearchableChildProperty
}
