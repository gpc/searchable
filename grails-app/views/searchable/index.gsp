<%@ page import="org.springframework.util.ClassUtils" %>
<%@ page import="org.codehaus.groovy.grails.plugins.searchable.SearchableUtils" %>
<%@ page import="org.codehaus.groovy.grails.plugins.searchable.lucene.LuceneUtils" %>
<html>  
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title><g:if test="${params.q && params.q?.trim() != ''}">${'\u201c'}${params.q}${'\u201d'} - </g:if>Grails Searchable Plugin</title>
    <style type="text/css">
      * {
        font-family: Arial, sans-serif;
        padding: 0;
        margin: 0;
      }

      body {
        font-size: 0.9em;
        padding: .5em;
      }

      #header form input {
        padding: .1em;
      }

      #header .hint {
        color: gray;
      }

      .title {
        margin: 1em 0;
        padding: .3em .5em;
        text-align: right;
        background-color: seashell;
        border-top: 1px solid lightblue;
      }

      .result {
        margin-bottom: 1em;
      }

      .result .displayLink {
        color: green;
      }

      .result .name {
        font-size: larger;
      }

      .paging a {
        padding: 0 .2em;
      }

      ul {
        margin: 1em 2em;
      }

      li, p {
        margin-bottom: 1em;
      }
    </style>
    <script type="text/javascript">
        var focusQueryInput = function() {
            document.getElementById("q").focus();
        }
    </script>
  </head>
  <body onload="focusQueryInput();">
  <div id="header">
    <a href="http://grails.org/Searchable+Plugin" target="_blank"><span class="hint" style="float: right">RTFM</span></a>
    <h1>Grails Searchable Plugin</h1>
    <g:form url='[controller: "searchable", action: "index"]' id="searchableForm" method="get">
        <g:textField name="q" value="${params.q}" size="50"/> <input type="submit" value="Search" />
    </g:form>
    <div style="clear: both;" class="hint">See <a href="http://lucene.apache.org/java/docs/queryparsersyntax.html">Lucene query syntax</a> for advanced queries</div>
  </div>
  <div id="main">
    <g:set var="haveQuery" value="${params.q?.trim()}" />
    <g:set var="haveResults" value="${searchResult?.results}" />
    <div class="title">
      <span>
        <g:if test="${haveQuery && haveResults}">
          Showing <strong>${searchResult.offset + 1}</strong> - <strong>${searchResult.results.size() + searchResult.offset}</strong> of <strong>${searchResult.total}</strong>
          results for <strong>${params.q}</strong>
        </g:if>
        <g:else>
        &nbsp;
        </g:else>
      </span>
    </div>

    <g:if test="${parseException}">
      <p>Your query - <strong>${params.q}</strong> - is not valid.</p>
      <p>Suggestions:</p>
      <ul>
        <li>Fix the query: see <a href="http://lucene.apache.org/java/docs/queryparsersyntax.html">Lucene query syntax</a> for examples</li>
        <g:if test="${LuceneUtils.queryHasSpecialCharacters(params.q)}">
          <li>Remove special characters like <strong>" - [ ]</strong>, before searching, eg, <em><strong>${LuceneUtils.cleanQuery(params.q)}</strong></em><br />
              <em>Use the Searchable Plugin's <strong>LuceneUtils#cleanQuery</strong> helper method for this: <g:link controller="searchable" action="index" params="[q: LuceneUtils.cleanQuery(params.q)]">Search again with special characters removed</g:link></em>
          </li>
          <li>Escape special characters like <strong>" - [ ]</strong> with <strong>\</strong>, eg, <em><strong>${LuceneUtils.escapeQuery(params.q)}</strong></em><br />
              <em>Use the Searchable Plugin's <strong>LuceneUtils#escapeQuery</strong> helper method for this: <g:link controller="searchable" action="index" params="[q: LuceneUtils.escapeQuery(params.q)]">Search again with special characters escaped</g:link></em><br />
              <em>Or use the Searchable Plugin's <strong>escape</strong> option: <em><g:link controller="searchable" action="index" params="[q: params.q, escape: true]">Search again with the <strong>escape</strong> option enabled</g:link></em>
          </li>
        </g:if>
      </ul>
    </g:if>
    <g:elseif test="${haveQuery && !haveResults}">
      <p>Nothing matched your query - <strong>${params.q}</strong></p>
    </g:elseif>
    <g:elseif test="${haveResults}">
      <!-- TODO implement highlighting -->
      <!-- TODO implement sort by domain class type, relevance, ... -->
      <!-- TODO show generated compass config settings: index dir, mapped classes and associations -->
      <!-- TODO tick boxes or similar for choosing specific mapped class(es)  -->
      <div class="results">
        <g:each var="result" in="${searchResult.results}" status="index">
          <div class="result">
            <g:set var="className" value="${ClassUtils.getShortName(result.getClass())}" />
            <g:set var="link" value="${createLink(controller: className[0].toLowerCase() + className[1..-1], action: 'show', id: result.id)}" />
            <div class="name"><a href="${link}">${className} #${result.id}</a></div>
            <g:set var="desc" value="${result.toString()}" />
            <g:if test="${desc.size() > 120}"><g:set var="desc" value="${desc[0..120] + '...'}" /></g:if>
            <div class="desc">${desc.encodeAsHTML()}</div>
            <div class="displayLink">${link}</div>
            <div></div>
          </div>
        </g:each>
      </div>

      <div>
        <g:set var="currentPage" value="${SearchableUtils.getCurrentPage(searchResult)}" />
        <g:set var="totalPages" value="${SearchableUtils.getTotalPages(searchResult)}" />
        <g:set var="startPage" value="${Math.max(1 as int, currentPage - 5 as int)}" />
        <g:set var="range" value="${startPage..Math.min(startPage + 10, totalPages)}" />
        <div class="paging">
          <g:if test="${searchResult.results.size() > 0}">
              Page:
              <%-- Previous --%>
              <g:if test="${currentPage > 1}">
                <g:link controller="searchable" action="index" params="[q: params.q, offset: SearchableUtils.getOffsetForPage(currentPage - 1, searchResult)]">&lt; previous</g:link>
              </g:if>
              <%-- First --%>
              <g:if test="${!range.contains(1)}">
                <g:if test="${currentPage == 1}">1</g:if>
                <g:else>
                  <g:link controller="searchable" action="index" params="[q: params.q, offset: 0]">1</g:link>
                </g:else>
              </g:if>
              <%-- Page list --%>
              <g:each var="itPage" in="${range}">
                <g:if test="${itPage == currentPage}"><strong>${itPage}</strong></g:if>
                <g:else>
                  <g:link controller="searchable" action="index" params="[q: params.q, offset: SearchableUtils.getOffsetForPage(itPage, searchResult)]">${itPage}</g:link>
                </g:else>
              </g:each>
              <%-- Last -- not sure how useful this is so commented out for now
              <g:if test="${!range.contains(totalPages)}">
                <g:if test="${currentPage == totalPages}">${totalPages}</g:if>
              </g:if> --%>
              <%-- Next --%>
              <g:if test="${currentPage < totalPages}">
                <g:link controller="searchable" action="index" params="[q: params.q, offset: SearchableUtils.getOffsetForPage(currentPage + 1, searchResult)]">next &gt;</g:link>
              </g:if>
          </g:if>
        </div>
      </div>
    </g:elseif>
  </div>
  </body>
</html>