/**
 * Run a Searchable Plugin integration test
 *
 * @author Maurice Nicholson
 */
USAGE = """
groovy RunIntegrationTest plugin-dir app-source temp-dir

plugin-dir       -- the plugin project dir: should contain one 'grails-searchable-xxx.zip'
app-template-dir -- the location of the test app template
temp-dir         -- the working directory to use for generated test apps
"""
if (args.size() < 3) {
    println USAGE
    System.exit(1)
}
def pluginDir = args[0]
assert new File(pluginDir).isDirectory(), "Plugin dir does not exist: $pluginDir"
def pluginZips = new File(pluginDir).listFiles().findAll { it.name ==~ /grails-searchable.*\.zip/ }
assert pluginZips.size() == 1, "Too many plugin zips found: ${pluginZips}. There should only be one"
def pluginZip = pluginZips[0].absolutePath
def appTemplateDir = args[1]
assert new File(appTemplateDir).isDirectory(), "App template dir does not exist or is not a directory: $appTemplateDir"
def appName = new File(appTemplateDir).name
def workingDir = args[2]

println "Running integration test\n  App: $appName ($appTemplateDir)\n  Plugin dist: $pluginZip\n  Working dir: $workingDir"
getTestScript(appName, workingDir, appTemplateDir, pluginZip).call()

def getTestScript(appName, dir, appDir, pluginZip) {
    def testScript = {
        condition(property: "grails", value: "grails.bat") {
            os(family: "windows")
        }
        property(name: "grails", value: "grails")

        // Clean previous
        delete(dir: dir)
        mkdir(dir: dir)

        // Make the vanilla app
        exec(executable: '${grails}', failonerror: "yes", dir: dir, newenvironment: 'yes') {
            arg(value: "create-app")
            arg(value: appName)
        }

        // Copy app files
        def targetAppDir = "${dir}/${appName}"
        copy(todir: targetAppDir) {
            fileset(dir: appDir)
        }
        // Install Searchable Plugin
        exec(executable: '${grails}', failonerror: "yes", dir: targetAppDir, newenvironment: 'yes') {
            arg(value: "install-plugin")
            arg(value: new File(pluginZip).absolutePath)
        }
        // Test app
        exec(executable: '${grails}', failonerror: "yes", dir: targetAppDir, newenvironment: 'yes') {
            arg(value: "test-app")
        }
        // Webtest
        exec(executable: '${grails}', failonerror: "yes", dir: targetAppDir, newenvironment: 'yes') {
            arg(value: "run-webtest")
        }
    }
    def ant = new AntBuilder()
    testScript.delegate = ant
    testScript
}


