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

import java.text.*

import org.compass.core.*
import org.compass.core.config.*

/**
 *
 *
 * @author Maurice Nicholson
 */
class GroovyCompassQueryBuilderTests extends GroovyTestCase {
    def compass

    void setUp() {
        compass = getCompass()
    }

    void tearDown() {
        compass.close()
        compass = null
    }

    void testDocumentationExamples() {
        withCompassSession { compassSession ->
            def cqb = compassSession.queryBuilder()
            def builder = new GroovyCompassQueryBuilder(cqb)
            def query

            query = builder.buildQuery {                    // <-- create an implicit boolean query
                lt('pages', 50)         // <-- uses CompassQueryBuilder#lt, and adds a boolean "should" clause
                term('type', 'poetry')  // <-- uses CompassQueryBuilder#term, and adds a boolean "should" clause
            }
            assert query.toString() == "pages:[* TO 50} type:poetry", query.toString()

            query = builder.buildQuery {                          // <-- creates an implicit boolean query
                lt('pages', 50)               // <-- uses CompassQueryBuilder#lt, and adds a boolean "should"
                must(term('type', 'poetry'))  // <-- uses CompassQueryBuilder#term, adds a boolean "must"
            }
            assert query.toString() == "pages:[* TO 50} +type:poetry", query.toString()

            query = builder.buildQuery {                           // <-- creates an implicit boolean query
                lt('pages', 50)                // <-- uses CompassQueryBuilder#lt, and adds a boolean "should"
                must(term('type', 'poetry'))   // <-- uses CompassQueryBuilder#term, adds a boolean "must"
                mustNot(term('theme', 'war'))  // <-- uses CompassQueryBuilder#term, adds a boolean "mustNot"
            }
            assert query.toString() == "pages:[* TO 50} +type:poetry -theme:war", query.toString()

            query = builder.buildQuery {                                   // <-- creates an implicit boolean query
                must(queryString("all hands on deck")) // <-- uses CompassQueryBuilder#queryString, and adds a boolean must
                lt('pages', 50)                        // <-- uses CompassQueryBuilder#lt, and adds a boolean "should"
                must(term('type', 'poetry'))           // <-- uses CompassQueryBuilder#term, adds a boolean "must"
                mustNot(term('theme', 'war'))          // <-- uses CompassQueryBuilder#term, adds a boolean "mustNot"
            }
            // "on" is obviously a stop word for the default analyzer, so it's removed
            assert query.toString() == "+(+all +hands +deck) pages:[* TO 50} +type:poetry -theme:war", query.toString()

            query = builder.buildQuery {                                      // <-- creates an implicit boolean query
                must(queryString("all hands on deck") {   // <-- creates a nested CompassQueryStringBuilder context
                    useAndDefaultOperator()               // <-- calls CompassQueryStringBuilder#useAndDefaultOperator
                    setDefaultSearchProperty('body')      // <-- calls CompassQueryStringBuilder#setDefaultSearchProperty
                })                                        // <-- added as boolean must to surrounding boolean
                lt('pages', 50)                           // <-- uses CompassQueryBuilder#lt, and adds a boolean "should"
                must(term('type', 'poetry'))              // <-- uses CompassQueryBuilder#term, adds a boolean "must"
                mustNot(term('theme', 'war'))             // <-- uses CompassQueryBuilder#term, adds a boolean "mustNot"
            }
            assert query.toString() == "+(+body:all +body:hands +body:deck) pages:[* TO 50} +type:poetry -theme:war", query.toString()

            query = builder.buildQuery {                                    // <-- creates an implicit boolean query
                must(queryString("all hands on deck", [useAndDefaultOperator: true, defaultSearchProperty: 'body']))
                    // ^^ add a "must" nested query string, calling useAndDefaultOperator() and setDefaultSearchProperty('body')
                    //    on the CompassQueryStringBuilder
                lt('pages', 50)                         // <-- uses CompassQueryBuilder#lt, and adds a boolean "should"
                must(term('type', 'poetry'))            // <-- uses CompassQueryBuilder#term, adds a boolean "must"
                mustNot(term('theme', 'war'))           // <-- uses CompassQueryBuilder#term, adds a boolean "mustNot"
            }
            assert query.toString() == "+(+body:all +body:hands +body:deck) pages:[* TO 50} +type:poetry -theme:war", query.toString()

            query = builder.buildQuery {                                    // <-- creates an implicit boolean query
                must(multiPhrase("body", [slop: 2]) {   // <-- creates a nested CompassMultiPhraseQueryBuilder context, calling setSlop(2)
                    add('all')                          // <-- calls CompassMultiPhraseQueryBuilder#add
                    add('hands')                        // <-- calls CompassMultiPhraseQueryBuilder#add
                    add('on')
                    add('deck')
                })                                      // <-- adds multiPhrase as boolean "must"
                lt('pages', 50)                         // <-- uses CompassQueryBuilder#lt, and adds a boolean "should"
                must(term('type', 'poetry'))            // <-- uses CompassQueryBuilder#term, adds a boolean "must"
                mustNot(term('theme', 'war'))           // <-- uses CompassQueryBuilder#term, adds a boolean "mustNot"
            }
            assert query.toString() == '+body:"all hands on deck"~2 pages:[* TO 50} +type:poetry -theme:war', query.toString()

            def date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2007-12-01 12:00:00")
            query = builder.buildQuery {                                    // <-- creates an implicit boolean query
                must(multiPhrase("body", [slop: 2]) {   // <-- creates a nested CompassMultiPhraseQueryBuilder context, and calls setSlop(2)
                    add('all')                          // <-- calls CompassMultiPhraseQueryBuilder#add
                    add('hands')                        // <-- calls CompassMultiPhraseQueryBuilder#add
                    add('on')
                    add('deck')
                })                                      // <-- adds multiPhrase as boolean "must"
                must {                                  // <-- creates an nested boolean query, implicitly
                    ge('pages', 50)                     // <-- uses CompassQueryBuilder#ge, adds a boolean "should"
                    lt('pages', 50, [boost: 1.5f])      // <-- uses CompassQueryBuilder#lt, calls setBoost(1.5f), adds a boolean "should"
                }                                       // <-- adds nested boolean as "must" clause to outer boolean
                must(term('type', 'poetry'))            // <-- uses CompassQueryBuilder#term, adds a boolean "must"
                mustNot(term('theme', 'war'))           // <-- uses CompassQueryBuilder#term, adds a boolean "mustNot"
                ge('publishedDate', date)
                sort(CompassQuery.SortImplicitType.SCORE)                  // <-- uses CompassQuery#addSort
                sort('authorSurname', CompassQuery.SortPropertyType.STRING) // <-- uses CompassQuery#addSort
            }
            assert query.toString() == '+body:"all hands on deck"~2 +(pages:[50 TO *] pages:[* TO 50}^1.5) +type:poetry -theme:war publishedDate:[2007-12-01-12-00-00-0-PM TO *]', query.toString()
        }
    }

