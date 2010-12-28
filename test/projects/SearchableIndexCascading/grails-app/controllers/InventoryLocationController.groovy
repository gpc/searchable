class InventoryLocationController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [inventoryLocationInstanceList: InventoryLocation.list(params), inventoryLocationInstanceTotal: InventoryLocation.count()]
    }

    def create = {
        def inventoryLocationInstance = new InventoryLocation()
        inventoryLocationInstance.properties = params
        return [inventoryLocationInstance: inventoryLocationInstance]
    }

    def test = {
        def inventoryLocationInstance = new InventoryLocation(params)
        if (inventoryLocationInstance.save()) {
            // With searchable cascading, a new inventory item must be saved independently
            // of its parent inventory location.
            inventoryLocationInstance.addToInventoryItems(name: "Dummy")
            inventoryLocationInstance.save()
            flash.message = message(
                    code: 'default.created.message',
                    args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'),
                    inventoryLocationInstance.id])
            redirect(action: "show", id: inventoryLocationInstance.id)
        }
        else {
            render(view: "create", model: [inventoryLocationInstance: inventoryLocationInstance])
        }
    }

    def search = {
        def items = InventoryItem.search(params.q)
        session.items = items.results
        redirect action: "showResults"
    }

    def showResults = {
        def items = session.items ?: []
        render items.collect { it.name + ': ' + it.inventoryLocation.inventoryItems }.join("\n")
    }

    def save = {
        def inventoryLocationInstance = new InventoryLocation(params)
        if (inventoryLocationInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'), inventoryLocationInstance.id])}"
            redirect(action: "show", id: inventoryLocationInstance.id)
        }
        else {
            render(view: "create", model: [inventoryLocationInstance: inventoryLocationInstance])
        }
    }

    def show = {
        def inventoryLocationInstance = InventoryLocation.get(params.id)
        if (!inventoryLocationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'), params.id])}"
            redirect(action: "list")
        }
        else {
            [inventoryLocationInstance: inventoryLocationInstance]
        }
    }

    def edit = {
        def inventoryLocationInstance = InventoryLocation.get(params.id)
        if (!inventoryLocationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [inventoryLocationInstance: inventoryLocationInstance]
        }
    }

    def update = {
        def inventoryLocationInstance = InventoryLocation.get(params.id)
        if (inventoryLocationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (inventoryLocationInstance.version > version) {
                    
                    inventoryLocationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'inventoryLocation.label', default: 'InventoryLocation')] as Object[], "Another user has updated this InventoryLocation while you were editing")
                    render(view: "edit", model: [inventoryLocationInstance: inventoryLocationInstance])
                    return
                }
            }
            inventoryLocationInstance.properties = params
            if (!inventoryLocationInstance.hasErrors() && inventoryLocationInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'), inventoryLocationInstance.id])}"
                redirect(action: "show", id: inventoryLocationInstance.id)
            }
            else {
                render(view: "edit", model: [inventoryLocationInstance: inventoryLocationInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def inventoryLocationInstance = InventoryLocation.get(params.id)
        if (inventoryLocationInstance) {
            try {
                inventoryLocationInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryLocation.label', default: 'InventoryLocation'), params.id])}"
            redirect(action: "list")
        }
    }
}
