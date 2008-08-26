// A domain class declaring itself "searchable" with a closure
class Album {
    static searchable = {
        alias 'work'
        all termVector: "yes" // required for more-like-this
        version: index: 'no'
        artist component: true
        name boost: 1.5
        genre index: 'un_tokenized'
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
