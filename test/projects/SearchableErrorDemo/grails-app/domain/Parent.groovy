class Parent {
    String name
    static searchable=true
    static hasMany = [children:Child]
    static constraints = {
    }
}
