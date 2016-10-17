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
package org.jamwiki.parser;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Interwiki;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicType;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.parser.image.ImageUtil;
import org.jamwiki.utils.PseudoTopicHandler;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * General utility methods for handling both wiki topic links and HTML links.
 * Wiki topic links are generally of the form "Topic?query=param#Section".
 * HTML links are of the form http://example.com/.
 */
public abstract class LinkUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(LinkUtil.class.getName());

	private static final Pattern INVALID_TOPIC_NAME_PATTERN = Pattern.compile(Environment.getValue(Environment.PROP_PATTERN_INVALID_TOPIC_PATTERN));
	// pattern for links of the form "http://example.com" or "mailto:email.com".  "(?:X)" means non-capturing group.
	private static final String LINK_PROTOCOL_REGEX = "(http(?:s)?|file|ftp|mailto|news):(?://)?(.*)";
	private static final Pattern LINK_PROTOCOL_PATTERN = Pattern.compile(LINK_PROTOCOL_REGEX, Pattern.CASE_INSENSITIVE);
	/** Path to the template used to format external links that open in the same browser window. */
	private static final String TEMPLATE_LINK_EXTERNAL = "templates/link-external.template";
	/** Path to the template used to format external links that open in a new browser window. */
	private static final String TEMPLATE_LINK_EXTERNAL_NEW_WINDOW = "templates/link-external-new-window.template";
	/** Path to the template used to format interwiki links. */
	private static final String TEMPLATE_LINK_INTERWIKI = "templates/link-interwiki.template";

	/**
	 * Build a query parameter.  If root is empty, this method returns
	 * "?param=value".  If root is not empty this method returns root +
	 * "&amp;param=value".  Note that param and value will be  URL encoded,
	 * and if "query" does not start with a "?" then one will be pre-pended.
	 *
	 * @param query The existing query parameter, if one is available.  If the
	 *  query parameter does not start with "?" then one will be pre-pended.
	 * @param param The name of the query parameter being appended.  This
	 *  value will be URL encoded.
	 * @param value The value of the query parameter being appended.  This
	 *  value will be URL encoded.
	 * @return The full query string generated using the input parameters.
	 */
	public static String appendQueryParam(String query, String param, String value) {
		String url = "?";
		if (!StringUtils.isBlank(query)) {
			if (query.charAt(0) != '?') {
				query = "?" + query;
			}
			url = query + "&amp;";
		}
		if (StringUtils.isBlank(param)) {
			return query;
		}
		url += Utilities.encodeAndEscapeTopicName(param) + "=";
		if (!StringUtils.isBlank(value)) {
			url += Utilities.encodeAndEscapeTopicName(value);
		}
		return url;
	}

	/**
	 * Convert plain text into a value suitable for an anchor name.  The HTML rules
	 * for such a value is that it must begin with a letter ([A-Za-z]) and may be
	 * followed by any number of letters, digits ([0-9]), hyphens ("-"), underscores
	 * ("_"), colons (":"), and periods (".").
	 */
	public static String buildAnchorText(String text) {
		if (StringUtils.isBlank(text)) {
			logger.warn("LinkUtil.buildAnchorText called with empty string as argument");
			return text;
		}
		// ensure that all characters in the name are valid for use in an anchor name
		String anchorText = Utilities.encodeAndEscapeTopicName(StringUtils.trim(text));
		anchorText = anchorText.replace('%', '.');
		if (!anchorText.substring(0, 1).matches("[A-Za-z]")) {
			// per the spec anchors must start with an ANSI letter
			anchorText = "a_" + anchorText;
		}
		return anchorText;
	}

	/**
	 * Utility method for building a URL link to a wiki edit page for a
	 * specified topic.
	 *
	 * @param context The servlet context for the link that is being created.
	 * @param virtualWiki The virtual wiki for the link that is being created.
	 * @param topic The name of the topic for which an edit link is being
	 *  created.
	 * @param query Any existing query parameters to append to the edit link.
	 *  This value may be either <code>null</code> or empty.
	 * @param section The section defined by the name parameter within the
	 *  HTML page for the topic being edited.  If provided then the edit link
	 *  will allow editing of only the specified section.
	 * @return A url that links to the edit page for the specified topic.
	 *  Note that this method returns only the URL, not a fully-formed HTML
	 *  anchor tag.
	 * @throws DataAccessException Thrown if any error occurs while builing the link URL.
	 */
	public static String buildEditLinkUrl(String context, String virtualWiki, String topic, String query, int section) throws DataAccessException {
		if (Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_CAPITALIZATION)) {
			topic = StringUtils.capitalize(topic);
		}
		query = LinkUtil.appendQueryParam(query, "topic", topic);
		if (section > 0) {
			query += "&amp;section=" + section;
		}
		// FIXME - hard coding
		WikiLink wikiLink = new WikiLink(context, virtualWiki, "Special:Edit");
		wikiLink.setQuery(query);
		return wikiLink.toRelativeUrl();
	}

	/**
	 * Parse a link of the form http://example.com and return the opening tag of the
	 * form <a href="http://example.com">.
	 */
	public static String buildExternalLinkHtml(String link, String cssClass, String linkText) throws ParserException {
		Matcher matcher = LINK_PROTOCOL_PATTERN.matcher(link);
		if (!matcher.matches()) {
			throw new ParserException("Invalid link " + link);
		}
		String protocol = matcher.group(1).toLowerCase();
		link = matcher.group(2);
		// make sure link values are properly escaped.
		link = StringUtils.replace(link, "<", "%3C");
		link = StringUtils.replace(link, ">", "%3E");
		link = StringUtils.replace(link, "\"", "%22");
		link = StringUtils.replace(link, "\'", "%27");
		String template = (Environment.getBooleanValue(Environment.PROP_EXTERNAL_LINK_NEW_WINDOW)) ? TEMPLATE_LINK_EXTERNAL_NEW_WINDOW : TEMPLATE_LINK_EXTERNAL;
		String dotSlashSlash = (protocol.equals("mailto")) ? ":" : "://";
		Object[] args = new Object[3];
		args[0] = (cssClass == null) ? "externallink" : cssClass;
		args[1] = protocol + dotSlashSlash + link;
		args[2] = linkText;
		try {
			return WikiUtil.formatFromTemplate(template, args);
		} catch (IOException e) {
			throw new ParserException(e);
		}
	}

	/**
	 * Build the HTML anchor link to a topic page for a given WikLink object.
	 *
	 * @param wikiLink The WikiLink object for which an HTML link is being
	 *  generated.
	 * @param text The text to display as the link content.
	 * @param style The CSS class to use with the anchor HTML tag.  This value
	 *  can be <code>null</code> or empty if no custom style is used.
	 * @param target The anchor link target, or <code>null</code> or empty if
	 *  no target is needed.
	 * @param escapeHtml Set to <code>true</code> if the link caption should
	 *  be HTML escaped.  This value should be <code>true</code> in any case
	 *  where the caption is not guaranteed to be free from potentially
	 *  malicious HTML code.
	 * @return An HTML anchor link that matches the given input parameters.
	 * @throws DataAccessException Thrown if any error occurs while retrieving
	 *  topic information.
	 */
	public static String buildInternalLinkHtml(WikiLink wikiLink, String text, String style, String target, boolean escapeHtml) throws DataAccessException {
		String url = LinkUtil.buildTopicUrl(wikiLink);
		String topic = wikiLink.getDestination();
		if (StringUtils.isBlank(text)) {
			text = topic;
			if (!StringUtils.isBlank(wikiLink.getSection())) {
				text += "#" + wikiLink.getSection();
			}
		}
		if (!wikiLink.getNamespace().getId().equals(Namespace.MEDIA_ID) && !StringUtils.isBlank(topic) && StringUtils.isBlank(style)) {
			String virtualWiki = ((wikiLink.getAltVirtualWiki() != null) ? wikiLink.getAltVirtualWiki().getName() : wikiLink.getVirtualWiki());
			if (WikiBase.getDataHandler().lookupInterwiki(virtualWiki) != null) {
				style = "interwiki";
			} else if (LinkUtil.isExistingArticle(virtualWiki, topic) == null && !wikiLink.isSpecial()) {
				style = "edit";
			}
		}
		String styleHtml = (!StringUtils.isBlank(style)) ? " class=\"" + style + "\"" : "";
		String targetHtml = (!StringUtils.isBlank(target)) ? " target=\"" + target + "\"" : "";
		if (StringUtils.isBlank(topic) && !StringUtils.isBlank(wikiLink.getSection())) {
			topic = wikiLink.getSection();
		}
		StringBuilder html = new StringBuilder();
		html.append("<a href=\"").append(url).append('\"').append(styleHtml);
		html.append(" title=\"").append(StringEscapeUtils.escapeHtml4(topic)).append('\"').append(targetHtml).append('>');
		if (escapeHtml) {
			html.append(StringEscapeUtils.escapeHtml4(text));
		} else {
			html.append(text);
		}
		html.append("</a>");
		return html.toString();
	}

	/**
	 * Build a URL to the topic page for a given topic.  This method performs
	 * additional processing beyond what {@link WikiLink#toRelativeUrl} does,
	 * including returning upload or edit URLs for non-existent images/topics,
	 * handling minor variations in case-sensitivity, etc.
	 *
	 * @param wikiLink The WikiLink object containing all relevant information
	 *  about the link being generated.
	 * @throws DataAccessException Thrown if any error occurs while retrieving topic
	 *  information.
	 */
	public static String buildTopicUrl(WikiLink wikiLink) throws DataAccessException {
		String url = null;
		String topic = wikiLink.getDestination();
		String virtualWiki = ((wikiLink.getAltVirtualWiki() != null) ? wikiLink.getAltVirtualWiki().getName() : wikiLink.getVirtualWiki());
		if (wikiLink.getNamespace().getId().equals(Namespace.MEDIA_ID)) {
			// for the "Media:" namespace, link directly to the file
			String filename = Namespace.namespace(Namespace.FILE_ID).getLabel(virtualWiki) + Namespace.SEPARATOR + wikiLink.getArticle();
			url = ImageUtil.buildImageFileUrl(wikiLink.getContextPath(), virtualWiki, filename, false);
			if (url == null) {
				wikiLink = new WikiLink(wikiLink.getContextPath(), virtualWiki, "Special:Upload");
				wikiLink.setQuery("topic=" + Utilities.encodeAndEscapeTopicName(filename));
				url = wikiLink.toRelativeUrl();
			}
		} else if (StringUtils.isBlank(topic) && !StringUtils.isBlank(wikiLink.getSection())) {
			// do not check existence for section links
			url = wikiLink.toRelativeUrl();
		} else {
			String targetTopic = LinkUtil.isExistingArticle(virtualWiki, topic);
			if (targetTopic == null && !wikiLink.isSpecial()) {
				url = LinkUtil.buildEditLinkUrl(wikiLink.getContextPath(), virtualWiki, topic, wikiLink.getQuery(), -1);
			} else if (!StringUtils.equals(topic, targetTopic) && !wikiLink.isSpecial()) {
				// topics might have differed by case or some other minor reason
				WikiLink altWikiLink = new WikiLink(wikiLink);
				altWikiLink.setDestination(targetTopic);
				url = altWikiLink.toRelativeUrl();
			} else {
				url = wikiLink.toRelativeUrl();
			}
		}
		return url;
	}

	/**
	 * Given an article name, return the appropriate comments topic article name.
	 * For example, if the article name is "Topic" then the return value is
	 * "Comments:Topic".
	 *
	 * @param virtualWiki The current virtual wiki.
	 * @param name The article name from which a comments article name is to
	 *  be constructed.
	 * @return The comments article name for the article name.
	 */
	public static String extractCommentsLink(String virtualWiki, String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Topic name must not be empty in extractCommentsLink");
		}
		WikiLink wikiLink = new WikiLink(null, virtualWiki, name);
		Namespace commentsNamespace = null;
		try {
			commentsNamespace = Namespace.findCommentsNamespace(wikiLink.getNamespace());
		} catch (DataAccessException e) {
			throw new IllegalStateException("Database error while retrieving comments namespace", e);
		}
		if (commentsNamespace == null) {
			throw new IllegalArgumentException("Topic " + virtualWiki + ':' + name + " does not have a comments namespace");
		}
		return (!StringUtils.isBlank(commentsNamespace.getLabel(virtualWiki))) ? commentsNamespace.getLabel(virtualWiki) + Namespace.SEPARATOR + wikiLink.getArticle() : wikiLink.getArticle();
	}

	/**
	 * Given an article name, extract an appropriate topic article name.  For
	 * example, if the article name is "Comments:Topic" then the return value
	 * is "Topic".
	 *
	 * @param virtualWiki The current virtual wiki.
	 * @param name The article name from which a topic article name is to be
	 *  constructed.
	 * @return The topic article name for the article name.
	 */
	public static String extractTopicLink(String virtualWiki, String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Topic name must not be empty in extractTopicLink");
		}
		WikiLink wikiLink = new WikiLink(null, virtualWiki, name);
		Namespace mainNamespace = Namespace.findMainNamespace(wikiLink.getNamespace());
		if (mainNamespace == null) {
			throw new IllegalArgumentException("Topic " + virtualWiki + ':' + name + " does not have a main namespace");
		}
		return (!StringUtils.isBlank(mainNamespace.getLabel(virtualWiki))) ? mainNamespace.getLabel(virtualWiki) + Namespace.SEPARATOR + wikiLink.getArticle() : wikiLink.getArticle();
	}

	/**
	 * Given a topic, if that topic is a redirect find the target topic of the redirection.
	 *
	 * @param parent The topic being queried.  If this topic is a redirect then the redirect
	 *  target will be returned, otherwise the topic itself is returned.
	 * @param attempts The maximum number of child topics to follow.  This parameter prevents
	 *  infinite loops if topics redirect back to one another.
	 * @return If the parent topic is a redirect then this method returns the target topic that
	 *  is being redirected to, otherwise the parent topic is returned.
	 * @throws DataAccessException Thrown if any error occurs while retrieving data.
	 */
	public static Topic findRedirectedTopic(Topic parent, int attempts) throws DataAccessException {
		int count = attempts;
		String target = parent.getRedirectTo();
		if (parent.getTopicType() != TopicType.REDIRECT || StringUtils.isBlank(target)) {
			logger.error("getRedirectTarget() called for non-redirect topic " + parent.getVirtualWiki() + ':' + parent.getName());
			return parent;
		}
		// avoid infinite redirection
		count++;
		if (count > 10) {
			//TODO throw new WikiException(new WikiMessage("topic.redirect.infinite"));
			return parent;
		}
		String virtualWiki = parent.getVirtualWiki();
		WikiLink wikiLink = LinkUtil.parseWikiLink(null, virtualWiki, target);
		if (wikiLink.getAltVirtualWiki() != null) {
			virtualWiki = wikiLink.getAltVirtualWiki().getName();
		}
		// get the topic that is being redirected to
		Topic child = WikiBase.getDataHandler().lookupTopic(virtualWiki, wikiLink.getNamespace(), wikiLink.getArticle(), false);
		if (child == null) {
			// child being redirected to doesn't exist, return parent
			return parent;
		}
		if (StringUtils.isBlank(child.getRedirectTo())) {
			// found a topic that is not a redirect, return
			return child;
		}
		// child is a redirect, keep looking
		return findRedirectedTopic(child, count);
	}

	/**
	 * Generate the HTML for an interwiki anchor link.
	 *
	 * @param wikiLink The WikiLink object containing all relevant information
	 *  about the link being generated.
	 * @return The HTML anchor tag for the interwiki link, or <code>null</code>
	 *  if there is no interwiki link defined for the WikiLink.
	 */
	public static String interwiki(WikiLink wikiLink) throws ParserException {
		if (wikiLink.getInterwiki() == null) {
			return null;
		}
		String url = wikiLink.getInterwiki().format(wikiLink.getDestination());
		if (!StringUtils.isBlank(wikiLink.getSection())) {
			url += "#" + wikiLink.getSection();
		}
		String text = !StringUtils.isBlank(wikiLink.getText()) ? wikiLink.getText() : wikiLink.getDestination();
		Object[] args = new Object[3];
		args[0] = text;
		args[1] = url;
		args[2] = text;
		try {
			return WikiUtil.formatFromTemplate(TEMPLATE_LINK_INTERWIKI, args);
		} catch (IOException e) {
			throw new ParserException(e);
		}
	}

	/**
	 * Given a topic name, determine if that name corresponds to a comments
	 * page.
	 *
	 * @param virtualWiki The current virtual wiki.
	 * @param topicName The topic name (non-null) to examine to determine if it
	 *  is a comments page or not.
	 * @return <code>true</code> if the page is a comments page, <code>false</code>
	 *  otherwise.
	 */
	public static boolean isCommentsPage(String virtualWiki, String topicName) {
		WikiLink wikiLink = new WikiLink(null, virtualWiki, topicName);
		if (wikiLink.getNamespace().getId().equals(Namespace.SPECIAL_ID)) {
			return false;
		}
		try {
			return (Namespace.findCommentsNamespace(wikiLink.getNamespace()) != null);
		} catch (DataAccessException e) {
			throw new IllegalStateException("Database error while retrieving comments namespace", e);
		}
	}

	/**
	 * Utility method for determining if an article name corresponds to a valid
	 * wiki link.  In this case an "article name" could be an existing topic, a
	 * "Special:" page, a user page, an interwiki link, etc.  This method will
	 * return the article name if the given name corresponds to a valid special
	 * page, user page, topic, or other existing article, or <code>null</code>
	 * if no valid article exists.
	 *
	 * @param virtualWiki The virtual wiki for the topic being checked.
	 * @param articleName The name of the article that is being checked.
	 * @return The article name if the given name and virtual wiki correspond
	 *  to a valid special page, user page, topic, or other existing article,
	 *  or <code>null</code> if no valid article exists.
	 * @throws DataAccessException Thrown if an error occurs during lookup.
	 */
	public static String isExistingArticle(String virtualWiki, String articleName) throws DataAccessException {
		if (StringUtils.isBlank(virtualWiki) || StringUtils.isBlank(articleName)) {
			return null;
		}
		WikiLink wikiLink = new WikiLink(null, virtualWiki, articleName);
		if (PseudoTopicHandler.isPseudoTopic(wikiLink.getDestination())) {
			return articleName;
		}
		if (wikiLink.getInterwiki() != null) {
			return articleName;
		}
		if (!Environment.isInitialized()) {
			// not initialized yet
			return null;
		}
		String topicName = WikiBase.getDataHandler().lookupTopicName(virtualWiki, wikiLink.getNamespace(), wikiLink.getArticle());
		if (topicName == null && Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_CAPITALIZATION)) {
			String alternativeArticleName = (StringUtils.equals(wikiLink.getArticle(), StringUtils.capitalize(wikiLink.getArticle()))) ? StringUtils.lowerCase(wikiLink.getArticle()) : StringUtils.capitalize(wikiLink.getArticle());
			topicName = WikiBase.getDataHandler().lookupTopicName(virtualWiki, wikiLink.getNamespace(), alternativeArticleName);
		}
		return topicName;
	}

	/**
	 *
	 */
	protected static int prefixPosition(String topicName) {
		int prefixPosition = topicName.indexOf(Namespace.SEPARATOR, 1);
		// if a match is found and it's not the last character of the name, it's a prefix.
		return (prefixPosition != -1 && (prefixPosition + 1) < topicName.length()) ? prefixPosition : -1;
	}

	/**
	 * Make sure a URL does not contain any extraneous characters such as "//" in
	 * places where it should not.
	 *
	 * @param url The URL to be normalized.
	 * @return The normalized URL.
	 */
	public static String normalize(String url) {
		if (StringUtils.isBlank(url)) {
			return url;
		}
		// first find the protocol
		int pos = url.indexOf("://");
		if (pos == -1 || pos == (url.length() - 1)) {
			return url;
		}
		String protocol = url.substring(0, pos + "://".length());
		String remainder = url.substring(protocol.length());
		return protocol + StringUtils.replace(remainder, "//", "/");
	}

	/**
	 * Parse a wiki topic link and return a <code>WikiLink</code> object
	 * representing the link.  Wiki topic links are of the form "Topic?Query#Section".
	 *
	 * @param contextPath The servlet context path.
	 * @param virtualWiki The current virtual wiki.
	 * @param raw The raw topic link text.
	 * @return A WikiLink object that represents the link.
	 */
	public static WikiLink parseWikiLink(String contextPath, String virtualWiki, String raw) {
		// note that this functionality was previously handled with a regular
		// expression, but the expression caused CPU usage to spike to 100%
		// with topics such as "Urnordisch oder Nordwestgermanisch?"
		String processed = raw.trim();
		WikiLink wikiLink = new WikiLink(contextPath, virtualWiki);
		if (wikiLink.getNamespace() == null) {
			throw new IllegalStateException("Unable to determine namespace for topic.  This error generally indicates a configuration or database issue.  Check the logs for additional information.");
		}
		if (StringUtils.isBlank(processed)) {
			return wikiLink;
		}
		// first look for a section param - "#..."
		int sectionPos = processed.indexOf('#');
		if (sectionPos != -1 && sectionPos < processed.length()) {
			String sectionString = processed.substring(sectionPos + 1);
			wikiLink.setSection(sectionString);
			if (sectionPos == 0) {
				// link is of the form #section, no more to process
				return wikiLink;
			}
			processed = processed.substring(0, sectionPos);
		}
		// now see if the link ends with a query param - "?..."
		int queryPos = processed.indexOf('?', 1);
		if (queryPos != -1 && queryPos < processed.length()) {
			String queryString = processed.substring(queryPos + 1);
			wikiLink.setQuery(queryString);
			processed = processed.substring(0, queryPos);
		}
		// search for a namespace or virtual wiki
		String topic = LinkUtil.processVirtualWiki(processed, wikiLink);
		if (wikiLink.getAltVirtualWiki() != null) {
			// strip the virtual wiki
			processed = topic;
			virtualWiki = wikiLink.getAltVirtualWiki().getName();
		}
		wikiLink.setText(processed);
		// set namespace & article
		wikiLink.initialize(processed);
		topic = wikiLink.getArticle();
		if (!wikiLink.getNamespace().getId().equals(Namespace.MAIN_ID)) {
			// store the display name WITH any extra spaces
			wikiLink.setText(processed);
			// update original text in case topic was of the form "xxx: topic"
			processed = wikiLink.getNamespace().getLabel(virtualWiki) + Namespace.SEPARATOR + topic;
		}
		// if no namespace or virtual wiki, see if there's an interwiki link
		if (wikiLink.getNamespace().getId().equals(Namespace.MAIN_ID) && wikiLink.getAltVirtualWiki() == null) {
			topic = LinkUtil.processInterWiki(processed, wikiLink);
			if (wikiLink.getInterwiki() != null) {
				// strip the interwiki
				processed = topic;
			}
		}
		if (wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
			// captions are handled differently for images, so clear the link text value.
			wikiLink.setText(null);
		} else if (!StringUtils.isBlank(wikiLink.getSection())) {
			wikiLink.setText(wikiLink.getText() + "#" + wikiLink.getSection());
		}
		wikiLink.setArticle(Utilities.decodeTopicName(topic, true));
		// destination is namespace + topic
		wikiLink.setDestination(Utilities.decodeTopicName(processed, true));
		return wikiLink;
	}

	/**
	 *
	 */
	private static String processInterWiki(String processed, WikiLink wikiLink) {
		// interwiki does not require a topic name, so do not use the prefixPosition method
		int prefixPosition = processed.indexOf(Namespace.SEPARATOR, 1);
		if (prefixPosition == -1) {
			return processed;
		}
		String linkPrefix = processed.substring(0, prefixPosition).trim();
		try {
			Interwiki interwiki = WikiBase.getDataHandler().lookupInterwiki(linkPrefix);
			if (interwiki != null) {
				wikiLink.setInterwiki(interwiki);
			}
		} catch (DataAccessException e) {
			// this should not happen, if it does then swallow the error
			logger.warn("Failure while trying to lookup interwiki: " + linkPrefix, e);
		}
		return (wikiLink.getInterwiki() != null) ? processed.substring(prefixPosition + Namespace.SEPARATOR.length()).trim(): processed;
	}

	/**
	 *
	 */
	private static String processVirtualWiki(String processed, WikiLink wikiLink) {
		// virtual wiki does not require a topic name, so do not use the prefixPosition method
		int prefixPosition = processed.indexOf(Namespace.SEPARATOR, 1);
		if (prefixPosition == -1) {
			return processed;
		}
		String linkPrefix = processed.substring(0, prefixPosition).trim();
		try {
			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(linkPrefix);
			if (virtualWiki != null) {
				wikiLink.setAltVirtualWiki(virtualWiki);
			}
		} catch (DataAccessException e) {
			// this should not happen, if it does then swallow the error
			logger.warn("Failure while trying to lookup virtual wiki: " + linkPrefix, e);
		}
		return (wikiLink.getAltVirtualWiki() != null) ? processed.substring(prefixPosition + Namespace.SEPARATOR.length()).trim(): processed;
	}

	/**
	 * Utility method for determining a topic namespace given a topic name.  This method
	 * accepts ONLY the topic name - if the topic name is prefixed with a virtual wiki,
	 * interwiki, or other value then it will not return the proper namespace.
	 */
	public static Namespace retrieveTopicNamespace(String virtualWiki, String topicName) {
		int prefixPosition = LinkUtil.prefixPosition(topicName);
		if (prefixPosition == -1) {
			return Namespace.namespace(Namespace.MAIN_ID);
		}
		String linkPrefix = topicName.substring(0, prefixPosition).trim();
		try {
			Namespace namespace = WikiBase.getDataHandler().lookupNamespace(virtualWiki, linkPrefix);
			return (namespace == null) ? Namespace.namespace(Namespace.MAIN_ID) : namespace;
		} catch (DataAccessException e) {
			// this should not happen, if it does then throw a runtime exception
			throw new IllegalStateException("Failure while trying to lookup namespace: " + linkPrefix, e);
		}
	}

	/**
	 * Utility method for determining a topic's page name given its namespace and full
	 * topic name.
	 *
	 * @param namespace The namespace for the topic name.
	 * @param virtualWiki The virtual wiki for the topic.
	 * @param topicName The full topic name that is being split.
	 * @return The pageName portion of the topic name.  If the topic is "Comments:Main Page"
	 *  then the page name is "Main Page".
	 */
	public static String retrieveTopicPageName(Namespace namespace, String virtualWiki, String topicName) {
		if (namespace.getId() == Namespace.MAIN_ID) {
			return topicName;
		}
		if (StringUtils.startsWithIgnoreCase(topicName, namespace.getLabel(virtualWiki))) {
			// translated namespace
			return topicName.substring(namespace.getLabel(virtualWiki).length() + Namespace.SEPARATOR.length());
		} else if (StringUtils.startsWithIgnoreCase(topicName, namespace.getDefaultLabel())) {
			// translated namespace available, but using the default namespace label
			return topicName.substring(namespace.getDefaultLabel().length() + Namespace.SEPARATOR.length());
		}
		throw new IllegalArgumentException("Invalid topic name & namespace combination: " + namespace.getId() + " / " + topicName);
	}

	/**
	 * Utility method for determining if a topic name is valid for use on the Wiki,
	 * meaning that it is not empty and does not contain any invalid characters.
	 *
	 * @param virtualWiki The current virtual wiki.
	 * @param name The topic name to validate.
	 * @param allowSpecial Set to <code>true</code> if topics in the Special: namespace
	 *  should be considered valid.  These topics cannot be created, so (for example)
	 *  this method should not allow them when editing topics.
	 * @throws WikiException Thrown if the topic name is invalid.
	 */
	public static void validateTopicName(String virtualWiki, String name, boolean allowSpecial) throws WikiException {
		WikiLink wikiLink = null;
		try {
			wikiLink = new WikiLink(null, virtualWiki, name);
		} catch (IllegalArgumentException e) {
			if (StringUtils.isBlank(virtualWiki)) {
				throw new WikiException(new WikiMessage("common.exception.novirtualwiki"));
			} else {
				throw new WikiException(new WikiMessage("common.exception.notopic"));
			}
		}
		LinkUtil.validateTopicName(wikiLink, allowSpecial);
	}

	/**
	 * Utility method for determining if a topic name is valid for use on the Wiki,
	 * meaning that it is not empty and does not contain any invalid characters.  This
	 * method offers improved performance for cases where a WikiLink object is
	 * already available.
	 *
	 * @param wikiLink The WikiLink object to validate.
	 * @param allowSpecial Set to <code>true</code> if topics in the Special: namespace
	 *  should be considered valid.  These topics cannot be created, so (for example)
	 *  this method should not allow them when editing topics.
	 * @throws WikiException Thrown if the topic name is invalid.
	 */
	public static void validateTopicName(WikiLink wikiLink, boolean allowSpecial) throws WikiException {
		if (StringUtils.isBlank(wikiLink.getVirtualWiki())) {
			throw new WikiException(new WikiMessage("common.exception.novirtualwiki"));
		}
		if (StringUtils.isBlank(wikiLink.getDestination())) {
			throw new WikiException(new WikiMessage("common.exception.notopic"));
		}
		if (!allowSpecial && PseudoTopicHandler.isPseudoTopic(wikiLink.getDestination())) {
			throw new WikiException(new WikiMessage("common.exception.pseudotopic", wikiLink.getDestination()));
		}
		if (StringUtils.startsWith(wikiLink.getArticle().trim(), "/")) {
			throw new WikiException(new WikiMessage("common.exception.name", wikiLink.getDestination()));
		}
		if (!allowSpecial && wikiLink.getNamespace().getId().equals(Namespace.SPECIAL_ID)) {
			throw new WikiException(new WikiMessage("common.exception.name", wikiLink.getDestination()));
		}
		Matcher m = LinkUtil.INVALID_TOPIC_NAME_PATTERN.matcher(wikiLink.getDestination());
		if (m.find()) {
			throw new WikiException(new WikiMessage("common.exception.name", wikiLink.getDestination()));
		}
	}
}
