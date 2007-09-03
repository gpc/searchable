import org.compass.core.CompassTermFreq

/**
*
*
* @author Maurice Nicholson
*/
class SearchableServiceTests extends GroovyTestCase {
    def searchableService

    void setUp() {
        def artist = new Artist(name: 'The Beatles')
        assert artist.validate(), artist.errors
        assert artist.save()
        artist.save()
        for (name in ['Revolver', 'Abbey Road', 'Help', 'Magical Mystery Tour', 'Sgt. Pepper\'s Lonely Hearts Club Band', 'Yellow Submarine', 'Red Album', 'Blue Album', 'White Album']) {
            def album = new Album(name: name, artist: artist, genre: "pop")
            assert album.validate(), album.errors
            assert album.save()
            artist.addToAlbums(album)
        }
        artist.reindex()
    }

    void testSearch() {
        def result = searchableService.search("beatles")
        assert result.results.size() == 10
        assert result.results[0] instanceof Artist
        assert result.results[0].name == 'The Beatles'

        result = searchableService.search("album")
        assert result.results.size() == 3
        assert result.results*.class.unique() == [Album]
        assert result.results*.name.containsAll(['Red Album', 'Blue Album', 'White Album'])
    }

    void testSearchTop() {
        def result = searchableService.searchTop("tour mystery")
        assert result instanceof Album
        assert result.name == 'Magical Mystery Tour'
    }

    void testSearchEvery() {
        def results = searchableService.searchEvery("beatles album")
        assert results*.class as Set == [Album, Artist] as Set
        assert results.size() == 10
    }

    void testTermFreqs() {
        // sanity test: try out some argument combos: more tests elsewhere
        def result = searchableService.termFreqs('name')
        assert result instanceof CompassTermFreq[]
        assert result.size() > 0
        result = searchableService.termFreqs('name', 'genre')
        assert result.size() > 0
        result = searchableService.termFreqs('name', 'genre')
        assert result.size() > 0
        result = searchableService.termFreqs('name', 'genre', size: 100)
        assert result.size() > 0
    }

    void testIndexAll() {
        // should not fail
        searchableService.indexAll()

        assert searchableService.countHits("beatles") == 10
        assert searchableService.countHits("red") == 1
    }

    void testReindexAll() {
        // should not fail
        searchableService.reindexAll()

        assert searchableService.countHits("beatles") == 10
        assert searchableService.countHits("red") == 1
    }

    void testUnindexAll() {
        // should not fail
        searchableService.unindexAll()

        assert searchableService.countHits("beatles") == 0
        assert searchableService.countHits("red") == 0
    }
}