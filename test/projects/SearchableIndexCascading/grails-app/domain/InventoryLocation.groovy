
class InventoryLocation {

    String name

    static hasMany = [inventoryItems: InventoryItem]

    String toString() {"${this.name}"}

    static searchable = {
        root false // only index as a component of InventoryItem.
        only = ['name']
        inventoryItems cascade:'all'
    }

}
