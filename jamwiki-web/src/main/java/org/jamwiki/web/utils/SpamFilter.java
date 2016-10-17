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
package org.jamwiki.web.utils;

import java.io.File;
import java.util.regex.PatternSyntaxException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.utils.ResourceUtil;
import org.jamwiki.utils.WikiLogger;

/**
 * Provide the capability for filtering content based on a predefined list of
 * regular expressions.
 */
public class SpamFilter {

	/** Logger */
	public static final WikiLogger logger = WikiLogger.getLogger(SpamFilter.class.getName());
	/** Spam blacklist file name. */
	private static final String SPAM_BLACKLIST_FILE = "spam-blacklist.txt";
	private static Pattern spamRegexPattern = null;

	/**
	 *
	 */
	private SpamFilter() {
	}

	/**
	 * Attempt to match the local spam blacklist patterns against a content
	 * string.  If any matches are found this method returns the matched
	 * text, otherwise <code>null</code> is returned.  Note that if the wiki
	 * is not configured to use the spam filter then this method will always
	 * return <code>null</code>.
	 *
	 * @param content The content that will be searched for values matching
	 *  those found in the spam blacklist.
	 * @return If any matches are found this method returns the matched text,
	 *  otherwise <code>null</code> is returned.
	 * @throws DataAccessException Thrown if any error occurs while reading, compiling,
	 *  or matching against the spam filter regular expressions.
	 */
	public static String containsSpam(String content) throws DataAccessException {
		if (!Environment.getBooleanValue(Environment.PROP_TOPIC_SPAM_FILTER)) {
			return null;
		}
		long start = System.currentTimeMillis();
		if (spamRegexPattern == null) {
			SpamFilter.initialize();
		}
		Matcher m = spamRegexPattern.matcher(content);
		String result = null;
		if (m.find()) {
			result = m.group(0);
		}
		if (logger.isDebugEnabled()) {
			long execution = System.currentTimeMillis() - start;
			logger.debug("Executed spam filter (" + (execution / 1000.000) + " s.)");
		}
		return result;
	}

	/**
	 *
	 */
	private static void initialize() throws DataAccessException {
		File file = null;
		try {
			file = ResourceUtil.getJAMWikiResourceFile(SPAM_BLACKLIST_FILE);
		} catch (IOException e) {
			throw new DataAccessException("I/O exception while initlaizing spam blacklist", e);
		}
		String regex = "";
		String regexText = null;
		try {
			regexText = FileUtils.readFileToString(file, "UTF-8").trim();
		} catch (IOException e) {
			throw new DataAccessException("I/O exception while initlaizing spam blacklist", e);
		}
		String[] tokens = regexText.split("\n");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (StringUtils.isBlank(token)) {
				continue;
			}
			if (i > 0) {
				regex += "|";
			}
			regex += token.trim();
		}
		try {
			spamRegexPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			throw new DataAccessException("Failure while parsing spam regular expression list", e);
		}
		logger.info("Loading spam filter regular expressions from " + file.getAbsolutePath());
	}

	/**
	 * Reload the spam-blacklist.txt file, updating the current spam regular
	 * expression patterns.
	 *
	 * @throws DataAccessException Thrown if any error occurs while reading or compiling
	 *  the spam filter regular expressions.
	 */
	public static void reload() throws DataAccessException {
		SpamFilter.initialize();
	}
}
