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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jamwiki.Environment;
import org.jamwiki.model.WikiUser;

/**
 * This class is a utility class used to hold configuration settings for the
 * parser.
 */
public class ParserInput {

	private boolean allowSectionEdit = true;
	private boolean allowTableOfContents = true;
	private String context;
	/** Depth is used to prevent infinite nesting of templates and other objects. */
	private int depth = 0;
	/**
	 * If an infinite loop is detected increment this counter so that the parser can
	 * halt parsing for infinite loop attacks.
	 */
	private int infiniteLoopCount = 0;
	private Locale locale;
	private TableOfContents tableOfContents;
	/** Template inclusion tracks whether or not template code is being parsed.  A counter is used to deal with nested templates. */
	private int templateDepth = 0;
	/** Map of generic temporary objects used during parsing. */
	private Map<String, Object> tempParams;
	private final String topicName;
	/** Display value for the current user, typically the IP address. */
	private String userDisplay;
	private final String virtualWiki;
	/** Current WikiUser (if any). */
	private WikiUser wikiUser;

	/**
	 *
	 */
	public ParserInput(String virtualWiki, String topicName) {
		this.virtualWiki = virtualWiki;
		this.topicName = topicName;
	}

	/**
	 * Copy constructor.
	 */
	public ParserInput(ParserInput parserInput) {
		this.allowSectionEdit = parserInput.allowSectionEdit;
		this.allowTableOfContents = parserInput.allowTableOfContents;
		this.context = parserInput.context;
		this.depth = parserInput.depth;
		this.infiniteLoopCount = parserInput.infiniteLoopCount;
		this.locale = parserInput.locale;
		this.templateDepth = parserInput.templateDepth;
		this.topicName = parserInput.topicName;
		this.userDisplay = parserInput.userDisplay;
		this.virtualWiki = parserInput.virtualWiki;
		this.wikiUser = parserInput.wikiUser;
	}

	/**
	 * This method will return <code>true</code> if edit links are allowed
	 * next to each section heading.  During preview and in some other
	 * instances that feature needs to be disabled.
	 *
	 * @return Returns <code>true</code> if edit links are allowed next to
	 *  each section heading.
	 */
	public boolean getAllowSectionEdit() {
		return allowSectionEdit;
	}

	/**
	 * Set method used to indicate whether or not to allow edit links
	 * next to each section heading.  During preview and in some other
	 * instances that feature needs to be disabled.
	 *
	 * @param allowSectionEdit Set to <code>true</code> if edits links are
	 *  allowed next to each section heading, <code>false</code> otherwise.
	 */
	public void setAllowSectionEdit(boolean allowSectionEdit) {
		this.allowSectionEdit = allowSectionEdit;
	}

	/**
	 * This method will return <code>true</code> if a table of contents
	 * can be included in the parsed topic content.  System topics such
	 * as the left menu and footer should not display a TOC.
	 *
	 * @return Returns <code>true</code> if a table of contents can be
	 *  included in the parsed topic content.
	 */
	public boolean getAllowTableOfContents() {
		return allowTableOfContents;
	}

	/**
	 * Set method to indicate whether or not a table of contents can be
	 * included in the parsed topic content.  System topics such as the
	 * left menu and footer should not display a TOC.
	 *
	 * @param allowTableOfContents Set to <code>true</code> if a table of
	 *  contents can be included in the parsed topic content.
	 */
	public void setAllowTableOfContents(boolean allowTableOfContents) {
		this.allowTableOfContents = allowTableOfContents;
	}

	/**
	 * Get the servlet context associated with the current parser input
	 * instance.  Servlet context is used when building links.
	 *
	 * @return The servlet context associated with the current parser
	 *  input instance.
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Set the servlet context associated with the current parser input
	 * instance.  Servlet context is used when building links.
	 *
	 * @param context The servlet context associated with the current parser
	 *  input instance.
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * Since it is possible to call a new parser instance from within another
	 * parser instance, depth provides a way to determine how many times the
	 * parser has nested, thus providing a way of avoiding infinite loops.
	 *
	 * @return The current nesting level of the parser instance.
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * This method decreases the current parser instance depth and should
	 * only be called when a parser instance exits.  Depth is useful as a
	 * way of avoiding infinite loops in the parser.
	 */
	public void decrementDepth() {
		this.depth--;
	}

	/**
	 * This method increases the current parser instance depth and should
	 * only be called when a instantiating a new parser instance.  Depth is
	 * useful as a way of avoiding infinite loops in the parser.
	 *
	 * @throws ExcessiveNestingException Thrown if incrementing the depth
	 *  would cause the depth to exceed the maximum configured depth.  Useful
	 *  for infinite loop detection.
	 */
	public void incrementDepth() throws ExcessiveNestingException {
		// avoid infinite loops
		if ((this.getDepth() + 1) >= Environment.getIntValue(Environment.PROP_PARSER_MAX_PARSER_ITERATIONS)) {
			this.infiniteLoopCount++;
			throw new ExcessiveNestingException("Potential infinite parsing loop - over " + this.getDepth() + " parser iterations while parsing topic " + this.getVirtualWiki() + ':' + this.getTopicName());
		}
		this.depth++;
	}

