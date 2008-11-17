sync-docs
=========

A simple tool to get and put wiki page sources from and to grails.org
(or for example a local server).

To download, from the trunk dir:

    groovy tools/sync-docs/scripts/get -server=www.grails.org -dir=src/doc/wiki \
                                       -username=user -password=password \
                                       -startPage='Searchable Plugin' \
                                       -match='^Searchable Plugin.+$'

which downloads all the pages matching the pattern that are linked from startPage
and prompt you if others should be downloaded.

To upload, from the trunk dir:

    roovy tools/sync-docs/scripts/put -server=localhost:8080 -dir=src/doc/wiki 
                                      -username=user -password=password

which uploads the pages in dir.
