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
package org.jamwiki.parser.jflex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiConfiguration;
import org.jamwiki.model.WikiConfigurationObject;
import org.jamwiki.parser.ParserException;
import org.jamwiki.utils.ResourceUtil;
import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class that is extended by the JFlex custom tag lexer.  Used in parsing
 * custom tags of the form <custom attribute="value">content</custom>.
 */
public abstract class AbstractJAMWikiCustomTagLexer extends JFlexLexer {

	protected static final WikiLogger logger = WikiLogger.getLogger(AbstractJAMWikiCustomTagLexer.class.getName());
	/** Registry of all active custom tags where the key is the tag name and the value is an instance of the tag class. */
	private static final Map<String, JFlexCustomTagItem> CUSTOM_TAG_REGISTRY = new HashMap<String, JFlexCustomTagItem>();
	static {
		initializeCustomTagRegistry();
	}
	/** Stack of currently parsed tag content. */
	private List<CustomTagItem> customTagStack;

	/**
	 * This method should be called after all content has been parsed in order
	 * to flush any remaining tags off of the stack.
	 *
	 * @return <code>null</code> if there is no content on the custom tag stack,
	 *  otherwise the parsed content will be returned if the stack was not empty.
	 */
	protected String flushCustomTagStack() {
		if (this.customTagStack == null || this.getCustomTagStack().isEmpty()) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		CustomTagItem customTagItem;
		while ((customTagItem = this.popCustomTag()) != null) {
			result.append(this.processText(customTagItem.getRawContent()));
		}
		return result.toString();
	}

	/**
	 *
	 */
	private List<CustomTagItem> getCustomTagStack() {
		if (this.customTagStack == null) {
			this.customTagStack = new ArrayList<CustomTagItem>();
		}
		return this.customTagStack;
	}

	/**
	 * Initialize the mapping of custom tag name to tag instance.
	 */
	private static void initializeCustomTagRegistry() {
		List<WikiConfigurationObject> parserCustomTags = WikiConfiguration.getInstance().getJflexParserCustomTags();
		for (WikiConfigurationObject wikiConfigurationObject : parserCustomTags) {
			String parserCustomTagClass = wikiConfigurationObject.getClazz();
			Object object = null;
			try {
				object = ResourceUtil.instantiateClass(parserCustomTagClass);
			} catch (IllegalStateException e) {
				logger.warn("Could not instantiate configured custom parser tag: " + parserCustomTagClass);
				continue;
			}
			if (!(object instanceof JFlexCustomTagItem)) {
				logger.warn("Custom tag does not implement interface JFlexCustomTagItem: " + parserCustomTagClass);
				continue;
			}
			logger.info("Initializing custom parser tag: " + parserCustomTagClass);
			JFlexCustomTagItem jflexCustomTagItem = (JFlexCustomTagItem)object;
			if (!StringUtils.isBlank(wikiConfigurationObject.getKey())) {
				jflexCustomTagItem.setTagName(wikiConfigurationObject.getKey());
			}
			if (StringUtils.isBlank(jflexCustomTagItem.getTagName())) {
				logger.warn("No tag name specified for custom tag: " + parserCustomTagClass);
				continue;
			}
			jflexCustomTagItem.initParams(wikiConfigurationObject.getInitParams());
			CUSTOM_TAG_REGISTRY.put(jflexCustomTagItem.getTagName(), jflexCustomTagItem);
		}
	}

	/**
	 * Parse a custom tag.
	 */
	private String parseCustomTag(CustomTagItem customTagItem) {
		HtmlTagItem htmlTagItem = customTagItem.getHtmlTagItem();
		JFlexCustomTagItem jflexCustomTagItem = CUSTOM_TAG_REGISTRY.get(htmlTagItem.getTagType());
		String result = customTagItem.getRawContent();
		if (jflexCustomTagItem == null) {
			logger.warn("parseCustomTag called with invalid tag type: " + htmlTagItem.getTagType());
		} else {
			try {
				result = this.processText(jflexCustomTagItem.parse(this, htmlTagItem.getAttributes(), customTagItem.getTagContent().toString()));
			} catch (ParserException e) {
				logger.warn("Failure while parsing custom tag " + htmlTagItem.getTagType() + " with content: " + customTagItem.getTagContent());
			}
		}
		return this.processText(result);
	}

