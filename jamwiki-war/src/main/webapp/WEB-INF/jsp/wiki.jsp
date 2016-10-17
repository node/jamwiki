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
    trimDirectiveWhitespaces="true"
%>

<%@ include file="page-init.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<%@ include file="head.jsp" %>

<body>

<div id="wiki-page">
<c:if test="${!upgradeInProgress}">
<div id="wiki-navigation">
	<div id="logo">
	<%-- FIXME - need image width and height --%>
	<a class="logo" href="<jamwiki:link value="${defaultTopic}" />"><img border="0" src="<c:url value="/images/${logo}" />" alt="" /></a>
	</div>
	<br />
	<c:if test="${!empty leftMenu && leftMenu != '<br/><br/>'}">
	<div id="nav-menu" class="portlet">
	<c:out value="${leftMenu}" escapeXml="false" />
	</div>
	</c:if>
	<div id="nav-search" class="portlet">
	<form method="get" action="<jamwiki:link value="Special:Search" />">
	<input type="text" name="text" value="" />
	<br />
	<input type="submit" name="search" value='<fmt:message key="generalmenu.search"/>'/>
	<input type="submit" name="jumpto" value='<fmt:message key="generalmenu.jumpto"/>'/>
	</form>
	</div>
	<c:if test="${!empty pageInfo.virtualWikiLinks}">
	<div id="p-lang" class="portlet">
	<h5><fmt:message key="generalmenu.title.virtualwiki" /></h5>
	<ul>
	<c:forEach items="${pageInfo.virtualWikiLinks}" var="virtualWikiLink">
	<li>${virtualWikiLink}</li>
	</c:forEach>
	</ul>
	</div>
	</c:if>
	<c:if test="${!empty pageInfo.interwikiLinks}">
	<div id="p-sites" class="portlet">
	<h5><fmt:message key="generalmenu.title.interwiki" /></h5>
	<ul>
	<c:forEach items="${pageInfo.interwikiLinks}" var="interwikiLink">
	<li>${interwikiLink}</li>
	</c:forEach>
	</ul>
	</div>
	</c:if>
</div>
</c:if>
<div id="wiki-content">
	<%@ include file="user-menu.jsp"%>
	<%@ include file="top-menu.jsp"%>
	<div id="contents" >
	<div id="siteNotice"><c:out value="${pageHeader}" escapeXml="false" /></div>
	<h1 id="contents-header"><jamwiki_t:wikiMessage message="${pageInfo.pageTitle}" /></h1>
	<c:if test="${!empty pageInfo.redirectUrl}">
	<div id="contentSub"><fmt:message key="topic.redirect.from"><fmt:param><a href="<c:out value="${pageInfo.redirectUrl}" />"><c:out value="${pageInfo.redirectName}" /></a></fmt:param></fmt:message></div>
	</c:if>
	<jsp:include page="${pageInfo.contentJsp}" flush="true" />
	<br />
	</div>
</div>
<div id="wiki-footer">
	<div id="footer-custom"><c:out value="${footer}" escapeXml="false" /></div>
	<div id="footer-logo"><a href="http://jamwiki.org/">JAMWiki</a> <fmt:message key="footer.message.version" /> <jamwiki:wiki-version/></div>
</div>
</div>

</body>
</html>
