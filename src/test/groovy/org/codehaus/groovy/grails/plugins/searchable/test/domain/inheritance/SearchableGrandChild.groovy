package org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance

class SearchableGrandChild extends SearchableChildOne {
    static searchable = true // inherits parent definitions and maps this class with default rules 

    String grandChildProperty
}
