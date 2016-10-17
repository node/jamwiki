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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to render a list of all topics that link to another topic.
 */
public class LinkToServlet extends JAMWikiServlet {

	/** Logger for this class and subclasses. */
	private static final WikiLogger logger = WikiLogger.getLogger(LinkToServlet.class.getName());
	/** The name of the JSP file used to render the servlet output for the link to results. */
	protected static final String JSP_LINK_TO = "link-to.jsp";

	/**
	 * This method handles the request after its parent class receives control. It gets the topic's name and the
	 * virtual wiki name from the uri, loads the topic and returns a view to the end user.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		this.view(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException, WikiException {
		String virtualWiki = pageInfo.getVirtualWikiName();
		String topicName = WikiUtil.getTopicFromRequest(request);
		if (StringUtils.isBlank(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		Pagination pagination = ServletUtil.loadPagination(request, next);
		// retrieve topic names for topics that link to this one
		List<String[]> allItems = WikiBase.getDataHandler().lookupTopicLinks(virtualWiki, topicName);
		List<String[]> items = Pagination.retrievePaginatedSubset(pagination, allItems);
		Map<String, List<String>> linkToMap = this.generateLinkToMap(items);
		if (!allItems.isEmpty()) {
			pageInfo.addMessage(new WikiMessage("linkto.overview", topicName));
		} else {
			pageInfo.addMessage(new WikiMessage("linkto.none", topicName));
		}
		next.addObject("items", items);
		next.addObject("itemCount", items.size());
		next.addObject("linkToMap", linkToMap);
		String rootUrl = "Special:LinkTo?topic=" + Utilities.encodeAndEscapeTopicName(topicName);
		next.addObject("rootUrl", rootUrl);
		pageInfo.setPageTitle(new WikiMessage("linkto.title", topicName));
		pageInfo.setContentJsp(JSP_LINK_TO);
		pageInfo.setSpecial(true);
		pageInfo.setTopicName(topicName);
	}

	/**
	 * Generate a map whose elements are the parent link to topic name, and if the
	 * topic is a redirect then all elements that link to that redirect.
	 */
	private Map<String, List<String>> generateLinkToMap(List<String[]> items) {
		Map<String, List<String>> linkToMap = new LinkedHashMap<String, List<String>>();
		for (String[] itemElement : items) {
			String parentTopic = itemElement[0];
			String childTopic = itemElement[1];
			List<String> childTopicNameList = linkToMap.get(parentTopic);
			if (childTopicNameList == null) {
				childTopicNameList = new ArrayList<String>();
			}
			if (!StringUtils.isBlank(childTopic)) {
				childTopicNameList.add(childTopic);
			}
			linkToMap.put(parentTopic, childTopicNameList);
		}
		return linkToMap;
	}
}