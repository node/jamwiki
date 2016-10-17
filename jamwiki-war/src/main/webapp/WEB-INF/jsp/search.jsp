<%--

  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.

  This program is free software; you can redistribute it and/or modify
  it under the terms of the latest version of the GNU Lesser General
  Public License as published by the Free Software Foundation;

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program (LICENSE.txt); if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

--%>
<%@ page errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<form id="searchForm" method="get" action="<jamwiki:link value="Special:Search" />">
<div class="message"><label for="searchTerm"><fmt:message key="search.for"/></label>&#160;&#160;<input type="text" name="text" size="50" value="<c:out value="${searchField}" />" id="searchTerm" />&#160;&#160;<input type="submit" name="Submit" value="<fmt:message key="search.search"/>" /></div>
<fieldset id="mw-searchoptions" style="margin:0em;"><legend><fmt:message key="search.caption.namespaces" /></legend>
<table><tr>
<%-- six elements per row with two spacers in between (so 8 columns total) --%>
<c:forEach items="${namespaces}" var="namespace" varStatus="status">
	<c:if test="${(status.count > 1 && status.count % 2 == 1 && status.count % 6 != 1)}"><td style="padding: 0 15px">&#160;</td></c:if>
	<c:if test="${(status.count > 1 && status.count % 6 == 1)}"></tr><tr></c:if>
	<td<c:if test="${status.last && (status.count) % 6 != 0}"> colspan="${8 - (status.count % 6) + 1}"</c:if>><input name="ns" type="checkbox" value="${namespace.key}" id="ns-${namespace.key}"<c:if test="${!empty selectedNamespaces[namespace.key]}"> checked="checked"</c:if>>&nbsp;<label for="ns-${namespace.key}">${namespace.value}</label></td>
</c:forEach>
</tr></table>
</fieldset>
</form>

<script type="text/javascript">document.getElementById("searchTerm").focus();</script>
<c:choose>
	<c:when test="${!empty notopic}">
		<%-- if "jump to" selected but no such topic exists --%>
		<div class="message"><fmt:message key="topic.notcreated"><fmt:param><c:out value="${notopic}" /></fmt:param><fmt:param><jamwiki:link value="${notopic}" text="${notopic}" /></fmt:param></fmt:message></div>
	</c:when>
	<c:when test="${empty searchField}">
		<div id="searchhints"><fmt:message key="search.hints"/></div>
	</c:when>
	<c:when test="${!empty results}">
		<c:forEach items="${results}" var="result">
			<div class="searchresult"><jamwiki:link value="${result.topic}" text="${result.topic}" /></div>
			<div class="searchsummary"><c:out value="${result.summary}" escapeXml="false" /></div>
		</c:forEach>
	</c:when>
	<c:otherwise>
		<div class="message"><fmt:message key="searchresult.notfound"><fmt:param><c:out value="${searchField}" escapeXml="true"/></fmt:param></fmt:message></div>
		<div class="message"><fmt:message key="topic.notcreated"><fmt:param><c:out value="${searchField}" escapeXml="true" /></fmt:param><fmt:param><jamwiki:link value="${searchField}" text="${searchField}" /></fmt:param></fmt:message></div>
	</c:otherwise>
</c:choose>

<div id="searchpower"><fmt:message key="search.poweredby" /> <fmt:message key="${searchConfig.key2}" /></div>
