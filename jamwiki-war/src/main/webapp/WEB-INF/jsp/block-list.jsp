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

<div id="blocks">

<c:choose>
	<c:when test="${empty items}">
		<div class="message"><fmt:message key="blocklist.caption.none" /></div>
	</c:when>
	<c:otherwise>
		<div class="message"><fmt:message key="common.caption.view" />: <jamwiki:pagination total="${itemCount}" rootUrl="Special:BlockList" /></div>
		<ul>
		<c:forEach items="${items}" var="userBlock">
		<li>
			<fmt:formatDate value="${userBlock.blockDate}" type="both" pattern="${pageInfo.datePatternDateAndTime}" timeZone="${pageInfo.timeZoneId}" />
			&#160;.&#160;.&#160;
			<fmt:message key="blocklist.caption.blocked">
				<fmt:param><jamwiki:link value="User:jerry">${userBlock.blockedUsernameOrIpAddress}</jamwiki:link></fmt:param>
				<fmt:param><jamwiki:link value="${pageInfo.namespaces[pageInfo.virtualWikiName]['User']}:${userBlock.blockedByUsername}">${userBlock.blockedByUsername}</jamwiki:link></fmt:param>
			</fmt:message>
			&#160;.&#160;.&#160;
			<c:choose>
				<c:when test="${empty userBlock.blockEndDate}">
					<fmt:message key="blocklist.caption.end"><fmt:param><fmt:message key="common.interval.infinite" /></fmt:param></fmt:message>
				</c:when>
				<c:otherwise>
					<fmt:message key="blocklist.caption.end"><fmt:param><fmt:formatDate value="${userBlock.blockEndDate}" type="both" pattern="${pageInfo.datePatternDateAndTime}" timeZone="${pageInfo.timeZoneId}" /></fmt:param></fmt:message>
				</c:otherwise>
			</c:choose>
			<c:if test="${!empty userBlock.blockReason}">&#160;(<i><c:out value="${userBlock.blockReason}" /></i>)</c:if>
			<security:authorize url="/Special:Unblock">
				&#160;-&#160;
				<jamwiki:link value="Special:Unblock"><jamwiki:linkParam key="user" value="${userBlock.blockedUsernameOrIpAddress}" /><fmt:message key="blocklist.caption.unblock" /></jamwiki:link>
			</security:authorize>
		</li>
		</c:forEach>
		</ul>
	</c:otherwise>
</c:choose>

</div>