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

<%--
Note: This page handles errors that occur during processing of a JSP page.  Servlet
errors should be caught by the servlet and handled more cleanly.  If this page is
called it means that a catastrophic error has occurred.
--%>

<%@ page import="
        org.apache.commons.lang3.StringEscapeUtils,
        org.jamwiki.utils.WikiLogger
    "
    isErrorPage="true"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<html>
<head>
<title><fmt:message key="error.title" /></title>
<script type="text/javascript">
function cancel() {
	history.go(-1);
}
</script>
</head>
<body>
<%
WikiLogger logger = WikiLogger.getLogger("org.jamwiki.jsp");
String errorMessage = "";
if (exception != null) {
	logger.error("Error in JSP page", exception);
	// escape XML to avoid potential XSS attacks such as /wiki/en/Special:Login?message=<script>alert("xss")</script>
	errorMessage = StringEscapeUtils.escapeXml(exception.toString());
	if (exception.getCause() != null) {
		errorMessage += " / " + StringEscapeUtils.escapeXml(exception.getCause().toString());
	}
}
%>

<p><fmt:message key="error.heading" /></p>
<c:choose>
	<c:when test="${!empty pageInfo.errors}">
		<p><font style="color: red;font-weight:bold"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></font></p>
	</c:when>
	<c:otherwise>
		<p><font style="color: red;font-weight:bold"><%= errorMessage %></font></p>
		<%
		if (exception != null) {
		%>
			<p><% exception.printStackTrace(); %></p>
		<%
		}
		%>
	</c:otherwise>
</c:choose>
<form action=""><input type="button" onClick="cancel();" value="<fmt:message key="common.button.back" />" /></form>

</body>
</html>
