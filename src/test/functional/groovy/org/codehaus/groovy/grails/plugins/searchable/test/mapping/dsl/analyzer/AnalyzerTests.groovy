/*
 * Copyright 2008 the original author or authors.
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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.analyzer

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class AnalyzerTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [A]
    }

    // todo use SearchableConfiguration instead of directly setting compass-settings

    void testAnalyzer() {
        def a = new A(id: 1L, value: "the quick brown fox jumped over the lazy dog")
        a.index()
        a = A.searchTop("value:the") // "the" is normally a stop word, so removed by the standard analyzer
        assert a
    }
}