import org.codehaus.groovy.grails.commons.*

/**
* Provides a quartz job that reindex's the searchable index for the Inventory domain class.
* With concurrent=false the next job starts after the previous job completes.
* We need a hibernate session otherwise we get a LazyInitializationException, default is true but we specify it to be sure.
* Rebuilding the index is required since components are not updated when they change, that is 
* until the parent is updated and reindexed.
*/
class InventoryReindexJob {

    def concurrent = false
    def sessionRequired = true

    def createDataService

    static triggers = {
        // Cron fields:
        // 'Seconds Minutes Hours DOM Month DOW Year(Optional)'
        // See: http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html
        // Trigger every hour on the hour:
        //cron name: 'RebuildInventoryIndex', cronExpression: "0 0 * * * ?"

        // Simple repeating trigger/
        simple name: "RebuildInventoryIndex",
                    startDelay: 50000,
                    repeatInterval: 1000
    }

    def execute() {

        // Some information can be accessed if we run with "def execute(context) ".
        // For more info see: http://quartz.sourceforge.net/javadoc/org/quartz/JobExecutionContext.html
        // log.debug context.getTrigger()
        // log.debug context.getPreviousFireTime()
        // log.debug context.getFireTime()

        // Reindex the Inventory domain class.
        log.info "Rebuilding searchable index, Inventory.reindex()."
        InventoryItem.reindex() // Blocks searching while indexing and may fail to rename.
//         InventoryItem.index() // Does not block searching while indexing and does not cause compass error: "Failed to rename index"
        log.info "Rebuilding searchable index, complete."

    } //execute()

} // end of class
