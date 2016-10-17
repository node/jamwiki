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
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class that is extended by the main JFlex lexer.
 */
public abstract class AbstractJAMWikiLexer extends JFlexLexer {

	protected static final WikiLogger logger = WikiLogger.getLogger(AbstractJAMWikiLexer.class.getName());
	/**
	 * Automatically insert a paragraph tag after all block-level tags EXCEPT
	 * for paragraph tags (since that is handled by the parser).  This is
	 * a hack-ish workaround for the fact that Mediawiki syntax makes it very
	 * difficult to determine when a new paragraph is needed, so this code
	 * is overzealous with paragraph insertion and empty paragraph tags are
	 * resolved to empty text by the JFlexTagItem.toHtml() method.
	 */
	private static final Map<String, String> PARAGRAPH_OPEN_LOCATION_LIST = Utilities.initializeLookupMap("blockquote", "center", "div", "dl", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "ol", "pre", "table", "ul");
	/** Stack of currently parsed tag content. */
	private final Stack<JFlexTagItem> tagStack = new Stack<JFlexTagItem>();

	/**
	 * Append content to the current tag in the tag stack.
	 */
	protected void append(String content) {
		this.peekTag().getTagContent().append(content);
	}

	/**
	 * Convert a wiki list character to an HTML list item type.  For example,
	 * "#" corresponds to "li" while ":" corresponds to "dd".
	 */
	private String calculateListItemType(char wikiSyntax) {
		if (wikiSyntax == '*' || wikiSyntax == '#') {
			return "li";
		}
		if (wikiSyntax == ';') {
			return "dt";
		}
		if (wikiSyntax == ':') {
			return "dd";
		}
		throw new IllegalArgumentException("Unrecognized wiki syntax: " + wikiSyntax);
	}

	/**
	 * Convert a wiki list character to an HTML list type.  For example,
	 * "#" corresponds to "ol".
	 */
	private String calculateListType(char wikiSyntax) {
		if (wikiSyntax == ';' || wikiSyntax == ':') {
			return "dl";
		}
		if (wikiSyntax == '#') {
			return "ol";
		}
		if (wikiSyntax == '*') {
			return "ul";
		}
		throw new IllegalArgumentException("Unrecognized wiki syntax: " + wikiSyntax);
	}

	/**
	 * Utility method used when parsing list tags to determine the current
	 * list nesting level.
	 */
	private int currentListDepth() {
		int depth = 0;
		int i = 0;
		// traverse backwards through the tag stack looking for list tags
		while (this.peekTag(++i) != null) {
			if (this.peekTag(i).isEmptyParagraphTag()) {
				// ignore paragraphs
				continue;
			}
			if (!this.peekTag(i).isListTag()) {
				break;
			}
			depth++;
		}
		// divide by two since lists always have a list and a list item tag
		return (depth / 2);
	}

	/**
	 * Override the parent method to allow tag stack initialization.
	 */
	protected void init(ParserInput parserInput, ParserOutput parserOutput, int mode) {
		super.init(parserInput, parserOutput, mode);
		this.tagStack.push(new JFlexTagItem(JFlexTagItem.ROOT_TAG));
	}

