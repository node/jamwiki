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

<div id="passwordReset">

<c:if test="${!empty pageInfo.messages}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<div>
	<!-- usually first case, when a user should enter his username -->
	<c:if test="${mailEnabled && empty rcode && !success && (empty function || function == 'sendMail')}">
		<form action="<jamwiki:link value="Special:PasswordReset" />" method="post">
		<fieldset>
			<legend><fmt:message key="password.reset.password" /></legend>
			<div class="row">
				<label for="loginUsername"><fmt:message key="login.username"/></label>
				<span><input type="text" name="loginUsername" /></span>
				<div class="formhelp"><fmt:message key="password.reset.password.help" /></div>
			</div>
			<div class="row">
				<span class="form-button"><input type="submit" value="<fmt:message key="password.reset.password.submit" />" /></span>
			</div>
			<input type="hidden" name="function" value="sendEmail" />
		</fieldset>
		</form>
	</c:if>
	<c:if test="${mailEnabled && !empty username && (challengeOk || !success)}">
		<form action="<jamwiki:link value="Special:PasswordReset" />" method="post">
		<fieldset>
			<legend><fmt:message key="password.reset.password" /></legend>
			<div class="row">
				<label for="registerNewPassword"><fmt:message key="register.caption.newpassword" /></label>
				<span><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="registerNewPassword" size="50" /></span>
				</div>
			<div class="row">
				<label for="registerConfirmPassword"><fmt:message key="register.caption.confirmpassword" /></label>
				<span><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="registerConfirmPassword" size="50" /></span>
			</div>
			<div class="row">
				<span class="form-button"><input type="submit" value="<fmt:message key="password.reset.password" />" /></span>
			</div>
			<input type="hidden" name="loginUsername" value="${username}" />
			<input type="hidden" name="function" value="resetPassword" />
		</fieldset>
		</form>
	</c:if>
	<c:if test="${showLoginLink}">
		<p><a href="<jamwiki:link value="Special:Login" />"><fmt:message key="common.login" /></a></p>
	</c:if>
</div>
