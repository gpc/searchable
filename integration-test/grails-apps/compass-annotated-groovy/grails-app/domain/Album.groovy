import org.compass.annotations.*

// A domain class declaring itself "searchable" with Compass annotations
@Searchable(alias = 'work')
@SearchableAllMetaData(termVector = TermVector.YES) // required for more-like-this
class Album {

    @SearchableId
    Long id

    @SearchableProperty(index = Index.NO)
    Long version

    static belongsTo = Artist

    @SearchableComponent(refAlias = 'composer')
    Artist artist

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
