// A domain class declaring itself "searchable" with a closure
class Album {
    static searchable = {
        artist(component: true)
//        artist(reference: true, component: true)
        name(boost: 1.5)
        genre(index: 'un_tokenized')
    }
    static belongsTo = Artist
    Artist artist
    String name
    String genre

    /**
     * Provide a useful String
     */
    String toString() {
        "$name (by ${artist.name})"
    }
}	
