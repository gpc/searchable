import org.compass.core.CompassTermFreq

class AlbumTests extends GroovyTestCase {
    def compass
    def compassGps

    void setUp() {
        def deLa = new Artist(name: 'De la Soul')
        for (name in ['3 Feet High and Rising', 'De La is Dead', 'Stakes is High', 'The Grind Date', 'AOI: Mosaic Thump', 'AOI: Bionix']) {
            def album = new Album(name: name, artist: deLa, genre: "rap/hip-hop")
            assert album.validate(), album.errors
            deLa.addToAlbums(album)
        }
        assert deLa.validate(), deLa.errors
        assert deLa.save()
    }

    void tearDown() {
        [Artist, Album].each { clazz -> clazz.findAll().each { it.delete() } }
    }

    void testSearch() {
        def deLa = Artist.findByName('De la Soul')
        assert deLa.albums.size() == 6

        def searchResult = Album.search("grind")
        assert searchResult.total == 1, searchResult.total
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
        println albums
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

    void testClassStaticIndex() {
        compassGps.stop()

        def deLa = Artist.findAll()[0]

        def album = new Album(name: 'Buhloone Mindstate', artist: deLa, genre: "rap/hip-hop")
        deLa.addToAlbums(album)
        assert album.save(), album.errors
        assert Album.countHits("de la soul") == 6

        // index a single instance
        Album.index(album)
        assert Album.countHits("de la soul") == 7, Album.countHits("de la soul")

        def s = compass.openSession()
        def tx = s.beginTransaction()

        s.delete(s.queryBuilder().matchAll())

        tx.commit()
        s.close()

        assert Album.countHits("de la soul") == 0

        // index all instances
        Album.index()
        assert Album.countHits("de la soul") == 7

        compassGps.start()
    }

    void testClassInstanceIndex() {
        compassGps.stop()

        def deLa = Artist.findAll()[0]

        assert Album.countHits("de la soul") == 6, Album.countHits("de la soul")
        def album = new Album(name: 'Buhloone Mindstate', artist: deLa, genre: "rap/hip-hop")
        deLa.addToAlbums(album)
        assert album.save(), album.errors

        assert Album.countHits("de la soul") == 6
        album.index()
        assert Album.countHits("de la soul") == 7

        compassGps.start()
    }

    void testClassStaticReindex() {
        compassGps.stop()

        def album = Album.findAll()[0]
        assert Album.countHits("b-sides unreleased") == 0

        album.name = "B-Sides and Unreleased"
        assert album.save(), album.errors
        assert Album.countHits("b-sides unreleased") == 0

        // re-index an instance
        Album.reindex(album)
        assert Album.countHits("b-sides unreleased") == 1

        // re-index all albums
        Album.reindex()
        assert Album.countHits("de la soul") == 6

        compassGps.start()
    }

    void testClassInstanceReindex() {
        compassGps.stop()

        def album = Album.findAll()[0]
        assert Album.countHits("b-sides unreleased") == 0

        album.name = "B-Sides and Unreleased"
        assert album.save(), album.errors
        assert Album.countHits("b-sides unreleased") == 0

        // re-index an instance
        album.reindex()
        assert Album.countHits("b-sides unreleased") == 1

        compassGps.start()
    }

    void testClassStaticUnindex() {
        compassGps.stop()

        assert Album.countHits("de la soul") == 6, Album.countHits("de la soul")

        // unindex a single instance
        Album.unindex(Album.findAll()[0])
        assert Album.countHits("de la soul") == 5, Album.countHits("de la soul")

        // unindex all instances
        Album.unindex()
        assert Album.countHits("de la soul") == 0, Album.countHits("de la soul")

        compassGps.start()
    }

    void testClassInstanceUnindex() {
        compassGps.stop()

        assert Album.countHits("de la soul") == 6, Album.countHits("de la soul")

        // unindex a single instance
        def album = Album.findAll()[0]

        album.unindex()
        assert Album.countHits("de la soul") == 5, Album.countHits("de la soul")

        compassGps.start()
    }
}