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
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.model.WikiReference;
import org.jamwiki.parser.ParserException;
import org.jamwiki.utils.WikiLogger;

/**
 * This class parses nowiki tags of the form <code>&lt;references /&gt;</code>.
 */
public class WikiReferencesTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiReferencesTag.class.getName());

	/**
	 *
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
		if (logger.isTraceEnabled()) {
			logger.trace("references: " + raw + " (" + lexer.yystate() + ")");
		}
		if (lexer.getMode() < JFlexParser.MODE_POSTPROCESS) {
			// return static text so that this is standardized for further
			// parsing
			return "<references />";
		}
		// retrieve all references, then loop through in order, building an HTML
		// reference list for display.  While looping, if there are multiple citations
		// for the same reference then include those in the output as well.
		List<WikiReference> references = JFlexParserUtil.retrieveReferences(lexer.getParserInput());
		if (references.isEmpty()) {
			return "";
		}
		StringBuffer html = new StringBuffer("<ol class=\"references\">");
		while (!references.isEmpty()) {
			WikiReference reference = references.get(0);
			references.remove(0);
			html.append("\n<li id=\"").append(reference.getNotationName()).append("\">");
			html.append("<sup>");
			int pos = 0;
			List<WikiReference> citations = new ArrayList<WikiReference>();
			while (pos < references.size()) {
				WikiReference temp = references.get(pos);
				if (temp.getName() != null && reference.getName() != null && reference.getName().equals(temp.getName())) {
					citations.add(temp);
					if (StringUtils.isBlank(reference.getContent()) && !StringUtils.isBlank(temp.getContent())) {
						reference.setContent(temp.getContent());
					}
					references.remove(pos);
					continue;
				}
				pos++;
			}
			if (!citations.isEmpty()) {
				html.append("<a href=\"#").append(reference.getReferenceName()).append("\">");
				html.append(reference.getCitation()).append('.').append(reference.getCount()).append("</a>&#160;");
				while (!citations.isEmpty()) {
					WikiReference citation = citations.get(0);
					html.append("&#160;<a href=\"#").append(citation.getReferenceName()).append("\">");
					html.append(citation.getCitation()).append('.').append(citation.getCount()).append("</a>&#160;");
					citations.remove(0);
				}
			} else {
				html.append("<a href=\"#").append(reference.getReferenceName()).append("\">");
				html.append(reference.getCitation()).append("</a>&#160;");
			}
			html.append("</sup>");
			html.append(JFlexParserUtil.parseFragment(lexer.getParserInput(), lexer.getParserOutput(), reference.getContent(), JFlexParser.MODE_PROCESS));
			html.append("</li>");
		}
		html.append("\n</ol>");
		return html.toString();
	}
}
