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

<div id="roles" class="admin">

<%-- sub-menu tabs --%>
<ul class="tab-menu" id="tab_submenu">
<li><a href="#group"><fmt:message key="roles.header.group" /></a></li>
<li><a href="#user"><fmt:message key="roles.header.user" /></a></li>
<li><a href="#create"><fmt:message key="roles.header.modify" /></a></li>
<li><a href="#createGroup"><fmt:message key="group.header.modify" /></a></li>
</ul>
<div class="submenu-tab-content">

<div class="message"><fmt:message key="roles.caption.instructions" /></div>

<c:if test="${!empty pageInfo.messages}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<%-- Assign Group Roles --%>
<div id="group" class="submenu-tab-item">
<form action="<jamwiki:link value="Special:Roles" />#group" method="post">
<input type="hidden" name="function" value="assignRole" />
<fieldset>
<legend><fmt:message key="roles.header.group" /></legend>
<div class="rowhelp"><fmt:message key="roles.help.grouproles" /></div>
<div class="row">
<table class="wiki-admin">
<tr>
	<th class="first"><fmt:message key="roles.caption.groupname" /></th>
	<th colspan="3"><fmt:message key="roles.caption.roles" /></th>
</tr>
<c:forEach items="${roleMapGroups}" var="roleMap">
	<c:if test="${!empty roleMap.groupId}">
<tr>
	<td>
		<input type="hidden" name="candidateGroup" value="<c:out value="${roleMap.groupId}" />" />
		<c:out value="${roleMap.groupName}" />
	</td>
	<c:forEach items="${roles}" var="role" varStatus="status">
		<c:if test="${((3 * status.index) % roleCount) < 3}"><td></c:if>
		<jamwiki:checkbox name="groupRole" value="${roleMap.userGroup}|${role.authority}" checked="${roleMap.roleNamesMap[role.authority]}" />&#160;<c:out value="${role.authority}" /><br />
		<c:if test="${((3 * status.count) % roleCount) < 3}"></td></c:if>
	</c:forEach>
</tr>
	</c:if>
</c:forEach>
</table>
</div>
<div align="center" style="padding:10px"><input type="submit" name="Submit" value="<fmt:message key="common.button.save" />" /></div>
</fieldset>
</form>
</div>

<%-- Assign User Roles --%>
<div id="user" class="submenu-tab-item">
<fieldset>
<legend><fmt:message key="roles.header.user" /></legend>
<form action="<jamwiki:link value="Special:Roles" />#user" method="post" name="searchRoleForm">
<input type="hidden" name="function" value="searchRole" />
<div class="rowhelp"><fmt:message key="roles.help.userroles" /></div>
<div class="row">
	<label><fmt:message key="roles.caption.searchlogin" />:</label>
	<span><input type="text" name="searchLogin" value="<c:out value="${searchLogin}" />" size="30" /></span>
</div>
<div class="row">
	<label><fmt:message key="roles.caption.searchrole" />:</label>
	<span>
		<select name="searchRole" id="searchRole" onchange="document.searchRoleForm.submit()">
		<option value=""></option>
		<c:forEach items="${roles}" var="role"><option value="<c:out value="${role.authority}" />" <c:if test="${role.authority == searchRole}">selected="selected"</c:if>><c:out value="${role.authority}" /></option></c:forEach>
		</select>
		<input type="checkbox" name="includeInheritedRoles" value="includeInheritedRoles" onchange="document.searchRoleForm.submit()" <c:if test="${includeInheritedRoles}">checked="checked"</c:if> />
		<fmt:message key="roles.caption.includeInheritedRoles" />
	</span>
</div>
<div class="row">
	<label><fmt:message key="roles.caption.searchgroup" />:</label>
	<span>
		<select name="searchGroup" id="searchGroup" onchange="document.searchRoleForm.submit()">
		<option value=""></option>
		<c:forEach items="${groups}" var="group"><option value="<c:out value="${group.groupId}" />" <c:if test="${group.groupId == searchGroup}">selected="selected"</c:if>><c:out value="${group.name}" /></option></c:forEach>
		</select>
	</span>
</div>
<div class="row">
	<span class="form-button"><input type="submit" name="search" value="<fmt:message key="search.search" />" /></span>
</div>
</form>
<c:if test="${!empty roleMapUsers}">
<form action="<jamwiki:link value="Special:Roles" />#user" method="post">
<input type="hidden" name="searchLogin" value="<c:out value="${searchLogin}" />" />
<input type="hidden" name="searchRole" value="<c:out value="${searchRole}" />" />
<input type="hidden" name="searchGroup" value="<c:out value="${searchGroup}" />" />
<input type="hidden" name="includeInheritedRoles" value="<c:out value="${includeInheritedRoles}" />" />
<input type="hidden" name="function" value="assignRole" />
<div class="row">
<table class="wiki-admin">
<tr>
	<th class="first"><fmt:message key="roles.caption.userlogin" /></th>
	<th><fmt:message key="roles.caption.groups" /></th>
	<th colspan="3"><fmt:message key="roles.caption.roles" /></th>
</tr>
<c:forEach items="${roleMapUsers}" var="roleMap">
	<c:if test="${!empty roleMap.userId}">
