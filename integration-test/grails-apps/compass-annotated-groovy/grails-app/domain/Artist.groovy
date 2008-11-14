import org.compass.annotations.*

// A domain class declaring itself "searchable" with Compass annotations
@Searchable(alias = 'composer')
@SearchableAllMetaData(termVector = TermVector.YES) // required for more-like-this
class Artist {
    static hasMany = [albums: Album]
    static constraints = {
        albums(nullable: true)
    }

    @SearchableId(accessor = 'property')
    Long id

    @SearchableProperty(index = Index.NO, accessor = 'property')
    Long version

    // not normally necessary, but needed since Groovy doesn't support generics yet and
    // also useful to test the the plugin honours user-defined aliases
    @SearchableReference(refAlias = 'work', accessor = 'property')
    Set albums

    @SearchableProperty(accessor = 'property')
    String name

    /**
     * Provide a useful String
     */
    String toString() {
        "$name (${albums?.size()} albums)"
    }
}
