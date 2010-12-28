class BootStrap {

    def init = { servletContext ->
        new InventoryLocation(name:'Bin1').save(failOnError: true)
        new InventoryLocation(name:'Bin2').save(failOnError: true)

        new InventoryItem(name:'Item1', inventoryLocation: InventoryLocation.get(1)).save(failOnError: true)
        new InventoryItem(name:'Item2', inventoryLocation: InventoryLocation.get(1)).save(failOnError: true)
    }

    def destroy = {
    }

}
