grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits "global"
    log "warn"
    
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
    }
    dependencies {
    }

    plugins {
        build( ":svn:1.0.0.M1") {
            export = false
        }
        build( ":release:1.0.0.M2" ) {
            export = false
        }
    }
}
