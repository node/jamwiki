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
<%@ page import="org.jamwiki.Environment"
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="virtualwiki" class="admin">

<%-- sub-menu tabs --%>
<ul class="tab-menu" id="tab_submenu">
<li><a href="#vwiki"><fmt:message key="admin.vwiki.title.virtualwiki" /></a></li>
<li><a href="#addnamespace"><fmt:message key="admin.vwiki.title.namespace.add" /></a></li>
<li><a href="#namespaces"><fmt:message key="admin.vwiki.title.namespace.translations" /></a></li>
<li><a href="#interwiki"><fmt:message key="admin.vwiki.title.interwiki" /></a></li>
</ul>
<div class="submenu-tab-content">

<%-- Update Default Virtual Wiki --%>
<div id="vwiki" class="submenu-tab-item">
<c:if test="${!empty pageInfo.messages}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<%-- Common Virtual Wiki Settings --%>
<form action="<jamwiki:link value="Special:VirtualWiki" />#commonvwiki" method="post">
<fieldset>
<legend><fmt:message key="admin.vwiki.title.common" /></legend>
<input type="hidden" name="function" value="commonvwiki" />
<c:if test="${!empty selected && selected.virtualWikiId != -1}">
	<input type="hidden" name="selected" value="${selected.virtualWikiId}" />
</c:if>
<div class="row">
	<label for="defaultVirtualWiki"><fmt:message key="admin.vwiki.caption.makedefault" /></label>
	<span>
		<select name="defaultVirtualWiki">
		<c:forEach items="${wikis}" var="wiki"><option value="${wiki.name}" <c:if test="${wiki.name == defaultVirtualWiki.name}">selected="selected"</c:if>>${wiki.name}</option></c:forEach>
		</select>
	</span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.makedefault" /></div>
</div>
<div class="row">
	<label for="<%= Environment.PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS %>"><fmt:message key="admin.parser.caption.specialvirtualwiki" /></label>
	<c:set var="PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS"><%= Environment.PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS %></c:set>
	<span><jamwiki:checkbox name="${PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS}" value="true" checked="${props[PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS]}" id="${PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS}" /></span>
	<div class="formhelp"><fmt:message key="admin.parser.help.specialvirtualwiki" /></div>
</div>
<div class="row">
	<label for="<%= Environment.PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE %>"><fmt:message key="admin.parser.caption.virtualwikiinline" /></label>
	<c:set var="PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE"><%= Environment.PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE %></c:set>
	<span><jamwiki:checkbox name="${PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE}" value="true" checked="${props[PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE]}" id="${PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE}" /></span>
	<div class="formhelp"><fmt:message key="admin.parser.help.virtualwikiinline" /></div>
</div>
<div class="row">
	<span class="form-button"><input type="submit" value="<fmt:message key="common.update" />" /></span>
</div>
</fieldset>
</form>
<%-- Add/Update Virtual Wiki --%>
<fieldset>
<legend><fmt:message key="admin.vwiki.title.virtualwiki" /></legend>
<form action="<jamwiki:link value="Special:VirtualWiki" />#vwiki" method="get" name="selectvwiki">
<input type="hidden" name="function" value="search" />
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.select" /></label>
	<span>
		<select name="selected" onchange="document.selectvwiki.submit()">
		<option value=""></option>
		<c:forEach items="${wikis}" var="wiki"><option value="${wiki.name}" <c:if test="${!empty selected && wiki.name == selected.name}">selected="selected"</c:if>>${wiki.name}</option></c:forEach>
		</select>
	</span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.search" /></div>
</div>
</form>
<form action="<jamwiki:link value="Special:VirtualWiki" />#vwiki" method="post">
<input type="hidden" name="function" value="virtualwiki" />
<c:if test="${!empty selected && selected.virtualWikiId != -1}">
	<input type="hidden" name="virtualWikiId" value="${selected.virtualWikiId}" />
	<input type="hidden" name="name" value="${selected.name}" />
</c:if>
<c:if test="${empty selected || selected.virtualWikiId == -1}">
<div class="row">
	<label for="name"><fmt:message key="common.name" /></label>
	<span><input type="text" name="name" id="name" size="30" value="<c:if test="${!empty selected}">${selected.name}</c:if>" /></span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.name" /></div>
