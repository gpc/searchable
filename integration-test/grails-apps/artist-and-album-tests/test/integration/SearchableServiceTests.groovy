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
        artist.save()
    }

    void testSearch() {
        def result = searchableService.search("name:beatles")
        assert result.results.size() == 1
        assert result.results[0] instanceof Artist
        assert result.results[0].name == 'The Beatles'

        result = searchableService.search("beatles album")
        assert result.results.size() == 4
        assert result.results*.class as Set == [Artist, Album] as Set
        assert result.results.findAll { it instanceof Album }.name.containsAll(['Red Album', 'Blue Album', 'White Album'])
    }

    void testSearchTop() {
        def result = searchableService.searchTop("tour mystery")
        assert result instanceof Album
        assert result.name == 'Magical Mystery Tour'
    }

    void testSearchEvery() {
        def results = searchableService.searchEvery("beatles album")
        assert results*.class as Set == [Album, Artist] as Set
        assert results.size() == 4
    }

    void testIndexAll() {
        // should not fail
        searchableService.indexAll()

        assert searchableService.countHits("name:beatles") == 1
        assert searchableService.countHits("name:album") == 3
    }

    void testReindexAll() {
        // should not fail
        searchableService.reindexAll()

        assert searchableService.countHits("name:beatles") == 1
        assert searchableService.countHits("name:album") == 3
    }

    void testUnindexAll() {
        // should not fail
        searchableService.unindexAll()

        assert searchableService.countHits("name:beatles") == 0
        assert searchableService.countHits("name:album") == 0
    }
}