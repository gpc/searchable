eventAllTestsStart = {
    def specTestTypeClass = loadSpecTestTypeClass.call()
    functionalTests << specTestTypeClass.newInstance('spock', "functional")
}
