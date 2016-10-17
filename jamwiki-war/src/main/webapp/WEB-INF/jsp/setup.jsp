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
<%@ page import="
        org.jamwiki.Environment,
        org.jamwiki.WikiBase
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title><jamwiki_t:wikiMessage message="${pageInfo.pageTitle}" /></title>
<style>
body {
	background: #f9f9f9;
	color: black;
	padding: 5px;
}
body, input, select {
	font: 95% sans-serif, tahoma;
}
#setup-container {
	padding: 10px;
	border: 2px solid #333;
}
#install-exists {
	color: red;
	padding: 10px 20px;
	border: 2px solid #333;
	margin: 10px 10px 20px 10px;
}
#continue {
	margin-top: 10px;
	text-align: center;
}
.message {
	margin: 20px 0;
}
.row {
	margin: 15px 0 5px;
}
.formcaption {
	width: 300px;
	float: left;
}
.formhelp {
	font-size: 85%;
	color: #5f5f5f;
	margin-bottom: 5px;
}
.red {
	font: verdana, helvetica, sans-serif;
	font-size: 110%;
	font-weight: bold;
	color: #ff0000;
	text-align: center;
}
.expander {
	display: none;
}
.expander-open {
	display: block !important;
}
</style>
	<script type="text/javascript" src="../js/jamwiki.js?setup"></script>
