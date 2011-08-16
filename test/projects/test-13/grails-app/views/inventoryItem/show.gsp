

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'inventoryItem.label', default: 'InventoryItem')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="inventoryItem.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: inventoryItemInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="inventoryItem.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: inventoryItemInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="inventoryItem.description.label" default="Description" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: inventoryItemInstance, field: "description")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="inventoryItem.comment.label" default="Comment" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: inventoryItemInstance, field: "comment")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="inventoryItem.inventoryLocation.label" default="Inventory Location" /></td>
                            
                            <td valign="top" class="value"><g:link controller="inventoryLocation" action="show" id="${inventoryItemInstance?.inventoryLocation?.id}">${inventoryItemInstance?.inventoryLocation?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="inventoryItem.isActive.label" default="Is Active" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${inventoryItemInstance?.isActive}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="inventoryItem.isObsolete.label" default="Is Obsolete" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${inventoryItemInstance?.isObsolete}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="inventoryItem.enableReorderListing.label" default="Enable Reorder Listing" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${inventoryItemInstance?.enableReorderListing}" /></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${inventoryItemInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
