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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.UserBlock;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;

/**
 * Display a list of currently blocked usernames and IP addresses.
 */
public class BlockListServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(BlockListServlet.class.getName());
	/** The name of the JSP file used to render the servlet output when searching. */
	protected static final String JSP_BLOCK_LIST = "block-list.jsp";

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
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException {
		Pagination pagination = ServletUtil.loadPagination(request, next);
		Map<Object, UserBlock> userBlocks = WikiBase.getDataHandler().getUserBlocks();
		List<UserBlock> allItems = new ArrayList<UserBlock>();
		for (UserBlock userBlock : userBlocks.values()) {
			if (!userBlock.isExpired()) {
				allItems.add(userBlock);
			}
		}
		List<UserBlock> items = Pagination.retrievePaginatedSubset(pagination, allItems);
		next.addObject("itemCount", items.size());
		next.addObject("items", items);
		pageInfo.setPageTitle(new WikiMessage("blocklist.title"));
		pageInfo.setContentJsp(JSP_BLOCK_LIST);
		pageInfo.setSpecial(true);
	}
}
