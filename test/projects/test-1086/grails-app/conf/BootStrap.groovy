class BootStrap {

    def init = { servletContext ->
        new Book(title: 'The $hining', author: "Stephen King").save(failOnError: true)
        new Book(title: "Misery", author: "Stephen King").save(failOnError: true)
        def book = new Book(title: "Kingdom", author: "Stephen King").save(failOnError: true)

        println book.toString()
    }
    def destroy = {
    }
}
