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

<p><fmt:message key="export.caption.overview" /></p>

<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<fieldset>
<legend><fmt:message key="export.caption.topics" /></legend>
<form name="form1" method="post" action="<jamwiki:link value="Special:Export" />">
<textarea rows="10" cols="40" name="topics" id="exportTopics">${topicNames}</textarea>
<br />
<input type="checkbox" name="history" id="exporthistory" value="1"<c:if test="${excludeHistory}"> checked="checked"</c:if> /><label for="exporthistory"><fmt:message key="export.caption.history" /></label>
<br />
<input type="checkbox" name="download" id="exportdownload" value="1"<c:if test="${download}"> checked="checked"</c:if> /><label for="exportdownload"><fmt:message key="export.caption.download" /></label>
<br />
<input type="submit" name="export" value="<fmt:message key="export.button.export" />" />
</form>
</fieldset>
