class BootStrap
{
    def createDataService

    def init = { servletContext ->

        /** Environment specific settings.
        * Note that (circa grails-1.2.2) if running "test test-app integration:" from "grails interactive"
        * the correct environment is applied for DataSource and Config,
        * but for BootStrap (and others) the development environment is applied.
        */
        environments {
            development {
                log.info "Starting DEVELOPMENT bootstrap."
                createDataService.createDemoInventoryLocations()
                createDataService.createDemoInventoryItems()
                createDataService.startSearchableIndex()
            }
            test {
                log.info "Starting TEST bootstrap."
                createDataService.createDemoInventoryLocations()
                createDataService.createDemoInventoryItems()
                createDataService.startSearchableIndex(false)
            }
            production {
                log.info "Starting PRODUCTION bootstrap."
                createDataService.createDemoInventoryLocations()
                createDataService.createDemoInventoryItems()
                createDataService.startSearchableIndex()
            }
        }

    } // init

    def destroy = {
    }

} // end class