</div>
</c:if>
<div class="row">
	<label for="rootTopicName"><fmt:message key="admin.caption.defaulttopic" /></label>
	<span>
		<c:set var="rootTopicName" value="${(!empty selected) ? selected.rootTopicName : defaultVirtualWiki.rootTopicName}" />
		<input type="text" name="rootTopicName" id="rootTopicName" value="${rootTopicName}" size="50" />
		&#160;&#160;&#160;<input type="checkbox" name="defaultRootTopicName" id="defaultRootTopicName" value="${defaultVirtualWiki.rootTopicName}"<c:if test="${empty selected || selected.defaultRootTopicName}"> checked="checked"</c:if> />&#160;<fmt:message key="admin.vwiki.caption.usedefault" />
	</span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.defaulttopic" /></div>
</div>
<div class="row">
	<label for="virtualWikiSiteName"><fmt:message key="admin.caption.sitename" /></label>
	<span>
		<c:set var="virtualWikiSiteName" value="${(!empty selected) ? selected.siteName : defaultVirtualWiki.siteName}" />
		<input type="text" name="virtualWikiSiteName" size="50" id="virtualWikiSiteName" value="${virtualWikiSiteName}" />
		&#160;&#160;&#160;<input type="checkbox" name="defaultVirtualWikiSiteName" id="defaultVirtualWikiSiteName" value="${defaultVirtualWiki.siteName}"<c:if test="${empty selected || selected.defaultSiteName}"> checked="checked"</c:if> />&#160;<fmt:message key="admin.vwiki.caption.usedefault" />
	</span>
	<div class="formhelp"><fmt:message key="admin.help.sitename" /></div>
</div>
<div class="row">
	<label for="virtualWikiLogoImageUrl"><fmt:message key="admin.caption.logoimage" /></label>
	<span>
		<c:set var="virtualWikiLogoImageUrl" value="${(!empty selected) ? selected.logoImageUrl : defaultVirtualWiki.logoImageUrl}" />
		<input type="text" name="virtualWikiLogoImageUrl" size="50" id="virtualWikiLogoImageUrl" value="${virtualWikiLogoImageUrl}" />
		&#160;&#160;&#160;<input type="checkbox" name="defaultVirtualWikiLogoImageUrl" id="defaultVirtualWikiLogoImageUrl" value="${defaultVirtualWiki.logoImageUrl}"<c:if test="${empty selected || selected.defaultLogoImageUrl}"> checked="checked"</c:if> />&#160;<fmt:message key="admin.vwiki.caption.usedefault" />
	</span>
	<div class="formhelp"><fmt:message key="admin.help.logoimage" /></div>
</div>
<div class="row">
	<label for="virtualWikiMetaDescription"><fmt:message key="admin.caption.metadescription" /></label>
	<span>
		<c:set var="virtualWikiMetaDescription" value="${(!empty selected) ? selected.metaDescription : defaultVirtualWiki.metaDescription}" />
		<textarea class="medium" name="virtualWikiMetaDescription" id="virtualWikiMetaDescription"><c:out value="${virtualWikiMetaDescription}" /></textarea>
		&#160;&#160;&#160;<input type="checkbox" name="defaultVirtualWikiMetaDescription" id="defaultVirtualWikiMetaDescription" value="${defaultVirtualWiki.metaDescription}"<c:if test="${empty selected || selected.defaultMetaDescription}"> checked="checked"</c:if> />&#160;<fmt:message key="admin.vwiki.caption.usedefault" />
	</span>
	<div class="formhelp"><fmt:message key="admin.help.metadescription" /></div>
</div>

<c:set var="buttonLabel"><fmt:message key="common.button.add" /></c:set>
<c:if test="${!empty selected}">
	<c:set var="buttonLabel"><fmt:message key="common.update" /></c:set>
</c:if>
<div class="row">
	<span class="form-button"><input type="submit" value="${buttonLabel}" /></span>
</div>
</form>
</fieldset>
</div>

