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
package org.jamwiki.parser.jflex.addon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.jflex.JFlexCustomTagItem;
import org.jamwiki.parser.jflex.JFlexLexer;
import org.jamwiki.parser.jflex.JFlexParserUtil;
import org.jamwiki.utils.WikiLogger;

/**
 * Add support for the HTML <iframe> tag for sites that want it.  Sample usage:
 *
 * &lt;iframe src="http://www.example.com" class="iframe-style" id="my_iframe" />
 */
public class IFrameTag implements JFlexCustomTagItem {

	private static final WikiLogger logger = WikiLogger.getLogger(IFrameTag.class.getName());
	private static List<String> ALLOWED_ATTRIBUTES = Arrays.asList(
		"accesskey",
		"align",
		"class",
		"dir",
		"frameborder",
		"height",
		"id",
		"lang",
		"longdesc",
		"marginwidth",
		"marginheight",
		"name",
		"sandbox",
		"scrolling",
		"seamless",
		"src",
		"srcdoc",
		"style",
		"tabindex",
		"title",
		"width"
	);
	private String tagName = "iframe";

	/**
	 * Return the tag name.  If the tag is "<custom>" then the tag name is "custom".
	 */
	public String getTagName() {
		return this.tagName;
	}

	/**
	 * Set the tag name.  If the tag is "<custom>" then the tag name is "custom".
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * Initialize the tag with any key-value params passed in from the configuration.
	 */
	public void initParams(Map<String, String> initParams) {
	}

	/**
	 * Convert the attribute map to a string of key-value pairs.
	 */
	private String formatAttributes(Map<String, String> attributes) {
		if (attributes == null || attributes.isEmpty()) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			if (!ALLOWED_ATTRIBUTES.contains(entry.getKey().toLowerCase())) {
				if (logger.isDebugEnabled()) {
					logger.debug("iframe tag called with invalid attribute: " + entry.getKey());
				}
				continue;
			}
			result.append(' ').append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
		}
		return result.toString();
	}

	/**
	 * Parse an iframe tag of the form <iframe>...</iframe> and return the
	 * resulting wiki text output.
	 */
	public String parse(JFlexLexer lexer, Map<String, String> attributes, String content) throws ParserException {
		String openTag = "<iframe" + this.formatAttributes(attributes) + ">";
		String closeTag = "</iframe>";
		if (StringUtils.isBlank(content)) {
			return JFlexParserUtil.formatAsNoParse(openTag + closeTag);
		} else {
			return JFlexParserUtil.formatAsNoParse(openTag) + content + JFlexParserUtil.formatAsNoParse(closeTag);
		}
	}
}