    void testBuildQueryWithDynamicMethodCallsAndClosures() {
        withCompassSession { compassSession ->
            def cqb = compassSession.queryBuilder()
            def builder = new GroovyCompassQueryBuilder(cqb)
            def query

            // should not fail
            query = builder.buildQuery {
                queryString("any terms optional", [defaultOperator: 'or'])
            }
            assert query.toString() == "any terms optional", query.toString()

            // as above but without brackets for options Map
            query = builder.buildQuery {
                queryString("any terms optional", defaultOperator: 'or')
            }
            assert query.toString() == "any terms optional", query.toString()

            query = builder.buildQuery {
                queryString "any terms optional", defaultOperator: 'or'
            }
            assert query.toString() == "any terms optional", query.toString()

            shouldFail(UnsupportedOperationException) {
                builder.buildQuery {
                    queryString("hello groovy") {
                        noSuchMethod()
                    }
                }
            }

            query = builder.buildQuery {
                queryString("all terms required") {
                    useAndDefaultOperator()
                }
            }
            assert query.toString() == "+all +terms +required", query.toString()

            // Date properties can be evaluated against date objects
            def date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2007-12-01 12:00:00")
            query = builder.buildQuery {
                gt("dateModified", date)
            }
            assert query.toString() == "dateModified:{2007-12-01-12-00-00-0-PM TO *]", query.toString()

            query = builder.buildQuery {
                term("type", "ARTS")
            }
            assert query.toString() == "type:ARTS", query.toString()

            query = builder.buildQuery {
                wildcard("name", "Lond*")
            }
            assert query.toString() == "name:Lond*", query.toString()

            query = builder.buildQuery {
                wildcard("name", "Lond*")
                addSort("name", CompassQuery.SortPropertyType.STRING)
            }
            assert query.toString() == "name:Lond*", query.toString()

            query = builder.buildQuery {
                bool {
                    addShould(wildcard("name", "Lond*"))
                    addShould(term("type", "ARTS") {
                        setBoost(2.0f)
                    })
                }
            }
            assert query.toString() == "name:Lond* type:ARTS^2.0", query.toString()

            // Same as above but using BigDecimal instead of float in method arg
            query = builder.buildQuery {
                bool {
                    addShould(wildcard("name", "Lond*"))
                    addShould(term("type", "ARTS") {
                        setBoost(2.0) // <-- BigDecimal
                    })
                }
            }
            assert query.toString() == "name:Lond* type:ARTS^2.0", query.toString()

            query = builder.buildQuery {
                bool(true) {
                    addShould(queryString("Lond*") {
                        setDefaultSearchProperty("name")
                    })
                    addShould(lt("createdAt", "20060212"))
                }
            }
            assert query.toString() == "name:lond* createdAt:[* TO 20060212}", query.toString()

            query = builder.buildQuery {
                bool {
                    addMust(term("name", "jack"))
                    addMustNot(term("familyName", "london"))
                    addShould(multiPhrase("text") {
                        add(["blood", "and", "guts"] as Object[])
                        setSlop(5)
                        setBoost(2.0f)
                    })
                }
                addSort("familyName", CompassQuery.SortPropertyType.STRING)
                addSort("birthdate", CompassQuery.SortPropertyType.INT)
            }
            assert query.toString() == "+name:jack -familyName:london text:\"(blood and guts)\"~5^2.0", query.toString()

            // Shorthand options: invalid option
            shouldFail(UnsupportedOperationException) {
                query = builder.buildQuery {
                    term('code', 'DEF', [noSuchOption: 'xyz'])
                }
            }

            // Shorthand options: one for the query
            query = builder.buildQuery {
                term('code', 'DEF', [boost: 2.5f])
            }
            assert query.toString() == 'code:DEF^2.5', query.toString()

            // Same as above but using BigDecimal as option value
            query = builder.buildQuery {
                term('code', 'DEF', [boost: 2.5]) // <-- BigDecimal 
            }
            assert query.toString() == 'code:DEF^2.5', query.toString()

            // Shorthand options: one for the multi-phrase builder and one for the query
            query = builder.buildQuery {
                multiPhrase("text", [slop: 5, boost: 2.0f]) {
                    add("door")
                    add("ajar")
                }
            }
            assert query.toString() == 'text:"door ajar"~5^2.0', query.toString()

            // Shorthand options: special case for the String query builder useAndDefaultOperator() method
            query = builder.buildQuery {
                queryString('Hawaii Five-O', [defaultSearchProperty: 'shows', useAndDefaultOperator: true])
            }
            assert query.toString() == '+hawaii +"five o"', query.toString() // todo test that defaultSearchProperty is right
//            assert query.toString() == '+shows:hawaii +shows:"five o"', query.toString()

            // Shorthand options: special case for the String query builder useOrDefaultOperator() method
            query = builder.buildQuery {
                queryString('Hawaii Five-O', [defaultSearchProperty: 'shows', useAndDefaultOperator: false])
            }
            assert query.toString() == 'hawaii "five o"', query.toString() // todo test that defaultSearchProperty is right
//            assert query.toString() == '+shows:hawaii +shows:"five o"', query.toString()

            // Shorthand options: special case for the String query builder useAndDefaultOperator() method
            query = builder.buildQuery {
                queryString('Hawaii Five-O', [defaultOperator: 'and'])
            }
            assert query.toString() == '+hawaii +"five o"', query.toString() // todo test that defaultSearchProperty is right

            // Shorthand options: special case for the String query builder useOrDefaultOperator() method
            query = builder.buildQuery {
                queryString('Hawaii Five-O', [defaultOperator: 'or'])
            }
            assert query.toString() == 'hawaii "five o"', query.toString() // todo test that defaultSearchProperty is right

            shouldFail(IllegalArgumentException) {
                query = builder.buildQuery {
                    queryString('Hawaii Five-O', [defaultOperator: 'quack'])
                }
            }

            // Same as above for multi property query string
            query = builder.buildQuery {
                multiPropertyQueryString('Hawaii Five-O', [useAndDefaultOperator: true]) {
                    add('shows')
                    add('titles')
                }
            }
            assert query.toString() == '+(shows:hawaii titles:hawaii) +(shows:"five o" titles:"five o")', query.toString()

            // When value is false means OR
            query = builder.buildQuery {
                queryString('Hawaii Five-O', [defaultSearchProperty: 'shows', useAndDefaultOperator: false])
            }
            assert query.toString() == 'hawaii "five o"', query.toString() // todo test that defaultSearchProperty is right
//            assert query.toString() == 'shows:hawaii shows:"five o"', query.toString()

            // When value is false means OR
            query = builder.buildQuery {
                multiPropertyQueryString('Hawaii Five-O', [useAndDefaultOperator: false]) {
                    add('shows')
                    add('titles')
                }
            }
            assert query.toString() == '(shows:hawaii titles:hawaii) (shows:"five o" titles:"five o")', query.toString()

            // Implicit boolean (implicit because there's no "bool") with method args
            query = builder.buildQuery {
                addMust(term("name", "jack"))
                addMustNot(term("familyName", "london"))
                addShould(queryString("blah"))
            }
            assert query.toString() == "+name:jack -familyName:london blah", query.toString()

            // Implicit boolean with closure arg
            query = builder.buildQuery {
                must {
                    term('category', 'shopping')
                    term('style', 'retro')
                }
                mustNot { // a closure isn't necessary for a single expression, but that's what's tested here
                    term('keywords', 'flares')
                }
            }
            assert query.toString() == "+(category:shopping style:retro) -(keywords:flares)", query.toString()

            // Implicit lazy boolean (lazy because it omits the "should" for gt() and queryString())
            query = builder.buildQuery {
                gt('books', 5)
                addMust(term("name", "jack"))
                addMustNot(term("familyName", "london"))
                queryString("blah")
            }
            assert query.toString() == "books:{5 TO *] +name:jack -familyName:london blah", query.toString()

            // Implicit lazy boolean
            query = builder.buildQuery {
                queryString('activity holiday')
                term('keywords', 'hot')
                must(lt('price', 300))
            }
            assert query.toString() == "(+activity +holiday) keywords:hot +price:[* TO 300}", query.toString()

            // Nested and semi-implicit boolean
            query = builder.buildQuery {
                must term('type', 'Airport')
                must {
                    if (true) { // would be real logic in real app
                        term('code', 'JFK', [boost: 2.0f]) // implicit should
                    }
                    should { // should is required due to nested query closure
                        must(prefix('name', "jfk"))
                    }
                    should { // should is required due to nested query closure
                        must(prefix('cityName', "jfk"))
                    }
                }
            }
            assert query.toString() == "+type:Airport +(code:JFK^2.0 (+name:jfk*) (+cityName:jfk*))", query.toString()

            query = builder.buildQuery {
                must term('type', 'Airport')
                must {
                    should { // should is required due to nested query closure
                        must(prefix('name', "jfk"))
                    }
                    should { // should is required due to nested query closure
                        must(prefix('cityName', "jfk"))
                    }
                    if (true) { // would be real logic in real app
                        term('code', 'JFK', [boost: 2.0f]) // implicit should
                    }
                }
            }
            assert query.toString() == "+type:Airport +((+name:jfk*) (+cityName:jfk*) code:JFK^2.0)", query.toString()

            query = builder.buildQuery {
                must term('type', 'Airport')
                must {
                    if (true) { // would be real logic in real app
                        term('code', 'NEW', [boost: 2.0f])
                    }
                    should {
                        must {
                            term('name', 'new')
                            term('name', 'york')
                        }
                    }
                }
            }
            assert query.toString() == "+type:Airport +(code:NEW^2.0 (+(name:new name:york)))", query.toString()

            // Nested implicit boolean with nested lazy boolean
            query = builder.buildQuery {
                must(term('keywords', 'book'))
                mustNot(term('keywords', 'audio'))
                must(bool {
                    queryString('color')
                    queryString('colour')
                    must(queryString('theory'))
                })
            }
            assert query.toString() == "+keywords:book -keywords:audio +(color colour +theory)", query.toString()

            // Nested implicit boolean with nested implicit lazy boolean (same as above without "bool" in last must())
            query = builder.buildQuery {
                must(term('keywords', 'book'))
                mustNot(term('keywords', 'audio'))
                must({
                    queryString('color')
                    queryString('colour')
                    must(queryString('theory'))
                })
            }
            assert query.toString() == "+keywords:book -keywords:audio +(color colour +theory)", query.toString()

            // Implicit lazy boolean, with shorter sort method name
            query = builder.buildQuery {
                must(term("name", "jack"))
                mustNot(term("familyName", "london"))
                must(multiPhrase("contents", [slop: 5, boost: 2.0f]) {
                    add(["blood", "and", "guts"] as Object[])
                })
                sort("familyName", CompassQuery.SortPropertyType.STRING)
                sort("birthdate", CompassQuery.SortPropertyType.INT)
            }
            assert query.toString() == '+name:jack -familyName:london +contents:"(blood and guts)"~5^2.0', query.toString()

            // Implicit lazy boolean, with shorter sort method name, and last boolean clause implicit should
            query = builder.buildQuery {
                must(term("name", "jack"))
                mustNot(term("familyName", "london"))
                multiPhrase("contents", [slop: 5, boost: 2.0f]) {
                    add(["blood", "and", "guts"] as Object[])
                }
                sort("familyName", CompassQuery.SortPropertyType.STRING)
                sort("birthdate", CompassQuery.SortPropertyType.INT)
            }
            assert query.toString() == '+name:jack -familyName:london contents:"(blood and guts)"~5^2.0', query.toString()

            // Use of supported method name as variable
            // "term" builds a term query when invoked as a method, but is here used as a variable
            // More a Groovy language asserion than builder unit test
            def terms = ["grails", "searchable", "plugin"]
            query = builder.buildQuery {
                for (term in terms) {
                    queryString(term)
                }
            }
            assert query.toString() == 'grails searchable plugin', query.toString()

            // Shorthand String query options; so it is more interchangeable with the String query API
            query = builder.buildQuery {
                queryString('ham and eggs', [defaultProperty: 'title', andDefaultOperator: true, parser: 'default'])
            }
            assert query.toString() == '+ham +eggs', query.toString()  // todo test that defaultSearchProperty is right
//            assert query.toString() == '+title:ham +title:eggs', query.toString()

            // Additional (non-Compass) String query options; so it is more interchangeable with the String query API
            query = builder.buildQuery {
                queryString('"ham "and "eggs', [escape: true, defaultOperator: 'or'])
            }
            assert query.toString() == 'ham eggs', query.toString()
        }
    }