<%-- Add Namesapce --%>
<div id="addnamespace" class="submenu-tab-item">
<c:if test="${!empty pageInfo.messages && function == 'addnamespace'}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.errors && function == 'addnamespace'}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<form action="<jamwiki:link value="Special:VirtualWiki" />#addnamespace" method="post">
<fieldset>
<legend><fmt:message key="admin.vwiki.title.namespace.add" /></legend>
<input type="hidden" name="function" value="addnamespace" />
<c:if test="${!empty selected}">
	<input type="hidden" name="selected" value="${selected.name}" />
</c:if>
<div class="rowhelp">
	<fmt:message key="admin.vwiki.help.namespace.add" />
</div>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.namespace.main" /></label>
	<span>
		<input type="text" name="mainNamespace" size="30" value="${mainNamespace}" />
	</span>
</div>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.namespace.comments" /></label>
	<span>
		<input type="text" name="commentsNamespace" size="30" value="${commentsNamespace}" />
	</span>
</div>
<div class="row">
	<span class="form-button"><input type="submit" value="<fmt:message key="common.button.add" />" /></span>
</div>
</fieldset>
</form>
</div>

<%-- Add/Update Namespace Translations --%>
<div id="namespaces" class="submenu-tab-item">
<c:if test="${!empty pageInfo.messages && function == 'namespaces'}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.errors && function == 'namespaces'}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<%-- Select Virtual Wiki --%>
<form action="<jamwiki:link value="Special:VirtualWiki" />#namespaces" method="get" name="selectnamespaces">
<input type="hidden" name="function" value="search" />
<fieldset>
<legend><fmt:message key="admin.vwiki.title.select" /></legend>
<div class="row">
	<label for="name"><fmt:message key="common.name" /></label>
	<span>
		<select name="selected" onchange="document.selectnamespaces.submit()">
		<option value=""></option>
		<c:forEach items="${wikis}" var="wiki"><option value="${wiki.name}" <c:if test="${!empty selected && wiki.name == selected.name}">selected="selected"</c:if>>${wiki.name}</option></c:forEach>
		</select>
	</span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.namespace.search" /></div>
</div>
</fieldset>
</form>
<c:if test="${!empty selected}">
	<form action="<jamwiki:link value="Special:VirtualWiki" />#namespaces" method="post">
	<input type="hidden" name="function" value="namespaces" />
	<input type="hidden" name="selected" value="${selected.name}" />
	<fieldset>
	<legend><fmt:message key="admin.vwiki.title.namespace.translations" /></legend>
	<div class="rowhelp">
		<p><fmt:message key="admin.vwiki.help.namespace.translations" /></p>
		<p><fmt:message key="admin.vwiki.help.namespace.translations.warning" /></p>
	</div>
	<c:forEach items="${namespaces}" var="namespace">
		<%-- suppress display of namespaces that cannot be translated --%>
		<div class="row">
			<label>${namespace.defaultLabel} [${namespace.id}]</label>
			<%-- do not allow translations of the Special: and JAMWiki: namespaces --%>
			<c:choose>
				<c:when test="${namespace.id >= 0 && namespace.id != 8}">
					<input type="hidden" name="namespace_id" value="${namespace.id}" />
					<input type="hidden" name="${namespace.id}_label" value="${namespace.defaultLabel}" />
					<span><input type="text" name="${namespace.id}_vwiki" size="30" value="${namespace.namespaceTranslations[selected.name]}" /></span>
				</c:when>
				<c:otherwise>
					<span><fmt:message key="admin.vwiki.caption.namespace.notallowed" /></span>
				</c:otherwise>
			</c:choose>
		</div>
	</c:forEach>
	<div class="row">
		<span class="form-button"><input type="submit" value="<fmt:message key="common.update" />" /></span>
	</div>
	</fieldset>
	</form>
</c:if>
</div>

