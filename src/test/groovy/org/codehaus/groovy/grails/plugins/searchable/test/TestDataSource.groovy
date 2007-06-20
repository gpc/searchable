package org.codehaus.groovy.grails.plugins.searchable.test

/**
 * Default test data source is in-memory HSQLDB for faster test runs
 *
 * @author Maurice Nicholson
 * @see MysqlTestDataSource for an additional test data source that tests should also executed against
 */
class TestDataSource {
   boolean pooling = true
   String dbCreate = 'create-drop' // one of 'create', 'create-drop','update'
   String url = "jdbc:hsqldb:mem:testDB"
   String driverClassName = "org.hsqldb.jdbcDriver" //"com.p6spy.engine.spy.P6SpyDriver"
   String username = "sa"
   String password = ""

//   def logSql = true
}