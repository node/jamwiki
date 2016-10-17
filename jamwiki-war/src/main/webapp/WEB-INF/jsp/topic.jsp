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

<%-- if viewing an old version display revision info --%>
<c:if test="${!empty version}">
	<div id="contentSub">
		<div id="revision">
			<fmt:message key="history.revision">
				<fmt:param><fmt:formatDate value="${version.changeDate}" type="both" pattern="${pageInfo.datePatternDateAndTime}" timeZone="${pageInfo.timeZoneId}" /></fmt:param>
				<fmt:param><jamwiki_t:userLinks pageInfo="${pageInfo}" userDisplay="${version.authorName}" /></fmt:param>
			</fmt:message>
			<c:if test="${!empty version.changeComment}">|&#160;(<span class="edit-comment"><jamwiki:editComment comment="${version.changeComment}" topic="${version.topicName}" /></span>)</c:if>
			<c:if test="${!empty version.changeTypeNotification}"><b>${version.changeTypeNotification}</b></c:if>
		</div>
		<div id="revision_nav">
			<c:if test="${!empty version.previousTopicVersionId}">
				(<jamwiki:link value="Special:Diff" escape="false"><jamwiki:linkParam key="topic" value="${version.topicName}" /><jamwiki:linkParam key="version1" value="${version.topicVersionId}" /><jamwiki:linkParam key="version2" value="${version.previousTopicVersionId}" /><fmt:message key="common.caption.diff" /></jamwiki:link>)
				<jamwiki:link value="Special:History" escape="false"><jamwiki:linkParam key="topic" value="${version.topicName}" /><jamwiki:linkParam key="topicVersionId" value="${version.previousTopicVersionId}" /><fmt:message key="history.revision.previous" /></jamwiki:link>&#160|
			</c:if>
			<jamwiki:link value="${version.topicName}" escape="false"><fmt:message key="history.revision.current" /></jamwiki:link>
			<c:if test="${!empty nextTopicVersionId}">
				|&#160<jamwiki:link value="Special:History" escape="false"><jamwiki:linkParam key="topic" value="${version.topicName}" /><jamwiki:linkParam key="topicVersionId" value="${nextTopicVersionId}" /><fmt:message key="history.revision.next" /></jamwiki:link>
				(<jamwiki:link value="Special:Diff" escape="false"><jamwiki:linkParam key="topic" value="${version.topicName}" /><jamwiki:linkParam key="version1" value="${nextTopicVersionId}" /><jamwiki:linkParam key="version2" value="${version.topicVersionId}" /><fmt:message key="common.caption.diff" /></jamwiki:link>)
			</c:if>
		</div>
	</div>
</c:if>

<%@ include file="view-topic-include.jsp" %>
<%@ include file="view-category-include.jsp" %>
