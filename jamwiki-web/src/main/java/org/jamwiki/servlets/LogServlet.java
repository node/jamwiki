/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.servlets;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.math.NumberUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.LogItem;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to build the Special:Log page for displaying log information.
 */
public class LogServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(LogServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_LOG = "log.jsp";

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		this.view(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		Pagination pagination = ServletUtil.loadPagination(request, next);
		int logType = NumberUtils.toInt(request.getParameter("logType"), -1);
		if (logType != -1) {
			next.addObject("logTypeSelected", logType);
		}
		List<LogItem> logItems = WikiBase.getDataHandler().getLogItems(virtualWiki, logType, pagination, true);
		next.addObject("logItems", logItems);
		next.addObject("logTypes", LogItem.LOG_TYPES);
		int numLogs = logItems.size();
		next.addObject("numLogs", numLogs);
		pageInfo.setPageTitle(new WikiMessage("log.title"));
		pageInfo.setContentJsp(JSP_LOG);
		pageInfo.setSpecial(true);
	}
}