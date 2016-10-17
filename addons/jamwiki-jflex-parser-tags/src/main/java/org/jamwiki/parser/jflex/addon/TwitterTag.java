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

import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.parser.jflex.JFlexCustomTagItem;
import org.jamwiki.parser.jflex.JFlexLexer;
import org.jamwiki.parser.jflex.JFlexParserUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * Implement functionality to allow social media integration with Twitter using a tag
 * of the form &lt;twitter />.  See http://twitter.com/about/resources/buttons#tweet
 * for additional details.  Allowed attributes:
 *
 * <dl>
 * <dt>data-href</dt>
 * <dd>The full URL of the page being Tweeted.  Defaults to the current page URL.</dd>
 * <dt>data-count</dt>
 * <dd>The "count" layout for the button.  One of "horizontal", "vertical" or "none",
       with "horizontal" being the default.</dd>
 * </dl>
 */
public class TwitterTag implements JFlexCustomTagItem {

	private static final WikiLogger logger = WikiLogger.getLogger(TwitterTag.class.getName());
	/** Tag attribute name for the "count" layout.  If not specifies defaults to "horizontal". */
	private static final String ATTRIBUTE_COUNT_TYPE = "data-count";
	/** Tag attribute name for the page URL.  If not specifies defaults to the current page URL. */
	private static final String ATTRIBUTE_HREF = "data-href";
	/** Twitter attribute value for the horizontal "count" layout. */
	private static final String COUNT_HORIZONTAL = "horizontal";
	/** Twitter attribute value for no "count" layout. */
	private static final String COUNT_NONE = "none";
	/** Twitter attribute value for the vertical "count" layout. */
	private static final String COUNT_VERTICAL = "vertical";
	/** Parameter used to hold a flag in the ParserInput object indicating whether shared code has been loaded. */
	private static final String TWITTER_SHARED_PARAM = TwitterTag.class.getName() + "-shared";
	/** Path to the template used to format the Twitter button code, relative to the classpath. */
	private static final String TEMPLATE_TWITTER_BUTTON = "templates/twitter-button.template";
	/** Path to the template used to format the shared Twitter code, relative to the classpath. */
	private static final String TEMPLATE_TWITTER_SHARED = "templates/twitter-shared.template";

	private String tagName = "twitter";

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
	 * Parse a Twitter integration tag of the form <twitter /> and return the
	 * resulting wiki text output.
	 */
	public String parse(JFlexLexer lexer, Map<String, String> attributes, String content) throws ParserException {
		String result = "";
		try {
			result += this.parseButtonCode(lexer, attributes);
			String sharedCode = this.parseSharedCode(lexer, attributes);
			if (!StringUtils.isBlank(sharedCode)) {
				result += '\n' + sharedCode;
			}
			return JFlexParserUtil.formatAsNoParse(result);
		} catch (IOException e) {
			throw new ParserException(e);
		}
	}

	/**
	 * Parse the "Tweet" button.
	 */
	private String parseButtonCode(JFlexLexer lexer, Map<String, String> attributes) throws IOException {
		String[] args = new String[2];
		if (StringUtils.isBlank(attributes.get(ATTRIBUTE_HREF))) {
			WikiLink wikiLink = new WikiLink(lexer.getParserInput().getContext(), lexer.getParserInput().getVirtualWiki(), lexer.getParserInput().getTopicName());
			args[0] = LinkUtil.normalize(Environment.getValue(Environment.PROP_SERVER_URL) + wikiLink.toRelativeUrl());
		} else {
			args[0] = attributes.get(ATTRIBUTE_HREF);
		}
		if (StringUtils.equalsIgnoreCase(attributes.get(ATTRIBUTE_COUNT_TYPE), COUNT_VERTICAL)) {
			args[1] = COUNT_VERTICAL;
		} else if (StringUtils.equalsIgnoreCase(attributes.get(ATTRIBUTE_COUNT_TYPE), COUNT_NONE)) {
			args[1] = COUNT_NONE;
		} else {
			args[1] = COUNT_HORIZONTAL;
		}
		return WikiUtil.formatFromTemplate(TEMPLATE_TWITTER_BUTTON, args);
	}

	/**
	 * If this is the first instantiation of a Twitter tag on the page then
	 * include the required shared code.
	 */
	private String parseSharedCode(JFlexLexer lexer, Map<String, String> attributes) throws IOException {
		if (lexer.getParserInput().getTempParam(TWITTER_SHARED_PARAM) != null) {
			return "";
		}
		lexer.getParserInput().addTempParam(TWITTER_SHARED_PARAM, true);
		return WikiUtil.formatFromTemplate(TEMPLATE_TWITTER_SHARED);
	}
}
