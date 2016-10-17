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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wrapper for an HTML or HTML-like tag of the form
 * <tag attribute1="value1" attribute2="value2">.  Used for utility purposes with
 * the JFlex parser.
 */
public class HtmlTagItem {

	protected enum Pattern {
		/** HTML open tag of the form &lt;tag> or &lt;tag attribute="value">. */
		OPEN,
		/** HTML close tag of the form &lt;/tag>. */
		CLOSE,
		/** HTML tag without content of the form &lt;tag /> or &lt;tag attribute="value" />. */
		EMPTY_BODY
	}
	/** The tag type, for example &lt;tag attribute="value"> has a tag type of "tag". */
	private final String tagType;
	/** The pattern type for the tag - open, close, or empty body. */
	private final Pattern tagPattern;
	/** The tag's attributes, mapped as an ordered list of key-value pairs. */
	private final Map<String, String> attributes;

	/**
	 *
	 */
	protected HtmlTagItem(String tagType, Pattern tagPattern, Map<String, String> attributes) {
		this.tagType = tagType;
		this.tagPattern = tagPattern;
		if (attributes != null && !attributes.isEmpty()) {
			this.attributes = new LinkedHashMap<String, String>(attributes);
		} else {
			this.attributes = Collections.emptyMap();
		}
	}

	/**
	 * Return a mapping of key-value pairs for all attributes of this tag.
	 */
	protected Map<String, String> getAttributes() {
		return this.attributes;
	}

	/**
	 * Return <code>true</code> if this tag's type is Pattern.EMPTY_BODY,
	 * otherwise return <code>false</code>.
	 */
	protected boolean isTagEmptyBody() {
		return (this.tagPattern == Pattern.EMPTY_BODY);
	}

	/**
	 * Return the tag pattern (open tag, close tag, empty body tag).
	 */
	protected Pattern getPattern() {
		return this.tagPattern;
	}

	/**
	 * Return the tag type (example: "ul").
	 */
	protected String getTagType() {
		return this.tagType;
	}

	/**
	 * Convert the tag to an HTML or HTML-like representation.
	 */
	public String toHtml() {
		StringBuilder result = new StringBuilder("<");
		if (this.tagPattern == Pattern.CLOSE) {
			result.append("/");
		}
		result.append(this.tagType);
		if (this.attributes != null) {
			for (Map.Entry<String, String> entry : this.attributes.entrySet()) {
				result.append(' ').append(entry.getKey());
				if (entry.getValue() != null) {
					result.append('=').append("\"").append(entry.getValue().trim()).append("\"");
				}
			}
		}
		if (this.isTagEmptyBody()) {
			result.append(" /");
		}
		result.append(">");
		return result.toString();
	}
}