<%-- Interwiki Links --%>
<div id="interwiki" class="submenu-tab-item">
<c:if test="${!empty pageInfo.messages && (function == 'addInterwiki' || function == 'updateInterwiki')}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.errors && (function == 'addInterwiki' || function == 'updateInterwiki')}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<%-- Common Interwiki Settings --%>
<form action="<jamwiki:link value="Special:VirtualWiki" />#commoniwiki" method="post">
<fieldset>
<legend><fmt:message key="admin.vwiki.title.interwiki.common" /></legend>
<input type="hidden" name="function" value="commoniwiki" />
<div class="row">
	<label for="<%= Environment.PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE %>"><fmt:message key="admin.parser.caption.interwikiinline" /></label>
	<c:set var="PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE"><%= Environment.PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE %></c:set>
	<span><jamwiki:checkbox name="${PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE}" value="true" checked="${props[PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE]}" id="${PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE}" /></span>
	<div class="formhelp"><fmt:message key="admin.parser.help.interwikiinline" /></div>
</div>
<div class="row">
	<span class="form-button"><input type="submit" value="<fmt:message key="common.update" />" /></span>
</div>
</fieldset>
</form>
<%-- Add Interwiki --%>
<form action="<jamwiki:link value="Special:VirtualWiki" />#interwiki" method="post">
<input type="hidden" name="function" value="addInterwiki" />
<input type="hidden" name="selected" value="${selected.name}" />
<fieldset>
<legend><fmt:message key="admin.vwiki.title.interwiki.add" /></legend>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.interwiki.prefix" /></label>
	<span><input type="text" name="interwikiPrefix" size="30" maxlength="30" value="${interwikiPrefix}" /></span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.interwiki.prefix" /></div>
</div>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.interwiki.pattern" /></label>
	<span><input type="text" name="interwikiPattern" size="30" maxlength="200" value="${interwikiPattern}" /></span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.interwiki.pattern" /></div>
</div>
<div class="row">
	<label for="name"><fmt:message key="admin.vwiki.caption.interwiki.display" /></label>
	<span><input type="text" name="interwikiDisplay" size="30" maxlength="30" value="${interwikiDisplay}" /></span>
	<div class="formhelp"><fmt:message key="admin.vwiki.help.interwiki.display" /></div>
</div>
<div class="row">
	<span class="form-button"><input type="submit" value="<fmt:message key="common.button.add" />" /></span>
</div>
</fieldset>
</form>
<c:if test="${!empty interwikis}">
<%-- Update Interwiki --%>
<form action="<jamwiki:link value="Special:VirtualWiki" />#interwiki" method="post">
<input type="hidden" name="function" value="updateInterwiki" />
<input type="hidden" name="selected" value="${selected.name}" />
<fieldset>
<legend><fmt:message key="admin.vwiki.title.interwiki.update" /></legend>
<c:if test="${!empty selected}">
	<input type="hidden" name="selected" value="${selected.name}" />
</c:if>
<c:if test="${!empty interwikis}">
<div class="row">
<table class="wiki-admin">
<tr>
	<th class="first"><fmt:message key="admin.vwiki.caption.interwiki.prefix" /></th>
	<th><fmt:message key="admin.vwiki.caption.interwiki.pattern" /></th>
	<th><fmt:message key="admin.vwiki.caption.interwiki.display" /></th>
	<th><fmt:message key="common.delete" /></th>
</tr>
<c:forEach items="${interwikis}" var="interwiki" varStatus="status">
<tr>
	<td>
		<input type="hidden" name="interwikiPrefix" value="${interwiki.interwikiPrefix}" />
		<label for="name">${interwiki.interwikiPrefix}</label>
	</td>
	<td class="center">
		<input type="text" name="pattern-${interwiki.interwikiPrefix}" size="50" maxlength="200" value="${interwiki.interwikiPattern}" />
	</td>
	<td class="center">
		<input type="text" name="display-${interwiki.interwikiPrefix}" size="30" maxlength="30" value="${interwiki.interwikiDisplay}" />
	</td>
	<td class="center">
		<input type="checkbox" name="delete-${interwiki.interwikiPrefix}" value="true" />
	</td>
</tr>
</c:forEach>
</table>
</div>
</c:if>
<div class="row">
	<span class="form-button"><input type="submit" value="<fmt:message key="common.update" />" /></span>
</div>
</fieldset>
</form>
</c:if>
</div>

</div>

</div>
