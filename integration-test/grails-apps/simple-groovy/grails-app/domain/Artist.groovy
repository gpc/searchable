// A domain class declaring itself "searchable" with boolean
class Artist {
    static searchable = true
    static constraints = {
        albums(nullable: true)
    }
    static hasMany = [albums: Album]
    String name
}	
