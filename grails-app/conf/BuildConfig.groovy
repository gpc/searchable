grails.project.work.dir = "target"

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {

    inherits "global"
    log "warn"

    repositories {
        grailsCentral()
        mavenRepo "http://repo.grails.org/grails/core"
        mavenCentral()
    }

    dependencies {
        compile "org.compass-project:compass:2.2.1"
        compile('org.apache.lucene:lucene-highlighter:2.4.1',
                'org.apache.lucene:lucene-analyzers:2.4.1',
                'org.apache.lucene:lucene-queries:2.4.1',
                'org.apache.lucene:lucene-snowball:2.4.1',
                'org.apache.lucene:lucene-spellchecker:2.4.1')
    }

    plugins {
        compile ":hibernate:3.6.10.14", {
            export = false
        }

        build ':release:3.0.1', ':rest-client-builder:1.0.3', {
            export = false
        }
    }
}
