// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder=false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable fo AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }

}

// log4j configuration
// log4j = {
//     // Example of changing the log pattern for the default console
//     // appender:
//     //
//     //appenders {
//     //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
//     //}
// 
// 
//     error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
// 	       'org.codehaus.groovy.grails.web.pages', //  GSP
// 	       'org.codehaus.groovy.grails.web.sitemesh', //  layouts
// 	       'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
// 	       'org.codehaus.groovy.grails.web.mapping', // URL mapping
// 	       'org.codehaus.groovy.grails.commons', // core / classloading
// 	       'org.codehaus.groovy.grails.plugins', // plugins
// 	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
// 	       'org.springframework',
// 	       'org.hibernate',
//            'net.sf.ehcache.hibernate'
// 
//     warn   'org.mortbay.log'
// }


/**
* Directory configuration.
* Pickup the Tomcat/Catalina directory else use the current or temp dir.
*/
def fs = File.separator // Local variable.
def tempDir = new File("target${fs}").isDirectory() ? "target${fs}" : ''
globalDirs.catalinaBase = System.properties.getProperty('catalina.base')
globalDirs.logDirectory = globalDirs.catalinaBase ? "${globalDirs.catalinaBase}${fs}logs${fs}" : tempDir
globalDirs.workDirectory = globalDirs.catalinaBase ? "${globalDirs.catalinaBase}${fs}work${fs}" : tempDir
globalDirs.searchableIndexDirectory = "${globalDirs.workDirectory}SearchableIndex${fs}${appName}${fs}"

/**
 * Log4j configuration.
 * Causing this file to reload (e.g. edit+save) may break the appLog destination
 * and further logs will be written to files or directories like "[:]".
 * For more info see http://logging.apache.org/log4j/1.2/manual.html
 * For log levels see http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Level.html
 * Basic log levels are ALL < TRACE < DEBUG < INFO < WARN < ERROR < FATAL < OFF
 */
log4j = {
    appenders {
        // Use if we want to prevent creation of a stacktrace.log file.
//         'null' name:'stacktrace'

        // Use this if we want to modify the default appender called 'stdout'.
        console name:'stdout', layout:pattern(conversionPattern: '[%t] %-5p %c{2} %x - %m%n')

        // Custom log file.
        rollingFile name:"appLog",
                        file:"${globalDirs.logDirectory}${appName}.log".toString(),
                        maxFileSize:'300kB',
                        maxBackupIndex:1,
                        layout:pattern(conversionPattern: '%d{[EEE, dd-MMM-yyyy @ HH:mm:ss.SSS]} [%t] %-5p %c %x - %m%n')
    }

    // This is for the built-in stuff and from the default Grails-1.2.1 config.
    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log' // Jetty

    error 'grails.app' // Set the default log level for our app code.
    info 'grails.app.bootstrap' // Set the log level per type and per type.class
    error 'grails.app.service.AuthService'
    error 'grails.app.service.NavigationService'
    error 'grails.app.service.com.zeddware.grails.plugins.filterpane.FilterService'
    error 'grails.app.task' // Quartz jobs.
    info 'grails.app.task.InventoryReindexJob'

    // Move anything that should behave differently into this section.
    switch(environment) {
        case 'development':
            // Configure the root logger to output to stdout and appLog appenders.
            root {
                error 'stdout','appLog'
                additivity = true
            }
            //debug "org.hibernate.SQL"
            debug 'grails.app.service'
            debug 'grails.app.controller'
            break
        case 'test':
            // Configure the root logger to only output to appLog appender.
            root {
                error 'stdout','appLog'
                additivity = true
            }
            debug 'grails.app.service'
            debug 'grails.app.controller'
            break
        case 'production':
            // Configure the root logger to only output to appLog appender.
            root {
                error 'appLog'
                additivity = true
            }
            warn 'grails.app.service'
            warn 'grails.app.controller'
            debug 'grails.app.service.AssetCsvService'
            debug 'grails.app.service.PersonCsvService'
            debug 'grails.app.service.InventoryCsvService'
            debug 'grails.app.service.AssetTreeService' /// @todo: remove after testing.
            break
    }
}


     