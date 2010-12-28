class  InventoryItem {

    InventoryLocation inventoryLocation

    String name

    String toString() {"${this.name}"}

    static searchable = {
        only = ['name', 'inventoryLocation']
        inventoryLocation component: true
    }
}
