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

<div id="change">

<div class="message"><fmt:message key="common.caption.view" />: <jamwiki:pagination total="${numChanges}" rootUrl="Special:Watchlist" /></div>

<c:if test="${!empty pageInfo.messages}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<c:choose>
	<c:when test="${empty changes}">
		<div class="message"><fmt:message key="watchlist.caption.empty" /></div>
	</c:when>
	<c:otherwise>
		<form name="num-changes" method="get" action="<jamwiki:link value="Special:Watchlist" />">

		<c:set var="previousDate"><fmt:formatDate value="${changes[0].changeDate}" type="both" pattern="${pageInfo.datePatternDateOnly}" timeZone="${pageInfo.timeZoneId}" /></c:set>
		<h4><c:out value="${previousDate}" /></h4>
		<ul>
		<c:forEach items="${changes}" var="change">
		<c:set var="currentDate"><fmt:formatDate value="${change.changeDate}" type="both" pattern="${pageInfo.datePatternDateOnly}" timeZone="${pageInfo.timeZoneId}" /></c:set>
		<c:if test="${currentDate != previousDate}">
		</ul>
		<h4><c:out value="${currentDate}" /></h4>
		<ul>
		</c:if>
		<li<c:if test="${change.delete}"> class="deletechange"</c:if><c:if test="${change.importChange}"> class="importchange"</c:if><c:if test="${change.minor}"> class="minorchange"</c:if><c:if test="${change.undelete}"> class="undeletechange"</c:if><c:if test="${change.move}"> class="movechange"</c:if><c:if test="${change.normal}"> class="standardchange"</c:if>>
			(<jamwiki:link value="Special:Diff"><jamwiki:linkParam key="topic" value="${change.topicName}" /><jamwiki:linkParam key="version2"><c:out value="${change.previousTopicVersionId}" /></jamwiki:linkParam><jamwiki:linkParam key="version1" value="${change.topicVersionId}" /><fmt:message key="common.caption.diff" /></jamwiki:link>)
			&#160;
			(<jamwiki:link value="Special:History"><jamwiki:linkParam key="topic" value="${change.topicName}" /><fmt:message key="common.caption.history" /></jamwiki:link>)
			&#160;
			<fmt:formatDate value="${change.changeDate}" type="both" pattern="${pageInfo.datePatternTimeOnly}" timeZone="${pageInfo.timeZoneId}" />
			&#160;
			<c:if test="${!change.delete}"><jamwiki:link value="${change.topicName}" text="${change.topicName}" /></c:if>
			<c:if test="${change.delete}"><c:out value="${change.topicName}" /></c:if>
			&#160;.&#160;.&#160;
			<%-- the "+" symbol could be added using a pattern attribute, but there does not seem to be a way to avoid having "+0" show up when that approach is used. --%>
			(<c:if test="${change.charactersChanged > 0}">+</c:if><fmt:formatNumber value="${change.charactersChanged}" />)
			&#160;.&#160;.&#160;
			<jamwiki_t:userLinks pageInfo="${pageInfo}" userDisplay="${change.authorName}" />
			<c:if test="${!empty change.changeTypeNotification}">&#160;<b><c:out value="${change.changeTypeNotification}" /></b></c:if>
			<c:if test="${!empty change.changeWikiMessage}">
				&#160;
				<jamwiki_t:wikiMessage message="${change.changeWikiMessage}" />
			</c:if>
			<c:if test="${!empty change.changeComment}">&#160;(<span class="edit-comment"><jamwiki:editComment comment="${change.changeComment}" topic="${change.topicName}" /></span>)</c:if>
		</li>
		<c:set var="previousDate" value="${currentDate}" />
		</c:forEach>
		</ul>
		</form>
	</c:otherwise>
</c:choose>

</div>