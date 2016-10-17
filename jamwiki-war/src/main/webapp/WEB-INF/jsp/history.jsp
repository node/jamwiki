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

<div class="message"><fmt:message key="common.caption.view" />: <jamwiki:pagination total="${numChanges}" rootUrl="Special:History?topic=${pageInfo.topicNameUrlEncoded}" /></div>

<form action="<jamwiki:link value="Special:History" />" method="get" name="historyForm">
<input type="hidden" name="topic" value='<c:out value="${pageInfo.topicName}"/>'/>

</form>

<form action="<jamwiki:link value="Special:Diff" />" method="get" name="diffForm">
<input type="hidden" name="topic" value='<c:out value="${pageInfo.topicName}" />' />

<input type="submit" value='<fmt:message key="history.diff" />' />

<br /><br />

<ul>
<c:set var="nextTopicVersionId" value="" />
<c:forEach items="${changes}" var="change" varStatus="status">
<li<c:if test="${change.delete}"> class="deletechange"</c:if><c:if test="${change.importChange}"> class="importchange"</c:if><c:if test="${change.minor}"> class="minorchange"</c:if><c:if test="${change.undelete}"> class="undeletechange"</c:if><c:if test="${change.move}"> class="movechange"</c:if><c:if test="${change.normal}"> class="standardchange"</c:if>>
	<c:if test="${!empty nextTopicVersionId}">(<jamwiki:link value="Special:Diff"><jamwiki:linkParam key="topic" value="${change.topicName}" /><jamwiki:linkParam key="version2" value="${change.topicVersionId}" /><jamwiki:linkParam key="version1" value="${nextTopicVersionId}" /><fmt:message key="history.caption.diffnext" /></jamwiki:link>)</c:if>
	<c:if test="${empty nextTopicVersionId}">(<fmt:message key="history.caption.diffnext" />)</c:if>
	<c:if test="${!empty change.previousTopicVersionId}">(<jamwiki:link value="Special:Diff"><jamwiki:linkParam key="topic" value="${change.topicName}" /><jamwiki:linkParam key="version2" value="${change.previousTopicVersionId}" /><jamwiki:linkParam key="version1" value="${change.topicVersionId}" /><fmt:message key="history.caption.diffprevious" /></jamwiki:link>)</c:if>
	<c:if test="${empty change.previousTopicVersionId}">(<fmt:message key="history.caption.diffprevious" />)</c:if>
	<c:set var="nextTopicVersionId" value="${change.topicVersionId}" />
	<c:if test="${numChanges > 1}">
	&#160;
	<input type="radio" name="version2" id="ver2_<c:out value="${change.topicVersionId}" />" onclick="JAMWiki.UI.historyRadio(this, 'version1', true)" value="<c:out value="${change.topicVersionId}" />" <c:if test="${status.index == 1}">checked="checked"</c:if> <c:if test="${status.first}">style="visibility:hidden"</c:if> />
	&#160;
	<input type="radio" name="version1" id="ver1_<c:out value="${change.topicVersionId}" />" onclick="JAMWiki.UI.historyRadio(this, 'version2', false)" value="<c:out value="${change.topicVersionId}" />" <c:if test="${status.first}">checked="checked"</c:if> <c:if test="${status.last}">style="visibility:hidden"</c:if> />
	</c:if>
	&#160;
	<jamwiki:link value="Special:History"><jamwiki:linkParam key="topicVersionId" value="${change.topicVersionId}" /><jamwiki:linkParam key="topic" value="${pageInfo.topicName}" /><fmt:formatDate value="${change.changeDate}" type="both" pattern="${pageInfo.datePatternDateAndTime}" timeZone="${pageInfo.timeZoneId}" /></jamwiki:link>
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
	<c:if test="${!empty change.changeComment}"><label for="<c:out value="diff:${change.topicVersionId}" />">&#160;(<span class="edit-comment"><jamwiki:editComment comment="${change.changeComment}" topic="${change.topicName}" /></span>)</label></c:if>
</li>
</c:forEach>
</ul>

<c:if test="${numChanges > 1}">
<script type="text/javascript">
JAMWiki.UI.historyRadio(document.getElementById('ver2_<c:out value="${changes[1].topicVersionId}" />'), 'version1', true)
</script>
</c:if>

<br />

<input type="submit" value='<fmt:message key="history.diff"/>'/>
</form>

</div>
