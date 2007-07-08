package org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance

class SearchableChildOne extends Parent {
    // Note no searchable property -- but parent is searchable inherits parent's mapping plus defaults for own properies

    String childOneProperty
}