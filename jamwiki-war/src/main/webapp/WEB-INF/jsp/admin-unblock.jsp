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
<%@ page import="
        java.util.Calendar
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="unblock">

<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.messages}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<div class="message"><fmt:message key="unblock.caption.overview" /></div>

<form id="block" name="block" method="post" action="<jamwiki:link value="Special:Unblock" />">
<div class="row">
	<label for="user"><fmt:message key="unblock.caption.user" /></label>
	<span><input type="text" name="user" value="<c:out value="${user}" />" id="user" size="60" /></span>
</div>
<div class="row">
	<label for="reason"><fmt:message key="unblock.caption.reason" /></label>
	<span><input type="text" name="reason" value="<c:out value="${reason}" />" id="user" size="60" /></span>
</div>
<div class="row">
	<span class="form-button"><input type="submit" name="unblock" value="<fmt:message key="unblock.button.unblock" />" /></span>
</div>
</form>

</div>
