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
<%@ page
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="items">

<c:if test="${!empty items}">
<div class="message"><fmt:message key="common.caption.view" />: <jamwiki:pagination total="${itemCount}" rootUrl="${rootUrl}" /></div>
</c:if>

<c:if test="${!empty namespaces}">
<div class="message">
	<form method="get" action="<jamwiki:link value="${rootUrl}" />">
	<input type="hidden" name="num" value="<c:out value="${param.num}" />" />
	<fieldset>
	<legend><fmt:message key="common.namespace" /></legend>
	<div class="row">
		<select name="namespace">
		<c:forEach items="${namespaces}" var="namespace">
			<option value="${namespace.key}"<c:if test="${namespace.key == param.namespace}"> selected="selected"</c:if>>${namespace.value}</option>
		</c:forEach>
		</select>
		&#160;&#160;
		<input type="submit" value="<fmt:message key="common.button.go" />" />
	</div>
	</fieldset>
	</form>
</div>
</c:if>
<c:if test="${!empty pageInfo.messages}">
<div class="message"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<ul>
<c:forEach items="${linkToMap}" var="linkToMapEntry">
	<c:set var="parentTopic" value="${linkToMapEntry.key}" />
	<c:set var="childTopicList" value="${linkToMapEntry.value}" />
	<c:choose>
		<c:when test="${empty childTopicList}">
			<li><jamwiki:link value="${parentTopic}" text="${parentTopic}" /></li>
		</c:when>
		<c:otherwise>
			<li>
				<jamwiki:link value="${parentTopic}" text="${parentTopic}"><jamwiki:linkParam key="redirect" value="no" /></jamwiki:link> <fmt:message key="linkto.redirect" />
				<ul>
					<c:forEach items="${childTopicList}" var="childTopic">
						<li><jamwiki:link value="${childTopic}" text="${childTopic}" /></li>
					</c:forEach>
				</ul>
			</li>
		</c:otherwise>
	</c:choose>
</c:forEach>
</ul>

</div>
