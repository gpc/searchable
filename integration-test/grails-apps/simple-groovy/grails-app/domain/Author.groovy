class Author {
    static searchable = true
    static constraints = {
        books(nullable: true)
    }
    static hasMany = [books: Book]
    String name
}	
