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

<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<jamwiki:enabled property="PROP_TOPIC_USE_PREVIEW">
<c:if test="${!empty editPreview}">
<%@ include file="view-topic-include.jsp" %>
</c:if>
</jamwiki:enabled>
<%@ include file="view-category-include.jsp" %>

<jamwiki:enabled property="PROP_TOPIC_USE_SHOW_CHANGES">
<c:if test="${!empty editShowChanges}">
<%@ include file="diff-include.jsp" %>
</c:if>
</jamwiki:enabled>

<fieldset>
<legend><fmt:message key="topic.caption.editlegend" /></legend>

<form method="post" name="editForm" id="editForm" action="<jamwiki:link value="Special:Edit" />">
<input type="hidden" name="topic" value="<c:out value="${pageInfo.topicName}" />" />
<input type="hidden" name="lastTopicVersionId" value="<c:out value="${lastTopicVersionId}" />" />
<input type="hidden" name="section" value="<c:out value="${section}" />" />
<input type="hidden" name="topicVersionId" value="<c:out value="${topicVersionId}" />" />
<div id="antispam-container">
<label for="jamAntispam"><fmt:message key="edit.caption.antispam" /></label>
<input type="text" name="jamAntispam" id="jamAntispam" value="" />
</div>

<c:choose>
	<c:when test="${editor == 'toolbar'}">
		<%@ include file="editor-toolbar-include.jsp" %>
	</c:when>
</c:choose>

<p>
<textarea id="topicContents" name="contents" rows="25" cols="80" accesskey=","><c:out value="${contents}" escapeXml="true" /></textarea>
</p>
<p><label for="editComment"><fmt:message key="edit.caption.comment" /></label>: <input type="text" name="editComment" value="<c:out value="${editComment}" />" size="60" id="editComment" /></p>
<c:if test="${!empty editComment}">
<div id="summaryPreview"><fmt:message key="edit.caption.previewsummary"><fmt:param><jamwiki:editComment comment="${editComment}" topic="${pageInfo.topicName}" /></fmt:param></fmt:message></div>
</c:if>
<c:if test="${recaptchaEnabled}">
<div class="captcha"><div class="captcha-label"><fmt:message key="common.caption.captcha" /></div><jamwiki:recaptcha /></div>
</c:if>
<p>
<input type="submit" name="save" value="<fmt:message key="common.button.save" />"  accesskey="s" />

<jamwiki:enabled property="PROP_TOPIC_USE_PREVIEW"><input type="submit" name="preview" value="<fmt:message key="edit.action.preview" />" accesskey="p" /></jamwiki:enabled>
<jamwiki:enabled property="PROP_TOPIC_USE_SHOW_CHANGES"><input type="submit" name="showChanges" value="<fmt:message key="edit.action.showchanges" />" accesskey="v" /></jamwiki:enabled>

&nbsp;&nbsp;&nbsp;
<input type="checkbox" value="true" name="minorEdit"<c:if test="${minorEdit}"> checked</c:if> id="minorEdit" accesskey="i" />
<label for="minorEdit"><fmt:message key="edit.caption.minor" /></label>
<security:authorize ifNotGranted="ROLE_ANONYMOUS">
&nbsp;&nbsp;&nbsp;
<input type="checkbox" value="true" name="watchTopic"<c:if test="${watchTopic}"> checked</c:if> id="watchTopic" accesskey="w" />
<label for="watchTopic"><fmt:message key="edit.caption.watch" /></label>
</security:authorize>
</p>

<c:if test="${!empty contentsResolve}">
<%@ include file="diff-include.jsp" %>
<p>
<textarea name="contentsResolve" rows="25" cols="80"><c:out value="${contentsResolve}" escapeXml="true" /></textarea>
</p>
</c:if>

</form>

</fieldset>
