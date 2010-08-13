

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'parent.label', default: 'Parent')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir: '')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${parentInstance}">
            <div class="errors">
                <g:renderErrors bean="${parentInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${parentInstance?.id}" />
                <g:hiddenField name="version" value="${parentInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="parent.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: parentInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${parentInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="children"><g:message code="parent.children.label" default="Children" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: parentInstance, field: 'children', 'errors')}">
                                    
<ul>
<g:each in="${parentInstance?.children?}" var="c">
    <li><g:link controller="child" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="child" action="create" params="['parent.id': parentInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'child.label', default: 'Child')])}</g:link>

                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
