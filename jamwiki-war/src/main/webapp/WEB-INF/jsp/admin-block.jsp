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
        java.util.Calendar
    "
    errorPage="/WEB-INF/jsp/error.jsp"
    contentType="text/html; charset=utf-8"
%>

<%@ include file="page-init.jsp" %>

<div id="block">

<c:if test="${!empty pageInfo.errors}">
<div class="message red"><c:forEach items="${pageInfo.errors}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>
<c:if test="${!empty pageInfo.messages}">
<div class="message green"><c:forEach items="${pageInfo.messages}" var="message"><jamwiki_t:wikiMessage message="${message}" /><br /></c:forEach></div>
</c:if>

<div class="message"><fmt:message key="block.caption.overview" /></div>

<form id="block" name="block" method="post" action="<jamwiki:link value="Special:Block" />">
<div class="row">
	<label for="user"><fmt:message key="block.caption.user" /></label>
	<span><input type="text" name="user" value="<c:out value="${user}" />" id="user" size="60" /></span>
</div>
<div class="row">
	<label for="durationNumber"><fmt:message key="block.caption.duration" /></label>
	<span>
		<input type="text" name="durationNumber" value="<c:out value="${durationNumber}" />" id="durationNumber" size="5" />
		<c:set var="calendarMinute"><%= Calendar.MINUTE %></c:set>
		<c:set var="calendarHour"><%= Calendar.HOUR %></c:set>
		<c:set var="calendarDay"><%= Calendar.DAY_OF_MONTH %></c:set>
		<c:set var="calendarWeek"><%= Calendar.WEEK_OF_YEAR %></c:set>
		<c:set var="calendarMonth"><%= Calendar.MONTH %></c:set>
		<select name="durationUnit">
			<option value="${calendarMinute}"<c:if test="${durationUnit == calendarMinute}"> selected="selected"</c:if>><fmt:message key="common.interval.minutes" /></option>
			<option value="${calendarHour}"<c:if test="${durationUnit == calendarHour}"> selected="selected"</c:if>><fmt:message key="common.interval.hours" /></option>
			<option value="${calendarDay}"<c:if test="${durationUnit == calendarDay}"> selected="selected"</c:if>><fmt:message key="common.interval.days" /></option>
			<option value="${calendarWeek}"<c:if test="${durationUnit == calendarWeek}"> selected="selected"</c:if>><fmt:message key="common.interval.weeks" /></option>
			<option value="${calendarMonth}"<c:if test="${durationUnit == calendarMonth}"> selected="selected"</c:if>><fmt:message key="common.interval.months" /></option>
			<option value="-1"<c:if test="${durationUnit == -1}"> selected="selected"</c:if>><fmt:message key="common.interval.infinite" /></option>
		</select>
	</span>
</div>
<div class="row">
	<label for="reason"><fmt:message key="block.caption.reason" /></label>
	<span><input type="text" name="reason" value="<c:out value="${reason}" />" id="user" size="60" /></span>
</div>
<c:if test="${!empty lastLoginIpAddress}">
<div class="row">
	<label for="user"><fmt:message key="block.caption.lastLoginIpAddress" /></label>
	<span>${lastLoginIpAddress}&#160;(<jamwiki:link value="Special:Contributions"><jamwiki:linkParam key="contributor" value="${lastLoginIpAddress}" /><fmt:message key="recentchanges.caption.contributions" /></jamwiki:link>&#160;|&#160;<jamwiki:link value="Special:Block"><jamwiki:linkParam key="user" value="${lastLoginIpAddress}" /><fmt:message key="recentchanges.caption.block" /></jamwiki:link>)</span>
</div>
</c:if>
<div class="row">
	<span class="form-button"><input type="submit" name="block" value="<fmt:message key="block.button.block" />" /></span>
</div>
</form>

</div>
