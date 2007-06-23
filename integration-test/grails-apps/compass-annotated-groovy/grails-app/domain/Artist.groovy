import org.compass.annotations.*

// A domain class declaring itself "searchable" with Compass annotations
@Searchable(alias = 'composer')
class Artist {
    static hasMany = [albums: Album]
    static constraints = {
        albums(nullable: true)
    }

    @SearchableId
    Long id

    // not normally necessary, but needed since Groovy doesn't support generics yet and
    // also useful to test the the plugin honours user-defined aliases
    @SearchableReference(refAlias = 'work')
    Set albums

    @SearchableProperty
    String name
}
