class InventoryLocation {

    String name
    Boolean isActive = true

    static hasMany = [inventoryItems: InventoryItem]

    static constraints = {
        name(maxSize:50)
    }

    String toString() {
        "${this.name}"
    }

    static searchable = {
        root false // only index as a component of InventoryItem.
        only = ['name']
    }

//     def afterUpdate = {
//         // Update the Inventory searchable index, since cascading in searchable-0.5.5 is broken.
//         inventoryItems.each() {
//             it.reindex()
//         }
//     }

}
