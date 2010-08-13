class ParentController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        [parentInstanceList: Parent.list(params), parentInstanceTotal: Parent.count()]
    }

    def create = {
        def parentInstance = new Parent()
        parentInstance.properties = params
        return [parentInstance: parentInstance]
    }

    def save = {
        def parentInstance = new Parent(params)
        if (parentInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'parent.label', default: 'Parent'), parentInstance.id])}"
            redirect(action: "show", id: parentInstance.id)
        }
        else {
            render(view: "create", model: [parentInstance: parentInstance])
        }
    }

    def show = {
        def parentInstance = Parent.get(params.id)
        if (!parentInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'parent.label', default: 'Parent'), params.id])}"
            redirect(action: "list")
        }
        else {
            [parentInstance: parentInstance]
        }
    }

    def edit = {
        def parentInstance = Parent.get(params.id)
        if (!parentInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'parent.label', default: 'Parent'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [parentInstance: parentInstance]
        }
    }

    def update = {
        def parentInstance = Parent.get(params.id)
        if (parentInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (parentInstance.version > version) {
                    
                    parentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'parent.label', default: 'Parent')], "Another user has updated this Parent while you were editing")
                    render(view: "edit", model: [parentInstance: parentInstance])
                    return
                }
            }
            parentInstance.properties = params
            if (!parentInstance.hasErrors() && parentInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'parent.label', default: 'Parent'), parentInstance.id])}"
                redirect(action: "show", id: parentInstance.id)
            }
            else {
                render(view: "edit", model: [parentInstance: parentInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'parent.label', default: 'Parent'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def parentInstance = Parent.get(params.id)
        if (parentInstance) {
            try {
                parentInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'parent.label', default: 'Parent'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'parent.label', default: 'Parent'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'parent.label', default: 'Parent'), params.id])}"
            redirect(action: "list")
        }
    }
}
