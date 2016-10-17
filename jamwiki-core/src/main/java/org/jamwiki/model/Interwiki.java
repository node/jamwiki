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
package org.jamwiki.model;

import java.io.Serializable;
import java.text.MessageFormat;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.Utilities;

/**
 * Provides an object representing an interwiki link record.
 */
public class Interwiki implements Serializable {

	private final String interwikiDisplay;
	private final String interwikiPattern;
	private final String interwikiPrefix;
	private int interwikiType = -1;

	/**
	 *
	 */
	public Interwiki(String interwikiPrefix, String interwikiPattern, String interwikiDisplay) {
		this.interwikiPrefix = interwikiPrefix;
		this.interwikiPattern = interwikiPattern;
		this.interwikiDisplay = interwikiDisplay;
	}

	/**
	 * Return a string appropriate for display to represent this interwiki link.
	 *
	 * @return A string appropriate for display to represent this interwiki link.
	 */
	public String getInterwikiDisplay() {
		return (this.interwikiDisplay != null) ? this.interwikiDisplay.trim() : null;
	}

	/**
	 * Return a pattern into which a topic name will be substitued to generate a full
	 * link to the interwiki target.
	 *
	 * @return A pattern into which a topic name will be substitued to generate a full
	 * link to the interwiki target.
	 */
	public String getInterwikiPattern() {
		return (this.interwikiPattern != null) ? this.interwikiPattern.trim() : null;
	}

	/**
	 * The prefix that generates links for this interwiki.  Note that Interwiki
	 * links are case-insensitive.
	 *
	 * @return The prefix that generates links for this interwiki.
	 */
	public String getInterwikiPrefix() {
		return (this.interwikiPrefix != null) ? this.interwikiPrefix.trim().toLowerCase() : null;
	}

	/**
	 *
	 */
	public int getInterwikiType() {
		return this.interwikiType;
	}

	/**
	 *
	 */
	public void setInterwikiType(int interwikiType) {
		this.interwikiType = interwikiType;
	}

	/**
	 * Given a topic name, process the interwiki link and return the resulting URL.
	 * For example, for the "wikipedia" interwiki object and a topic name of
	 * "Main Page" this method should return "http://en.wikipedia.org/wiki/Main_Page".
	 *
	 * @param topicName The page or topic name that is being linked to.
	 * @return Returns a formatted URL that links to the page specified by the
	 *  namespace and value.
	 * @throws IllegalArgumentException Thrown if the interwiki pattern is invalid
	 *  or if the topic name is invalid.
	 */
	public String format(String topicName) throws IllegalArgumentException {
		if (StringUtils.isBlank(topicName)) {
			topicName = "";
		}
		Object[] objects = {Utilities.encodeAndEscapeTopicName(topicName)};
		return MessageFormat.format(this.getInterwikiPattern(), objects);
	}

	/**
	 * Utility method to determine if the Interwiki object is valid.
	 *
	 * @throws WikiException Thrown if the Interwiki object is invalid.
	 */
	public void validate() throws WikiException {
		if (StringUtils.isBlank(this.getInterwikiPrefix()) || this.getInterwikiPrefix().length() > 30 || !this.getInterwikiPrefix().matches("[\\p{L}0-9\\._\\-]+")) {
			throw new WikiException(new WikiMessage("admin.vwiki.error.interwiki.prefix", this.getInterwikiPrefix()));
		}
		if (StringUtils.isBlank(this.getInterwikiPattern()) || this.getInterwikiPattern().length() > 200 || this.getInterwikiPattern().indexOf("{0}") == -1) {
			throw new WikiException(new WikiMessage("admin.vwiki.error.interwiki.pattern", this.getInterwikiPattern()));
		}
		try {
			this.format("Test");
		} catch (IllegalArgumentException e) {
			// a failure indicates an invalid pattern
			throw new WikiException(new WikiMessage("admin.vwiki.error.interwiki.pattern", this.getInterwikiPattern()));
		}
		if (StringUtils.isBlank(this.getInterwikiDisplay()) || this.getInterwikiDisplay().length() > 30) {
			throw new WikiException(new WikiMessage("admin.vwiki.error.interwiki.display", this.getInterwikiDisplay()));
		}
	}
}