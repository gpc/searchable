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

/**
 * Search and index using compass
 *
 * @author Maurice Nicholson
 */
class SearchableService {
    boolean transactional = true
    def compassGps
    def searchableMethodFactory

    /**
     * Returns a SearchResult object for the given query and options
     *
     * @param query the query string
     * @param options an optional Map of options, which may contain a "page" number and/or a requested result "size"
     * @return a SearchResult object
     */
    def search(Object[] args) {
        searchableMethodFactory.getMethod("search").invoke(*args)
    }

    /**
     * Search with the given query and return every result
     *
     * Note that this could be a bad idea if you expect a large number of hits
     *
     * @param query the search query
     * @return the matching results in order of relevance
     */
    def searchEvery(Object[] args) {
        searchableMethodFactory.getMethod("searchEvery").invoke(*args)
    }

    /**
     * Search with the given query and return every result
     *
     * Note that this could be a bad idea if you expect a large number of hits
     *
     * @param query the search query
     * @return the matching results in order of relevance
     */
    def searchTop(Object[] args) {
        searchableMethodFactory.getMethod("searchTop").invoke(*args)
    }

    /**
     * Returns the number of hits for the given query
     *
     * @param query the search query
     * @return the number of hits
     */
    def countHits(Object[] args) {
        searchableMethodFactory.getMethod("countHits").invoke(*args)
    }

    /**
     * Index
     */
    def indexAll(Object[] args) {
        searchableMethodFactory.getMethod("indexAll").invoke(*args)
    }

    /**
     * Index
     */
    def index(Object[] args) {
        searchableMethodFactory.getMethod("index").invoke(*args)
    }

    /**
     * Perform a bulk unindex (ie, delete from index)
     */
    def unindexAll(Object[] args) {
        searchableMethodFactory.getMethod("unindexAll").invoke(*args)
    }

    /**
     * Perform a bulk unindex (ie, delete from index)
     */
    def unindex(Object[] args) {
        searchableMethodFactory.getMethod("unindex").invoke(*args)
    }

    /**
     * Perform a bulk reindex (ie, delete from then save to index)
     */
    def reindexAll(Object[] args) {
        searchableMethodFactory.getMethod("reindexAll").invoke(*args)
    }

    /**
     * Perform a reindex (ie, delete from then save to index)
     */
    def reindex(Object[] args) {
        searchableMethodFactory.getMethod("reindex").invoke(*args)
    }

    /**
     * Start data mirroring
     */
    def startMirroring() {
        compassGps.start()
    }

    /**
     * Stop data mirroring
     */
    def stopMirroring() {
        compassGps.stop()
    }
}