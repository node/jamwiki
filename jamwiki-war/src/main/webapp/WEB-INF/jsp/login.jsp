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

<div id="login">

<jamwiki:authmsg css="message red" />

<fieldset>
<legend><fmt:message key="${pageInfo.pageTitle.key}" /></legend>

<form method="post" action="<c:url value="${springSecurityLoginUrl}" />">
<input type="hidden" name="<c:out value="${springSecurityTargetUrlField}" />" value="<c:out value="${springSecurityTargetUrl}"/>" />
<div class="row">
	<label for="loginUsername"><fmt:message key="login.username"/></label>
	<span><input type="text" name="<c:out value="${springSecurityUsernameField}" />" value="<c:out value="${param.username}" />" id="loginUsername" /></span>
</div>
<div class="row">
	<label for="loginPassword"><fmt:message key="login.password"/></label>
	<span><input type="password" name="<c:out value="${springSecurityPasswordField}" />" id="loginPassword" /></span>
</div>
<div class="row">
	<span class="form-button"><input type="checkbox" value="true" name="<c:out value="${springSecurityRememberMeField}" />" id="loginRemember" />&#160;<label for="loginRemember" class="nonblock"><fmt:message key="login.rememberme" /></label></span>
</div>
<div class="row">
	<span class="form-button"><input type="submit" name="function" value="<fmt:message key="login.submit" />" /></span>
</div>
</form>

<c:if test="${mailEnabled}">
	<p><a href="<jamwiki:link value="Special:PasswordReset" />"><fmt:message key="password.reset.password.ask.user" /></a></p>
</c:if>

</fieldset>

</div>
