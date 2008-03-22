
//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'Ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
// Ant.mkdir(dir:"/tmp/newplugin/grails-app/jobs")
//

Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

// TODO this is ancient - remove in next version
if (new File(basedir, 'grails-app/conf/SearchablePluginConfiguration.groovy').exists()) {
    println """
    
        STOP!

        It looks like you have an older version of the Searchable Plugin config file at:

            grails-app/conf/SearchablePluginConfiguration.groovy

        The file name has changed, so you need to rename it to:

            grails-app/conf/SearchableConfiguration.groovy
"""
}

println """

Thanks for installing the Grails Searchable Plugin!


Documentation is available at http://grails.org/Searchable+Plugin

Help is available from user@grails.codehaus.org

Issues and improvements should be raised at http://jira.codehaus.org/browse/GRAILSPLUGINS

"""
