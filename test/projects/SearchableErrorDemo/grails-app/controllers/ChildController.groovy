class ChildController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        [childInstanceList: Child.list(params), childInstanceTotal: Child.count()]
    }

    def create = {
        def childInstance = new Child()
        childInstance.properties = params
        return [childInstance: childInstance]
    }

    def save = {
        def childInstance = new Child(params)
        if (childInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'child.label', default: 'Child'), childInstance.id])}"
            redirect(action: "show", id: childInstance.id)
        }
        else {
            render(view: "create", model: [childInstance: childInstance])
        }
    }

    def show = {
        def childInstance = Child.get(params.id)
        if (!childInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'child.label', default: 'Child'), params.id])}"
            redirect(action: "list")
        }
        else {
            [childInstance: childInstance]
        }
    }

    def edit = {
        def childInstance = Child.get(params.id)
        if (!childInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'child.label', default: 'Child'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [childInstance: childInstance]
        }
    }

    def update = {
        def childInstance = Child.get(params.id)
        if (childInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (childInstance.version > version) {
                    
                    childInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'child.label', default: 'Child')], "Another user has updated this Child while you were editing")
                    render(view: "edit", model: [childInstance: childInstance])
                    return
                }
            }
            childInstance.properties = params
            if (!childInstance.hasErrors() && childInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'child.label', default: 'Child'), childInstance.id])}"
                redirect(action: "show", id: childInstance.id)
            }
            else {
                render(view: "edit", model: [childInstance: childInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'child.label', default: 'Child'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def childInstance = Child.get(params.id)
        if (childInstance) {
            try {
                childInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'child.label', default: 'Child'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'child.label', default: 'Child'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'child.label', default: 'Child'), params.id])}"
            redirect(action: "list")
        }
    }
}
