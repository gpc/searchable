class Book {
    String title
    String author
    Date dateCreated

    String toString() {
        return "$title - $author ($dateCreated)"
    }

    static searchable = [except: 'dateCreated']

    static constraints = {
        title(blank: false)
        author(blank: false)
    }
}
