/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.compass.core.CompassTermFreq

/**
*
*
* @author Maurice Nicholson
*/
class SearchableServiceTests extends GroovyTestCase {
    def searchableService
    def compassGps

    void setUp() {
        def artist = new Artist(name: 'The Beatles')
        for (name in ['Revolver', 'Abbey Road', 'Help', 'Magical Mystery Tour', 'Sgt. Pepper\'s Lonely Hearts Club Band', 'Yellow Submarine', 'Red Album', 'Blue Album', 'White Album']) {
            def album = new Album(name: name, artist: artist, genre: "pop")
            assert album.validate(), album.errors
            artist.addToAlbums(album)
        }
        assert artist.validate(), artist.errors
        assert artist.save()
    }

    void tearDown() {
        [Artist, Album].each { clazz -> clazz.findAll().each { it.delete() } }
    }

    void testSearch() {
        def result = searchableService.search("beatles")
        assert result.results.size() == 10, result.results.size()
        assert result.results.find { it instanceof Artist }.name == 'The Beatles'
        assert result.results.findAll { it instanceof Album }.size() == 9,result.results.findAll { it instanceof Album }.size()

        result = searchableService.search("album")
        assert result.results.size() == 3
        assert result.results*.class.unique() == [Album]
        assert result.results*.name.containsAll(['Red Album', 'Blue Album', 'White Album'])

        // top result
        result = searchableService.search("tour mystery", result: 'top')
        assert result instanceof Album
        assert result.name == 'Magical Mystery Tour'

        // every result hit object
        def results = searchableService.search("beatles album", result: 'every')
        assert results.size() == 3, results.size()
        assert results*.class as Set == [Album] as Set, results*.class as Set
        assert results.collect { it.name }.sort() == ['Blue Album', 'Red Album', 'White Album'], results.collect { it.name }.sort()
    }

    void testSearchTop() {
        def result = searchableService.searchTop("tour mystery")
        assert result instanceof Album
        assert result.name == 'Magical Mystery Tour'
    }

    void testSearchEvery() {
        def results = searchableService.searchEvery("beatles album")
        assert results.size() == 3, results.size()
        assert results*.class as Set == [Album] as Set, results*.class as Set
        assert results.collect { it.name }.sort() == ['Blue Album', 'Red Album', 'White Album'], results.collect { it.name }.sort()
    }

    void testCountHits() {
        def count = searchableService.countHits("beatles album")
        assert count == 3, count
    }

    void testMoreLikeThis() {
        def album = Album.findAll()[0]
        def more = searchableService.moreLikeThis(album, minResourceFreq: 1, minTermFreq: 1)
        assert more.results.size() > 0, more.results.size()

        more = searchableService.moreLikeThis(Album, album.id, minResourceFreq: 1, minTermFreq: 1)
        assert more.results.size() > 0, more.results.size()

        more = searchableService.moreLikeThis(class: Album, id: album.id, minResourceFreq: 1, minTermFreq: 1)
        assert more.results.size() > 0, more.results.size()
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

    void testIndex() {
        searchableService.index()

        assert searchableService.countHits("beatles") == 10
        assert searchableService.countHits("red") == 1
    }

    void testReindex() {
        searchableService.reindex()

        assert searchableService.countHits("beatles") == 10
        assert searchableService.countHits("red") == 1
    }

    void testUnindex() {
        searchableService.unindex()

        assert searchableService.countHits("beatles") == 0
        assert searchableService.countHits("red") == 0
    }

    // these methods are now deprecated
    void testIndexAll() {
        searchableService.indexAll()

        assert searchableService.countHits("beatles") == 10
        assert searchableService.countHits("red") == 1
    }

    void testReindexAll() {
        searchableService.reindexAll()

        assert searchableService.countHits("beatles") == 10
        assert searchableService.countHits("red") == 1
    }

    void testUnindexAll() {
        searchableService.unindexAll()

        assert searchableService.countHits("beatles") == 0
        assert searchableService.countHits("red") == 0
    }
}