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

<div id="blocked">

<div class="message"><fmt:message key="userblock.caption.overview" /></div>

<c:if test="${!empty pageInfo.messages}">
<p><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></p>
</c:if>
<c:if test="${!empty userBlock}">
<p>
	<fmt:message key="userblock.caption.blockedby"><fmt:param><jamwiki:link value="${pageInfo.namespaces[pageInfo.virtualWikiName]['User']}:${userBlock.blockedByUsername}">${userBlock.blockedByUsername}</jamwiki:link></fmt:param></fmt:message>
	<c:if test="${!empty userBlock.blockReason}"><fmt:message key="userblock.caption.reason"><fmt:param value="${userBlock.blockReason}" /></fmt:message></c:if>
</p>
<ul>
<li><fmt:message key="userblock.caption.start"><fmt:param><fmt:formatDate value="${userBlock.blockDate}" type="both" pattern="${pageInfo.datePatternDateAndTime}" timeZone="${pageInfo.timeZoneId}" /></fmt:param></fmt:message></li>
<li>
	<c:choose>
		<c:when test="${empty userBlock.blockEndDate}">
			<fmt:message key="userblock.caption.end"><fmt:param><fmt:message key="common.interval.infinite" /></fmt:param></fmt:message>
		</c:when>
		<c:otherwise>
			<fmt:message key="userblock.caption.end"><fmt:param><fmt:formatDate value="${userBlock.blockEndDate}" type="both" pattern="${pageInfo.datePatternDateAndTime}" timeZone="${pageInfo.timeZoneId}" /></fmt:param></fmt:message>
		</c:otherwise>
	</c:choose>
</li>
<li><fmt:message key="userblock.caption.target"><fmt:param value="${userBlock.blockedUsernameOrIpAddress}" /></fmt:message></li>
</ul>
</c:if>

</div>
