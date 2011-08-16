class InventoryItemController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [inventoryItemInstanceList: InventoryItem.list(params), inventoryItemInstanceTotal: InventoryItem.count()]
    }

    def create = {
        def inventoryItemInstance = new InventoryItem()
        inventoryItemInstance.properties = params
        return [inventoryItemInstance: inventoryItemInstance]
    }

    def save = {
        def inventoryItemInstance = new InventoryItem(params)
        if (inventoryItemInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'inventoryItem.label', default: 'InventoryItem'), inventoryItemInstance.id])}"
            redirect(action: "show", id: inventoryItemInstance.id)
        }
        else {
            render(view: "create", model: [inventoryItemInstance: inventoryItemInstance])
        }
    }

    def show = {
        def inventoryItemInstance = InventoryItem.get(params.id)
        if (!inventoryItemInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryItem.label', default: 'InventoryItem'), params.id])}"
            redirect(action: "list")
        }
        else {
            [inventoryItemInstance: inventoryItemInstance]
        }
    }

    def edit = {
        def inventoryItemInstance = InventoryItem.get(params.id)
        if (!inventoryItemInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryItem.label', default: 'InventoryItem'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [inventoryItemInstance: inventoryItemInstance]
        }
    }

    def update = {
        def inventoryItemInstance = InventoryItem.get(params.id)
        if (inventoryItemInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (inventoryItemInstance.version > version) {
                    
                    inventoryItemInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'inventoryItem.label', default: 'InventoryItem')] as Object[], "Another user has updated this InventoryItem while you were editing")
                    render(view: "edit", model: [inventoryItemInstance: inventoryItemInstance])
                    return
                }
            }
            inventoryItemInstance.properties = params
            if (!inventoryItemInstance.hasErrors() && inventoryItemInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'inventoryItem.label', default: 'InventoryItem'), inventoryItemInstance.id])}"
                redirect(action: "show", id: inventoryItemInstance.id)
            }
            else {
                render(view: "edit", model: [inventoryItemInstance: inventoryItemInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryItem.label', default: 'InventoryItem'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def inventoryItemInstance = InventoryItem.get(params.id)
        if (inventoryItemInstance) {
            try {
                inventoryItemInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'inventoryItem.label', default: 'InventoryItem'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'inventoryItem.label', default: 'InventoryItem'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'inventoryItem.label', default: 'InventoryItem'), params.id])}"
            redirect(action: "list")
        }
    }
}
