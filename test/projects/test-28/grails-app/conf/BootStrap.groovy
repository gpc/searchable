import org.joda.time.LocalDate;

import searchablebug.Person;

class BootStrap {

    def init = { servletContext ->
        Person person = new Person(name: 'Mr Smith', birthday: new LocalDate(1980, 1, 1))
        person.save(failOnError:true)
    }

    def destroy = {
    }
}
