// A domain class that becomes "searchable" because of an external Compass mapping XML file
class Artist {
    static constraints = {
        albums(nullable: true)
    }
    static hasMany = [albums: Album]
    String name
}	
