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

<div id="edit-toolbar" class="hidden">
	<span class="toolbar-group">
		<fmt:message key="edit.button.bold" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-bold" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.italic" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-italic" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.underline" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-underline" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.strike" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-strike" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.subscript" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-subscript" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.superscript" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-superscript" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.nowiki" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-nowiki" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
	</span>
	<span class="toolbar-group">
		<fmt:message key="edit.button.ordered.list" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-orderedlist" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.unordered.list" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-unorderedlist" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.indent" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-indent" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
	</span>
	<span class="toolbar-group">
		<fmt:message key="edit.button.internal.link" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-wikilink" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.external.link" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-link" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.image" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-image" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.signature" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-signature" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.line" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-hr" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
	</span>
	<span class="toolbar-group">
		<fmt:message key="edit.button.head1" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-heading1" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.head2" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-heading2" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
		<fmt:message key="edit.button.head3" var="toolbarButtonText" />
		<span class="toolbar-button"><a id="button-heading3" title="<c:out value="${toolbarButtonText}" />" href="#"><span class="toolbar-icon">&#160;</span></a></span>
	</span>
</div>

<script type="text/javascript">
/*<![CDATA[*/
JAMWiki.UI.removeClass(document.getElementById("edit-toolbar"), "hidden");
JAMWiki.Editor.initButton("button-bold", "'''", "'''", "<fmt:message key="edit.button.bold.text"/>");
JAMWiki.Editor.initButton("button-italic", "''", "''", "<fmt:message key="edit.button.italic.text"/>");
JAMWiki.Editor.initButton("button-underline", "<u>", "</u>", "<fmt:message key="edit.button.underline.text"/>");
JAMWiki.Editor.initButton("button-strike", "<s>", "</s>", "<fmt:message key="edit.button.strike.text"/>");
JAMWiki.Editor.initButton("button-subscript", "<sub>", "</sub>", "<fmt:message key="edit.button.subscript.text"/>");
JAMWiki.Editor.initButton("button-superscript", "<sup>", "</sup>", "<fmt:message key="edit.button.superscript.text"/>");
JAMWiki.Editor.initButton("button-nowiki", "<nowiki>", "</nowiki>", "<fmt:message key="edit.button.nowiki.text"/>");

JAMWiki.Editor.initButton("button-orderedlist", "\n# ", "\n", "<fmt:message key="edit.button.ordered.list.text"/>");
JAMWiki.Editor.initButton("button-unorderedlist", "\n* ", "\n", "<fmt:message key="edit.button.unordered.list.text"/>");
JAMWiki.Editor.initButton("button-indent", "\n: ", "\n", "<fmt:message key="edit.button.indent.text"/>");

JAMWiki.Editor.initButton("button-wikilink", "[[", "]]", "<fmt:message key="edit.button.internal.link.text"/>");
JAMWiki.Editor.initButton("button-link", "[", "]", "<fmt:message key="edit.button.external.link.text"/>");
JAMWiki.Editor.initButton("button-image", "[[${pageInfo.namespaces[pageInfo.virtualWikiName]['Image']}:", "]]", "<fmt:message key="edit.button.image.text"/>");
JAMWiki.Editor.initButton("button-signature", "-- ~~~~", "", "");
JAMWiki.Editor.initButton("button-hr", "\n----\n", "", "");

JAMWiki.Editor.initButton("button-heading1", "\n= ", " =\n", "<fmt:message key="edit.button.head.text"/>");
JAMWiki.Editor.initButton("button-heading2", "\n== ", " ==\n", "<fmt:message key="edit.button.head.text"/>");
JAMWiki.Editor.initButton("button-heading3", "\n=== ", " ===\n", "<fmt:message key="edit.button.head.text"/>");
/*]]>*/ 
</script>
