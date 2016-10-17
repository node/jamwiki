<%@ tag body-content="empty"
    description="Render a WikiMessage object"
    trimDirectiveWhitespaces="true"
%>

<%@ taglib prefix="jamwiki" uri="http://jamwiki.org/taglib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="ApplicationResources" />

<%@ attribute name="message" required="true" rtexprvalue="true" type="org.jamwiki.WikiMessage" description="The WikiMessage object to render" %>

<%-- resin fails if the fmt:message tag has anything but fmt:param in it, hence the messy logic below --%>
<fmt:message key="${message.key}">
	<fmt:param>
		<c:if test="${message.paramsLength >= 1}">
			<c:choose>
				<c:when test="${message.params[0].wikiLink}"><jamwiki:link value="${message.params[0].param}" text="${message.params[0].paramText}" /></c:when>
				<c:otherwise>${message.params[0]}</c:otherwise>
			</c:choose>
		</c:if>
	</fmt:param>
	<fmt:param>
		<c:if test="${message.paramsLength >= 2}">
			<c:choose>
				<c:when test="${message.params[1].wikiLink}"><jamwiki:link value="${message.params[1].param}" text="${message.params[1].paramText}" /></c:when>
				<c:otherwise>${message.params[1]}</c:otherwise>
			</c:choose>
		</c:if>
	</fmt:param>
	<fmt:param>
		<c:if test="${message.paramsLength >= 3}">
			<c:choose>
				<c:when test="${message.params[2].wikiLink}"><jamwiki:link value="${message.params[2].param}" text="${message.params[2].paramText}" /></c:when>
				<c:otherwise>${message.params[2]}</c:otherwise>
			</c:choose>
		</c:if>
	</fmt:param>
	<fmt:param>
		<c:if test="${message.paramsLength >= 4}">
			<c:choose>
				<c:when test="${message.params[3].wikiLink}"><jamwiki:link value="${message.params[3].param}" text="${message.params[3].paramText}" /></c:when>
				<c:otherwise>${message.params[3]}</c:otherwise>
			</c:choose>
		</c:if>
	</fmt:param>
</fmt:message>
