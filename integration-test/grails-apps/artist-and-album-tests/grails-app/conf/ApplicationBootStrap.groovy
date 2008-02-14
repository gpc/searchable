class ApplicationBootStrap {

     def init = { servletContext ->
        def data = [
            [   name: 'Radiohead',
                genre: 'rock/pop',
                albums: [
                    [name: 'Pablo Honey'],
                    [name: 'The Bends'],
                    [name: 'Ok Computer'],
                    [name: 'Kid A'],
                    [name: 'Amnesiac'],
                    [name: 'Hal to the Thief']
                ]
            ],
            [   name: 'Rage Against The Machine',
                genre: 'rap-metal',
                albums: [
                    [name: 'Rage Against The Machine'],
                    [name: 'The Battle of Los Angeles'],
                    [name: 'Evil Empire'],
                ]
            ],
            [   name: 'Pantera',
                genre: 'thrash-metal',
                albums: [
                    [name: 'Cowboys From Hell'],
                    [name: 'Vulgar Display of Power'],
                    [name: 'Far Beyond Driven'],
                    [name: 'The Great Southern Trendkill'],
                ]
            ],
            [   name: 'Pink Floyd',
                genre: 'prog-rock',
                albums: [
                    [name: 'Meddle'],
                    [name: 'Relics'],
                    [name: 'Animals'],
                    [name: 'The Wall'],
                    [name: 'Obscured by Clouds'],
                    [name: 'Wish You Were Here'],
                    [name: 'Dark Side Of The Moon']
                ]
            ],
            [   name: 'Morrisey',
                genre: 'rock/pop',
                albums: [
                    [name: 'Kill Uncle'],
                    [name: 'Malajusted'],
                    [name: 'Ringleader of The Tormentors'],
                    [name: 'Southpaw Grammar'],
                    [name: 'Vauxhall And I'],
                    [name: 'Viva Hate'],
                    [name: 'Your Arsenal'],
                    [name: 'You Are The Quarry']
                ]
            ],
            [   name: 'Beck',
                genre: 'rock/pop',
                albums: [
                    [name: 'Guero'],
                    [name: 'Mellow Gold'],
                    [name: 'Midnight Vultures'],
                    [name: 'Mutations'],
                    [name: 'Odelay'],
                    [name: 'One Foot In The Grave'],
                    [name: 'Sea Change'],
                    [name: 'The Information'],
                    [name: 'Stereopathetic Soul Manure']
                ]
            ],
            [   name: 'Ben Folds Five',
                genre: 'rock/pop',
                albums: [
                    [name: 'Ben Folds Five'],
                    [name: 'Naked Baby Photos'],
                    [name: 'The Unauthorized Biography of Reinhold Messner'],
                    [name: 'Whatever And Ever Amen']
                ]
            ],
            [   name: 'Ben Folds',
                genre: 'rock/pop',
                albums: [
                    [name: 'Rockin\' The Suburbs'],
                    [name: 'Sonds For Silverman'],
                    [name: 'Supersunnyspeedgraphic: The LP']
                ]
            ],
            [   name: 'Big Punisher',
                genre: 'rap/hip-hop',
                albums: [
                    [name: 'Yeeeah Baby'],
                    [name: 'Big Pun Endagered Species'],
                    [name: 'Capital Punishment']
                ]
            ],
            [   name: 'Busta Rhymes',
                genre: 'rap/hip-hop',
                albums: [
                    [name: 'It Ain\'t Safe No More'],
                    [name: 'When Disaster Strikes'],
                    [name: 'The Big Bang'],
                    [name: 'Anarchy'],
                    [name: 'Extinction Level Event - The Final World Front'],
                    [name: 'Genesis']
                ]
            ],
            [   name: 'Metallica',
                genre: 'thrash-metal',
                albums: [
                    [name: '...And Justice For All'],
                    [name: 'Black Album'],
                    [name: 'Garage Inc'],
                    [name: 'Kill \'em All'],
                    [name: 'Ride The Lightning'],
                    [name: 'Load'],
                    [name: 'St. Anger'],
                    [name: 'Master of Puppets']
                ]
            ],
            [   name: 'Ugly Duckling',
                genre: 'rap/hip-hop',
                albums: [
                    [name: 'Bang For The Buck'],
                    [name: 'Journey To Anywhere'],
                    [name: 'Taste The Secret'],
                    [name: 'Fresh Mode']
                ]
            ]
        ]

         println "Just before boostrap begining to save shit"
         println "Albums: " + Album.count()
         println "Artists: " + Artist.count()
        for (entry in data) {
            def artist = new Artist(name: entry.name)
            assert artist.validate(), artist.errors
            assert artist.save()
            def genre = entry.genre
            for (item in entry.albums) {
                def album = new Album(name: item.name, genre: genre, artist: artist)
                assert album.validate(), album.errors
                assert album.save()
                artist.addToAlbums(album)
            }
            artist.reindex()
        }
     }

     def destroy = {
     }
} 