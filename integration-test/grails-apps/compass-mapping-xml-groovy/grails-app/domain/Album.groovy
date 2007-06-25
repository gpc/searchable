// A domain class that becomes "searchable" because of an external Compass mapping XML file
class Album {
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
