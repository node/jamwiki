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
 * Implement functionality to allow social media integration with Facebook using a tag
 * of the form &lt;facebook />.  See http://developers.facebook.com/docs/reference/plugins/like/
 * for additional details.  Allowed attributes:
 *
 * <dl>
 * <dt>data-href</dt>
 * <dd>The full URL of the page to like/recommend on Facebook.  Defaults to the
 *     current page URL.</dd>
 * <dt>data-send</dt>
 * <dd>Either <code>true</code> or <code>false</code>, depending on whether to
 *     include a "Send" button.  Defaults to <code>false</code>.</dd>
 * <dt>data-layout</dt>
 * <dd>The like/recommend button's layout style.  Must be one of "button_count",
 *     "standard" or "box_count", with the default being "standard".</dd>
 * <dt>data-width</dt>
 * <dd>The width in pixels of the generate like/recommend box.  Must be specified
 *     as an integer, with the default being 450.</dd>
 * <dt>data-show-faces</dt>
 * <dd>Either <code>true</code> or <code>false</code>, depending on whether to
 *     include profile pictures.  Defaults to <code>false</code>.</dd>
 * <dt>data-action</dt>
 * <dd>Tag attribute name for the verb to use - either "like" or "recommend".
 *     Defaults to "like".</dd>
 * <dt>app-id</dt>
 * <dd>The Facebook application ID to tie the like/recommend to.  Defaults empty.</dd>
 * <dt>locale</dt>
 * <dd>Locale used by the Facebook button, useful for localizing button text.  Only
 *     locales that are supported by Facebook are valid.  Example "en_GB".  Defaults
 *     to "en_US".</dd>
 * </dl>
 */
public class FacebookTag implements JFlexCustomTagItem {

	private static final WikiLogger logger = WikiLogger.getLogger(FacebookTag.class.getName());
	/** Facebook attribute value for "like" buttons. */
	private static final String ACTION_LIKE = "like";
	/** Facebook attribute value for "recommend" buttons. */
	private static final String ACTION_RECOMMEND = "recommend";
	/** Tag attribute name for the verb to use - either "like" or "recommend".  Defaults to "like". */
	private static final String ATTRIBUTE_ACTION = "data-action";
	/** Tag attribute name for the application ID.  Defaults empty. */
	private static final String ATTRIBUTE_APP_ID = "app-id";
	/** Tag attribute name for the page URL.  If not specifies defaults to the current page URL. */
	private static final String ATTRIBUTE_HREF = "data-href";
	/** Tag attribute name for the "layout" attribute - one of "button_count", "standard" or "box_count", defaults to "standard". */
	private static final String ATTRIBUTE_LAYOUT_TYPE = "data-layout";
	/**
	 * Tag attribute name for the optional locale.  Only locales supported by Facebook
	 * are valid. Defaults to "en_US".
	 */
	private static final String ATTRIBUTE_LOCALE = "locale";
	/** Tag attribute name for the "send" attribute.  If not specified defaults to <code>false</code>. */
	private static final String ATTRIBUTE_SEND = "data-send";
	/** Tag attribute name for the "show faces" attribute.  Defaults to <code>false</code>. */
	private static final String ATTRIBUTE_SHOW_FACES = "data-show-faces";
	/** Tag attribute name for the "width" attribute, specified in pixels. */
	private static final String ATTRIBUTE_WIDTH = "data-width";
	/** Facebook attribute value for box layout. */
	private static final String LAYOUT_BOX = "box_count";
	/** Facebook attribute value for button layout. */
	private static final String LAYOUT_BUTTON = "button_count";
	/** Facebook attribute value for standard layout. */
	private static final String LAYOUT_STANDARD = "standard";
	/** Parameter used to hold a flag in the ParserInput object indicating whether shared code has been loaded. */
	private static final String FACEBOOK_SHARED_PARAM = FacebookTag.class.getName() + "-shared";
	/** Path to the template used to format the Facebook button code, relative to the classpath. */
	private static final String TEMPLATE_FACEBOOK_BUTTON = "templates/facebook-button.template";
	/** Path to the template used to format the shared Facebook code, relative to the classpath. */
	private static final String TEMPLATE_FACEBOOK_SHARED = "templates/facebook-shared.template";

	private String tagName = "facebook";

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
	 * Parse a Facebook integration tag of the form <facebook /> and return the
	 * resulting wiki text output.
	 */
	public String parse(JFlexLexer lexer, Map<String, String> attributes, String content) throws ParserException {
		try {
			String result = this.parseSharedCode(lexer, attributes);
			if (!StringUtils.isBlank(result)) {
				result += '\n';
			}
			result += this.parseButtonCode(lexer, attributes);
			return JFlexParserUtil.formatAsNoParse(result);
		} catch (IOException e) {
			throw new ParserException(e);
		}
	}

	/**
	 * Parse the like/recommend button.
	 */
	private String parseButtonCode(JFlexLexer lexer, Map<String, String> attributes) throws IOException {
		String[] args = new String[6];
		if (StringUtils.isBlank(attributes.get(ATTRIBUTE_HREF))) {
			WikiLink wikiLink = new WikiLink(lexer.getParserInput().getContext(), lexer.getParserInput().getVirtualWiki(), lexer.getParserInput().getTopicName());
			args[0] = LinkUtil.normalize(Environment.getValue(Environment.PROP_SERVER_URL) + wikiLink.toRelativeUrl());
		} else {
			args[0] = attributes.get(ATTRIBUTE_HREF);
		}
		args[1] = (StringUtils.equalsIgnoreCase(attributes.get(ATTRIBUTE_SEND), "true")) ? "true" : "false";
		if (StringUtils.equalsIgnoreCase(attributes.get(ATTRIBUTE_LAYOUT_TYPE), LAYOUT_BOX)) {
			args[2] = LAYOUT_BOX;
		} else if (StringUtils.equalsIgnoreCase(attributes.get(ATTRIBUTE_LAYOUT_TYPE), LAYOUT_BUTTON)) {
			args[2] = LAYOUT_BUTTON;
		} else {
			args[2] = LAYOUT_STANDARD;
		}
		try {
			args[3] = new Integer(attributes.get(ATTRIBUTE_WIDTH)).toString();
		} catch (NumberFormatException e) {
			args[3] = "450";
		}
		args[4] = (StringUtils.equalsIgnoreCase(attributes.get(ATTRIBUTE_SHOW_FACES), "true")) ? "true" : "false";
		args[5] = (StringUtils.equalsIgnoreCase(attributes.get(ATTRIBUTE_ACTION), ACTION_RECOMMEND)) ? ACTION_RECOMMEND : ACTION_LIKE;
		return WikiUtil.formatFromTemplate(TEMPLATE_FACEBOOK_BUTTON, args);
	}

	/**
	 * If this is the first instantiation of a Facebook tag on the page then
	 * include the required shared code.
	 */
	private String parseSharedCode(JFlexLexer lexer, Map<String, String> attributes) throws IOException {
		if (lexer.getParserInput().getTempParam(FACEBOOK_SHARED_PARAM) != null) {
			return "";
		}
		lexer.getParserInput().addTempParam(FACEBOOK_SHARED_PARAM, true);
		String[] args = new String[2];
		args[0] = (attributes.get(ATTRIBUTE_LOCALE) != null) ? attributes.get(ATTRIBUTE_LOCALE) : "en_US";
		args[1] = (attributes.get(ATTRIBUTE_APP_ID) != null) ? attributes.get(ATTRIBUTE_APP_ID) : "";
		return WikiUtil.formatFromTemplate(TEMPLATE_FACEBOOK_SHARED, args);
	}
}
