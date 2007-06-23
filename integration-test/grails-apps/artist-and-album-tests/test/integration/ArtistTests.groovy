class ArtistTests extends GroovyTestCase {

    void setUp() {
        for (name in ['Elvis Costello', 'Elvis Presley', 'Loudon Wainwright III', 'Martha Wainwright']) {
            def a = new Artist(name: name)
            assert a.validate(), a.errors
            assert a.save()
        }
    }

	void testSearch() {
		def searchResult = Artist.search("loudon")
		assert searchResult.results.size() == 1
        assert searchResult.results[0] instanceof Artist
        assert searchResult.results[0].name == 'Loudon Wainwright III'

        searchResult = Artist.search("wainwright")
        assert searchResult.results.size() == 2
        assert searchResult.results*.class.unique() == [Artist]
        assert searchResult.results*.name.containsAll(['Loudon Wainwright III', 'Martha Wainwright'])
	}

	void testSearchTop() {
        def top = Artist.searchTop("elvis presley")
        assert top instanceof Artist
        assert top.name == 'Elvis Presley'
    }

	void testSearchEvery() {
        def hits = Artist.searchEvery("elvis")
        assert hits.size() == 2
        assert hits*.class.unique() == [Artist]
        assert hits*.name.containsAll(['Elvis Costello', 'Elvis Presley'])
    }
}