	/**
	 * Examine a close tag and determine if it is a custom tag.  If it is
	 * a custom tag and if that tag was previously opened then parse the tag
	 * and its content and then pop the stack.
	 *
	 * @param closeTag The close tag, such as "</custom>".
	 */
	protected String parsePossibleCustomTagClose(String closeTag) {
		HtmlTagItem htmlTagItem = null;
		try {
			htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(closeTag);
		} catch (ParserException e) {
			return this.processText(closeTag);
		}
		for (int i = (this.getCustomTagStack().size() - 1); i >= 0; i--) {
			// see if the tag is open
			if (this.getCustomTagStack().get(i).getHtmlTagItem().getTagType().equals(htmlTagItem.getTagType())) {
				// tag is open, pop it and everything after it
				return this.popCustomTagAtPosition(i);
			}
		}
		// tag is not open, return as plain text
		return this.processText(closeTag);
	}

	/**
	 * Examine an open tag and determine if it represents a custom tag.  If
	 * it is a custom tag and it does not have body content ("<custom />")
	 * then parse it, otherwise add the tag to the stack and wait until (if)
	 * a matching close tag is found.
	 *
	 * @param openTag The open tag of the form <custom attribute="value"> or
	 *  <custom />.
	 */
	protected String parsePossibleCustomTagOpen(String openTag) {
		HtmlTagItem htmlTagItem = null;
		try {
			htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(openTag);
		} catch (ParserException e) {
			return this.processText(openTag);
		}
		if (!CUSTOM_TAG_REGISTRY.containsKey(htmlTagItem.getTagType())) {
			// not a custom tag
			return this.processText(openTag);
		}
		if (htmlTagItem.isTagEmptyBody()) {
			// custom tag of the form <custom />
			return this.parseCustomTag(new CustomTagItem(htmlTagItem, openTag));
		}
		this.getCustomTagStack().add(new CustomTagItem(htmlTagItem, openTag));
		return "";
	}

	/**
	 * Pop the custom tag stack, removing the most recently added item and
	 * returning it.  If the stack is empty return <code>null</code>.
	 */
	private CustomTagItem popCustomTag() {
		return (this.customTagStack == null || this.getCustomTagStack().isEmpty()) ? null : this.getCustomTagStack().remove(this.getCustomTagStack().size() - 1);
	}

	/**
	 * Remove the tag at the specified position in the stack, returning any
	 * output that is generated (note: in most cases an empty string will be
	 * returned since output will be appended to the previous item in the stack).
	 *
	 * @param pos The position of the tag to pop, starting at zero.  For
	 *  example, if there are five items on the stack and this value is two
	 *  then the fourth and fifth items will be treated as plain text and the
	 *  third item will be popped.
	 */
	private String popCustomTagAtPosition(int pos) {
		if (pos < 0 || pos >= this.getCustomTagStack().size()) {
			logger.warn("popCustomTagAtPosition called with invalid index " + pos);
			return "";
		}
		CustomTagItem customTagItem;
		for (int i = (this.getCustomTagStack().size() - 1); i > pos; i--) {
			// pop every tag as text that is after pos in the stack
			customTagItem = this.popCustomTag();
			this.processText(customTagItem.getRawContent());
		}
		customTagItem = this.popCustomTag();
		return this.parseCustomTag(customTagItem);
	}

	/**
	 * Either add text to the current stack item or, if there is no item on
	 * the stack, return the text directly to the lexer.
	 *
	 * @param text Text that will either be appended to the content of the last
	 *  tag in the stack, or if the stack is empty that will be returned directly
	 *  to the lexer.
	 * @return Returns the text that was passed to this method if the custom tag
	 *  stack is empty, otherwise returns an empty string.
	 */
	protected String processText(String text) {
		if (this.customTagStack == null || this.getCustomTagStack().isEmpty()) {
			return text;
		}
		this.getCustomTagStack().get(this.getCustomTagStack().size() - 1).getTagContent().append(text);
		return "";
	}

	/**
	 * Internal class used to model custom tags of the form
	 * <custom attribute="value">content</custom>.
	 */
	class CustomTagItem {

		/** A parsed HtmlTagItem representing the tag that this object wraps. */
		HtmlTagItem htmlTagItem;
		/** The content of the tag that this object wraps. */
		StringBuilder tagContent = new StringBuilder();
		/** The raw open tag used to create this object. */
		String tagPatternRaw;

		/**
		 *
		 */
		CustomTagItem(HtmlTagItem htmlTagItem, String tagPatternRaw) {
			this.htmlTagItem = htmlTagItem;
			this.tagPatternRaw = tagPatternRaw;
		}

		/**
		 *
		 */
		HtmlTagItem getHtmlTagItem() {
			return this.htmlTagItem;
		}

		/**
		 * Return the original, unmodified text used to create this item.  Useful
		 * in cases where the tag was not properly closed and thus no custom tag
		 * processing can occur.
		 */
		String getRawContent() {
			return this.tagPatternRaw + this.tagContent.toString();
		}

		/**
		 *
		 */
		StringBuilder getTagContent() {
			return this.tagContent;
		}
	}
}
