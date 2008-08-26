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
package org.codehaus.groovy.grails.plugins.searchable.test.mapping.dsl.property.excludefromall

import org.codehaus.groovy.grails.plugins.searchable.test.SearchableFunctionalTestCase

/**
 * @author Maurice Nicholson
 */
class ExcludeFromAllTests extends SearchableFunctionalTestCase {

    public getDomainClasses() {
        return [No, NoAnalyzed, Yes]
    }

    void testExcludeFromAllNo() {
        new No(id: 1l, value: "exclude from all").index()
        assert No.searchTop("exclude")
        assert No.searchTop("value:exclude")
    }

    void testExcludeFromAllNoAnalyzed() {
        new NoAnalyzed(id: 1l, value: "exclude from all").index()
        assert NoAnalyzed.searchTop("exclude") // tokenized in all
        assert NoAnalyzed.searchTop("value:exclude") == null // NOT tokenized for property
        assert NoAnalyzed.searchTop {
            term("value", "exclude from all")
        }
    }

    void testExcludeFromAllYes() {
        new Yes(id: 1l, value: "exclude from all").index()
        assert Yes.searchTop("exclude") == null
        assert Yes.searchTop("value:exclude")
    }
}
