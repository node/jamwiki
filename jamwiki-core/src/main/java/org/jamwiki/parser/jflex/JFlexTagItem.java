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

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.parser.ParserException;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility class used during parsing.  This class holds the elements of an
 * HTML tag (open tag, close tag, content) as it is generated during parsing.
 */
class JFlexTagItem {

	private static final WikiLogger logger = WikiLogger.getLogger(JFlexTagItem.class.getName());

	private static final Map<String, String> EMPTY_BODY_TAGS = Utilities.initializeLookupMap("br", "col", "div", "hr", "td", "th");
	private static final Map<String, String> LIST_ITEM_TAGS = Utilities.initializeLookupMap("dd", "dt", "li");
	private static final Map<String, String> LIST_TAGS = Utilities.initializeLookupMap("dd", "dl", "dt", "li", "ol", "ul");
	private static final Map<String, String> NON_NESTING_TAGS = Utilities.initializeLookupMap("col", "colgroup", "dd", "dl", "dt", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "li", "ol", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "ul");
	private static final Map<String, String> NON_TEXT_BODY_TAGS = Utilities.initializeLookupMap("col", "colgroup", "dl", "ol", "table", "tbody", "tfoot", "thead", "tr", "ul");
	private static final Map<String, String> NON_INLINE_TAGS = Utilities.initializeLookupMap("blockquote", "caption", "center", "col", "colgroup", "dd", "div", "dl", "dt", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "li", "ol", "p", "pre", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "ul", NoParseDirectiveTag.NOPARSE_DIRECTIVE);
	private static final Map<String, String> TABLE_TAGS = Utilities.initializeLookupMap("caption", "col", "colgroup", "table", "tbody", "td", "tfoot", "th", "thead", "tr");
	protected static final String ROOT_TAG = "jflex-root";
	private String closeTagOverride;
	private final HtmlTagItem htmlTagItem;
	private final StringBuilder tagContent = new StringBuilder();
	private String tagType;

	/**
	 *
	 */
	JFlexTagItem(String tagType) {
		if (tagType == null) {
			throw new IllegalArgumentException("tagType must not be null");
		}
		this.htmlTagItem = null;
		this.tagType = tagType;
	}

	/**
	 *
	 */
	JFlexTagItem(HtmlTagItem htmlTagItem) {
		if (htmlTagItem == null) {
			throw new IllegalArgumentException("htmlTagItem must not be null");
		}
		this.htmlTagItem = htmlTagItem;
		this.tagType = this.htmlTagItem.getTagType();
	}

	/**
	 *
	 */
	JFlexTagItem(String tagType, String openTagRaw) throws ParserException {
		this.htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(openTagRaw);
		if (tagType == null && this.htmlTagItem == null) {
			throw new IllegalArgumentException("tagType must not be null");
		}
		this.tagType = ((tagType == null) ? this.htmlTagItem.getTagType() : tagType);
	}

	/**
	 * This method exists solely for those cases where a mis-matched HTML tag
	 * is being parsed (<u><strong>text</u></strong>) and the parser closes the
	 * inner tag and needs to provide an indication on the stack that the next
	 * tag should ignore the close tag it finds and use an overridden closing tag.
	 *
	 * @return A close tag to use that differs from the close tag that will be
	 *  found by the parser.
	 */
	protected String getCloseTagOverride() {
		return this.closeTagOverride;
	}

	/**
	 * This method exists solely for those cases where a mis-matched HTML tag
	 * is being parsed (<u><strong>text</u></strong>) and the parser closes the
	 * inner tag and needs to provide an indication on the stack that the next
	 * tag should ignore the close tag it finds and use an overridden closing tag.
	 *
	 * @param closeTagOverride A close tag to use that differs from the close tag
	 *  that will be found by the parser.
	 */
	protected void setCloseTagOverride(String closeTagOverride) {
		this.closeTagOverride = closeTagOverride;
	}

	/**
	 *
	 */
	protected StringBuilder getTagContent() {
		return this.tagContent;
	}

	/**
	 *
	 */
	protected String getTagType() {
		return this.tagType;
	}

	/**
	 * This method should generally not be called.  It exists primarily to support
	 * wikibold tags, which generate cases where the bold and italic tags could be
	 * switched in the stack, such as '''''bold''' then italic''.
	 */
	protected void changeTagType(String tagType) {
		this.tagType = tagType;
	}

	/**
	 * An empty body tag is one that contains no content, such as "br".
	 */
	private boolean isEmptyBodyTag() {
		if (this.isRootTag()) {
			return true;
		}
		return (EMPTY_BODY_TAGS.containsKey(this.tagType));
	}

	/**
	 * Determine if the tag is an empty paragraph tag.
	 */
	protected boolean isEmptyParagraphTag() {
		return (this.getTagType().equals("p") && StringUtils.isBlank(this.getTagContent()));
	}

	/**
	 * An inline tag is a tag that does not affect page flow such as
	 * "b" or "i".  A non-inline tag such as "div" is one that creates
	 * its own display box.
	 */
	protected boolean isInlineTag() {
		if (this.isRootTag()) {
			return true;
		}
		return (!NON_INLINE_TAGS.containsKey(this.tagType));
	}

	/**
	 * Determine if the tag is a list item tag (dd, dt, li).
	 */
	protected boolean isListItemTag() {
		return JFlexTagItem.isListItemTag(this.tagType);
	}

	/**
	 * Determine if the tag is a list item tag (dd, dt, li).
	 */
	protected static boolean isListItemTag(String tagType) {
		return (LIST_ITEM_TAGS.containsKey(tagType));
	}