    void testQueryBuildingMethodDirectlyOnBuilder() {
        withCompassSession { compassSession ->
            def cqb = compassSession.queryBuilder()
            def builder = new GroovyCompassQueryBuilder(cqb)
            def query

            query = builder.term("type", "ARTS")
            assert query.toString() == "type:ARTS", query.toString()

            // boolean with short method names
            query = builder.bool {
                must(term('keywords', 'book'))
                mustNot(term('keywords', 'audio'))
                should(queryString('color theory'))
            }
            assert query.toString() == "+keywords:book -keywords:audio (+color +theory)", query.toString()

            // Semi-implicit boolean: the "should" is omitted for the should clause
            query = builder.bool {
                must(term('keywords', 'book'))
                mustNot(term('keywords', 'audio'))
                queryString('color theory')
            }
            assert query.toString() == "+keywords:book -keywords:audio (+color +theory)", query.toString()
        }
    }

    void testMultiThreadedBuildQueryWithCurriedClosure() {
        // This is acts as a generic parameterised query builder closure
        // that could be re-used across multiple threads and queries
        def termQueryClosure = { t ->
            term('field', t)
        }

        // as top-level closure
        def failed = false
        def threads = []
        10.times { // Threads
            10.times { // runs
                threads << Thread.start {
                    withCompassSession { compassSession ->
                        def cqb = compassSession.queryBuilder()
                        def builder = new GroovyCompassQueryBuilder(cqb)
                        def query = builder.buildQuery(termQueryClosure.curry(Thread.currentThread().name))
                        if (query.toString() != 'field:' + Thread.currentThread().name) {
                            failed = true
                            assert query.toString() == 'field:' + Thread.currentThread().name // for console error message
                        }
                    }
                }
            }
        }

        // Wait for threads to complete
        waitForThreads(threads)
        assert !failed

        // This is acts as a generic parameterised query builder closure
        // that could be re-used across multiple threads and queries
        def addMultiPhraseTerms = { terms ->
            for (i in 0..<terms.size()) {
                add(terms[i])
            }
        }

        // as nested closure
        threads = []
        10.times { // Threads
            10.times { // runs
                threads << Thread.start {
                    withCompassSession { compassSession ->
                        def cqb = compassSession.queryBuilder()
                        def builder = new GroovyCompassQueryBuilder(cqb)
                        def query = builder.buildQuery {
                            multiPhrase('field', addMultiPhraseTerms.curry(['search', Thread.currentThread().name]))
                            termQueryClosure.curry(Thread.currentThread().name)
                        }
                        if (query.toString() != "field:\"search ${Thread.currentThread().name}\"") {
                            failed = true
                            assert query.toString() == "field:\"search ${Thread.currentThread().name}\"" // for console error message
                        }
                    }
                }
            }
        }

        // Wait for threads to complete
        waitForThreads(threads)
        assert !failed
    }

