package org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance

class Associate {
    static searchable = true

    Long id
    Long version
    String name
    Parent polyInstance
    SearchableChildOne specificInstance
}