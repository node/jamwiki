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
        org.jamwiki.utils.WikiUtil
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="move">

<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<div class="message"><fmt:message key="move.overview" /></div>

<%-- FIXME: change from table to divs --%>
<form name="delete" method="post" action="<jamwiki:link value="Special:Move" />">
<input type="hidden" name="<%= WikiUtil.PARAMETER_TOPIC %>" value="<c:out value="${pageInfo.topicName}" />" />
<div class="row">
	<label for="moveDestination"><fmt:message key="move.destination" /></label>
	<span><input type="text" name="moveDestination" value="<c:out value="${moveDestination}" />" id="moveDestination" size="60" /></span>
</div>
<div class="row">
	<label for="moveComment"><fmt:message key="move.comment" /></label>
	<span><input type="text" name="moveComment" value="<c:out value="${moveComment}" />" id="moveComment" size="60" /></span>
</div>
<c:if test="${!empty moveCommentsPage}">
<div class="row">
	<label for="moveCommentsPage"><fmt:message key="move.commentspage" /></label>
	<span><input type="checkbox" name="moveCommentsPage" value="<c:out value="${moveCommentsPage}" />" id="moveCommentsPage" /></span>
</div>
</c:if>
<div class="row">
	<span class="form-button"><input type="submit" name="move" value="<fmt:message key="move.button.move" />" /></span>
</div>
</form>

</div>
