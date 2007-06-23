// A domain class declaring itself "searchable" with a closure
class Album {
    static searchable = {
        name(boost: 1.5)
        genre(index: 'un_tokenized')
    }
    static belongsTo = Artist
    Artist artist
    String name
    String genre
}	
