import org.codehaus.groovy.grails.plugins.searchable.compass.SearchableCompassUtils

/**
 * Grails Searchable Plugin configuration
 *
 * This file is created by "grails install-searchable-config"
 */
class SearchableConfiguration {

    /**
     * The location of the Compass index
     *
     * Examples: "/home/app/compassindex", "ram://app-index" or null to use the default
     */
    String compassConnection = SearchableCompassUtils.getDefaultConnection()

    /**
     * Any settings you wish to pass to Compass
     *
     * Use this to configure custom/override default analyzers, query parsers, eg
     *
     *     Map compassSettings = [
     *         'compass.engine.analyzer.german.type': 'German'
     *     ]
     *
     * gives you an analyzer called "german" you can then use in mappings and queries, like
     *
     *    class Book {
     *        static searchable = { content analyzer: 'german' }
     *        String content
     *    }
     *
     *    Book.search("unter", analyzer: 'german')
     *
     * Documentation for Compass settings is here: http://www.compass-project.org/docs/1.2.1/reference/html/core-settings.html
     */
    Map compassSettings = [:]

    /**
     * Default mapping property exclusions
     *
     * No properties matching the given names will be mapped by default
     * ie, when using "searchable = true"
     *
     * This does not apply for classes using "searchable = [only/except: [...]]"
     * mapping by closure
     */
    List defaultExcludedProperties = ["password"]

    /**
     * Default property formats
     *
     * Value is a Map between Class and format string, eg
     *
     *     [(Date): "yyyy-MM-dd'T'HH:mm:ss"]
     *
     * Only applies to class properties mapped as "searchable properties", which are typically
     * simple class types that can be represented as Strings (rather than references
     * or components) AND only required if overriding the built-in format.
     */
    Map defaultFormats

    /**
     * Default search method options
     *
     * @deprecated Use {@link #defaultMethodOptions instead}
     */
    Map defaultSearchOptions

    /**
     * Set default options for each SearchableService/Domain-class method, by method name.
     *
     * These can be overriden on a per-query basis by passing the method a Map of options
     * containing those you want to override.
     *
     * You may want to customise the options used by the search method, which are:
     *
     * @param reload          whether to reload domain class instances from the DB: true|false
     *                        If true, the search  will be slower but objects will be associated
     *                        with the current Hibernate session
     * @param escape          whether to escape special characters in string queries: true|false
     * @param offset          the 0-based hit offset of the first page of results.
     *                        Normally you wouldn't change it from 0, it's only here because paging
     *                        works by using an offset + max combo for a specific page
     * @param max             the page size, for paged search results
     * @param defaultOperator if the query does not otherwise indicate, then the default operator
     *                        applied: "or" or "and".
     *                        If "and" means all terms are required for a match, if "or" means
     *                        any term is required for a match
     * @param suggestQuery    if true and search method is returning a search-result object
     *                        (rather than a domain class instance, list or count) then a
     *                        "suggestedQuery" property is also added to the search-result.
     *                        This can also be a Map of options as supported by the suggestQuery
     *                        method itself
     *
     * For the options supported by other methods, please see the documentation
     * http://grails.org/Searchable+Plugin
     */
    Map defaultMethodOptions = [
        search: [reload: false, escape: false, offset: 0, max: 10, defaultOperator: "and"],
        suggestQuery: [userFriendly: true]
    ]

    /**
     * Should changes made through GORM/Hibernate be mirrored to the index
     * automatically (using Compass::GPS)?
     *
     * If false, you must manage the index manually using index/indexAll/unindex/unindexAll/reindex/renindexAll
     */
    boolean mirrorChanges = true

    /**
     * Should the database be indexed at startup (using Compass:GPS)?
     *
     * Possible values: true|false|"fork"
     *
     * The value may be a boolean true|false or a string "fork", which means true,
     * and fork a thread for it
     *
     * If you use BootStrap.groovy to insert your data then you should use "true",
     * which means do a non-forking, otherwise "fork" is recommended
     */
    def bulkIndexOnStartup = true

    /**
     * Should index locks be removed (if present) at startup?
     */
    def releaseLocksOnStartup = true
}