    void testMultiThreadedQueryBuildingMethodDirectlyOnBuilder() {
        // This is acts as a generic parameterised query builder closure
        // that could be re-used across multiple threads and queries
        def addMultiPhraseTerms = { terms ->
            for (i in 0..<terms.size()) {
                add(terms[i])
            }
        }

        // as top-level closure
        def failed = false
        def threads = []
        10.times { // Threads
            10.times { // runs
                threads << Thread.start {
                    withCompassSession { compassSession ->
                        def cqb = compassSession.queryBuilder()
                        def builder = new GroovyCompassQueryBuilder(cqb)
                        def query = builder.multiPhrase('text', addMultiPhraseTerms.curry(["this", "is", Thread.currentThread().name + "'s", "query"]))
                        if (query.toString() != "text:\"this is ${Thread.currentThread().name}'s query\"") {
                            failed = true
                            assert query.toString() == "text:\"this is ${Thread.currentThread().name}'s query\"" // for console error message
                        }
                    }
                }
            }
        }

        // Wait for threads to complete
        waitForThreads(threads)
        assert !failed

        // as nested closure
        threads = []
        10.times { // Threads
            10.times { // runs
                threads << Thread.start {
                    withCompassSession { compassSession ->
                        def cqb = compassSession.queryBuilder()
                        def builder = new GroovyCompassQueryBuilder(cqb)
                        def query = builder.bool() {
                            term('test', 'test')
                            multiPhrase('text', addMultiPhraseTerms.curry(["this", "is", Thread.currentThread().name + "'s", "query"]))
                        }
                        if (query.toString() != "test:test text:\"this is ${Thread.currentThread().name}'s query\"") {
                            failed = true
                            assert query.toString() == "test:test text:\"this is ${Thread.currentThread().name}'s query\"" // for console error message
                        }
                    }
                }
            }
        }

        waitForThreads(threads)
        assert !failed
    }

