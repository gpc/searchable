package searchablebug

import org.joda.time.LocalDate;

class Person {
    String name
    LocalDate birthday

    static constraints = {
    }

    static searchable = {
        only = ['name', 'birthday']
        birthday(index: 'not_analyzed', converter: "jodaLocal")
    }
}