</head>
<body>
<div id="setup-container">
	<h3><jamwiki_t:wikiMessage message="${pageInfo.pageTitle}" /></h3>
	<c:if test="${!empty messageObject}">
		<p class="red"><jamwiki_t:wikiMessage message="${messageObject}" /></p>
	</c:if>
	<form name="setup" method="post">
	<input type="hidden" value="<c:out value="${installExists}" />" />
	<c:if test="${!empty pageInfo.errors}">
		<div class="red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
	</c:if>
	<c:if test="${!empty installExists}">
		<div id="install-exists">
			<fmt:message key="setup.error.installexists" />
			<br />
			<div id="continue"><input type="submit" name="override" value="<fmt:message key="common.button.continue" />" /></div>
		</div>
	</c:if>
	<div class="row">
		<div class="formcaption"><label for="<%= Environment.PROP_BASE_FILE_DIR %>"><fmt:message key="admin.caption.filedir" /></label>:</div>
		<div><input type="text" name="<%= Environment.PROP_BASE_FILE_DIR %>" value="<%= Environment.getValue(Environment.PROP_BASE_FILE_DIR) %>" size="50" id="<%= Environment.PROP_BASE_FILE_DIR %>" /></div>
	</div>
	<div class="formhelp"><fmt:message key="admin.help.filedir" /></div>
	<div class="row">
		<div class="formcaption"><label for="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>"><fmt:message key="admin.persistence.caption" /></label>:</div>
		<div class="formelement">
			<select name="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>" id="<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>">
			<c:set var="persistenceType"><%= Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE) %></c:set>
			<c:set var="persistenceTypeInternal"><%= WikiBase.PERSISTENCE_INTERNAL %></c:set>
			<c:set var="persistenceTypeExternal"><%= WikiBase.PERSISTENCE_EXTERNAL %></c:set>
			<option value="<%= WikiBase.PERSISTENCE_INTERNAL %>"<c:if test="${persistenceType == persistenceTypeInternal}"> selected</c:if>><fmt:message key="admin.persistencetype.internal"/></option>
			<option value="<%= WikiBase.PERSISTENCE_EXTERNAL %>"<c:if test="${persistenceType == persistenceTypeExternal}"> selected</c:if>><fmt:message key="admin.persistencetype.database"/></option>
			</select>
		</div>
	</div>
	<div class="formhelp"><fmt:message key="admin.persistence.help" /></div>
	<div id="db-details" class="expander expander-open">
		<div class="row">
			<div class="formcaption"><label for="<%= Environment.PROP_DB_TYPE %>"><fmt:message key="admin.persistence.caption.type" /></label>:</div>
			<div class="formelement">
				<select name="<%= Environment.PROP_DB_TYPE %>" id="<%= Environment.PROP_DB_TYPE %>">
				<option value=""></option>
				<c:set var="selectedQueryHandler"><%= Environment.getValue(Environment.PROP_DB_TYPE) %></c:set>
				<c:forEach items="${queryHandlers}" var="queryHandler">
				<option value="<c:out value="${queryHandler.clazz}" />"<c:if test="${selectedQueryHandler == queryHandler.clazz}"> selected</c:if>><c:if test="${!empty queryHandler.key}"><fmt:message key="${queryHandler.key}" /></c:if><c:if test="${empty queryHandler.key}"><c:out value="${queryHandler.name}" /></c:if><c:if test="${queryHandler.experimental}"> (<fmt:message key="common.caption.experimental" />)</c:if></option>
				</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="formcaption"><label for="<%= Environment.PROP_DB_DRIVER %>"><fmt:message key="admin.persistence.caption.driver" /></label>:</div>
			<div class="formelement"><input type="text" name="<%= Environment.PROP_DB_DRIVER %>" id="<%= Environment.PROP_DB_DRIVER %>" value="<%= Environment.getValue(Environment.PROP_DB_DRIVER) %>" size="50"></div>
		</div>
		<div class="row">
			<div class="formcaption"><label for="<%= Environment.PROP_DB_URL %>"><fmt:message key="admin.persistence.caption.url" /></label>:</div>
			<div class="formelement"><input type="text" name="<%= Environment.PROP_DB_URL %>" id="<%= Environment.PROP_DB_URL %>" value="<%= Environment.getValue(Environment.PROP_DB_URL) %>" size="50"></div>
		</div>
		<div class="row">
			<div class="formcaption"><label for="<%= Environment.PROP_DB_USERNAME %>"><fmt:message key="admin.persistence.caption.user" /></label>:</div>
			<div class="formelement"><input type="text" name="<%= Environment.PROP_DB_USERNAME %>" id="<%= Environment.PROP_DB_USERNAME %>" value="<%= Environment.getValue(Environment.PROP_DB_USERNAME) %>" size="15"></div>
		</div>
		<div class="row">
			<div class="formcaption"><label for="<%= Environment.PROP_DB_PASSWORD %>"><fmt:message key="admin.persistence.caption.pass" /></label>:</div>
			<div class="formelement"><input type="password" name="<%= Environment.PROP_DB_PASSWORD %>" id="<%= Environment.PROP_DB_PASSWORD %>" value="<c:out value="${dbPassword}" />" size="15"></div>
		</div>
	</div>
	<div class="row">
		<div class="formcaption"><label for="<%= Environment.PROP_FILE_UPLOAD_STORAGE %>"><fmt:message key="admin.upload.caption.storage" /></label></div>
		<div class="formelement">
			<select name="<%= Environment.PROP_FILE_UPLOAD_STORAGE %>" id="<%= Environment.PROP_FILE_UPLOAD_STORAGE %>">
			<c:set var="PROP_FILE_UPLOAD_STORAGE"><%= Environment.PROP_FILE_UPLOAD_STORAGE %></c:set>
			<c:set var="selectedStorageType"><%= Environment.getValue(Environment.PROP_FILE_UPLOAD_STORAGE) %></c:set>
			<option value="JAMWIKI"<c:if test="${props[PROP_FILE_UPLOAD_STORAGE] == 'JAMWIKI'}"> selected="selected"</c:if>><fmt:message key="admin.upload.storage.default" /> (<fmt:message key="common.caption.default" />)</option>
			<option value="DOCROOT"<c:if test="${props[PROP_FILE_UPLOAD_STORAGE] == 'DOCROOT'}"> selected="selected"</c:if>><fmt:message key="admin.upload.storage.docroot" /></option>
			<option value="DATABASE"<c:if test="${props[PROP_FILE_UPLOAD_STORAGE] == 'DATABASE'}"> selected="selected"</c:if>><fmt:message key="admin.upload.storage.database" /> (<fmt:message key="common.caption.experimental" />)</option>
			</select>
		</div>
	</div>
	<div class="formhelp"><fmt:message key="admin.upload.help.storage" /> <fmt:message key="admin.upload.help.storage.note" /></div>
	<div id="upload-details" class="expander expander-open">
		<div class="row">
			<div class="formcaption"><label for="<%= Environment.PROP_FILE_DIR_FULL_PATH %>"><fmt:message key="admin.upload.caption.uploaddir" /></label>:</div>
			<div class="formelement"><input type="text" name="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH) %>" size="50" id="<%= Environment.PROP_FILE_DIR_FULL_PATH %>" /></div>
		</div>
		<div class="formhelp"><fmt:message key="admin.upload.help.uploaddir" /></div>
		<div class="row">
			<div class="formcaption"><label for="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>"><fmt:message key="admin.upload.caption.uploaddirrel" /></label>:</div>
			<div class="formelement"><input type="text" name="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" value="<%= Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) %>" size="50" id="<%= Environment.PROP_FILE_DIR_RELATIVE_PATH %>" /></div>
		</div>
		<div class="formhelp"><fmt:message key="admin.upload.help.uploaddirrel" /></div>
	</div>
	<div class="row">
		<div class="formcaption"><label for="setupLogin"><fmt:message key="setup.caption.adminlogin"/></label>:</div>
		<div class="formelement"><input type="text" name="username" value="<c:out value="${username}" />" id="setupLogin" /></div>
	</div>
	<div class="row">
		<div class="formcaption"><label for="setupNewPassword"><fmt:message key="register.caption.newpassword" /></label>:</div>
		<div class="formelement"><input type="password" name="newPassword" value="<c:out value="${newPassword}" />" id="setupNewPassword" /></div>
	</div>
	<div class="row">
		<div class="formcaption"><label for="setupConfirmPassword"><fmt:message key="register.caption.confirmpassword" /></label>:</div>
		<div class="formelement"><input type="password" name="confirmPassword" value="<c:out value="${confirmPassword}" />" id="setupConfirmPassword" /></div>
	</div>
	<div class="message"><jamwiki_t:wikiMessage message="${logMessage}" /></div>
	<div align="center"><input type="submit" name="function" value="<fmt:message key="admin.action.save" />" /></div>
	</form>
	<c:if test="${!empty messages}">
		<div class="message">
		<c:forEach items="${messages}" var="message">
			<div><c:out value="${message}" /></div>
		</c:forEach>
		</div>
	</c:if>
	<%@ include file="shared-db-javascript.jsp" %>
	<script type="text/javascript">
	JAMWiki.Admin.toggleDisableOnSelect(document.getElementById("<%= Environment.PROP_BASE_PERSISTENCE_TYPE %>"), "<%= WikiBase.PERSISTENCE_EXTERNAL %>", DATABASE_ELEMENT_IDS, document.getElementById("db-details"), "expander-open");
	JAMWiki.Admin.sampleDatabaseValues(document.getElementById("<%= Environment.PROP_DB_TYPE %>"), "<%= Environment.PROP_DB_DRIVER %>", "<%= Environment.PROP_DB_URL %>", DATABASE_SAMPLE_VALUES);
	JAMWiki.Admin.toggleDisableOnSelect(document.getElementById("<%= Environment.PROP_FILE_UPLOAD_STORAGE %>"), "<%= WikiBase.UPLOAD_STORAGE.DOCROOT %>", UPLOAD_ELEMENT_IDS, document.getElementById("upload-details"), "expander-open");
	</script>
</div>
</body>
</html>