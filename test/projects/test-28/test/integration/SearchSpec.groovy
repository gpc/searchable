import grails.plugin.spock.IntegrationSpec

import org.joda.time.LocalDate
import searchablebug.Person

class SearchSpec extends IntegrationSpec {
    def searchableService

    def "Simple search"() {
        given: "A person"
        new Person(name: 'Mr Smith', birthday: new LocalDate(1980, 1, 1))

        when: "I search for people with the name Smith"
        def results = Person.search("Smith")

        then: "I get 1 result"
        results.total == 1
    }
}