	/**
	 * Utility method to walk the current tag stack to determine if the top of the stack
	 * contains list tags followed by a tag of a specific type.
	 */
	private boolean isNextAfterListTags(String tagType) {
		int i = 0;
		while (this.peekTag(++i) != null) {
			if (this.peekTag(i).getTagType().equals(tagType)) {
				return true;
			}
			if (!this.peekTag(i).isListTag()) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Utility method to determine if the parser is at the start of the file.
	 */
	protected boolean isStartOfFile() {
		if (this.peekTag().getTagContent().length() != 0) {
			// if there is content already then this is not the start of the file
			return false;
		}
		if (this.peekTag().isRootTag()) {
			// if this is the root tag and it's empty, this is the start of the file
			return true;
		}
		if (this.tagStack.size() == 2 && this.tagStack.get(0).getTagContent().length() == 0 && this.peekTag().isEmptyParagraphTag()) {
			// if this is the paragraph tag that is pre-pended prior to parsing
			// and the root tag is empty, this is the start of the file
			return true;
		}
		return false;
	}

	/**
	 * Execute the lexer, returning the parsed content.  Override the parent
	 * method to use the tag stack.
	 */
	protected String lex() throws Exception {
		String line;
		if (this.mode == JFlexParser.MODE_LAYOUT) {
			// push a paragraph at start of lexing - if it turns out that an
			// opening paragraph is not needed then it will resolve to empty
			// text via JFlexTagItem.toHtml().
			this.pushTag("p", null);
		}
		// parse all text
		while ((line = this.yylex()) != null) {
			this.append(line);
		}
		// parse for any paragraph tags that might need closing at EOF
		if (this.paragraphIsOpen()) {
			this.parse(TAG_TYPE_PARAGRAPH, "\n");
		}
		return this.popAllTags();
	}

	/**
	 * Utility method to determine if a paragraph is currently open.
	 */
	protected boolean paragraphIsOpen() {
		// traverse backwards through the tag stack looking for an open paragraph tag
		int i = 0;
		while (this.peekTag(++i) != null) {
			if (this.peekTag(i).getTagType().equals("p")) {
				return true;
			}
			if (!this.peekTag(i).isInlineTag()) {
				// if a block tag is encountered there should be no
				// need to check further
				break;
			}
		}
		return false;
	}

	/**
	 * Take Wiki text of the form "|" or "| style='foo' |" and convert to
	 * and HTML <td> or <th> tag.
	 *
	 * @param text The text to be parsed.
	 * @param tagType The HTML tag type, either "td" or "th".
	 * @param markup The Wiki markup for the tag, either "|", "|+" or "!"
	 */
	protected void parseTableCell(String text, String tagType, String markup) throws ParserException {
		if (text == null) {
			throw new IllegalArgumentException("No text specified while parsing table cell");
		}
		text = text.trim();
		String openTagRaw = null;
		int pos = StringUtils.indexOfAnyBut(text, markup);
		if (pos != -1) {
			text = text.substring(pos);
			pos = text.indexOf('|');
			if (pos != -1) {
				text = text.substring(0, pos);
			}
			openTagRaw = "<" + tagType + " " + text.trim() + ">";
		}
		this.pushTag(tagType, openTagRaw);
	}

	/**
	 * Peek at the current tag from the lexer stack and see if it matches
	 * the given tag type.
	 */
	protected JFlexTagItem peekTag() {
		return this.tagStack.peek();
	}

	/**
	 * Peek at the current tag from the lexer stack and see if it matches
	 * the given tag type.
	 *
	 * @param pos Depth of the tag stack to peek.  If looking at the last tag
	 *  then specify one, the second-to-last-tag specify two, etc.
	 * @return The tag as the specified position in the stack, or <code>null</code>
	 *  if the stack is smaller than the specified index.
	 */
	protected JFlexTagItem peekTag(int pos) {
		if (pos < 1) {
			throw new IllegalArgumentException("Cannot call peekTag with an index less than one");
		}
		if (pos > this.tagStack.size()) {
			return null;
		}
		return this.tagStack.get(this.tagStack.size() - pos);
	}

	/**
	 * Pop all list tags off of the current stack.
	 */
	protected void popAllListTags() throws ParserException {
		// before clearing a list, first make sure that any open inline tags or paragraph tags
		// have been closed (example: "<i><ul>" is invalid.  close the <i> first).
		while (!this.peekTag().isRootTag() && (this.peekTag().getTagType().equals("p") || this.peekTag().isInlineTag())) {
			this.popTag(this.peekTag().getTagType());
		}
		this.popListTags(this.currentListDepth());
	}

	/**
	 * Pop all tags off of the stack and return a string representation.
	 */
	private String popAllTags() throws ParserException {
		// pop the stack down to (but not including) the root tag
		while (this.tagStack.size() > 1) {
			this.popTag(this.peekTag().getTagType());
		}
		// now pop the root tag
		if (this.mode >= JFlexParser.MODE_LAYOUT) {
			return this.tagStack.pop().toHtml().toString().trim();
		} else {
			return this.tagStack.pop().toHtml().toString();
		}
	}

	/**
	 * Pop the next X number of list tags off of the current tag stack.
	 */
	private void popListTags(int depth) throws ParserException {
		if (depth < 0) {
			throw new IllegalArgumentException("Cannot pop a negative number: " + depth);
		}
		for (int i = 0; i < depth; i++) {
			// the open tag stack will be of the form <ol><li><p>, so pop all of them
			if (this.peekTag().getTagType().equals("p")) {
				this.popTag(this.peekTag().getTagType());
			}
			this.popTag(this.peekTag().getTagType());
			this.popTag(this.peekTag().getTagType());
		}
	}

	/**
	 * Handle popping a tag that is not the current tag on the stack.
	 */
	private void popMismatchedTag(String tagType) throws ParserException {
		// check to see if a close tag override was previously set, which happens
		// from the inner tag of unbalanced HTML.  Example: "<u><strong>text</u></strong>"
		// would set a close tag override when the "</u>" is parsed to indicate that
		// the "</strong>" should actually be parsed as a "</u>".
		if (StringUtils.equals(this.peekTag().getTagType(), this.peekTag().getCloseTagOverride())) {
			this.popTag(this.peekTag().getCloseTagOverride());
			return;
		}
		// if the open tag is a paragraph then close it - paragraph tags are added in
		// many places where they might not need to be since the parser is trying to
		// guess what a newline is supposed to mean.
		if (this.peekTag().getTagType().equals("p")) {
			this.popTag("p");
			this.popTag(tagType);
			return;
		}
		// check to see if the parent tag is a list and the current tag is in the tag
		// stack.  if so close the list and pop the current tag.
		if (!JFlexTagItem.isListTag(tagType) && this.peekTag().isListItemTag() && this.isNextAfterListTags(tagType)) {
			this.popAllListTags();
			this.popTag(tagType);
			return;
		}
		// check to see if the parent tag matches the current close tag.  if so then
		// this is unbalanced HTML of the form "<u><strong>text</u></strong>" and
		// it should be parsed as "<u><strong>text</strong></u>".
		JFlexTagItem parent = this.peekTag(2);
		if (parent != null && parent.getTagType().equals(tagType)) {
			parent.setCloseTagOverride(tagType);
			this.popTag(this.peekTag().getTagType());
			return;
		}
		// if the above checks fail then this is an attempt to pop a tag that is not
		// currently open, so append the escaped close tag to the current tag
		// content without modifying the tag stack.
		this.peekTag().getTagContent().append("&lt;/" + tagType + "&gt;");
	}

	/**
	 * Pop the most recent HTML tag from the lexer stack.
	 */
	protected void popTag(String tagType) throws ParserException {
		if (this.tagStack.size() <= 1) {
			logger.warn("popTag called on an empty tag stack or on the root stack element.  Please report this error on jamwiki.org, and provide the wiki syntax for the topic being parsed.");
		}
		if (!this.peekTag().getTagType().equals(tagType)) {
			// handle the case where the tag being popped is not the current tag on
			// the stack.  this can happen with mis-matched HTML
			// ("<u><strong>text</u></strong>"), tags that aren't automatically
			// closed, and other more random scenarios.
			this.popMismatchedTag(tagType);
			return;
		}
		JFlexTagItem currentTag = this.peekTag();
		if (this.tagStack.size() > 1) {
			// only pop if not the root tag
			currentTag = this.tagStack.pop();
		}
		CharSequence html = currentTag.toHtml();
		if (StringUtils.isBlank(html)) {
			// if the tag results in no content being generated then there is
			// nothing more to do.
			return;
		}
		JFlexTagItem previousTag = this.peekTag();
		if (!currentTag.isInlineTag() || currentTag.getTagType().equals("pre")) {
			// if the current tag is not an inline tag, make sure it is on its own lines
			if (previousTag.getTagContent().length() > 0 && Character.isWhitespace(previousTag.getTagContent().charAt(previousTag.getTagContent().length() - 1))) {
				String trimmedContent = StringUtils.stripEnd(previousTag.getTagContent().toString(), null);
				previousTag.getTagContent().delete(trimmedContent.length(), previousTag.getTagContent().length());
			}
			previousTag.getTagContent().append('\n');
			previousTag.getTagContent().append(html);
			previousTag.getTagContent().append('\n');
		} else {
			previousTag.getTagContent().append(html);
		}
		if (PARAGRAPH_OPEN_LOCATION_LIST.containsKey(tagType)) {
			// force a paragraph open after block tags.  this tag may not actually
			// end up in the final output if there aren't any newlines in the
			// wikitext after the block tag.  make sure that PARAGRAPH_OPEN_LOCATION_LIST
			// does not contain "p", otherwise we loop infinitely.
			this.pushTag("p", null);
		}
	}

	/**
	 * Wiki lists are of the form ":#;" and depend on the previous list entries,
	 * so given the current tag stack and the wiki syntax for the current list
	 * syntax, update the tag stack accordingly.
	 */
	protected void processListStack(String wikiSyntax) throws ParserException {
		// before adding to a list, first make sure that any open inline tags or paragraph tags
		// have been closed (example: "<i><ul>" is invalid.  close the <i> first).
		while (!this.peekTag().isRootTag() && (this.peekTag().getTagType().equals("p") || this.peekTag().isInlineTag())) {
			this.popTag(this.peekTag().getTagType());
		}
		int previousDepth = this.currentListDepth();
		int currentDepth = wikiSyntax.length();
		// if list was previously open to a greater depth, close the old list down to the
		// current depth.
		int tagsToPop = (previousDepth - currentDepth);
		if (tagsToPop > 0) {
			this.popListTags(tagsToPop);
			previousDepth -= tagsToPop;
		}
		// now look for differences in the current list stacks.  for example, if
		// the previous list was "::;" and the current list is "###" then there are
		// three lists that must be closed.  first, walk back the current stack
		// to find the list open tags.
		List<String> listTagTypes = null;
		int j = 0;
		while (this.peekTag(++j) != null) {
			if (this.peekTag(j).isListTag() && !this.peekTag(j).isListItemTag()) {
				if (listTagTypes == null) {
					listTagTypes = new ArrayList<String>();
				}
				listTagTypes.add(this.peekTag(j).getTagType());
			}
		}
		// now verify whether the list open tags match the current syntax, ie
		// whether "::;" matches whatever tags are already open
		String tagType;
		for (int i = 1; i <= previousDepth; i++) {
			if (listTagTypes != null && (listTagTypes.size() - i) < 0) {
				logger.warn("processListStack has encountered an invalid list stack.  Please report this error on jamwiki.org, and provide the wiki syntax for the topic being parsed.");
				break;
			}
			tagType = listTagTypes.get(listTagTypes.size() - i);
			if (tagType.equals(this.calculateListType(wikiSyntax.charAt(i - 1)))) {
				continue;
			}
			// if the above test did not match, then the stack needs to be popped
			// to this point.
			tagsToPop = (previousDepth - (i - 1));
			this.popListTags(tagsToPop);
			previousDepth -= tagsToPop;
			break;
		}
		if (previousDepth == 0) {
			// if no list is open, open one
			this.pushTag(this.calculateListType(wikiSyntax.charAt(0)), null);
			// add the new list item to the stack
			this.pushTag(this.calculateListItemType(wikiSyntax.charAt(0)), null);
		} else if (previousDepth == currentDepth) {
			// pop the previous list item
			tagType = this.peekTag().getTagType();
			this.popTag(tagType);
			// add the new list item to the stack
			this.pushTag(this.calculateListItemType(wikiSyntax.charAt(previousDepth - 1)), null);
		}
		// if the new list has additional elements, push them onto the stack
		int counterStart = (previousDepth > 1) ? previousDepth : 1;
		String previousTagType;
		for (int i = counterStart; i < wikiSyntax.length(); i++) {
			// handle a weird corner case.  if a "dt" is open and there are
			// sub-lists, close the dt and open a "dd" for the sub-list
			previousTagType = this.peekTag().getTagType();
			if (previousTagType.equals("p") && this.tagStack.size() > 2) {
				// ignore paragraph tags
				previousTagType = this.peekTag(2).getTagType();
			}
			if (previousTagType.equals("dt")) {
				this.popTag("dt");
				if (!this.calculateListType(wikiSyntax.charAt(i)).equals("dl")) {
					this.popTag("dl");
					this.pushTag("dl", null);
				}
				this.pushTag("dd", null);
			}
			// push the new list tag, and its tag item, onto the stack
			this.pushTag(this.calculateListType(wikiSyntax.charAt(i)), null);
			this.pushTag(this.calculateListItemType(wikiSyntax.charAt(i)), null);
		}
	}

	/**
	 * Make sure any open table tags that need to be closed are closed.
	 */
	protected void processTableStack() throws ParserException {
		// before updating the table make sure that any open inline tags or paragraph tags
		// have been closed (example: "<td><b></td>" won't work.
		while (!this.peekTag().isRootTag() && (this.peekTag().getTagType().equals("p") || this.peekTag().isInlineTag())) {
			this.popTag(this.peekTag().getTagType());
		}
		String previousTagType = this.peekTag().getTagType();
		if (!previousTagType.equals("caption") && !previousTagType.equals("th") && !previousTagType.equals("td")) {
			// no table cell was open, so nothing to close
			return;
		}
		// pop the previous tag
		this.popTag(previousTagType);
	}

	/**
	 * Push a new HTML tag onto the lexer stack.
	 */
	protected void pushTag(String tagType, String openTagRaw) throws ParserException {
		this.pushTag(new JFlexTagItem(tagType, openTagRaw));
	}

	/**
	 * Push a new HTML tag onto the lexer stack.
	 */
	protected void pushTag(JFlexTagItem tag) throws ParserException {
		if (!tag.isInlineTag() && this.peekTag().getTagType().equals("p")) {
			// make sure any open paragraph is closed before opening a block tag
			this.popTag("p");
		}
		// many HTML tags cannot nest (ie "<li><li></li></li>" is invalid), so if a non-nesting
		// tag is being added and the previous tag is of the same type, close the previous tag
		// note the special case for dt && dd tags
		if (tag.isNonNestingTag() && (this.peekTag().getTagType().equals(tag.getTagType()) || (tag.isListItemTag() && this.peekTag().isListItemTag()))) {
			this.popTag(this.peekTag().getTagType());
		}
		this.tagStack.push(tag);
	}
}
