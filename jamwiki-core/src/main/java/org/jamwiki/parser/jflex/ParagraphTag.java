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
import org.jamwiki.parser.ParserException;
import org.jamwiki.utils.WikiLogger;

/**
 * This class handles opening and closing of paragraphs.
 */
public class ParagraphTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(ParagraphTag.class.getName());

	/**
	 * Parse a pargraph open or close tag, which will generally consist of one or
	 * more newlines.  In some cases this method may be invoked where a paragraph
	 * is not needed, in which case no paragraph tag will be opened or closed.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
		if (lexer.getMode() < JFlexParser.MODE_LAYOUT) {
			return raw;
		}
		if (!(lexer instanceof JAMWikiLexer)) {
			logger.warn("ParagraphTag can only be invoked with an instance of the JAMWikiLexer - there is a bug in the code.");
			return raw;
		}
		JAMWikiLexer jamwikiLexer = (JAMWikiLexer)lexer;
		if (jamwikiLexer.peekTag().isListTag()) {
			// this should never happen for a wiki list, but if parsing an HTML
			// list do not perform any paragraph parsing within the list, just
			// convert newlines to a single space.
			return " ";
		}
		if (jamwikiLexer.peekTag().isTableTag() && !jamwikiLexer.peekTag().getTagType().equals("td") && !jamwikiLexer.peekTag().getTagType().equals("th")) {
			// this should never happen for a wiki table, but if parsing an HTML
			// table do not perform any paragraph parsing within the table, just
			// convert newlines to a single space.
			return " ";
		}
		// raw will be null only when forcing a paragraph close
		int newlineCount = (raw == null) ? 2 : this.paragraphNewlineCount(jamwikiLexer, raw);
		if (newlineCount == 1 && jamwikiLexer.paragraphIsOpen()) {
			// a single newline within a paragraph should just be converted to a space
			jamwikiLexer.append(" ");
			return "";
		}
		// pop the stack to find either the open paragraph tag OR the point where an
		// open paragraph tag needs to be inserted.
		while (!jamwikiLexer.peekTag().isRootTag() && jamwikiLexer.peekTag().isInlineTag()) {
			// this is content that belongs within the paragraph so pop it and
			// continue searching
			jamwikiLexer.popTag(jamwikiLexer.peekTag().getTagType());
		}
		if (!jamwikiLexer.paragraphIsOpen()) {
			jamwikiLexer.pushTag("p", null);
		}
		if (newlineCount >= 3) {
			// a minimum of three newlines is necessary to trigger an empty paragraph,
			// and every two newlines thereafter triggers another one.
			for (int i = 3; i <= newlineCount; i += 2) {
				jamwikiLexer.pushTag("p", null);
				jamwikiLexer.append("<br />\n");
				if (i == newlineCount - 1) {
					// if this is the last iteration but there are an even
					// number of newlines close out the current paragraph
					// and start a new (empty) paragraph
					jamwikiLexer.pushTag("p", null);
				}
			}
		} else if (newlineCount == 1 && jamwikiLexer.paragraphIsOpen()) {
			// a single newline within a paragraph should be converted to a space
			jamwikiLexer.append(" ");
		} else {
			jamwikiLexer.pushTag("p", null);
		}
		return "";
	}

	/**
	 * Paragraph syntax is dependent on newlines, so determine how many
	 * newlines to count in the current raw text for paragraph parsing
	 * purposes.
	 */
	private int paragraphNewlineCount(JAMWikiLexer jamwikiLexer, String raw) {
		int newlineCount = 0;
		if (raw != null) {
			if (jamwikiLexer.isStartOfFile()) {
				// if the topic starts with blank lines that weren't automatically
				// trimmed then treat the file open as a blank line.
				newlineCount += 2;
			}
			newlineCount += StringUtils.countMatches(raw, "\n");
		}
		return newlineCount;
	}
}