	/**
	 * Determine if the tag is a list tag (dd, dl, dt, li, ol, ul).
	 */
	protected boolean isListTag() {
		return JFlexTagItem.isListTag(this.tagType);
	}

	/**
	 * Determine if the tag is a list tag (dd, dl, dt, li, ol, ul).
	 */
	protected static boolean isListTag(String tagType) {
		return (LIST_TAGS.containsKey(tagType));
	}

	/**
	 * A non-nesting tag is a tag such as "li" which cannot be nested within
	 * another "li" tag.
	 */
	protected boolean isNonNestingTag() {
		return JFlexTagItem.isNonNestingTag(this.tagType);
	}

	/**
	 * A non-nesting tag is a tag such as "li" which cannot be nested within
	 * another "li" tag.
	 */
	protected static boolean isNonNestingTag(String tagType) {
		return (NON_NESTING_TAGS.containsKey(tagType));
	}

	/**
	 *
	 */
	private boolean isNonInlineTagEnd(String tagText) {
		// this method is frequently invoked by the parser, so minimize the performance
		// overhead as much as possible.
		if (tagText.length() < 4) {
			// minimum length string is four characters ("</a>")
			return false;
		}
		if (tagText.charAt(tagText.length() - 1) != '>') {
			// must end with ">"
			return false;
		}
		int pos = tagText.lastIndexOf("</");
		if (pos == -1) {
			// must also contain "</"
			return false;
		}
		// test the portion of the string that contains the tag type ("</type>")
		return NON_INLINE_TAGS.containsKey(tagText.substring(pos + 2, tagText.length() - 1));
	}

	/**
	 *
	 */
	private boolean isNonInlineTagStart(String tagText) {
		// this method is frequently invoked by the parser, so minimize the performance
		// overhead as much as possible.
		if (tagText.length() < 3) {
			// minimum length string is three characters ("<a>")
			return false;
		}
		if (tagText.charAt(0) != '<') {
			// must start with "<"
			return false;
		}
		int pos = StringUtils.indexOfAny(tagText, ' ', '>');
		if (pos <= 1) {
			return false;
		}
		// test the portion of the string that contains the tag type ("<type>" or "<type key=value>")
		return NON_INLINE_TAGS.containsKey(tagText.substring(1, pos));
	}

	/**
	 * Determine if the tag is a table tag.
	 */
	protected boolean isTableTag() {
		return (TABLE_TAGS.containsKey(this.tagType));
	}

	/**
	 * Determine whether the tag allows text body content.  Some tags, such
	 * as "table", allow only tag content and no text content.
	 */
	private boolean isTextBodyTag() {
		if (this.isRootTag()) {
			return true;
		}
		return (!NON_TEXT_BODY_TAGS.containsKey(this.tagType));
	}

	/**
	 * Evaluate the tag to determine whether it is the parser root tag
	 * that indicates the bottom of the parser tag stack.
	 */
	protected boolean isRootTag() {
		return this.tagType.equals(JFlexTagItem.ROOT_TAG);
	}

	/**
	 *
	 */
	protected CharSequence toHtml() {
		// root tag just returns its contents
		if (this.isRootTag()) {
			return this.tagContent;
		}
		// handle empty tags
		if (StringUtils.isBlank(this.tagContent)) {
			if (!this.isEmptyBodyTag()) {
				// if no content do not generate a tag
				return "";
			} else if (this.htmlTagItem != null) {
				// generate an empty tag
				return this.htmlTagItem.toHtml();
			}
		}
		StringBuilder result = new StringBuilder();
		if (this.htmlTagItem != null) {
			result.append(this.htmlTagItem.toHtml());
		} else {
			result.append('<').append(this.tagType).append('>');
		}
		if (this.tagType.equals("pre")) {
			// pre-formatted, no trimming but make sure the open and close tags appear on their own lines
			if (this.tagContent.charAt(0) != '\n') {
				result.append('\n');
			}
			result.append(this.tagContent);
			if (this.tagContent.charAt(this.tagContent.length() - 1) != '\n') {
				result.append('\n');
			}
			result.append("</").append(this.tagType).append('>');
		} else if (this.isTextBodyTag()) {
			String trimmedTagContent = this.tagContent.toString().trim();
			// ugly hack to handle cases such as "<li><ul>" where the "<ul>" should be on its own line
			if (this.isNonInlineTagStart(trimmedTagContent)) {
				result.append('\n');
			}
			result.append(trimmedTagContent);
			// ugly hack to handle cases such as "</ul></li>" where the "</li>" should be on its own line
			if (this.isNonInlineTagEnd(trimmedTagContent)) {
				result.append('\n');
			}
			result.append("</").append(this.tagType).append('>');
			if (this.isInlineTag() && !this.tagType.equals("script") && this.tagContent.length() != trimmedTagContent.length()) {
				// work around issues such as "text''' text'''", where the output should
				// be "text <b>text</b>", by moving the whitespace to the parent tag
				int firstWhitespaceIndex = this.tagContent.indexOf(trimmedTagContent);
				if (firstWhitespaceIndex > 0) {
					result.insert(0, this.tagContent.substring(0, firstWhitespaceIndex));
				}
				int lastWhitespaceIndex = firstWhitespaceIndex + trimmedTagContent.length();
				if (lastWhitespaceIndex > this.tagContent.length()) {
					result.append(this.tagContent.substring(lastWhitespaceIndex));
				}
			}
		} else {
			result.append('\n');
			result.append(this.tagContent.toString().trim());
			result.append('\n');
			result.append("</").append(this.tagType).append('>');
		}
		return result;
	}
}
