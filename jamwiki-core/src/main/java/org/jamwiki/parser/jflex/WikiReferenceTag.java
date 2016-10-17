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

import java.util.List;
import org.jamwiki.model.WikiReference;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.utils.WikiLogger;

/**
 * This class parses nowiki tags of the form <code>&lt;ref name="name"&gt;content&lt;/ref&gt;</code>.
 */
public class WikiReferenceTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiReferenceTag.class.getName());
	protected static final String REFERENCES_PARAM = "WikiReferenceTag.REFERENCES_PARAM";

	/**
	 *
	 */
	private WikiReference buildReference(ParserInput parserInput, String raw) {
		String name = buildReferenceName(raw);
		String content = JFlexParserUtil.tagContent(raw);
		List<WikiReference> references = JFlexParserUtil.retrieveReferences(parserInput);
		int count = 0;
		int citation = 1;
		for (WikiReference temp : references) {
			// loop through existing attributes to determine max citation number,
			// or if a named reference the citation and count number
			if (temp.getName() != null && name != null && name.equals(temp.getName())) {
				count++;
				citation = temp.getCitation();
			}
			if (count == 0 && citation <= temp.getCitation()) {
				citation = temp.getCitation() + 1;
			}
		}
		WikiReference reference = new WikiReference(name, content, citation, count);
		return reference;
	}

	/**
	 *
	 */
	private String buildReferenceName(String raw) {
		return this.tagAttribute(raw, "name");
	}

	/**
	 *
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) {
		if (logger.isTraceEnabled()) {
			logger.trace("reference: " + raw + " (" + lexer.yystate() + ")");
		}
		if (lexer.getMode() < JFlexParser.MODE_PROCESS) {
			return raw;
		}
		WikiReference reference = buildReference(lexer.getParserInput(), raw);
		this.processMetadata(lexer.getParserInput(), reference);
		StringBuilder html = new StringBuilder();
		html.append("<sup id=\"");
		html.append(reference.getReferenceName());
		html.append("\" class=\"reference\"><a href=\"#");
		html.append(reference.getNotationName());
		html.append("\">[" + reference.getCitation() + "]</a></sup>");
		return html.toString();
	}

	/**
	 *
	 */
	private void processMetadata(ParserInput parserInput, WikiReference reference) {
		// FIXME - why is a local variable stored here but never used ???
		List<WikiReference> references = JFlexParserUtil.retrieveReferences(parserInput);
		references.add(reference);
	}

	/**
	 *
	 */
	// FIXME - this needs to be a general utility method
	private String tagAttribute(String raw, String name) {
		int pos = raw.indexOf('>');
		if (pos == -1) {
			return null;
		}
		pos = raw.toLowerCase().indexOf(name.toLowerCase());
		if (pos == -1) {
			return null;
		}
		int start = raw.indexOf('\"', pos);
		if (start == -1 || (start + 1) >= raw.length()) {
			return null;
		}
		int end = raw.indexOf("\"", start + 1);
		if (end == -1 || end == (start + 1)) {
			return null;
		}
		return raw.substring(start + 1, end);
	}
}
