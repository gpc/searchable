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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.spellcheck

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class SpellCheckTests extends SearchableFunctionalTestCase {
    def searchableService
    def compass

    public getDomainClasses() {
        return [Include, Exclude]
    }

    protected Map getCompassSettings() {
        ["compass.engine.spellcheck.schedule": "false"]
    }

    void testSpellCheckInclude() {
        new Include(id: 1l, value: "sea").index()
        searchableService.rebuildSpellingSuggestions()

        def suggestions = compass.spellCheckManager.suggestBuilder("see").subIndexes("include").suggest().suggestions
        assert suggestions[0] == 'sea'
    }

    void testSpellCheckExclude() {
        new Exclude(id: 1l, value: "sea").index()
        searchableService.rebuildSpellingSuggestions()

        // todo test only passes with spellCheck "exclude" on class - bug in Compass?
        def suggestions = compass.spellCheckManager.suggestBuilder("see").suggest().suggestions
        assert suggestions.length == 0
    }
}
