class Book {
    String title
    String author
    Integer numOfPages

    static searchable = true

    static constraints = {
        title blank: false, unique: true
        author blank: false
    }
}
