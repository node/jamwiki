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

import org.apache.commons.lang3.StringEscapeUtils;
import org.jamwiki.parser.ParserException;
import org.jamwiki.utils.WikiLogger;

/**
 * This class handles the internal-use-only <__NOPARSE></__NOPARSE> directive, which
 * instructs the parser to ignore any content between the tags.  This directive can
 * not be included in wiki syntax directly, instead it may be added as part of the
 * parsing process by other parser tags to allow for syntax that should not be
 * modified during other phases of the parser, such as when using a custom tag to
 * add HTML that would otherwise be escaped by the parser.
 *
 * NOTE: since content enclosed within a NOPARSE tag will not be further
 * processed be aware that normal paragraph parsing and whitespace handling will
 * not apply to the tag or its contents.  Thus, while two newlines might normally
 * cause a new paragraph to begin, when using a NOPARSE tag that may not be the
 * case.
 */
public class NoParseDirectiveTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(NoParseDirectiveTag.class.getName());
	/**
	 * Parser directive that can be used in custom tag output when the generated
	 * custom tag content contains HTML or other content that would not normally
	 * be permitted by the parser.  Example: "<__NOPARSE><script></script></__NOPARSE>".
	 */
	protected static final String NOPARSE_DIRECTIVE = "__NOPARSE";
	/** Open tag for the NOPARSE directive. */
	protected static final String NOPARSE_DIRECTIVE_OPEN = "<" + NOPARSE_DIRECTIVE + ">";
	/** Close tag for the NOPARSE directive. */
	protected static final String NOPARSE_DIRECTIVE_CLOSE = "</" + NOPARSE_DIRECTIVE + ">";

	/**
	 * Parse a call to a Mediawiki NOPARSE directive of the form
	 * "<__NOPARSE>text</__NOPARSE>" and return the resulting output.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
		if (lexer.getMode() < JFlexParser.MODE_CUSTOM) {
			// not allowed at this stage of parsing - probably came from end user
			return StringEscapeUtils.escapeHtml4(raw);
		}
		if (lexer.getMode() >= JFlexParser.MODE_POSTPROCESS) {
			// during the final stage of parsing remove the tags and return the content
			return JFlexParserUtil.tagContent(raw);
		}
		if (lexer instanceof JAMWikiLexer) {
			// during main processing handle this as a push and a pop to allow
			// paragraphs to be processed correctly.
			JAMWikiLexer jamwikiLexer = (JAMWikiLexer)lexer;
			jamwikiLexer.pushTag(new JFlexTagItem(NOPARSE_DIRECTIVE));
			jamwikiLexer.peekTag().getTagContent().append(JFlexParserUtil.tagContent(raw));
			jamwikiLexer.popTag(NOPARSE_DIRECTIVE);
			return "";
		}
		// at all other times leave the content unmodified.
		return raw;
	}
}
