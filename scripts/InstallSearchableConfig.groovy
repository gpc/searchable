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

/**
 * A Grails Gant script to install the Searchable Plugin configuration file for a user
 * to customise: only required if you need to override default behavoir
 *
 * @author Maurice Nicholson
 */
Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )

task ('default': "Installs the Searchable Plugin configuration file: only required if you need to override default behavoir") {
    depends(checkVersion)

    def dest = "${basedir}/grails-app/conf"
    def destFile = new File(dest, "SearchableConfiguration.groovy")
    def exists = false
    if (destFile.exists()) {
        exists = true
        Ant.input(message: """
        STOP!
        Searchable configuration already exists:

            ${destFile}

        If you continue it will be overwritten.
        Are you sure you want to continue?
            """,
			validargs:"y,n",
			addproperty:"grails.install.searchable.config.warning")

       def answer = Ant.antProject.properties."grails.install.searchable.config.warning"

       if (answer == "n") exit(0)
           Ant.delete(file: destFile.toString())
    }
    Ant.copy(file: "${basedir}/plugins/searchable-0.4.2/src/conf/SearchableConfiguration.groovy", todir: dest)
    Ant.echo(message: """
        Searchable configuration file ${exists ? 're-' : ''}created:

            ${destFile}
        
    """)
}