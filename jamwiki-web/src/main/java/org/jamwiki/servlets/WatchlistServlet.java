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
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.authentication.WikiUserDetailsImpl;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Role;
import org.jamwiki.model.Watchlist;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to handle updating and viewing a user's watchlist.
 */
public class WatchlistServlet extends JAMWikiServlet {

	/** Logger for this class and subclasses. */
	private static final WikiLogger logger = WikiLogger.getLogger(WatchlistServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_WATCHLIST = "watchlist.jsp";

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topic = WikiUtil.getTopicFromRequest(request);
		if (!StringUtils.isBlank(topic)) {
			update(request, next, pageInfo);
		} else {
			view(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void update(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
		if (userDetails.hasRole(Role.ROLE_ANONYMOUS)) {
			throw new WikiException(new WikiMessage("watchlist.error.loginrequired"));
		}
		String topicName = WikiUtil.getTopicFromRequest(request);
		String virtualWiki = pageInfo.getVirtualWikiName();
		Watchlist watchlist = ServletUtil.currentWatchlist(request, virtualWiki);
		WikiUser user = ServletUtil.currentWikiUser();
		WikiBase.getDataHandler().writeWatchlistEntry(watchlist, virtualWiki, topicName, user.getUserId());
		String article = LinkUtil.extractTopicLink(virtualWiki, topicName);
		if (watchlist.containsTopic(topicName)) {
			// added to watchlist
			pageInfo.addMessage(new WikiMessage("watchlist.caption.added", article));
		} else {
			// removed from watchlist
			pageInfo.addMessage(new WikiMessage("watchlist.caption.removed", article));
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		Pagination pagination = ServletUtil.loadPagination(request, next);
		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
		if (userDetails.hasRole(Role.ROLE_ANONYMOUS)) {
			throw new WikiException(new WikiMessage("watchlist.error.loginrequired"));
		}
		WikiUser user = ServletUtil.currentWikiUser();
		List<RecentChange> changes = WikiBase.getDataHandler().getWatchlist(virtualWiki, user.getUserId(), pagination);
		next.addObject("numChanges", changes.size());
		next.addObject("changes", changes);
		pageInfo.setPageTitle(new WikiMessage("watchlist.title"));
		pageInfo.setContentJsp(JSP_WATCHLIST);
		pageInfo.setSpecial(true);
	}
}
