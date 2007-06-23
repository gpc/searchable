import org.compass.annotations.*

// A domain class declaring itself "searchable" with Compass annotations
@Searchable(alias = 'work')
class Album {
    static belongsTo = Artist

    @SearchableReference(refAlias = 'composer')
    Artist artist

    @SearchableId
    Long id

    @SearchableProperty(boost = 1.5f)
    String name

    @SearchableProperty(index = Index.UN_TOKENIZED)
    String genre
}	
