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
package org.codehaus.groovy.grails.plugins.searchable.util

/**
 * @author Maurice Nicholson
 */
class StringQueryUtilsTests extends GroovyTestCase {

    void testHighlightTermDiffs() {
        assert StringQueryUtils.highlightTermDiffs("aaa bbb ccc", "aaa bee ccc") == "aaa <b><i>bee</i></b> ccc"

        assert StringQueryUtils.highlightTermDiffs("aaa somefield:bbb ccc", "aaa somefield:bee ccc") == "aaa somefield:<b><i>bee</i></b> ccc"

        assert StringQueryUtils.highlightTermDiffs("Aaa somefield:BBB ccc", "Aaa somefield:BEE ccc") == "Aaa somefield:<b><i>BEE</i></b> ccc"
    }

    void testHighlightTermDiffsWithHighlightPattern() {
        assert StringQueryUtils.highlightTermDiffs("aaa bbb ccc", "aaa bee ccc", ">>{0}<<") == "aaa >>bee<< ccc"
    }
}