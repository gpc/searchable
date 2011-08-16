class  InventoryItem {
    InventoryLocation inventoryLocation
    String name
    String description = ""
    String comment = ""
    boolean isActive = true
    boolean isObsolete = false
    boolean enableReorderListing = true

//     static belongsTo = []

    static constraints = {
        name(unique:true, blank:false, maxSize:50)
        description(maxSize:255)
        comment(maxSize:500)
        inventoryLocation()
        isActive()
        isObsolete()
        enableReorderListing()
    }

    String toString() {"${this.name}"}

    static searchable = {
        only = ['name', 'description', 'comment', 'isActive', 'isObsolete', 'inventoryLocation']
        //name boost: 1.5
        //inventoryLocation component: true
        inventoryLocation component: [cascade: 'all']
        //inventoryGroup component: true
        //spareFor component: true
    }

}
