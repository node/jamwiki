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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.parser.ParserException;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * Handle redirects of the form "#REDIRECT [[Target]]".
 */
public class RedirectTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(RedirectTag.class.getName());
	/** Pattern to determine if the topic is a redirect. */
	private static final Pattern REDIRECT_PATTERN = Pattern.compile("#REDIRECT[ ]*\\[\\[([^\\n\\r\\]]+)\\]\\]", Pattern.CASE_INSENSITIVE);

	/**
	 * Parse a call to a Mediawiki redirect tag of the form
	 * "#REDIRECT [[Target]]".
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
		if (lexer.getMode() <= JFlexParser.MODE_MINIMAL) {
			return raw;
		}
		String redirect = this.retrieveTarget(raw);
		if (StringUtils.isBlank(redirect)) {
			// if there's no redirect then the syntax is bad
			return raw;
		}
		// store the redirect value & also record it as a "link to" record
		lexer.getParserOutput().setRedirect(redirect);
		lexer.getParserOutput().addLink(redirect);
		return "";
	}

	/**
	 * Use a regular expression to retrieve the redirection target from the
	 * redirect text.
	 */
	private String retrieveTarget(String raw) throws ParserException {
		if (StringUtils.isBlank(raw)) {
			return null;
		}
		Matcher m = REDIRECT_PATTERN.matcher(raw.trim());
		String result = (m.matches()) ? Utilities.decodeAndEscapeTopicName(m.group(1).trim(), true) : null;
		if (result == null) {
			return null;
		}
		boolean colon = (result.length() > 1 && result.charAt(0) == ':');
		if (colon) {
			result = result.substring(1);
		}
		return result;
	}
}
