package org.codehaus.groovy.grails.plugins.searchable.test.domain.inheritance

class SearchableChildTwo extends Parent {
    // Note defines custom searchable mapping
    static searchable = {
        childTwoProperty(index: 'un_tokenized')
    }

    String childTwoProperty
}