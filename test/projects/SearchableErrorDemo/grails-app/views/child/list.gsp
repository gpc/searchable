

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'child.label', default: 'Child')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir: '')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'child.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'child.name.label', default: 'Name')}" />
                        
                            <th><g:message code="child.parent.label" default="Parent" /></th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${childInstanceList}" status="i" var="childInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${childInstance.id}">${fieldValue(bean: childInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: childInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: childInstance, field: "parent")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${childInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
