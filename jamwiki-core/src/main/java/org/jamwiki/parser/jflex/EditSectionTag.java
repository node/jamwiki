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
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.WikiLogger;

/**
 * This class parses section links in edit comments of the form "/ * section * /".
 */
public class EditSectionTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(EditSectionTag.class.getName());
	private static final String CSS_SECTION_COMMENT = "section-link";

	/**
	 * Parse a section link of the form "/ * section * /" and return the
	 * resulting HTML output.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
		String sectionName = this.retrieveSectionName(raw);
		return this.parseSectionName(lexer.getParserInput(), sectionName);
	}

	/**
	 * Strip the section tags and return the section name.
	 */
	private String retrieveSectionName(String raw) {
		if (raw.length() <= 4) {
			// tag is of the form "/**/"
			return "";
		}
		// strip the opening and closing tags
		return raw.substring(2, raw.length() - 2);
	}

	/**
	 * Process the edit comment and return a parsed output string.
	 */
	private String parseSectionName(ParserInput parserInput, String sectionName) throws ParserException {
		if (StringUtils.isBlank(sectionName)) {
			return "";
		}
		sectionName = sectionName.trim();
		String virtualWiki = parserInput.getVirtualWiki();
		WikiLink wikiLink = new WikiLink(parserInput.getContext(), virtualWiki, parserInput.getTopicName());
		wikiLink.setSection(sectionName);
		StringBuilder result = new StringBuilder();
		result.append("<span class=\"").append(CSS_SECTION_COMMENT).append("\">");
		try {
			result.append(LinkUtil.buildInternalLinkHtml(wikiLink, "&rarr;", null, null, false));
		} catch (DataAccessException e) {
			logger.error("Failure while building section link for " + sectionName, e);
			throw new ParserException(e);
		}
		result.append(ParserUtil.parseEditComment(parserInput, sectionName));
		result.append("</span>");
		return result.toString();
	}
}