

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'inventoryItem.label', default: 'InventoryItem')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${inventoryItemInstance}">
            <div class="errors">
                <g:renderErrors bean="${inventoryItemInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="inventoryItem.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryItemInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" maxlength="50" value="${inventoryItemInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description"><g:message code="inventoryItem.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryItemInstance, field: 'description', 'errors')}">
                                    <g:textArea name="description" cols="40" rows="5" value="${inventoryItemInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment"><g:message code="inventoryItem.comment.label" default="Comment" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryItemInstance, field: 'comment', 'errors')}">
                                    <g:textArea name="comment" cols="40" rows="5" value="${inventoryItemInstance?.comment}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="inventoryLocation"><g:message code="inventoryItem.inventoryLocation.label" default="Inventory Location" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryItemInstance, field: 'inventoryLocation', 'errors')}">
                                    <g:select name="inventoryLocation.id" from="${InventoryLocation.list()}" optionKey="id" value="${inventoryItemInstance?.inventoryLocation?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="isActive"><g:message code="inventoryItem.isActive.label" default="Is Active" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryItemInstance, field: 'isActive', 'errors')}">
                                    <g:checkBox name="isActive" value="${inventoryItemInstance?.isActive}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="isObsolete"><g:message code="inventoryItem.isObsolete.label" default="Is Obsolete" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryItemInstance, field: 'isObsolete', 'errors')}">
                                    <g:checkBox name="isObsolete" value="${inventoryItemInstance?.isObsolete}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="enableReorderListing"><g:message code="inventoryItem.enableReorderListing.label" default="Enable Reorder Listing" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: inventoryItemInstance, field: 'enableReorderListing', 'errors')}">
                                    <g:checkBox name="enableReorderListing" value="${inventoryItemInstance?.enableReorderListing}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
