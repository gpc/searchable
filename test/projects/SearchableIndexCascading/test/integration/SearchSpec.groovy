class SearchSpec extends spock.lang.Specification {
    def "GString in search() method"() {
        given: "a simple query"
        def q = "Bin1"

        when: "I search inventory items with a GString including that query"
        def results = InventoryItem.search("*${q}*")

        then: "I get 2 results"
        results.total == 2
    }
}
