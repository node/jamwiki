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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://jamwiki.org/taglib" prefix="jamwiki" %>
<%@ taglib prefix="jamwiki_t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="security" %>

<%
// no-cache headers
response.setHeader("Cache-Control", "private, s-maxage=0, max-age=0, must-revalidate");
response.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
response.setHeader("Pragma", "no-cache");
%>
<fmt:setBundle basename="ApplicationResources" />
<%
response.setContentType("text/html");
response.setCharacterEncoding("UTF-8");
%>
