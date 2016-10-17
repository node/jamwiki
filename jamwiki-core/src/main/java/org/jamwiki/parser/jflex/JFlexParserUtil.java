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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.WikiReference;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility methods used with the Mediawiki lexers.
 */
public class JFlexParserUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(JFlexParserUtil.class.getName());

	/**
	 *
	 */
	private JFlexParserUtil() {
	}

	/**
	 * Search through content, starting at a specific position, and search for the
	 * first position of a matching end tag for a specified start tag.  For instance,
	 * if called with a start tag of "<b>" and an end tag of "</b>", this method
	 * will operate as follows:
	 *
	 * "01<b>567</b>23" returns 8.
	 * "01<b>56<b>01</b>67</b>23" returns 18.
	 *
	 * @param content The string to be searched.
	 * @param start The position within the string to start searching from (inclusive).
	 *  Only characters after this position in the string will be examined.
	 * @param startToken The opening tag to match.
	 * @param endToken The closing tag to match.
	 * @return -1 if no matching end tag is found, or the index within the string of the first
	 *  character of the end tag.
	 */
	public static int findMatchingEndTag(CharSequence content, int start, String startToken, String endToken) {
		// do some initial searching to make sure the tokens are available
		if (content == null || start < 0 || start >= content.length()) {
			return -1;
		}
		String contentString = content.toString();
		int lastEndToken = contentString.lastIndexOf(endToken);
		if (lastEndToken == -1 || lastEndToken < start) {
			return -1;
		}
		int firstStartToken = contentString.indexOf(startToken, start);
		if (firstStartToken == -1) {
			return -1;
		}
		int pos = firstStartToken;
		int count = 0;
		int nextStart = firstStartToken;
		int nextEnd = lastEndToken;
		// search for matches within the area that tokens have already been found
		while (pos >= firstStartToken && pos < (lastEndToken + endToken.length())) {
			if (nextStart != -1 && nextStart < nextEnd) {
				// cursor is currently at the match of a start token
				count++;
				pos += startToken.length();
			} else {
				// cursor is currently at the match of an end token
				count--;
				if (count == 0) {
					// this tag closes a match, return the position of the
					// start of the tag
					return pos;
				}
				pos += endToken.length();
			}
			// jump to the next start or end token
			nextEnd = contentString.indexOf(endToken, pos);
			if (nextEnd == -1) {
				// no more matching end patterns, no match
				break;
			}
			nextStart = contentString.indexOf(startToken, pos);
			pos = (nextStart == -1) ? nextEnd : Math.min(nextStart, nextEnd);
		}
		return -1;
	}

	/**
	 * Search through content, starting at a specific position, and search backwards for the
	 * first position of a matching start tag for a specified end tag.  For instance,
	 * if called with an end tag of "</b>" and a start tag of "<b>", this method
	 * will operate as follows:
	 *
	 * "01<b>567</b>23" returns 2.
	 * "01234567</b>23" returns -1.
	 *
	 * @param content The string to be searched.
	 * @param start The position within the string to start searching from (inclusive).
	 *  Only characters before this position in the string will be examined.
	 * @param startToken The opening tag to match.
	 * @param endToken The closing tag to match.
	 * @return -1 if no matching start tag is found, or the index within the string of the first
	 *  character of the start tag.
	 */
	public static int findMatchingStartTag(CharSequence content, int start, String startToken, String endToken) {
		// do some initial searching to make sure the tokens are available
		if (content == null || start < 0 || start >= content.length()) {
			return -1;
		}
		int firstStartToken = StringUtils.indexOf(content, startToken);
		if (firstStartToken == -1 || firstStartToken > start) {
			return -1;
		}
		int lastEndToken = StringUtils.lastIndexOf(content, endToken, start);
		if (lastEndToken == -1) {
			return -1;
		}
		int pos = start;
		if (pos >= (lastEndToken + endToken.length())) {
			pos = lastEndToken + endToken.length() - 1;
		}
		int count = 0;
		String contentString = content.toString();
		String substring;
		// search for matches within the area that tokens have already been found
		while (pos >= firstStartToken && pos < (lastEndToken + endToken.length())) {
			substring = contentString.substring(0, pos + 1);
			// search for matches from end-to-beginning
			if (substring.endsWith(endToken)) {
				count++;
				pos -= endToken.length();
			} else if (substring.endsWith(startToken)) {
				count--;
				if (count == 0) {
					// this tag opens a match, return the position of the
					// start of the tag
					return (pos - startToken.length()) + 1;
				}
				pos -= startToken.length();
			} else {
				pos--;
			}
		}
		return -1;
	}

	/**
	 * Given a string, determine if it is a valid HTML entity (such as &trade; or
	 * &#160;).
	 *
	 * @param text The text that is being examined.
	 * @return <code>true</code> if the text is a valid HTML entity.
	 */
	public static boolean isHtmlEntity(String text) {
		if (text == null) {
			return false;
		}
		// see if it was successfully converted, in which case it is an entity
		try {
			return (!text.equals(StringEscapeUtils.unescapeHtml4(text)));
		} catch (IllegalArgumentException e) {
			// "&#xffffff;" seems to be throwing errors
			return false;
		}
	}

	/**
	 * Utility method for returning the submitted content enclosed by NOPARSE
	 * directives.  This method should be used only be internal parser code
	 * when parsing to indicate that content should not be further modified
	 * by the parser, such as when using a custom tag to add HTML that would
	 * otherwise be escaped by the parser.
	 *
	 * @see NoParseDirectiveTag
	 */
	public static String formatAsNoParse(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		return NoParseDirectiveTag.NOPARSE_DIRECTIVE_OPEN + content + NoParseDirectiveTag.NOPARSE_DIRECTIVE_CLOSE;
	}

	/**
	 * Provide a way to run the pre-processor against a fragment of text, such
	 * as an image caption.  This method should be used sparingly since it is
	 * not very efficient.
	 */
	public static String parseFragment(ParserInput parserInput, ParserOutput parserOutput, String raw, int mode) throws ParserException {
		if (StringUtils.isBlank(raw)) {
			return raw;
		}
		return WikiBase.getParserInstance().parseFragment(parserInput, parserOutput, raw, mode);
	}

	/**
	 * Provide a way to run the pre-processor against a fragment of text, such
	 * as an image caption.  This method differs from @link{#parseFragment} in
	 * that content that would normally be parsed as a beginning-of-line
	 * expression such as wikipre or lists ("* item") will be treated as if
	 * they are not beginning of line elements.
	 */
	public static String parseFragmentNonLineStart(ParserInput parserInput, ParserOutput parserOutput, String raw, int mode) throws ParserException {
		if (raw == null) {
			return raw;
		}
		// this is a hack.  pre-pend a character that will be stripped out
		// prior to returning so that text like "#if" won't be treated as if
		// it was its own line and parsed as a list.
		raw = "x" + raw.trim();
		return JFlexParserUtil.parseFragment(parserInput, parserOutput, raw, mode).substring(1);
	}

	/**
	 * Parse a raw Wiki link of the form "[[link|text]]", and return a WikiLink
	 * object representing the link.
	 *
	 * @param parserInput Input configuration settings for this parser instance.
	 * @param raw The raw Wiki link text.
	 * @return A WikiLink object that represents the link.
	 */
	protected static WikiLink parseWikiLink(ParserInput parserInput, ParserOutput parserOutput, String raw) throws ParserException {
		String virtualWiki = parserInput.getVirtualWiki();
		if (StringUtils.isBlank(raw)) {
			throw new IllegalArgumentException("Cannot call JFlexParserUtil.parseWikiLink() without text to parse.");
		}
		raw = raw.trim();
		String suffix = ((!raw.endsWith("]]")) ? raw.substring(raw.lastIndexOf("]]") + 2) : null);
		// for performance reasons use String methods rather than regex
		// private static final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[\\s*(\\:\\s*)?\\s*(.+?)(\\s*\\|\\s*(.+))?\\s*\\]\\]([a-z]*)");
		raw = raw.substring(raw.indexOf("[[") + 2, raw.lastIndexOf("]]")).trim();
		// parse in case there is a template or magic word - [[{{PAGENAME}}]]
		raw = JFlexParserUtil.parseFragment(parserInput, parserOutput, raw, JFlexParser.MODE_TEMPLATE);
		boolean colon = false;
		if (raw.startsWith(":")) {
			colon = true;
			raw = raw.substring(1).trim();
		}
		String text = null;
		int pos = raw.indexOf('|');
		if (pos != -1 && pos != (raw.length() - 1)) {
			text = raw.substring(pos + 1).trim();
			raw = raw.substring(0, pos).trim();
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(parserInput.getContext(), virtualWiki, raw);
		if (!colon && wikiLink.getNamespace().getId().equals(Namespace.CATEGORY_ID)) {
			// do not set default text for categories
			wikiLink.setText(null);
		}
		if (wikiLink.getAltVirtualWiki() != null && !StringUtils.equals(wikiLink.getAltVirtualWiki().getName(), virtualWiki) && StringUtils.isBlank(wikiLink.getDestination())) {
			// use the root topic name as the destination
			wikiLink.setDestination(wikiLink.getAltVirtualWiki().getRootTopicName());
			if (StringUtils.isBlank(wikiLink.getText())) {
				wikiLink.setText(wikiLink.getAltVirtualWiki().getName() + Namespace.SEPARATOR);
			}
		}
		if (wikiLink.getInterwiki() != null && StringUtils.isBlank(wikiLink.getDestination()) && StringUtils.isBlank(wikiLink.getText())) {
			wikiLink.setText(wikiLink.getInterwiki().getInterwikiPrefix() + Namespace.SEPARATOR);
		}
		wikiLink.setColon(colon);
		if (text != null) {
			wikiLink.setText(text);
		}
		if (!StringUtils.isBlank(suffix)) {
			wikiLink.setText(wikiLink.getText() + suffix);
		}
		return wikiLink;
	}

	/**
	 * Parse arguments of the form "arg1|arg2", trimming excess whitespace, handling
	 * arguments of the form "arg1|{{{arg2|arg3}}}|arg4", and returning an array of
	 * results.
	 */
	protected static String[] retrieveTokenizedArgumentArray(ParserInput parserInput, ParserOutput parserOutput, int mode, String argumentTokenString) throws ParserException {
		if (StringUtils.isBlank(argumentTokenString)) {
			return new String[0];
		}
		List<String> parserFunctionArgumentList = JFlexParserUtil.tokenizeParamString(argumentTokenString);
		String[] parserFunctionArgumentArray = new String[parserFunctionArgumentList.size()];
		// trim results and store in array
		int i = 0;
		for (String argument : parserFunctionArgumentList) {
			parserFunctionArgumentArray[i++] = JFlexParserUtil.parseFragment(parserInput, parserOutput, argument.trim(), mode);
		}
		return parserFunctionArgumentArray;
	}

	/**
	 * Given a tag of the form "<tag>content</tag>", return all content between
	 * the tags.  Consider the following examples:
	 *
	 * "<tag>content</tag>" returns "content".
	 * "<tag />" returns and empty string.
	 * "<tag><sub>content</sub></tag>" returns "<sub>content</sub>".
	 *
	 * @param raw The raw tag content to be analyzed.
	 * @return The content for the tag being analyzed.
	 */
	protected static String tagContent(String raw) {
		int start = raw.indexOf('>') + 1;
		int end = raw.lastIndexOf('<');
		if (start == 0) {
			// no tags
			return raw;
		}
		if (end <= start) {
			return "";
		}
		return raw.substring(start, end);
	}

	/**
	 * During parsing the reference objects will be stored as a temporary array.  This method
	 * parses that array and returns the reference objects.
	 *
	 * @param parserInput The current ParserInput object for the topic that is being parsed.
	 * @return A list of reference objects (never <code>null</code>) for the current topic that
	 *  is being parsed.
	 */
	protected static List<WikiReference> retrieveReferences(ParserInput parserInput) {
		List<WikiReference> references = (List<WikiReference>)parserInput.getTempParam(WikiReferenceTag.REFERENCES_PARAM);
		if (references == null) {
			references = new ArrayList<WikiReference>();
			parserInput.addTempParam(WikiReferenceTag.REFERENCES_PARAM, references);
		}
		return references;
	}

	/**
	 * Parse an opening or closing HTML tag to validate attributes and make sure it is XHTML compliant.
	 *
	 * @param tag The HTML tag to be parsed.
	 * @return An HtmlTagItem containing the parsed content, or <code>null</code> if a
	 *  null or empty string is passed as the argument.
	 * @throws ParserException Thrown if any error occurs during parsing.
	 */
	public static HtmlTagItem sanitizeHtmlTag(String tag) throws ParserException {
		if (StringUtils.isBlank(tag)) {
			return null;
		}
		// strip any newlines from the tag
		tag = tag.replace('\n', ' ');
		JAMWikiHtmlTagLexer lexer = new JAMWikiHtmlTagLexer(new StringReader(tag));
		try {
			while (lexer.yylex() != null) {
				// there is no need to store the result since the HtmlTagItem that
				// is generated by the parser is the item of interest.
			}
		} catch (Exception e) {
			throw new ParserException("Failure while parsing: " + tag, e);
		}
		return lexer.getHtmlTagItem();
	}

	/**
	 * Strip all HTML tags from a string.  For example, "A <b>bold</b> word" will be
	 * returned as "A bold word".  This method treats an tags that are between brackets
	 * as HTML, whether it is valid HTML or not.
	 *
	 * @param value The value that will have HTML stripped from it.
	 * @return The value submitted to this method with all HTML tags removed from it.
	 */
	public static String stripMarkup(String value) {
		return StringUtils.trim(value.replaceAll("<[^>]+>", ""));
	}

	/**
	 * Parse a template string of the form "param1|param2|param3" into tokens
	 * (param1, param2, and param3 in the example), handling such cases as
	 * "param1|[[foo|bar]]|param3" correctly.
	 */
	protected static List<String> tokenizeParamString(String content) {
		List<String> tokens = new ArrayList<String>();
		if (content.indexOf('|') == -1) {
			// nothing to tokenize
			tokens.add(content);
			return tokens;
		}
		int pos = 0;
		int endPos = -1;
		int closeTagSize = 0;
		String substring;
		StringBuilder value = new StringBuilder(content.length());
		while (pos < content.length()) {
			substring = content.substring(pos);
			endPos = -1;
			closeTagSize = 2;
			if (substring.startsWith("{{{")) {
				// template parameter
				endPos = JFlexParserUtil.findMatchingEndTag(content, pos, "{{{", "}}}");
				closeTagSize = 3;
			} else if (substring.startsWith("{{")) {
				// template
				endPos = JFlexParserUtil.findMatchingEndTag(content, pos, "{{", "}}");
			} else if (substring.startsWith("[[")) {
				// link
				endPos = JFlexParserUtil.findMatchingEndTag(content, pos, "[[", "]]");
			} else if (substring.startsWith("{|")) {
				// table
				endPos = JFlexParserUtil.findMatchingEndTag(content, pos, "{|", "|}");
			} else if (content.charAt(pos) == '|') {
				// new token
				tokens.add(value.toString());
				value = new StringBuilder();
				pos++;
				continue;
			}
			if (endPos != -1) {
				value.append(content.substring(pos, endPos + closeTagSize));
				pos = endPos + closeTagSize;
			} else {
				value.append(content.charAt(pos));
				pos++;
			}
		}
		// add the last one
		tokens.add(value.toString());
		return tokens;
	}
}
