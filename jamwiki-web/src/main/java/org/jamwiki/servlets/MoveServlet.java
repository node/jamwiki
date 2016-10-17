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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.authentication.WikiUserDetailsImpl;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Role;
import org.jamwiki.model.Topic;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to handle moving a topic to a new name.
 */
public class MoveServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(MoveServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_MOVE = "move.jsp";

	/**
	 * Override defaults to enable user blocking.
	 */
	public MoveServlet() {
		this.blockable = true;
	}

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
		if (!userDetails.hasRole(Role.ROLE_MOVE)) {
			WikiMessage messageObject = new WikiMessage("login.message.move");
			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
		}
		if (request.getParameter("move") == null) {
			view(request, next, pageInfo);
		} else {
			move(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void move(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		if (StringUtils.isBlank(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
		pageInfo.setPageTitle(pageTitle);
		pageInfo.setTopicName(topicName);
		String moveDestination = Utilities.decodeAndEscapeTopicName(request.getParameter("moveDestination"), true);
		if (!movePage(request, next, pageInfo, topicName, moveDestination)) {
			return;
		}
		String moveCommentsPage = Utilities.decodeAndEscapeTopicName(request.getParameter("moveCommentsPage"), true);
		if (!StringUtils.isBlank(moveCommentsPage)) {
			String virtualWiki = pageInfo.getVirtualWikiName();
			String commentsDestination = LinkUtil.extractCommentsLink(virtualWiki, moveDestination);
			if (LinkUtil.isCommentsPage(virtualWiki, moveCommentsPage) && !moveCommentsPage.equals(topicName) && !commentsDestination.equals(moveDestination)) {
				if (!movePage(request, next, pageInfo, moveCommentsPage, commentsDestination)) {
					return;
				}
			}
		}
		String virtualWiki = pageInfo.getVirtualWikiName();
		ServletUtil.redirect(next, virtualWiki, moveDestination);
	}

	/**
	 *
	 */
	private boolean movePage(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String moveFrom, String moveDestination) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		Topic fromTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, moveFrom, false);
		if (fromTopic == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		if (StringUtils.isBlank(moveDestination)) {
			pageInfo.addError(new WikiMessage("move.exception.nodestination"));
			this.view(request, next, pageInfo);
			return false;
		}
		WikiLink fromWikiLink = new WikiLink(request.getContextPath(), virtualWiki, moveFrom);
		WikiLink destinationWikiLink = new WikiLink(request.getContextPath(), virtualWiki, moveDestination);
		if (fromWikiLink.getNamespace() != destinationWikiLink.getNamespace()) {
			// do not allow moving into or out of image & category namespace
			if (fromWikiLink.getNamespace().getId().equals(Namespace.CATEGORY_ID)
					|| fromWikiLink.getNamespace().getId().equals(Namespace.CATEGORY_COMMENTS_ID)
					|| destinationWikiLink.getNamespace().getId().equals(Namespace.CATEGORY_ID)
					|| destinationWikiLink.getNamespace().getId().equals(Namespace.CATEGORY_COMMENTS_ID)
				) {
				pageInfo.addError(new WikiMessage("move.exception.namespacecategory"));
				this.view(request, next, pageInfo);
				return false;
			} else if (fromWikiLink.getNamespace().getId().equals(Namespace.FILE_ID)
					|| fromWikiLink.getNamespace().getId().equals(Namespace.FILE_COMMENTS_ID)
					|| destinationWikiLink.getNamespace().getId().equals(Namespace.FILE_ID)
					|| destinationWikiLink.getNamespace().getId().equals(Namespace.FILE_COMMENTS_ID)
				) {
				pageInfo.addError(new WikiMessage("move.exception.namespaceimage"));
				this.view(request, next, pageInfo);
				return false;
			}
		}
		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
		if (!ServletUtil.isMoveable(virtualWiki, moveFrom, userDetails)) {
			this.view(request, next, pageInfo);
			pageInfo.addError(new WikiMessage("move.exception.permission", moveFrom));
			return false;
		}
		if (!WikiBase.getDataHandler().canMoveTopic(fromTopic, moveDestination)) {
			this.view(request, next, pageInfo);
			pageInfo.addError(new WikiMessage("move.exception.destinationexists", moveDestination));
			return false;
		}
		WikiBase.getDataHandler().moveTopic(fromTopic, moveDestination, ServletUtil.currentWikiUser(), ServletUtil.getIpAddress(request), request.getParameter("moveComment"));
		return true;
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		String virtualWiki = pageInfo.getVirtualWikiName();
		if (StringUtils.isBlank(topicName)) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false);
		if (topic == null) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		String commentsPage = LinkUtil.extractCommentsLink(virtualWiki, topicName);
		Topic commentsTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, commentsPage, false);
		if (commentsTopic != null) {
			// add option to also move comments page
			next.addObject("moveCommentsPage", commentsPage);
		}
		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
		pageInfo.setPageTitle(pageTitle);
		pageInfo.setContentJsp(JSP_MOVE);
		pageInfo.setTopicName(topicName);
		String moveDestination = Utilities.decodeAndEscapeTopicName(request.getParameter("moveDestination"), true);
		if (StringUtils.isBlank(moveDestination)) {
			moveDestination = topicName;
		}
		next.addObject("moveDestination", moveDestination);
		next.addObject("moveComment", request.getParameter("moveComment"));
	}
}
