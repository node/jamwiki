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

<p><fmt:message key="import.caption.overview" /></p>

<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty successfulImports}">
<div class="message">
	<fmt:message key="import.message.success" />
	<ul>
	<c:forEach items="${successfulImports}" var="successfulImport"><li><jamwiki:link value="${successfulImport}" text="${successfulImport}" /></li></c:forEach>
	</ul>
</div>
</c:if>

<fieldset>
<legend><fmt:message key="import.caption.source" /></legend>
<form name="form1" method="post" action="<jamwiki:link value="Special:Import" />" enctype="multipart/form-data">
<input type="file" name="contents" size="50" id="importFile" />
<input type="submit" name="save" value="<fmt:message key="import.button.import" />" />
</form>
</fieldset>
