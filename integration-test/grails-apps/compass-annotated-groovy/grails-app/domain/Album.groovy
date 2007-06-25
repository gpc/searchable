import org.compass.annotations.*

// A domain class declaring itself "searchable" with Compass annotations
@Searchable(alias = 'work')
class Album {
    static belongsTo = Artist

    @SearchableComponent(refAlias = 'composer')
    @SearchableReference(refAlias = 'composer')
    Artist artist

    @SearchableId
    Long id

    @SearchableProperty(boost = 1.5f)
    String name

    @SearchableProperty(index = Index.UN_TOKENIZED)
    String genre

    /**
     * Provide a useful String
     */
    String toString() {
        "$name (by ${artist.name})"
    }
}
