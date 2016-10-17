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

<c:choose>
	<c:when test="${empty diffs}"><div class="message"><fmt:message key="diff.nochange" /></div></c:when>
	<c:otherwise>
		<table id="diff">
			<col class="diff-marker">
			<col class="diff-content">
			<col class="diff-marker">
			<col class="diff-content">
			<c:set var="previousPosition" value="-10" />
			<c:forEach items="${diffs}" var="diff">
				<c:if test="${diff.position > (previousPosition + 1)}">
					<tr><td class="diff-line" colspan="4"><fmt:message key="diff.line" /> <c:out value="${diff.position + 1}" />:</td></tr>
				</c:if>
				<tr class="diff-entry">
					<c:choose>
						<c:when test="${!empty diff.oldText && diff.change}">
							<td class="diff-indicator">-</td>
							<td class="diff-delete">
								<c:set var="subDiffChange" value="false" />
								<c:forEach items="${diff.subDiffs}" var="subDiff"><c:if test="${!subDiffChange && subDiff.change}"><c:set var="subDiffChange" value="true" /><span class="diff-change"></c:if><c:if test="${subDiffChange && !subDiff.change}"><c:set var="subDiffChange" value="false" /></span></c:if><c:out value="${subDiff.oldText}" /></c:forEach>
								<c:if test="${subDiffChange}"></span></c:if>
								&#160;
							</td>
						</c:when>
						<c:otherwise>
							<td class="diff-indicator">&#160;</td>
							<td class="diff-unchanged"><c:out value="${diff.oldText}" />&#160;</td>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${!empty diff.newText && diff.change}">
							<td class="diff-indicator">+</td>
							<td class="diff-add">
								<c:set var="subDiffChange" value="false" />
								<c:forEach items="${diff.subDiffs}" var="subDiff"><c:if test="${!subDiffChange && subDiff.change}"><c:set var="subDiffChange" value="true" /><span class="diff-change"></c:if><c:if test="${subDiffChange && !subDiff.change}"><c:set var="subDiffChange" value="false" /></span></c:if><c:out value="${subDiff.newText}" /></c:forEach>
								<c:if test="${subDiffChange}"></span></c:if>
								&#160;
							</td>
						</c:when>
						<c:otherwise>
							<td class="diff-indicator">&#160;</td>
							<td class="diff-unchanged"><c:out value="${diff.newText}" />&#160;</td>
						</c:otherwise>
					</c:choose>
				</tr>
				<c:set var="previousPosition" value="${diff.position}" />
			</c:forEach>
		</table>
	</c:otherwise>
</c:choose>
