class Users {
    String name
    String countryCode

    static searchable = {
        countryCode analyzer: "simple"
    }
}
