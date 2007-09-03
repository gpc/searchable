import org.compass.core.CompassTermFreq

class AlbumTests extends GroovyTestCase {

    void setUp() {
        def deLa = new Artist(name: 'De la Soul')
        assert deLa.validate(), deLa.errors
        assert deLa.save()
        for (name in ['3 Feet High and Rising', 'De La is Dead', 'Stakes is High', 'The Grind Date', 'AOI: Mosaic Thump', 'AOI: Bionix']) {
            def album = new Album(name: name, artist: deLa, genre: "rap/hip-hop")
            assert album.validate(), album.errors
            assert album.save()
            deLa.addToAlbums(album)
        }
        deLa.save()
    }

    void testSearch() {
        def deLa = Artist.findByName('De la Soul')
        assert deLa.albums.size() == 6

        def searchResult = Album.search("grind")
        assert searchResult.total == 1
        assert searchResult.results.size() == 1
        assert searchResult.results[0] instanceof Album
        assert searchResult.results[0].name == 'The Grind Date'
        assert searchResult.results.every { it.artist }

        searchResult = Album.search("high")
        assert searchResult.total == 2
        assert searchResult.results.size() == 2
        assert searchResult.results[0] instanceof Album
        assert searchResult.results*.name.containsAll(['3 Feet High and Rising', 'Stakes is High'])
    }

    void testSearchEvery() {
        def albums = Album.searchEvery("stakes OR grind OR aoi")
        assert albums.size() == 4
        assert albums*.name.containsAll(['Stakes is High', 'The Grind Date', 'AOI: Mosaic Thump', 'AOI: Bionix'])
        assert albums.every { it.artist }
    }

    void testSearchTop() {
        def album = Album.searchTop("stakes")
        assert album instanceof Album
        assert album.name == 'Stakes is High'
        assert album.artist
    }

    void testTermFreqs() {
        // sanity test: try out some argument combos: more tests elsewhere
        def result = Album.termFreqs('name')
        assert result instanceof CompassTermFreq[]
        assert result.length > 0
        result = Album.termFreqs() // defaults to "all" property
        assert result.length > 0
    }
}