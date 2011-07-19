import grails.plugin.spock.IntegrationSpec
import morelikethisnpe.FindMe

class SearchSpec extends IntegrationSpec {
    def searchableService

    def "GString in search() method"() {
        given: "a simple query"
        def q = "Bin1"

        when: "I search inventory items with a GString including that query"
        def results = InventoryItem.search("*${q}*")

        then: "I get 2 results"
        results.total == 2
    }

    def "Query builder with integer constraints"() {
        given: "A bunch of books"
        new Book(title: "The Shining", author: "Stephen King", numOfPages: 300).save()
        new Book(title: "Misery", author: "Stephen King", numOfPages: 200).save()
        new Book(title: "Colossus", author: "Niall Ferguson", numOfPages: 450).save()
        new Book(title: "The Liar", author: "Stephen Fry", numOfPages: 320).save()
        new Book(title: "Carrie", author: "Stephen King", numOfPages: 189).save()

        when: "I search for all books with fewer than 300 pages"
        def result = Book.search {
            lt "numOfPages", 300
        }

        then: "I get 2 books back"
        result.total == 2

        when: "I search for all books with 300 or fewer pages"
        result = Book.search {
            must(le("numOfPages", 300))
        }

        then: "I get 3 books back"
        result.total == 3
    }

    /**
     * Test case for <a href="http://jira.grails.org/browse/GPSEARCHABLE-207">GPSEARCHABLE-207</a>.
     */
    def "moreLikeThis() should work"() {
        given: "A new domain instance"
        def fm	= new FindMe(title: "t2").save(failOnError: true)

        when: "moreLikeThis() is called with that domain instance"
        def results = searchableService.moreLikeThis( fm )
        
        then: "we should get no results back"
        results.total == 0
    }

    /**
     * Test case for <a href="http://jira.grails.org/browse/GPSEARCHABLE-207">GPSEARCHABLE-207</a>.
     */
    def "moreLikeThis() should work with a transient domain instance"() {
        given: "A new, unsaved domain instance"
        def fm	= new FindMe(title: "t2")

        when: "moreLikeThis() is called with that domain instance"
        searchableService.moreLikeThis( fm )
        
        then: "An IllegalArgumentException should be thrown because it has no ID"
        thrown(IllegalArgumentException)
    }
}