<tr>
	<td>
		<input type="hidden" name="candidateUser" value="<c:out value="${roleMap.userId}" />" />
		<input type="hidden" name="candidateUsername" value="<c:out value="${roleMap.userLogin}" />" />
		<c:out value="${roleMap.userLogin}" />
	</td>
	<td>
	<c:if test="${!empty groups}">
	<c:forEach items="${groups}" var="group">
		<input type="checkbox" name="userGroup" value="${roleMap.userLogin}|${group.groupId}" <c:if test="${!empty groupMaps[roleMap.userLogin].groupIdMap[group.groupId]}"> checked="checked"</c:if> />&#160;<c:out value="${group.name}" /><br />
	</c:forEach>
	</c:if>
	</td>
	<c:forEach items="${roles}" var="role" varStatus="status">
		<c:if test="${((3 * status.index) % roleCount) < 3}"><td></c:if>
		<c:choose>
			<c:when test="${!empty groupMaps && !empty groupMaps[roleMap.userLogin].groupMapRoles[role.authority]}">
				<span class="inherited-role">
			</c:when>
			<c:otherwise>
				<span>
			</c:otherwise>
		</c:choose>
		<jamwiki:checkbox name="userRole" value="${roleMap.userGroup}|${role.authority}" checked="${roleMap.roleNamesMap[role.authority]}" />
		&#160;<c:out value="${role.authority}" /></span><br />
		<c:if test="${((3 * status.count) % roleCount) < 3}"></td></c:if>
	</c:forEach>
</tr>
	</c:if>
</c:forEach>
<c:if test="${!empty roleMapUsers}">
<tr>
	<td colspan="3">
		<div class="formhelp"><fmt:message key="roles.help.inherited" /></div>
	</td>
</tr>
</c:if>
</table>
</div>
<div class="row">
	<span class="form-button"><input type="submit" name="Submit" value="<fmt:message key="common.button.save" />" /></span>
</div>
</form>
</c:if>
</fieldset>
</div>

<%-- Create/Update Roles --%>
<div id="create" class="submenu-tab-item">
<form action="<jamwiki:link value="Special:Roles" />#create" name="modifyRoleForm" method="post">
<input type="hidden" name="function" value="modifyRole" />
<fieldset>
<legend><fmt:message key="roles.header.modify" /></legend>
<div class="row">
	<label for="updateRole"><fmt:message key="roles.caption.selectrole" />:</label>
	<span>
		<select name="updateRole" id="updateRole" onchange="document.modifyRoleForm.submit()">
		<option value=""></option>
		<c:forEach items="${roles}" var="role"><option value="<c:out value="${role.authority}" />" <c:if test="${role.authority == roleName}">selected="selected"</c:if>><c:out value="${role.authority}" /></option></c:forEach>
		</select>
	</span>
	<div class="formhelp"><fmt:message key="roles.help.selectrole" /></div>
</div>
<div class="row">
	<label for="roleName"><fmt:message key="roles.caption.rolename" /></label>
	<span><input type="text" name="roleName" id="roleName" value="<c:out value="${roleName}" />" size="30" <c:if test="${!empty roleName}">disabled="disabled"</c:if> /></span>
	<div class="formhelp"><fmt:message key="roles.help.rolename" /></div>
</div>
<div class="row">
	<label for="roleDescription"><fmt:message key="roles.caption.roledescription" /></label>
	<span><textarea class="medium" name="roleDescription" id="roleDescription"><c:out value="${roleDescription}" /></textarea></span>
	<div class="formhelp"><fmt:message key="roles.help.roledescription" /></div>
</div>
<div class="row">
	<span class="form-button"><input type="submit" name="Submit" value="<fmt:message key="common.button.save" />" /></span>
</div>
</fieldset>
</form>
</div>

<%-- Create/Update Groups --%>
<div id="createGroup" class="submenu-tab-item">
<form action="<jamwiki:link value="Special:Roles" />#createGroup" name="modifyGroupForm" method="post">
<input type="hidden" name="function" value="modifyGroup" />
<fieldset>
<legend><fmt:message key="group.header.modify" /></legend>
<div class="row">
	<label for="updateGroup"><fmt:message key="group.caption.selectgroup" />:</label>
	<span>
		<select name="updateGroup" id="updateGroup" onchange="document.modifyGroupForm.submit()">
		<option value=""></option>
		<c:forEach items="${groups}" var="group"><option value="<c:out value="${group.name}" />" <c:if test="${group.name == groupName}">selected="selected"</c:if>><c:out value="${group.name}" /></option></c:forEach>
		</select>
	</span>
	<div class="formhelp"><fmt:message key="group.help.selectgroup" /></div>
</div>
<div class="row">
	<label for="groupName"><fmt:message key="group.caption.groupname" /></label>
	<span><input type="text" name="groupName" id="groupName" value="<c:out value="${groupName}" />" size="30" <c:if test="${!empty groupName}">disabled="disabled"</c:if> /></span>
	<div class="formhelp"><fmt:message key="group.help.groupname" /></div>
</div>
<div class="row">
	<label for="groupDescription"><fmt:message key="group.caption.groupdescription" /></label>
	<span><textarea class="medium" name="groupDescription" id="groupDescription"><c:out value="${groupDescription}" /></textarea></span>
	<div class="formhelp"><fmt:message key="group.help.groupdescription" /></div>
</div>
<div class="row">
	<span class="form-button"><input type="submit" name="Submit" value="<fmt:message key="common.button.save" />" /></span>
</div>
</fieldset>
</form>
</div>

</div>
