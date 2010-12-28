import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.URLENC

class SearchableSpec extends spock.lang.Specification {
    def client = new RESTClient("http://localhost:8080/SearchableIndexCascading/")

    def "Searchable cascading but no reference"() {
        given:
        client.post(path: "inventoryLocation/test",
                    body: [name: "Bin3"],
                    requestContentType: URLENC)

        when: "an Inventory Location is stored in the session and then displayed"
        // 
        def response = client.get(path: "inventoryLocation/search", query: [q: "Bin3"])

        then:
        assert response.data == "Dummy: []"
    }
}
