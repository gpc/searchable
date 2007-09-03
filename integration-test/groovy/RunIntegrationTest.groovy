/**
 * Run a Searchable Plugin integration test
 *
 * @author Maurice Nicholson
 */
USAGE = """
groovy RunIntegrationTest [-v[erbose]] plugin-dir working-dir app-dir [app-dir2 ... [app-dirN]]

plugin-dir       -- the plugin project dir: should contain one 'grails-searchable-xxx.zip'
working-dir      -- the working directory to use for generated test apps
app-dir          -- the location of the test app template
app-dir2/N       -- more test app template dirs

-v or -verbose   -- echo command output
"""

args = args as List
def verbose = false
for (v in ["-verbose", "-v"]) {
    if (args.indexOf(v) > -1) {
        verbose = true
        args.remove(v)
    }
}
if (args.size() < 3) {
    println USAGE
    System.exit(1)
}

def pluginDir = args[0]
assert new File(pluginDir).isDirectory(), "Plugin dir does not exist: $pluginDir"
def pluginZips = new File(pluginDir).listFiles().findAll { it.name ==~ /grails-searchable.*\.zip/ }
assert pluginZips.size() == 1, "Too many plugin zips found: ${pluginZips}. There should only be one"
def pluginZip = pluginZips[0].absolutePath

def workingDir = args[1]

def appTemplateDirs = args[2..-1]
appTemplateDirs.each { dir ->
    assert new File(dir).isDirectory(), "App template dir does not exist or is not a directory: $dir"
}
def appName = new File(appTemplateDirs[0]).name

println "Running integration test\n  App: $appName $appTemplateDirs\n  Plugin dist: $pluginZip\n  Working dir: $workingDir"
getTestScript(verbose, appName, workingDir, appTemplateDirs, pluginZip).call()

def getTestScript(verbose, appName, dir, appDirs, pluginZip) {
    def testScript = {
        // A unique number for generated property names, since Ant will not overwrite
        // a property once set
        def uniquePropertyNum = 0

        // Wraps Ant's exec to swallow output unless there are errors
        def executeQuietly = { Map attrs ->
            uniquePropertyNum++
            assert attrs.executable, "attrs.executable is requied"
            assert attrs.dir, "attrs.dir is required"
//            println "running ${attrs.executable} ${attrs.args.join(' ')} in ${attrs.dir}"
            def ouputPropertyName = "exec.output.$uniquePropertyNum"
            def resultPropertyName = "exec.result.$uniquePropertyNum"
            if (verbose) {
                println "Executing: [${attrs.executable}], dir: [${attrs.dir}], args: ${attrs.args}"
            }
            exec(executable: attrs.executable, dir: attrs.dir, failonerror: "yes", newenvironment: 'yes', outputproperty: ouputPropertyName, resultproperty: resultPropertyName) {
                for (value in attrs.args) {
                    arg(value: value)
                }
            }
//            println "exit status " + project.properties['exec.result']
            if (project.properties[resultPropertyName] != '0') {
                println "\nFAILED!\n"
                println "Command: \"${attrs.executable} ${attrs.args.join(' ')}\""
                println "Exit code: ${project.properties[resultPropertyName]}"
                println "Output:"
                println "-" * 60
                println project.properties[ouputPropertyName]
                println "-" * 60
            } else if (verbose) {
                println "Exit code: [${project.properties[resultPropertyName]}]"
                println "Output: [\n${project.properties[ouputPropertyName]}\n]"
            }

        }

        condition(property: "grails", value: "grails.bat") {
            os(family: "windows")
        }
        property(name: "grails", value: "grails")
        def grails = project.properties['grails']

        // Clean previous
        delete(dir: dir)
        mkdir(dir: dir)

        // Make the vanilla app
        println "    Making app $appName"
        executeQuietly.call(executable: grails, dir: dir, args: ['create-app', appName])

        // Copy app files
        println "    Copying template files"
        def targetAppDir = "${dir}/${appName}"
        for (appDir in appDirs) {
            copy(todir: targetAppDir, overwrite: true) {
                fileset(dir: appDir)
            }
        }

        // Install Searchable Plugin
        println "    Installing plugin"
        executeQuietly.call(executable: grails, dir: targetAppDir, args: ['install-plugin', new File(pluginZip).absolutePath])

        // Test app
        println "    Running unit/integration tests"
        executeQuietly.call(executable: grails, dir: targetAppDir, args: ['test-app'])

        // Webtest
        println "    Running web tests"
        executeQuietly.call(executable: grails, dir: targetAppDir, args: ['run-webtest'])

        // Check for webtest success (currently Grails doesn't check for success)
        def webestResult = new File(targetAppDir, "webtest/reports/WebTestResults.xml")
        assert webestResult.exists(), "webtest result file not found: ${webestResult.absolutePath}"
        def xml = new XmlSlurper().parse(webestResult)
        if (xml.testresult.@successful != 'yes') {
            fail(message: """

FAILED

See file://${new File(targetAppDir, 'webtest/reports/WebTestResults.html').absolutePath}
""")
        }
    }
    def ant = new AntBuilder()
    testScript.delegate = ant
    testScript
}


