class BookTests extends GroovyTestCase {
    static cleaningOrder = [Book, Author]

    void setUp() {
        def sk = new Author(name: 'Stephen King')
        sk.save()
        for (title in ['Carrie', 'The Shinning', 'Pet Cemetary', 'Stand by Me', 'The Stand']) {
            def book = new Book(title: title, author: sk)
            assert book.save()
            sk.addToBooks(book)
        }
        sk.save()
    }
    
    void testSearch() {
        def sk = Author.findByName('Stephen King')
        assert sk
        assert sk.books.size() == 5

        def searchResult = Book.search("The Shinning")
        assert searchResult.total == 1
        assert searchResult.results.size() == 1
        assert searchResult.results[0] instanceof Book
        assert searchResult.results[0].title == 'The Shinning'

        searchResult = Book.search("Stand")
        assert searchResult.total == 2
        assert searchResult.results.size() == 2
        assert searchResult.results[0] instanceof Book
        println searchResult.results*.title
        assert searchResult.results*.title.containsAll(['The Stand', 'Stand By Me'])
    }
}
