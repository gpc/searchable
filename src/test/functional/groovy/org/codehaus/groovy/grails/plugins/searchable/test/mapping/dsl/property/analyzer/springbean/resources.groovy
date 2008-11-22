package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.analyzer.springbean

import org.apache.lucene.analysis.standard.StandardAnalyzer

beans = {
    'default'(StandardAnalyzer, new HashSet()) // there are now no stop words
}
