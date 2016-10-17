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

import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiException;
import org.jamwiki.model.Namespace;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * This class parses wiki links of the form <code>[[Topic to Link To|Link Text]]</code>.
 */
public class WikiLinkTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiLinkTag.class.getName());
	// temporary parameter passed to indicate that the fragment being parsed is a link caption
	protected static final String LINK_CAPTION = "link-caption";

	/**
	 * Determine if the link is valid or not.  An invalid link is one
	 * that contains an invalid character or has other issues.
	 */
	private boolean isValidLink(ParserInput parserInput, WikiLink wikiLink) {
		if (!StringUtils.isBlank(wikiLink.getDestination())) {
			try {
				LinkUtil.validateTopicName(wikiLink, true);
			} catch (WikiException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Parse a Mediawiki link of the form "[[topic|text]]" and return the
	 * resulting HTML output.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
		WikiLink wikiLink = JFlexParserUtil.parseWikiLink(lexer.getParserInput(), lexer.getParserOutput(), raw);
		if (wikiLink.getInterwiki() == null && StringUtils.isBlank(wikiLink.getDestination()) && StringUtils.isBlank(wikiLink.getSection())) {
			// no destination or section
			return raw;
		}
		if (lexer.getMode() == JFlexParser.MODE_EDIT_COMMENT) {
			return this.parseEditComment(lexer.getParserInput(), lexer.getParserOutput(), wikiLink, raw);
		}
		boolean containsNestedLinks = (args.length > 0 && StringUtils.equals(args[0].toString(), "nested"));
		if (containsNestedLinks) {
			// if there is a nested link it must be an image, otherwise the syntax is invalid.
			if (wikiLink.getColon() || !wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
				int start = raw.indexOf("[[");
				int end = raw.lastIndexOf("]]");
				String content = raw.substring(start + "[[".length(), end);
				return "[[" + JFlexParserUtil.parseFragmentNonLineStart(lexer.getParserInput(), lexer.getParserOutput(), content, lexer.getMode()) + "]]";
			}
		}
		if (!this.isValidLink(lexer.getParserInput(), wikiLink)) {
			// do not process the link if it's an invalid topic name
			return raw;
		}
		raw = this.processLinkMetadata(lexer.getParserInput(), lexer.getParserOutput(), lexer.getMode(), raw, wikiLink);
		if (lexer.getMode() <= JFlexParser.MODE_PREPROCESS) {
			// do not parse to HTML when in preprocess mode
			return raw;
		}
		if (!wikiLink.getColon() && wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
			// parse as an image
			return lexer.parse(JFlexLexer.TAG_TYPE_IMAGE_LINK, raw);
		}
		return this.parseWikiLink(lexer.getParserInput(), lexer.getParserOutput(), wikiLink, raw, lexer.getMode());
	}

	/**
	 *
	 */
	private String parseWikiLink(ParserInput parserInput, ParserOutput parserOutput, WikiLink wikiLink, String raw, int mode) throws ParserException {
		try {
			if (wikiLink.getInterwiki() != null) {
				// inter-wiki link
				if (mode != JFlexParser.MODE_EDIT_COMMENT && !wikiLink.getColon() && !Environment.getBooleanValue(Environment.PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE)) {
					wikiLink.setText(wikiLink.getInterwiki().getInterwikiDisplay());
					String url = LinkUtil.interwiki(wikiLink);
					parserOutput.addInterwikiLink(url);
					return "";
				} else {
					return LinkUtil.interwiki(wikiLink);
				}
			}
			String virtualWiki = parserInput.getVirtualWiki();
			if (wikiLink.getAltVirtualWiki() != null && !StringUtils.equals(wikiLink.getAltVirtualWiki().getName(), virtualWiki)) {
				// link to another virtual wiki
				virtualWiki = wikiLink.getAltVirtualWiki().getName();
				if (mode != JFlexParser.MODE_EDIT_COMMENT && !wikiLink.getColon() && !Environment.getBooleanValue(Environment.PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE)) {
					wikiLink.setText(wikiLink.getAltVirtualWiki().getName() + Namespace.SEPARATOR + wikiLink.getDestination());
					String url = LinkUtil.buildInternalLinkHtml(wikiLink, wikiLink.getText(), null, null, false);
					parserOutput.addVirtualWikiLink(url);
					return "";
				}
			}
			if (StringUtils.isBlank(wikiLink.getText()) && !StringUtils.isBlank(wikiLink.getDestination())) {
				wikiLink.setText(wikiLink.getDestination());
				if (!StringUtils.isBlank(wikiLink.getSection())) {
					wikiLink.setText(wikiLink.getText() + "#" + Utilities.decodeAndEscapeTopicName(wikiLink.getSection(), true));
				}
			} else if (StringUtils.isBlank(wikiLink.getText()) && !StringUtils.isBlank(wikiLink.getSection())) {
				wikiLink.setText(Utilities.decodeAndEscapeTopicName("#" + wikiLink.getSection(), true));
			} else if (mode != JFlexParser.MODE_EDIT_COMMENT) {
				// pass a parameter via the parserInput to prevent nested links from being generated
				parserInput.addTempParam(LINK_CAPTION, true);
				wikiLink.setText(JFlexParserUtil.parseFragmentNonLineStart(parserInput, parserOutput, wikiLink.getText(), mode));
				parserInput.removeTempParam(LINK_CAPTION);
			}
			if (StringUtils.equals(wikiLink.getDestination(), parserInput.getTopicName()) && StringUtils.equals(virtualWiki, parserInput.getVirtualWiki()) && StringUtils.isBlank(wikiLink.getSection())) {
				// same page, bold the text and return
				return "<b>" + (StringUtils.isBlank(wikiLink.getText()) ? wikiLink.getDestination() : wikiLink.getText()) + "</b>";
			}
			// do not escape text html - already done by parser
			return LinkUtil.buildInternalLinkHtml(wikiLink, wikiLink.getText(), null, null, false);
		} catch (DataAccessException e) {
			logger.error("Failure while parsing link " + raw, e);
			return "";
		} catch (ParserException e) {
			logger.error("Failure while parsing link " + raw, e);
			return "";
		}
	}

	/**
	 * Parse a wiki link for use as an edit comment, which means the available
	 * outputs are limited.
	 */
	public String parseEditComment(ParserInput parserInput, ParserOutput parserOutput, WikiLink wikiLink, String raw) throws ParserException {
		if (!this.isValidLink(parserInput, wikiLink)) {
			// do not process the link if it's an invalid topic name
			return raw;
		}
		return this.parseWikiLink(parserInput, parserOutput, wikiLink, raw, JFlexParser.MODE_EDIT_COMMENT);
	}

	/**
	 *
	 */
	private String processLinkMetadata(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw, WikiLink wikiLink) throws ParserException {
		if (wikiLink.getInterwiki() != null || (wikiLink.getAltVirtualWiki() != null && !StringUtils.equals(wikiLink.getAltVirtualWiki().getName(), parserInput.getVirtualWiki()))) {
			// no link metadata for interwiki or virtual wiki links
			return raw;
		}
		String result = raw;
		if (!wikiLink.getColon() && wikiLink.getNamespace().getId().equals(Namespace.CATEGORY_ID)) {
			String sortKey = wikiLink.getText();
			if (!StringUtils.isBlank(sortKey)) {
				sortKey = JFlexParserUtil.parseFragment(parserInput, parserOutput, sortKey, JFlexParser.MODE_PREPROCESS);
			}
			parserOutput.addCategory(wikiLink.getDestination(), sortKey);
			if (mode > JFlexParser.MODE_MINIMAL) {
				// keep the category around in minimal parsing mode, otherwise suppress it from the output
				result = "";
			}
		}
		if (wikiLink.getInterwiki() == null && (wikiLink.getAltVirtualWiki() == null || StringUtils.equals(wikiLink.getAltVirtualWiki().getName(), parserInput.getVirtualWiki())) && !StringUtils.isBlank(wikiLink.getDestination())) {
			parserOutput.addLink(wikiLink.getDestination());
		}
		return result;
	}
}