    // Wait for threads to complete
    private waitForThreads(threads) {
        for (thread in threads) {
            if (thread.alive) {
                thread.join()
            }
        }
        assert !threads.any { it.alive }
    }

    void testBuildQueryWithCompassQueryBuilderClosureArg() {
        withCompassSession { compassSession ->
            def cqb = compassSession.queryBuilder()
            def builder = new GroovyCompassQueryBuilder(cqb)
            def query

            query = builder.buildQuery { CompassQueryBuilder queryBuilder -> // with type
                queryBuilder.term("type", "ARTS")
            }
            assert query.toString() == "type:ARTS"

            query = builder.buildQuery { queryBuilder -> // without type
                queryBuilder.wildcard("name", "Lond*")
            }
            assert query.toString() == "name:Lond*"

            query = builder.buildQuery { queryBuilder ->
                queryBuilder.
                    wildcard("name", "Lond*").
                    addSort("name", CompassQuery.SortPropertyType.STRING)
            }
            assert query.toString() == "name:Lond*"

            query = builder.buildQuery { queryBuilder ->
                queryBuilder.
                    bool().
                        addShould(queryBuilder.wildcard("name", "Lond*")).
                        addShould(queryBuilder.term("type", "ARTS").setBoost(2.0f)).toQuery()
            }
            assert query.toString() == "name:Lond* type:ARTS^2.0"

            query = builder.buildQuery { queryBuilder ->
                queryBuilder.
                    bool(true).
                        addShould(queryBuilder.queryString("Lond*").
                            setDefaultSearchProperty("name").toQuery()).
                        addShould(queryBuilder.lt("createdAt", "20060212")).toQuery()
            }
            assert query.toString() == "name:lond* createdAt:[* TO 20060212}"

            query = builder.buildQuery { queryBuilder ->
                queryBuilder.
                    bool().
                        addMust(queryBuilder.term("name", "jack")).
                        addMustNot(queryBuilder.term("familyName", "london")).
                        addShould(queryBuilder.multiPhrase("text").
                            add(["blood", "and", "guts"] as Object[]).
                            setSlop(5).toQuery().
                            setBoost(2.0f)).toQuery().
                        addSort("familyName", CompassQuery.SortPropertyType.STRING).
                        addSort("birthdate", CompassQuery.SortPropertyType.INT)
                }
            assert query.toString() == "+name:jack -familyName:london text:\"(blood and guts)\"~5^2.0"
        }
    }

    def getCompass() {
        def conf = new CompassConfiguration()
        conf.connection = "ram://testindex"
        return conf.buildCompass()
    }

    def withCompassSession(closure) {
        def session = compass.openSession()
        def trans = session.beginTransaction()
        def result
        try {
            result = closure(session)
        } finally {
            trans.commit()
            session.close()
        }
        return result
    }
}
