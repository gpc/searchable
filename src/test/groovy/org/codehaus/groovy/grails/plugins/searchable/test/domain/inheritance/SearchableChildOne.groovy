package org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance

class SearchableChildOne extends Parent {
    // Note no searchable property -- inherited value of true

    String childOneProperty
}