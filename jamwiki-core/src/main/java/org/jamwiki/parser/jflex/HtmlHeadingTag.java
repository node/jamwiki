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

import org.jamwiki.parser.ParserException;
import org.jamwiki.utils.WikiLogger;

/**
 * Handle HTML heading tags such as <h1>...</h1>.
 */
public class HtmlHeadingTag extends AbstractHeadingTag {

	private static final WikiLogger logger = WikiLogger.getLogger(HtmlHeadingTag.class.getName());

	/**
	 *
	 */
	protected int generateTagLevel(String raw, Object... args) throws ParserException {
		String openTag = this.generateTagOpen(raw, args);
		int tagLevelPos = openTag.toLowerCase().indexOf("h") + 1;
		return Integer.parseInt(openTag.substring(tagLevelPos, tagLevelPos + 1));
	}

	/**
	 *
	 */
	protected String generateTagOpen(String raw, Object... args) throws ParserException {
		int pos = raw.indexOf('>');
		String openTagRaw = raw.substring(0, pos + 1);
		return JFlexParserUtil.sanitizeHtmlTag(openTagRaw).toHtml();
	}

	/**
	 *
	 */
	protected String generateTagText(String raw, Object... args) throws ParserException {
		// get end of opening tag
		int pos = raw.indexOf('>');
		String tagText = raw.substring(pos + 1);
		// get start of closing tag
		pos = tagText.lastIndexOf('<');
		return tagText.substring(0, pos).trim();
	}
}
