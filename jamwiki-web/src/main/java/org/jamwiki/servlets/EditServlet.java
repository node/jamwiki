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
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Role;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicType;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.Watchlist;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.jamwiki.validator.ReCaptchaUtil;
import org.jamwiki.web.model.WikiDiff;
import org.jamwiki.web.utils.DiffUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to process topic edits including saving an edit, preview, resolving
 * conflicts and dealing with spam.
 */
public class EditServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(EditServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_EDIT = "edit.jsp";

	/**
	 * Override defaults to enable user blocking.
	 */
	public EditServlet() {
		this.blockable = true;
	}

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// verify that the user is allowed to edit - since there are different permissions
		// for editing new vs existing topics the Spring Security permissions are not
		// sufficient for handling all checks.
		ModelAndView loginRequiredModelAndView = loginRequired(request, pageInfo);
		if (loginRequiredModelAndView != null) {
			return loginRequiredModelAndView;
		}
		if (isSave(request)) {
			save(request, next, pageInfo);
		} else {
			edit(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void edit(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		String virtualWiki = pageInfo.getVirtualWikiName();
		Topic topic = loadTopic(virtualWiki, topicName);
		// topic name might be updated by loadTopic
		topicName = topic.getName();
		Integer lastTopicVersionId = retrieveLastTopicVersionId(request, topic);
		next.addObject("lastTopicVersionId", lastTopicVersionId);
		String contents = (String)request.getParameter("contents");
		if (isPreview(request)) {
			pageInfo.addError(new WikiMessage("edit.warning.preview"));
			preview(request, next, pageInfo);
		} else if (isShowChanges(request)) {
			showChanges(request, next, pageInfo, virtualWiki, topicName, lastTopicVersionId);
		} else if (!StringUtils.isBlank(request.getParameter("topicVersionId"))) {
			// editing an older version
			Integer topicVersionId = Integer.valueOf(request.getParameter("topicVersionId"));
			TopicVersion topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId);
			if (topicVersion == null) {
				throw new WikiException(new WikiMessage("common.exception.notopic"));
			}
			contents = topicVersion.getVersionContent();
			if (!lastTopicVersionId.equals(topicVersionId)) {
				next.addObject("topicVersionId", topicVersionId);
				pageInfo.addError(new WikiMessage("edit.warning.oldversion"));
			}
		} else if (!StringUtils.isBlank(request.getParameter("section"))) {
			// editing a section of a topic
			int section = Integer.valueOf(request.getParameter("section"));
			String[] sliceResults = ParserUtil.parseSlice(request.getContextPath(), request.getLocale(), virtualWiki, topicName, section);
			contents = sliceResults[1];
			String sectionName = sliceResults[0];
			String editComment = "/* " + sectionName + " */ ";
			next.addObject("editComment", editComment);
		} else {
			// editing a full new or existing topic
			contents = (topic == null) ? "" : topic.getTopicContent();
		}
		this.loadEdit(request, next, pageInfo, contents, virtualWiki, topicName, true);
	}

	/**
	 *
	 */
	private boolean isPreview(HttpServletRequest request) {
		return !StringUtils.isBlank(request.getParameter("preview"));
	}

	/**
	 *
	 */
	private boolean isSave(HttpServletRequest request) {
		return !StringUtils.isBlank(request.getParameter("save"));
	}

	/**
	 *
	 */
	private boolean isShowChanges(HttpServletRequest request) {
		return !StringUtils.isBlank(request.getParameter("showChanges"));
	}

	/**
	 *
	 */
	private void loadDiff(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String contents1, String contents2) throws Exception {
		List<WikiDiff> diffs = DiffUtil.diff(contents1, contents2);
		next.addObject("diffs", diffs);
	}

	/**
	 *
	 */
	private void loadEdit(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String contents, String virtualWiki, String topicName, boolean useSection) throws Exception {
		WikiUser user = ServletUtil.currentWikiUser();
		ParserInput parserInput = this.parserInput(request, user, virtualWiki, topicName);
		ParserOutput parserOutput = ParserUtil.parseMetadata(parserInput, contents);
		pageInfo.setPageTitle(new WikiMessage("edit.title", ((parserOutput.getPageTitle() != null) ? parserOutput.getPageTitle() : topicName)));
		pageInfo.setTopicName(topicName);
		WikiLink wikiLink = new WikiLink(request.getContextPath(), virtualWiki, topicName);
		if (wikiLink.getNamespace().getId().equals(Namespace.CATEGORY_ID)) {
			ServletUtil.loadCategoryContent(request, next, virtualWiki, topicName);
		}
		if (request.getParameter("editComment") != null) {
			next.addObject("editComment", request.getParameter("editComment"));
		}
		if (useSection && request.getParameter("section") != null) {
			next.addObject("section", request.getParameter("section"));
		}
		next.addObject("minorEdit", (request.getParameter("minorEdit") != null));
		Watchlist watchlist = ServletUtil.currentWatchlist(request, virtualWiki);
		if (request.getParameter("watchTopic") != null || (watchlist.containsTopic(topicName) && !isPreview(request))) {
			next.addObject("watchTopic", true);
		}
		pageInfo.setContentJsp(JSP_EDIT);
		String editor = user.getPreference("user.preferred.editor");
		next.addObject("editor", editor);
		next.addObject("contents", contents);
		next.addObject("recaptchaEnabled", ReCaptchaUtil.isEditEnabled(user));
	}

	/**
	 * Initialize topic values for the topic being edited.  If a topic with
	 * the specified name already exists then it will be initialized,
	 * otherwise a new topic is created.
	 */
	private Topic loadTopic(String virtualWiki, String topicName) throws Exception {
		Topic topic = ServletUtil.initializeTopic(virtualWiki, topicName);
		if (topic.getReadOnly()) {
			throw new WikiException(new WikiMessage("error.readonly"));
		}
		return topic;
	}

	/**
	 *
	 */
	private ModelAndView loginRequired(HttpServletRequest request, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		String virtualWiki = pageInfo.getVirtualWikiName();
		WikiUserDetailsImpl user = ServletUtil.currentUserDetails();
		if (ServletUtil.isEditable(virtualWiki, topicName, user)) {
			return null;
		}
		if (!user.hasRole(Role.ROLE_EDIT_EXISTING)) {
			WikiMessage messageObject = new WikiMessage("login.message.edit");
			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
		}
		if (!user.hasRole(Role.ROLE_EDIT_NEW) && WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false) == null) {
			WikiMessage messageObject = new WikiMessage("login.message.editnew");
			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
		}
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false);
		if (topic == null) {
			// this should never trigger, but better safe than sorry...
			return null;
		}
		if (topic.getAdminOnly()) {
			WikiMessage messageObject = new WikiMessage("login.message.editadmin", topicName);
			return ServletUtil.viewLogin(request, pageInfo, WikiUtil.getTopicFromURI(request), messageObject);
		}
		if (topic.getReadOnly()) {
			throw new WikiException(new WikiMessage("error.readonly"));
		}
		// it should be impossible to get here...
		throw new WikiException(new WikiMessage("error.unknown", "Unable to determine topic editing permissions"));
	}

	/**
	 *
	 */
	private ParserInput parserInput(HttpServletRequest request, WikiUser user, String virtualWiki, String topicName) {
		ParserInput parserInput = new ParserInput(virtualWiki, topicName);
		parserInput.setContext(request.getContextPath());
		parserInput.setLocale(request.getLocale());
		parserInput.setWikiUser(user);
		parserInput.setUserDisplay(ServletUtil.getIpAddress(request));
		return parserInput;
	}

	/**
	 * Functionality to handle the "Preview" button being clicked.
	 */
	private void preview(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		String virtualWiki = pageInfo.getVirtualWikiName();
		String contents = (String)request.getParameter("contents");
		WikiLink wikiLink = new WikiLink(null, virtualWiki, topicName);
		Topic previewTopic = new Topic(virtualWiki, wikiLink.getNamespace(), wikiLink.getArticle());
		previewTopic.setTopicContent(contents);
		next.addObject("editPreview", "true");
		ServletUtil.viewTopic(request, next, pageInfo, null, previewTopic, false, false);
	}

	/**
	 *
	 */
	private void resolve(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		String virtualWiki = pageInfo.getVirtualWikiName();
		Topic lastTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false);
		String contents1 = lastTopic.getTopicContent();
		String contents2 = request.getParameter("contents");
		next.addObject("lastTopicVersionId", lastTopic.getCurrentVersionId());
		next.addObject("contentsResolve", contents2);
		pageInfo.addError(new WikiMessage("edit.exception.conflict"));
		this.loadDiff(request, next, pageInfo, contents1, contents2);
		this.loadEdit(request, next, pageInfo, contents1, virtualWiki, topicName, false);
	}

	/**
	 *
	 */
	private Integer retrieveLastTopicVersionId(HttpServletRequest request, Topic topic) throws Exception {
		return (!StringUtils.isBlank(request.getParameter("lastTopicVersionId"))) ? Integer.valueOf(request.getParameter("lastTopicVersionId")) : topic.getCurrentVersionId();
	}

	/**
	 * Functionality to handle the "Save" button being clicked.
	 */
	private void save(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String topicName = WikiUtil.getTopicFromRequest(request);
		String virtualWiki = pageInfo.getVirtualWikiName();
		Topic topic = loadTopic(virtualWiki, topicName);
		Topic lastTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false);
		if (lastTopic != null && !lastTopic.getCurrentVersionId().equals(retrieveLastTopicVersionId(request, topic))) {
			// someone else has edited the topic more recently
			resolve(request, next, pageInfo);
			return;
		}
		String contents = request.getParameter("contents");
		String sectionName = "";
		if (!StringUtils.isBlank(request.getParameter("section"))) {
			// load section of topic
			int section = Integer.valueOf(request.getParameter("section"));
			ParserOutput parserOutput = new ParserOutput();
			String[] spliceResult = ParserUtil.parseSplice(parserOutput, request.getContextPath(), request.getLocale(), virtualWiki, topicName, section, contents);
			contents = spliceResult[1];
			sectionName = parserOutput.getSectionName();
		}
		if (contents == null) {
			logger.warn("The topic " + topicName + " has no content");
			throw new WikiException(new WikiMessage("edit.exception.nocontent", topicName));
		}
		// strip line feeds
		contents = StringUtils.remove(contents, '\r');
		String lastTopicContent = (lastTopic != null) ? StringUtils.remove(lastTopic.getTopicContent(), '\r') : "";
		if (lastTopic != null && StringUtils.equals(lastTopicContent, contents)) {
			// topic hasn't changed. redirect to prevent user from refreshing and re-submitting
			ServletUtil.redirect(next, virtualWiki, topic.getName());
			return;
		}
		String editComment = request.getParameter("editComment");
		if (handleSpam(request, pageInfo, topicName, contents, editComment)) {
			this.loadEdit(request, next, pageInfo, contents, virtualWiki, topicName, false);
			return;
		}
		WikiUser user = ServletUtil.currentWikiUser();
		if (!ReCaptchaUtil.isValidForEdit(request, user)) {
			pageInfo.addError(new WikiMessage("common.exception.recaptcha"));
			this.loadEdit(request, next, pageInfo, contents, virtualWiki, topicName, false);
			return;
		}
		ParserInput parserInput = this.parserInput(request, user, virtualWiki, topicName);
		ParserOutput parserOutput = ParserUtil.parseMetadata(parserInput, contents);
		// parse signatures and other values that need to be updated prior to saving
		contents = ParserUtil.parseMinimal(parserInput, contents);
		topic.setTopicContent(contents);
		if (!StringUtils.isBlank(parserOutput.getRedirect())) {
			// set up a redirect
			topic.setRedirectTo(parserOutput.getRedirect());
			topic.setTopicType(TopicType.REDIRECT);
		} else if (topic.getTopicType() == TopicType.REDIRECT) {
			// no longer a redirect
			topic.setRedirectTo(null);
			topic.setTopicType(TopicType.ARTICLE);
		}
		int charactersChanged = StringUtils.length(contents) - StringUtils.length(lastTopicContent);
		TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), editComment, contents, charactersChanged);
		if (request.getParameter("minorEdit") != null) {
			topicVersion.setEditType(TopicVersion.EDIT_MINOR);
		}
		WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
		// update watchlist
		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
		if (!userDetails.hasRole(Role.ROLE_ANONYMOUS)) {
			Watchlist watchlist = ServletUtil.currentWatchlist(request, virtualWiki);
			boolean watchTopic = (request.getParameter("watchTopic") != null);
			if (watchlist.containsTopic(topicName) != watchTopic) {
				WikiBase.getDataHandler().writeWatchlistEntry(watchlist, virtualWiki, topicName, user.getUserId());
			}
		}
		// redirect to prevent user from refreshing and re-submitting
		String target = topic.getName();
		if (!StringUtils.isBlank(sectionName)) {
			target += "#" + sectionName;
		}
		ServletUtil.redirect(next, virtualWiki, target);
	}

	/**
	 * Functionality to handle the "Show Changes" button being clicked.
	 */
	private void showChanges(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String virtualWiki, String topicName, Integer lastTopicVersionId) throws Exception {
		String contents1 = request.getParameter("contents");
		String contents2 = "";
		if (!StringUtils.isBlank(request.getParameter("section"))) {
			// editing a section of a topic
			int section = Integer.valueOf(request.getParameter("section"));
			String[] sliceResults = ParserUtil.parseSlice(request.getContextPath(), request.getLocale(), virtualWiki, topicName, section);
			contents2 = sliceResults[1];
		} else if (lastTopicVersionId != null) {
			// get the full topic version
			TopicVersion lastTopicVersion = WikiBase.getDataHandler().lookupTopicVersion(lastTopicVersionId);
			contents2 = lastTopicVersion.getVersionContent();
		}
		this.loadDiff(request, next, pageInfo, contents1, contents2);
		next.addObject("editShowChanges", "true");
	}
}
