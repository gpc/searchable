import org.compass.core.CompassTermFreq

class ArtistTests extends GroovyTestCase {
    def compassGps
    def compass

    void setUp() {
        for (name in ['Elvis Costello', 'Elvis Presley', 'Loudon Wainwright III', 'Martha Wainwright']) {
            def artist = new Artist(name: name)
            for (n in ['Greatest Hits I', 'Greatest Hits II']) {
                def album = new Album(name: n, genre: 'rock/pop', artist: artist)
                assert album.validate(), album.errors
                artist.addToAlbums(album)
            }
            assert artist.validate(), artist.errors
            assert artist.save()
        }
        compass.getSearchEngineIndexManager().refreshCache()
    }

    void tearDown() {
        [Artist, Album].each { clazz -> clazz.findAll().each { it.delete() } }
    }

    void testSearch() {
		def searchResult = Artist.search("loudon")
		assert searchResult.results.size() == 1
        assert searchResult.results[0] instanceof Artist
        assert searchResult.results[0].name == 'Loudon Wainwright III'
        assert searchResult.results[0].albums.size() == 2

        searchResult = Artist.search("wainwright")
        assert searchResult.results.size() == 2
        assert searchResult.results*.class.unique() == [Artist]
        assert searchResult.results*.name.containsAll(['Loudon Wainwright III', 'Martha Wainwright'])
        assert searchResult.results.every { it.albums.size() == 2 }

        // top search hit
        def top = Artist.search("elvis presley", result: 'top')
        assert top instanceof Artist
        assert top.name == 'Elvis Presley'
        assert top.albums.size() == 2

        // every search hit
        def hits = Artist.search("elvis", result: 'every')
        assert hits.size() == 2
        assert hits*.class.unique() == [Artist]
        assert hits*.name.containsAll(['Elvis Costello', 'Elvis Presley'])
        assert hits.every { it.albums.size() == 2 }
	}

	void testSearchTop() {
        def top = Artist.searchTop("elvis presley")
        assert top instanceof Artist
        assert top.name == 'Elvis Presley'
        assert top.albums.size() == 2
    }

	void testSearchEvery() {
        def hits = Artist.searchEvery("elvis")
        println hits
        assert hits.size() == 2
        assert hits*.class.unique() == [Artist]
        assert hits*.name.containsAll(['Elvis Costello', 'Elvis Presley'])
        assert hits.every { it.albums.size() == 2 }
    }

    void testCountHits() {
        def count = Artist.countHits("elvis")
        assert count == 2, count
    }

    void testTermFreqs() {
        // sanity test: try out some argument combos: more tests elsewhere
        def result = Artist.termFreqs('name')
        assert result instanceof CompassTermFreq[]
        assert result.length > 0

        result = Artist.termFreqs() // defaults to "all" field
        assert result instanceof CompassTermFreq[]
        assert result.length > 0
    }
}
