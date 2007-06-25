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
package pages

import components.*

/**
 * Models the Searchable search page
 *
 * @author Maurice Nicholson
 */
class SearchableSearchPage extends AbstractPage {

    /**
     * Get the search form
     */
    FormComponent getSearchForm() {
        new FormComponent(ant: ant, formName: "searchableForm")
    }

    /**
     * Get the search results (if any)
     */
    SearchResultsComponent getSearchResults() {
        new SearchResultsComponent(ant: ant)
    }

    /**
     * Get the pagination
     */
    PaginationComponent getPagination() {
        new PaginationComponent(ant: ant, paginationLinkPage: SearchableSearchPage)
    }
}