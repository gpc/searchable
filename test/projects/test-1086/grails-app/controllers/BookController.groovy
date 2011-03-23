import grails.converters.JSON

class BookController {
    static scaffold = Book

    def search = {
        def results = Book.search(params.q, [reload: true])

        render results.results as JSON
    }
}
