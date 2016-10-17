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
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to display the edit history information for a topic.
 */
public class HistoryServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(HistoryServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_HISTORY = "history.jsp";

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (!StringUtils.isBlank(request.getParameter("topicVersionId"))) {
			viewVersion(request, next, pageInfo);
		} else {
			history(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void history(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		String topicName = WikiUtil.getTopicFromRequest(request);
		if (StringUtils.isBlank(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		pageInfo.setContentJsp(JSP_HISTORY);
		pageInfo.setTopicName(topicName);
		pageInfo.setPageTitle(new WikiMessage("history.title", topicName));
		Pagination pagination = ServletUtil.loadPagination(request, next);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, true);
		List<RecentChange> changes = WikiBase.getDataHandler().getTopicHistory(topic, pagination, true);
		next.addObject("changes", changes);
		next.addObject("numChanges", changes.size());
	}

	/**
	 *
	 */
	private void viewVersion(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// display an older version
		String virtualWiki = pageInfo.getVirtualWikiName();
		String topicName = WikiUtil.getTopicFromRequest(request);
		int topicVersionId = Integer.parseInt(request.getParameter("topicVersionId"));
		TopicVersion topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId);
		if (topicVersion == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, true);
		if (topic == null) {
			throw new WikiException(new WikiMessage("history.message.notopic", topicName));
		}
		topic.setTopicContent(topicVersion.getVersionContent());
		WikiUser user = (topicVersion.getAuthorId() != null) ? WikiBase.getDataHandler().lookupWikiUser(topicVersion.getAuthorId()) : null;
		String author = ((user != null) ? user.getUsername() : topicVersion.getAuthorDisplay());
		next.addObject("version", RecentChange.initRecentChange(topic, topicVersion, author));
		if (topic.getDeleted()) {
			ServletUtil.viewTopicSource(next, pageInfo, topic);
		} else {
			Integer nextTopicVersionId = WikiBase.getDataHandler().lookupTopicVersionNextId(topicVersion.getTopicVersionId());
			next.addObject("nextTopicVersionId", nextTopicVersionId);
			WikiMessage pageTitle = new WikiMessage("topic.title", topicName);
			ServletUtil.viewTopic(request, next, pageInfo, pageTitle, topic, false, false);
		}
	}
}
