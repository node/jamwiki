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
<%@ page import="org.jamwiki.model.WikiUser"
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="register">
<div class="message"><fmt:message key="register.form.info" /></div>
<form name="form1" method="post" action="<jamwiki:link value="Special:Account" />">
<input type="hidden" name="userId" value="<c:out value="${newuser.userId}" />" />
<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.messages}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<c:choose>
	<c:when test="${newuser.userId < 1}">
		<%-- new user --%>
		<fieldset>
		<legend><fmt:message key="register.caption.accountdetails" /></legend>
		<div class="row">
			<label for="registerLogin"><fmt:message key="login.username" /></label>
			<span><input type="text" name="login" value="<c:out value="${newuser.username}" />" id="registerLogin" size="50" /></span>
		</div>
		<div class="row">
			<label for="registerNewPassword"><fmt:message key="register.caption.newpassword" /></label>
			<span><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="registerNewPassword" size="50" /></span>
		</div>
		<div class="row">
			<label for="registerConfirmPassword"><fmt:message key="register.caption.confirmpassword" /></label>
			<span><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="registerConfirmPassword" size="50" /></span>
		</div>
		<div class="row">
			<label for="registerDisplayName"><fmt:message key="register.caption.displayname" /></label>
			<span><input type="text" name="displayName" value="<c:out value="${newuser.displayName}" />" id="registerDisplayName" size="50" /></span>
			<div class="formhelp"><fmt:message key="register.help.displayname" /></div>
		</div>
		<div class="row">
			<label for="registerEmail"><fmt:message key="register.caption.email" /></label>
			<span><input type="text" name="email" value="<c:out value="${newuser.email}" />" id="registerEmail" size="50" /></span>
			<div class="formhelp"><fmt:message key="register.help.email" /></div>
		</div>
		<div class="row">
			<c:set var="USER_PREFERENCE_DEFAULT_LOCALE"><%= WikiUser.USER_PREFERENCE_DEFAULT_LOCALE %></c:set>
			<label for="${USER_PREFERENCE_DEFAULT_LOCALE}"><fmt:message key="user.default.locale.label" /></label>
			<span>
				<select name="${USER_PREFERENCE_DEFAULT_LOCALE}" id="${USER_PREFERENCE_DEFAULT_LOCALE}">
				<c:forEach items="${userPreferences.availableLocales}" var="availableLocale">
					<option value="<c:out value="${availableLocale.key}" />"<c:if test="${newuser.defaultLocale == availableLocale.key}"> selected="selected"</c:if>><c:out value="${availableLocale.value}" /></option>
				</c:forEach>
				</select>
			</span>
			<div class="formhelp"><fmt:message key="user.default.locale.help" /></div>
		</div>
		<c:if test="${recaptchaEnabled}">
			<div class="row">
				<div class="captcha"><div class="captcha-label"><fmt:message key="common.caption.captcha" /></div><jamwiki:recaptcha /></div>
			</div>
		</c:if>
		</fieldset>
		<div id="antispam-container">
			<label for="jamAntispam"><fmt:message key="edit.caption.antispam" /></label>
			<input type="text" name="jamAntispam" id="jamAntispam" value="" />
		</div>
	</c:when>
	<c:otherwise>
		<%-- existing user --%>
		<fieldset>
		<legend><fmt:message key="register.caption.accountdetails" /></legend>
		<div class="row">
			<input type="hidden" name="login" value="<c:out value="${newuser.username}" />" />
			<label><fmt:message key="login.username" /></label>
			<span><c:out value="${newuser.username}" /></span>
		</div>
		<div class="row">
			<label for="registerDisplayName"><fmt:message key="register.caption.displayname" /></label>
			<span><input type="text" name="displayName" value="<c:out value="${newuser.displayName}" />" id="registerDisplayName" size="50" /></span>
			<div class="formhelp"><fmt:message key="register.help.displayname" /></div>
		</div>
		<div class="row">
			<label for="registerEmail"><fmt:message key="register.caption.email" /></label>
			<span><input type="text" name="email" value="<c:out value="${newuser.email}" />" id="registerEmail" size="50" /></span>
			<div class="formhelp"><fmt:message key="register.help.email" /></div>
		</div>
		</fieldset>
		<fieldset>
		<legend><fmt:message key="register.caption.userpreferences" /></legend>
		<c:if test="${!empty userPreferences}">
		<c:forEach var="group" items="${userPreferences.groups}">
			<fieldset>
			<legend><fmt:message key="${group.key}" /></legend>
			<c:forEach var="preference" items="${group.value}">
			<div class="row">
				<label for="${preference.key}"><fmt:message key="${preference.value.label}" /></label>
				<!-- handle content type -->
				<span>
					<c:choose>
						<c:when test="${!empty preference.value.map}">
							<select name="${preference.key}" id="${preference.key}">
								<c:forEach var="item" items="${preference.value.map}">
								<option value="<c:out value="${item.key}" />"<c:if test="${newuser.preferences[preference.key] == item.key}"> selected="selected"</c:if>><c:out value="${item.value}" /></option>
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							<input type="text" name="${preference.key}" value="<c:out value="${newuser.preferences[preference.key]}" />" id="${preference.key}" size="50" />
						</c:otherwise>
					</c:choose>
				</span>
				<div class="formhelp"><fmt:message key="${preference.value.help}" /></div>
				<c:if test="${!empty preference.value.preview}">
					<div class"row">
						<label><fmt:message key="common.current"><fmt:param><fmt:message key="${preference.value.label}" /></fmt:param></fmt:message></label>
						<span><c:out value="${preference.value.preview}" escapeXml="false" /></span>
					</div>
				</c:if>
			</div>
			</c:forEach>
			</fieldset>
		</c:forEach>
		</c:if>
		</fieldset>
		<fieldset>
		<legend><fmt:message key="register.caption.changepassword" /></legend>
		<div class="row">
			<label for="registerOldPassword"><fmt:message key="register.caption.oldpassword" /></label>
			<span><input type="password" name="oldPassword" value="<c:out value="${oldPassword}" />" id="registerOldPassword" size="50" /></span>
		</div>
		<div class="row">
			<label for="registerNewPassword"><fmt:message key="register.caption.newpassword" /></label>
			<span><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="registerNewPassword" size="50" /></span>
		</div>
		<div class="row">
			<label for="registerConfirmPassword"><fmt:message key="register.caption.confirmpassword" /></label>
			<span><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="registerConfirmPassword" size="50" /></span>
		</div>
		</fieldset>
	</c:otherwise>
</c:choose>

<div class="row">
	<span class="form-button"><input type="submit" name="function" value="<fmt:message key="common.button.save" />" /></span>
</div>
</form>

</div>