	/**
	 * The infinite loop count records how many times the parser has found what
	 * it believes to be an infinite loop while parsing a topic.  Each time such
	 * a loop is encountered the parser will abort and move on to the next tag,
	 * so this counter provides a way of essentially saying "enough is enough"
	 * if too many such loops are encountered.
	 */
	public int getInfiniteLoopCount() {
		return this.infiniteLoopCount;
	}

	/**
	 * Get the locale associated with the current parser input instance.
	 * Locale is used primarily when building links or displaying messages.
	 *
	 * @return The locale associated with the current parser input instance.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Set the locale associated with the current parser input instance.
	 * Locale is used primarily when building links or displaying messages.
	 *
	 * @param locale The locale associated with the current parser input
	 *  instance.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Get the table of contents object associated with the current parser
	 * input instance.  The table of contents is used for building an internal
	 * set of links to headings in the current document.
	 *
	 * @return The table of contents object associated with the current parser
	 *  input instance.
	 */
	public TableOfContents getTableOfContents() {
		if (this.tableOfContents == null) {
			this.tableOfContents = new TableOfContents();
		}
		return this.tableOfContents;
	}

	/**
	 * If the map of arbitrary temporary parameters contains the specified
	 * key then return its value, otherwise return <code>null</code>.
	 *
	 * @return The value of the specified key in the temporary parameters
	 *  map, or <code>null</code> if no such value exists.
	 */
	public Object getTempParam(String key) {
		if (this.tempParams == null) {
			return null;
		}
		return this.tempParams.get(key);
	}

	/**
	 * Add an item to the map of arbitrary temporary parameters.  If an
	 * item with the same key is already in the map then it will be
	 * overwritten.
	 *
	 * @param key The key to use for the map entry.
	 * @param value The value to store in the map for the specified key.
	 */
	public void addTempParam(String key, Object value) {
		if (this.tempParams == null) {
			this.tempParams = new HashMap<String, Object>();
		}
		this.tempParams.put(key, value);
	}

	/**
	 * Remove an item to the map of arbitrary temporary parameters.  If
	 * no item with the specified key is in the map then this function
	 * has no effect.
	 *
	 * @param key The key for the map entry being removed.
	 */
	public void removeTempParam(String key) {
		if (this.tempParams == null) {
			return;
		}
		this.tempParams.remove(key);
	}

	/**
	 * Get the depth level when template code is being parsed.
	 *
	 * @return The current number of template inclusions.
	 */
	public int getTemplateDepth() {
		return templateDepth;
	}

	/**
	 * This method decreases the current template inclusion depth and should
	 * only be called when a template finishes processing.
	 */
	public void decrementTemplateDepth() {
		this.templateDepth--;
	}

	/**
	 * This method decreases the current template inclusion depth and should
	 * only be called when a template begins processing.
	 */
	public void incrementTemplateDepth() {
		this.templateDepth++;
	}

	/**
	 * Get the topic name for the topic being parsed by this parser input
	 * instance.
	 *
	 * @return The topic name for the topic being parsed by this parser input
	 * instance.
	 */
	public String getTopicName() {
		return this.topicName;
	}

	/**
	 * Get the user display name associated with the current parser input
	 * instance.  This value is typically the user IP address and is used
	 * primarily when parsing signatures.
	 *
	 * @return The user display, typically the user IP address, associated
	 *  with the current parser input instance.
	 */
	public String getUserDisplay() {
		return this.userDisplay;
	}

	/**
	 * Set the user display name associated with the current parser input
	 * instance.  This value is typically the user IP address and is used
	 * primarily when parsing signatures.
	 *
	 * @param userDisplay The user display, typically the user IP address,
	 *  associated with the current parser input instance.
	 */
	public void setUserDisplay(String userDisplay) {
		this.userDisplay = userDisplay;
	}

	/**
	 * Get the virtual wiki name associated with the current parser input
	 * instance.  The virtual wiki name is used primarily when parsing links.
	 *
	 * @return The virtual wiki name associated with the current parser input
	 * instance.
	 */
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 * Get the wiki user object associated with the current parser input
	 * instance.  The wiki user object is used primarily when parsing
	 * signatures.
	 *
	 * @return The wiki user object associated with the current parser input
	 * instance.
	 */
	public WikiUser getWikiUser() {
		return this.wikiUser;
	}

	/**
	 * Set the wiki user object associated with the current parser input
	 * instance.  The wiki user object is used primarily when parsing
	 * signatures.
	 *
	 * @param user The wiki user object associated with the current
	 *  parser input instance.
	 */
	public void setWikiUser(WikiUser user) {
		this.wikiUser = user;
	}
}
