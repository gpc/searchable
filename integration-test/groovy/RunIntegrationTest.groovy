/**
 * Run a Searchable Plugin integration test
 *
 * @author Maurice Nicholson
 */
USAGE = """
groovy RunIntegrationTest plugin-dir working-dir app-dir [app-dir2 ... [app-dirN]]

plugin-dir       -- the plugin project dir: should contain one 'grails-searchable-xxx.zip'
working-dir      -- the working directory to use for generated test apps
app-dir          -- the location of the test app template
app-dir2/N       -- more test app template dirs
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

def workingDir = args[1]

def appTemplateDirs = args[2..-1]
appTemplateDirs.each { dir ->
    assert new File(dir).isDirectory(), "App template dir does not exist or is not a directory: $dir"
}
def appName = new File(appTemplateDirs[0]).name

println "Running integration test\n  App: $appName $appTemplateDirs\n  Plugin dist: $pluginZip\n  Working dir: $workingDir"
getTestScript(appName, workingDir, appTemplateDirs, pluginZip).call()

def getTestScript(appName, dir, appDirs, pluginZip) {
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
        for (appDir in appDirs) {
            copy(todir: targetAppDir, overwrite: true) {
                fileset(dir: appDir)
            }
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


