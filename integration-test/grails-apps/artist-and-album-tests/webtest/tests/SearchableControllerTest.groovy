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
import support.*
import pages.*
import components.*

/**
 * Tests the searchable controller
 *
 * @author Maurice Nicholson
 */
class SearchableControllerTest extends grails.util.WebTest {

    // Unlike unit tests, functional tests are often sequence dependent.
    // Specify that sequence here.
    void suite() {
        testSearchableController()
        // add tests for more operations here
    }

    def testSearchableController() {
        webtest('SearchableController search'){

            def browser = new Browser(ant: ant)
            def page
            group(description: "search") {
                page = browser.open("searchable", SearchableSearchPage)
                page.verifyText("Grails Searchable Plugin")

                def form = page.searchForm
                page = form.submit(q: 'b*', SearchableSearchPage)

                page.searchResults.verifyNumberOfResults(10)

                page.pagination.verifyCurrentPage(1)
                page = page.pagination.clickNextLink()
                page.verifyText("Grails Searchable Plugin")

                page.pagination.verifyCurrentPage(2)
                page.searchResults.verifyPresent()

                page = page.pagination.clickPreviousLink()
                page.verifyText("Grails Searchable Plugin")
                page.pagination.verifyCurrentPage(1)
                page.searchResults.verifyNumberOfResults(10)

                page = page.pagination.clickLinkForPage(2)
                page.verifyText("Grails Searchable Plugin")
                page.pagination.verifyCurrentPage(2)
                page.searchResults.verifyPresent()
            }

            group(description: "invalid query") {
                page = browser.open("searchable", SearchableSearchPage)
                page.verifyText("Grails Searchable Plugin")

                def form = page.searchForm
                page = form.submit(q: '"this query causes a "parse exception"', SearchableSearchPage)

                page.verifyMessages("Your query", "is not valid", "Suggestions", "Fix the query", "Remove special characters", "Escape special characters")
            }

            group(description: "no results") {
                page = browser.open("searchable", SearchableSearchPage)
                page.verifyText("Grails Searchable Plugin")

                def form = page.searchForm
                page = form.submit(q: 'lalalalalala', SearchableSearchPage)

                page.verifyMessages("Nothing matched your query", "lalalalalala")
            }

            // TODO options: max/offset/escape/...

            // TODO pagination
        }
    }
}