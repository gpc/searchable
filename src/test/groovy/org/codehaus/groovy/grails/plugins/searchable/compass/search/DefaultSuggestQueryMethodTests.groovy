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
package org.codehaus.groovy.grails.plugins.searchable.compass.search

import org.codehaus.groovy.grails.plugins.searchable.compass.search.DefaultSuggestQueryMethod.SuggestedQueryStringBuilder as SuggestedQueryStringBuilder

/**
 * @author Maurice Nicholson
 */
class DefaultSuggestQueryMethodTests extends GroovyTestCase {

    void testUserFriendly() {
        // suggested queries are typically re-written to their translated form, but
        // if we are going to show the suggested query to the user, then we should try
        // and make it look like their original query
        assert new SuggestedQueryStringBuilder("a b c", "+a +be +c").toSuggestedQueryString() == "a be c"

        // default operator = AND; + are added
        assert new SuggestedQueryStringBuilder("this that", "+thus +that").toSuggestedQueryString() == "thus that"

        // default operator = AND; OR is removed
        assert new SuggestedQueryStringBuilder("this OR that", "thus that").toSuggestedQueryString() == "thus OR that"
        assert new SuggestedQueryStringBuilder("this AND that", "+thus +that").toSuggestedQueryString() == "thus AND that"

        // suggested query converts boost to float
        assert new SuggestedQueryStringBuilder("see^5", "sea^5.0").toSuggestedQueryString() == "sea^5"

        // with brackets
        assert new SuggestedQueryStringBuilder("see OR (what white)", "sea (+white +white)").toSuggestedQueryString() == "sea OR (white white)"
        assert new SuggestedQueryStringBuilder("see OR (white what) lite", "sea (+white +white) light").toSuggestedQueryString() == "sea OR (white white) light"

        // with field qualifiers
        assert new SuggestedQueryStringBuilder("site:bbc.co.uk cooking title:donuts", "site:bbc.co.uk cooking title:doughnuts").toSuggestedQueryString() == "site:bbc.co.uk cooking title:doughnuts"
    }

    void testUserFriendlyFalse() {
        // when set to false, just returns the suggested query - case is not emulated

        assert new SuggestedQueryStringBuilder("a b c", "+a +be +c").userFriendly(false).toSuggestedQueryString() == "+a +be +c"

        assert new SuggestedQueryStringBuilder("Very BIG dog", "+very +big +dog").userFriendly(false).toSuggestedQueryString() == "+very +big +dog"
    }

    void testEmulateCapitilsation() {
        // basic emulation of captialised words
        // this option is enabled by default

        def suggestion = new SuggestedQueryStringBuilder("REST web services", "rest web services").toSuggestedQueryString()
        assert suggestion == "REST web services", suggestion

        assert new SuggestedQueryStringBuilder("Micheal Carltone", "michael carlton").toSuggestedQueryString() == "Michael Carlton"
    }

    void testEmulateCapitalsFalse() {
        assert new SuggestedQueryStringBuilder("REST web services", "rest web services").emulateCapitalisation(false).toSuggestedQueryString() == "rest web services"

        assert new SuggestedQueryStringBuilder("Micheal Carltone", "michael carlton").emulateCapitalisation(false).toSuggestedQueryString() == "michael carlton"
    }

    void testEscape() {
        // note suggested query does not have brackets since this is typically coming from the query suggestion engine
        // which strips away such characters
        assert new SuggestedQueryStringBuilder("[this is a bad query]", "this is also bad query").escape(true).toSuggestedQueryString() == "[this is also bad query]", new SuggestedQueryStringBuilder("[this is a bad query]", "this is also bad query").escape(true).toSuggestedQueryString()
    }

    void testEscapeFalse() {
        shouldFail {
            new SuggestedQueryStringBuilder("[this is a bad query]", "[this is also bad query]").escape(false).toSuggestedQueryString()
        }
    }

    void testAllowSame() {
        assert new SuggestedQueryStringBuilder("foo bar baz", "foo bar baz").allowSame(true).toSuggestedQueryString() == "foo bar baz"
    }

    void testAllowSameFalse() {
        assert new SuggestedQueryStringBuilder("foo bar baz", "foo bar baz").allowSame(false).toSuggestedQueryString() == null
    }
}