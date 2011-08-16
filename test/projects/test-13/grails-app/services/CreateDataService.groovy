/**
* Provides a data service to create base and demo data.
* Beware that most, if not all, base data is referenced by "Id" throughout the program.
* This allows changing the text of the 'name' property to something of the same meaning.
* But be sure to maintain the correct Id during creation, indicated by #1, #2 etc.
*/
class  CreateDataService {

    boolean transactional = false

    def sessionFactory
    def searchableService

    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    def startTime
    def lastBatchStarted

    def createDemoInventoryLocations() {

        // InventoryLocation
        def inventoryLocationInstance

        // InventoryLocation #1
        inventoryLocationInstance = new InventoryLocation(name: "X1")
        inventoryLocationInstance.save()

        def start = InventoryLocation.count() + 1
        def end = start + 10000

        def range = start..end

        def baseName = "Y"

        // Create batch.
        startTime = System.currentTimeMillis()
        lastBatchStarted = startTime
        range.each() {

            if(it % 100 == 0) {
                logStatus("Creating inventoryLocation #" + it)
                cleanUpGorm()
            }

            // InventoryLocation #2
            inventoryLocationInstance = new InventoryLocation(name: baseName + it)
            inventoryLocationInstance.save()
        } // each()

    } // createDemoInventoryLocations()

    def createDemoInventoryItems() {

        // InventoryItem
        def inventoryItemInstance
        def inventoryLocationInstance = InventoryLocation.read(1)

        // InventoryItem #1
        inventoryItemInstance = new InventoryItem(name: "2705",
                                                                                description: "Bearing",
                                                                                inventoryLocation: inventoryLocationInstance)
        inventoryItemInstance.save()

        // InventoryItem #2
        inventoryItemInstance = new InventoryItem(name: "Cotton Rope",
                                                                                description: "Natural cotton rope.",
                                                                                inventoryLocation: inventoryLocationInstance)
        inventoryItemInstance.save()

        def start = InventoryItem.count() + 1
        def end = start + 10000

        def range = start..end

        def baseName = "25M-"
        inventoryLocationInstance = InventoryLocation.read(2)

        // Create batch.
        startTime = System.currentTimeMillis()
        lastBatchStarted = startTime
        range.each() {

            if(it % 100 == 0) {
                logStatus("Creating inventoryItem #" + it)
                cleanUpGorm()
            }

            // InventoryItem #3
            inventoryItemInstance = new InventoryItem(name: baseName + it,
                                                                                    description: "Contactor.",
                                                                                    inventoryLocation: inventoryLocationInstance)
            inventoryItemInstance.save()
        } // each()

    } // createDemoInventoryItems()

    /**
    * This cleans up the hibernate session and a grails map.
    * For more info see: http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql/
    * The hibernate session flush is normal for hibernate.
    * The map is apparently used by grails for domain object validation errors.
    * A starting point for clean up is every 100 objects.
    */
    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    def logStatus(String message) {
        def batchEnded = System.currentTimeMillis()
        def seconds = (batchEnded-lastBatchStarted)/1000
        def total = (batchEnded-startTime)/1000
        log.info "${message}, last: ${seconds}s, total: ${total}s"
        lastBatchStarted = batchEnded
    }

    /**
    * SearchableIndex and mirroring is disabled at startup.
    * Use this to start indexing after creating bootstrap data.
    * @param indexInNewThread Whether to run the index in a new thread, defaults to true.
    */
    def startSearchableIndex(Boolean indexInNewThread = true) {
        log.info "Start mirroring searchable index."
        searchableService.startMirroring()
        if(indexInNewThread) {
            Thread.start {
                log.info "Rebuilding searchable index, bulkIndex (new thread)."
                searchableService.index()
                log.info "Rebuilding searchable index, complete."
            }
        }
        else {
            log.info "Rebuilding searchable index, bulkIndex."
            searchableService.index()
            log.info "Rebuilding searchable index, complete."
        }
    }

    /**
    * SearchableIndex index and mirroring during bulk data creation may be slow.
    * Use this to stop indexing and restart with startSearchableIndex() after data creation.
    */
    def stopSearchableIndex() {
        log.info "Stop mirroring searchable index."
        searchableService.stopMirroring()
    }

} // end of class