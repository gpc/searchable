import java.util.regex.Pattern

class Get {
    static classLoader = Thread.currentThread().contextClassLoader
    static {
        def dir = new File(Get.class.protectionDomain.codesource.location.path).absoluteFile.parentFile.parentFile
        new File(dir, 'lib').listFiles().each { file ->
            if (!file.name.startsWith('.')) {
                classLoader.addURL(new URL('file:' + file.absolutePath))
            }
        }
    }

    def server
    def dir
    def ignoreFile
    def startPage
    def username
    def password
    def done = new HashSet()
    def ignored = new HashSet()
    def match
    def extension = '.gdoc'

    static main(args) {
        def get = new Get(args)
        get.run()
    }

    Get(args) {
        parseArgs(args)
        for (required in ['server', 'dir', 'username', 'password', 'startPage']) {
            assert this.getProperty(required), "Please specify a ${required} with -${required}=xxx"
        }
        if (!match) {
            println "Note: you can specify a pattern to automatically confirm pages to get with -match=xxx"
        }

        if (match) {
            match = Pattern.compile(match)
        }
        if (!server.startsWith('http://')) {
            server = 'http://' + server
        }
        dir = new File(dir)
        ignoreFile = new File(dir, ".ignore")
    }

    def run() {
        makeDir(dir)
        readIgnored()
        doGet()
    }

    def parseArgs(args) {
        for (arg in args) {
            def matcher = arg =~ /-(.+)=(.+)/
            if (matcher.matches()) {
                this.setProperty(matcher[0][1], matcher[0][2])
            }
        }
    }

    def makeDir(dir, messageIfMade = true) {
        if (!dir.exists()) {
            makeDir(dir.getAbsoluteFile().getParentFile(), false)
            dir.mkdir()
            if (messageIfMade) {
                println "Created directory: \"${dir.absolutePath}\""
            }
        }
    }

    def readIgnored() {
        if (ignoreFile.exists()) {
            ignoreFile.eachLine { line ->
                line = line.trim()
                if (!line.startsWith('#')) {
                    ignored << line
                }
            }
        }
    }

    def addIgnore(pageName) {
        if (!ignoreFile.exists()) {
            ignoreFile.write("# This file lists the ignored pages\n" + pageName + "\n")
            println "Created file: \"${ignoreFile.absolutePath}\""
            ignored << pageName
        } else {
            ignoreFile.append(pageName + "\n")
        }
    }

    def doGet() {
        def client = classLoader.loadClass('org.apache.http.impl.client.DefaultHttpClient').newInstance()
        login(client)
        getPage(client, startPage)
    }

    def getPage(client, pageName, andSay = true) {
        if (andSay) {
            println "Getting page \"${pageName}\""
        }
        def post = classLoader.loadClass('org.apache.http.client.methods.HttpPost').newInstance(server + "/edit/" + URLEncoder.encode(pageName))
        def resp = responseToText(client.execute(post))
        
        def start = resp.indexOf('<textarea ')
        if (start == -1) {
            println "No wiki content found on page ${pageName}"
            return
        }
        start = resp.indexOf('>', start) + 1
        def end = resp.indexOf('</textarea>')
        def text = resp.substring(start, end)

        new File(dir, pageName + extension).write(text)
        done << pageName.replaceAll('\\+', ' ')
 
        processLinkedPages(text, client, pageName)
    }

    def processLinkedPages(text, client, fromPage) {
        // todo avoid code blocks and inline code
        for (int i = 0; i < text.size(); i++) {
            def c = text.charAt(i)
            if (c == '@') {
                i = text.indexOf('@', i + 1)
            }
            if (c == '{' && text.size() > i + 4) {
                if (text.substring(i, i + 5) == '{code') {
                    // move past the code block
//                    println "found code at ${i}"
                    def end = text.indexOf('{code}', i + 6) + 6
//                    println "moved to ${i}, skipped ${text.substring(i, end)}"
                    i = end
                    continue
                }
            }
            if (c == '[') {
                def end = text.indexOf(']', i)
//                println "found link ${i} -> ${end} -- ${text.substring(i + 1, end)}"
                processLink(client, text.substring(i + 1, end), fromPage)
                i = end
            }
        }
    }

    def processLink(client, inner, fromPage) {
        def link
        def matcher = inner =~ /(.+)\|(.+)/
        if (matcher.matches()) {
            link = matcher[0][2]
        } else {
            link = inner
        }
        if (link.contains('#')) {
            def i = link.indexOf('#')
            link = link.substring(0, i)
        }
        if (link && !link.startsWith('http') && !done.contains(link)) {
            if (!ignored.contains(link)) {
                if (confirmGet(link)) {
                    while (true) {
                        println "Get \"${link}\" (linked from ${fromPage})? [Y,n]"
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
                        def answer = br.readLine(); 
                        if (answer == '' || answer.equalsIgnoreCase('y')) {
                            getPage(client, link, false)
                            break
                        } else if (answer.equalsIgnoreCase('n')) {
                            addIgnore(link)
                            break
                        }
                    }
                } else {
                    getPage(client, link)
                }
            }
        }
    }

    def confirmGet(link) {
        if (new File(dir, link).exists() || (match && match.matcher(link).matches())) {
            return false
        }
        return true
    }

    def login(client) {
        def post = classLoader.loadClass('org.apache.http.client.methods.HttpPost').newInstance(server + "/login?login=${username}&password=${password}")
        def resp = responseToText(client.execute(post))
//        response.entity.writeTo(System.out)
//        response.entity.consumeContent()
        if (resp.indexOf('<form action="/login"') != -1) {
            println "Invalid username/password?"
            System.exit(1)
        }
        return client
    }

    def responseToText(response) {
        def os = new ByteArrayOutputStream()
        response.entity.writeTo(os)
        os.toString()
    }
}
