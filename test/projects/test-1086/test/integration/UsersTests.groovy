import grails.test.*

class UsersTests extends GrailsUnitTestCase {
    def searchableService
    protected void setUp() {
        super.setUp()
        new Users(name:"test3", countryCode:"ITT").save(failOnError: true)
        new Users(name:"test7", countryCode:"ITT").save(failOnError: true)

        new Users(name:"test1", countryCode:"FR").save(failOnError: true)
        new Users(name:"test8", countryCode:"FR").save(failOnError: true)
        new Users(name:"test5", countryCode:"FR").save(failOnError: true)

        new Users(name:"test2", countryCode:"BE").save(failOnError: true)
        new Users(name:"test6", countryCode:"BE").save(failOnError: true)
        
        new Users(name:"test4", countryCode:"PL").save(failOnError: true)

        searchableService.reindex()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSearchable() {
        def itResult = Users.search("countryCode:'ITT'")
        def frResult = Users.search("countryCode:'FR'")
        def beResult = Users.search("+countryCode:'BE'")
        def plResult = Users.search("countryCode:'PL'")
        println "Number Of IT : " + itResult.total 
        println "Number Of FR : " + frResult.total 
        println "Number Of BE : " + beResult.total 
        println "Number Of PL : " + plResult.total
        assert itResult.total == 2
        assert frResult.total == 3
        assert beResult.total == 2
        assert plResult.total == 1
    }
}
