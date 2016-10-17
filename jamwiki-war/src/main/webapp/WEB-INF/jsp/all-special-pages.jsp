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

<div id="special">

<p><fmt:message key="specialpages.caption.overview" /></p>

<h3><fmt:message key="specialpages.heading.allusers" /></h3>

<ul>
<li><jamwiki:link value="Special:TopicsAdmin"><fmt:message key="specialpages.caption.topicsadmin" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Categories"><fmt:message key="specialpages.caption.categories" /></jamwiki:link></li>
<li><jamwiki:link value="Special:FileList"><fmt:message key="specialpages.caption.filelist" /></jamwiki:link></li>
<li><jamwiki:link value="Special:ImageList"><fmt:message key="specialpages.caption.imagelist" /></jamwiki:link></li>
<li><jamwiki:link value="Special:AllPages"><fmt:message key="specialpages.caption.allpages" /></jamwiki:link></li>
<li><jamwiki:link value="Special:ListUsers"><fmt:message key="specialpages.caption.listusers" /></jamwiki:link></li>
<li><jamwiki:link value="Special:BlockList"><fmt:message key="specialpages.caption.blocklist" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Log"><fmt:message key="specialpages.caption.logs" /></jamwiki:link></li>
<li><jamwiki:link value="Special:OrphanedPages"><fmt:message key="specialpages.caption.orphanedpages" /></jamwiki:link></li>
<li><jamwiki:link value="Special:RecentChanges"><fmt:message key="specialpages.caption.recentchanges" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Search"><fmt:message key="specialpages.caption.search" /></jamwiki:link></li>
</ul>

<h3><fmt:message key="specialpages.heading.usertools" /></h3>

<ul>
<li><jamwiki:link value="Special:Account"><fmt:message key="specialpages.caption.account" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Login"><fmt:message key="specialpages.caption.login" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Logout"><fmt:message key="specialpages.caption.logout" /></jamwiki:link></li>
</ul>

<h3><fmt:message key="specialpages.heading.loadingtools" /></h3>

<ul>
<li><jamwiki:link value="Special:Export"><fmt:message key="specialpages.caption.export" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Import"><fmt:message key="specialpages.caption.import" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Upload"><fmt:message key="specialpages.caption.upload" /></jamwiki:link></li>
</ul>

<h3><fmt:message key="specialpages.heading.administrative" /></h3>

<ul>
<li><jamwiki:link value="Special:Admin"><fmt:message key="specialpages.caption.admin" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Maintenance"><fmt:message key="specialpages.caption.maintenance" /></jamwiki:link></li>
<li><jamwiki:link value="Special:VirtualWiki"><fmt:message key="specialpages.caption.vwiki" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Roles"><fmt:message key="specialpages.caption.roles" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Translation"><fmt:message key="specialpages.caption.translation" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Block"><fmt:message key="specialpages.caption.block" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Unblock"><fmt:message key="specialpages.caption.unblock" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Setup"><fmt:message key="specialpages.caption.setup" /></jamwiki:link></li>
<li><jamwiki:link value="Special:Upgrade"><fmt:message key="specialpages.caption.upgrade" /></jamwiki:link></li>
</ul>

</div>