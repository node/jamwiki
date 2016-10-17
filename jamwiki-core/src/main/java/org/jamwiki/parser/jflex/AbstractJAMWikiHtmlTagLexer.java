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
import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class that is extended by the JFlex HTML tag lexer.  Used in parsing
 * HTML tags of the form <div id="value">, </div>, <br />, etc.
 */
public abstract class AbstractJAMWikiHtmlTagLexer extends JFlexLexer {

	protected static final WikiLogger logger = WikiLogger.getLogger(AbstractJAMWikiHtmlTagLexer.class.getName());
	/** During parsing, the attribute (if any) that is currently being parsed. */
	protected String currentAttributeKey;
	/** The raw HTML being parsed. */
	protected String html;
	/** An indicator of whether this is an open tag, a close tag, or an empty body tag. */
	protected HtmlTagItem.Pattern tagPattern;
	/** The type of HTML tag being parsed, for example "b", "br", "p", etc. */
	protected String tagType;
	/** A map of parsed tag attributes. */
	protected Map<String, String> attributes = Collections.emptyMap();

	/**
	 *
	 */
	protected void addAttribute(String key, String value) {
		if (this.attributes.isEmpty()) {
			// this field is initialized to an immutable map, so if it is empty
			// reset it to a mutable map.
			this.attributes = new LinkedHashMap<String, String>();
		}
		this.attributes.put(key, value);
	}

	/**
	 *
	 */
	protected String closeTag() {
		return new HtmlTagItem(this.tagType, this.tagPattern, this.attributes).toHtml();
	}

	/**
	 *
	 */
	protected Map<String, String> getAttributes() {
		return this.attributes;
	}

	/**
	 * Return the HTML tag item that this processor generates.  Note that the tag
	 * item is only created when the tag parser processes the last character of the
	 * close tag, so this method can only be called after a parser pass completes.
	 */
	protected HtmlTagItem getHtmlTagItem() {
		return new HtmlTagItem(this.tagType, this.tagPattern, this.attributes);
	}

	/**
	 *
	 */
	protected void initialize(HtmlTagItem.Pattern tagPattern) {
		this.attributes = Collections.emptyMap();
		this.currentAttributeKey = null;
		this.tagPattern = tagPattern;
		this.html = yytext();
		this.tagType = null;
	}

	/**
	 *
	 */
	protected void initializeCurrentAttribute(String key) {
		this.currentAttributeKey = key.toLowerCase();
		if (this.attributes.isEmpty()) {
			// this field is initialized to an immutable map, so if it is empty
			// reset it to a mutable map.
			this.attributes = new LinkedHashMap<String, String>();
		}
		this.attributes.put(this.currentAttributeKey, null);
	}

	/**
	 *
	 */
	protected void removeAttribute(String key) {
		if (this.attributes.isEmpty()) {
			return;
		}
		this.attributes.remove(key);
	}
}
