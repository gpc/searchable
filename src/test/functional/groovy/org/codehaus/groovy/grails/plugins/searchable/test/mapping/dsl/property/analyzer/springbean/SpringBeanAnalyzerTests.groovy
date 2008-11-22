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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.analyzer.springbean

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class SpringBeanAnalyzerTests extends SearchableFunctionalTestCase {
    def searchableService

    public getDomainClasses() {
        return [OverridenDefault]
    }

    void testAnalyzerWithSpringBeanAnalyzer() {
        new OverridenDefault(id: 1l, value: "the quick brown fox jumped over the lazy dogs").index()

        def hits = searchableService.searchEvery("value:quick")
        assert hits.size() == 1, hits.size()

        // stop words have been eliminated in the overriden default analyzer
        hits = searchableService.searchEvery("value:the")
        assert hits.size() == 1, hits.size()
    }
}