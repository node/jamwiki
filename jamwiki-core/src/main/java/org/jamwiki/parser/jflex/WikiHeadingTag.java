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
 * This class parses wiki headings of the form <code>==heading content==</code>.
 */
public class WikiHeadingTag extends AbstractHeadingTag {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiHeadingTag.class.getName());

	/**
	 *
	 */
	protected int generateTagLevel(String raw, Object... args) throws ParserException {
		if (args.length == 0) {
			throw new IllegalArgumentException("Must pass heading depth to WikiHeadingTag.parse");
		}
		return (Integer)args[0];
	}

	/**
	 *
	 */
	protected String generateTagOpen(String raw, Object... args) throws ParserException {
		return "<h" + this.generateTagLevel(raw, args) + ">";
	}

	/**
	 *
	 */
	protected String generateTagText(String raw, Object... args) throws ParserException {
		int level = this.generateTagLevel(raw, args);
		return raw.substring(level, raw.length() - level).trim();
	}
}
