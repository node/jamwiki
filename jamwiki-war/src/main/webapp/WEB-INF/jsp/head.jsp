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

<head>
	<title><jamwiki_t:wikiMessage message="${pageInfo.pageTitle}" /> - ${pageInfo.siteName}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="DC.Title" content="<jamwiki_t:wikiMessage message="${pageInfo.pageTitle}" /> - <c:out value="${pageInfo.siteName}" />" />
<c:if test="${!empty pageInfo.metaDescription}">
	<meta name="description" content="<c:out value="${pageInfo.metaDescription}" />" />
</c:if>
<c:if test="${!empty defaultTopic}">
	<link rel="start" title="<c:out value="${defaultTopic}" />" href="<jamwiki:link value="${defaultTopic}" />" />
	<link rel="home" title="<c:out value="${defaultTopic}" />" href="<jamwiki:link value="${defaultTopic}" />" />
</c:if>
<c:if test="${!empty pageInfo.canonicalUrl}">
	<link rel="canonical" href="${pageInfo.canonicalUrl}" />
</c:if>
<c:if test="${!empty pageInfo.topicEditLink}">
	<%-- see http://universaleditbutton.org/ --%>
	<link rel="alternate" type="application/x-wiki" title="<fmt:message key="common.caption.universaledit"><fmt:param><c:out value="${pageInfo.topicName}" /></fmt:param></fmt:message>" href="<jamwiki:link value="${pageInfo.topicEditLink}" />"/>
</c:if>
<jamwiki:enabled property="PROP_RSS_ALLOWED">
	<%-- This RSS link is automatically recognized by (some) browsers --%>
	<link rel="alternate" type="application/rss+xml" title="<c:out value="${pageInfo.RSSTitle}" /> (<c:out value="${virtualWiki}"/>): <fmt:message key="recentchanges.rss.feed1" />" href="<jamwiki:link value="Special:RecentChangesFeed"/>" />
	<link rel="alternate" type="application/rss+xml" title="<c:out value="${pageInfo.RSSTitle}" /> (<c:out value="${virtualWiki}"/>): <fmt:message key="recentchanges.rss.feed2" />" href="<jamwiki:link value="Special:RecentChangesFeed?minorEdits=true"/>" />
	<link rel="alternate" type="application/rss+xml" title="<c:out value="${pageInfo.RSSTitle}" /> (<c:out value="${virtualWiki}"/>): <fmt:message key="recentchanges.rss.feed3" />" href="<jamwiki:link value="Special:RecentChangesFeed?linkToVersion=true"/>" />
	<link rel="alternate" type="application/rss+xml" title="<c:out value="${pageInfo.RSSTitle}" /> (<c:out value="${virtualWiki}"/>): <fmt:message key="recentchanges.rss.feed4" />" href="<jamwiki:link value="Special:RecentChangesFeed?minorEdits=true&amp;linkToVersion=true"/>" />
</jamwiki:enabled>
	<link href="<jamwiki:link value="jamwiki.css?${cssRevision}" />" type="text/css" rel="stylesheet" />
	<script type="text/javascript" src="<c:url value="/js/jamwiki.js?${jsRevision}" />"></script>
</head>
