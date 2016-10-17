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
package org.jamwiki;

import java.util.List;
import org.jamwiki.utils.WikiMessageParam;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is a utility class useful for storing messages key and object
 * values that can later be displayed using the jstl fmt:message tag.
 *
 * @see org.jamwiki.utils.WikiMessageParam
 */
public class WikiMessage {

	private final String key;
	private WikiMessageParam[] params = null;

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 */
	public WikiMessage(String key) {
		this.key = key;
	}

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value using a single parameter.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 * @param param1 The parameter that corresponds to the {0} param in the
	 *  specified message key value.  Note that this parameter is automatically
	 *  HTML escaped to prevent erorrs in display.
	 */
	public WikiMessage(String key, String param1) {
		this.key = key;
		this.params = new WikiMessageParam[1];
		params[0] = new WikiMessageParam(this.escapeHtml4(param1));
	}

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value using two parameters.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 * @param param1 The parameter that corresponds to the {0} param in the
	 *  specified message key value.  Note that this parameter is automatically
	 *  HTML escaped to prevent erorrs in display.
	 * @param param2 The parameter that corresponds to the {1} param in the
	 *  specified message key value.  Note that this parameter is automatically
	 *  HTML escaped to prevent erorrs in display.
	 */
	public WikiMessage(String key, String param1, String param2) {
		this.key = key;
		this.params = new WikiMessageParam[2];
		params[0] = new WikiMessageParam(this.escapeHtml4(param1));
		params[1] = new WikiMessageParam(this.escapeHtml4(param2));
	}

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value using an array of parameters.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 * @param params An array of parameters that correspond to the {0}, {1}, etc
	 *  params in the specified message key value.  Note that these parameters are
	 *  automatically HTML escaped to prevent erorrs in display.
	 */
	public WikiMessage(String key, String[] params) {
		this.key = key;
		if (params != null) {
			this.params = new WikiMessageParam[params.length];
			for (int i = 0; i < params.length; i++) {
				this.params[i] = new WikiMessageParam(this.escapeHtml4(params[i]));
			}
		}
	}

	/**
	 * Create a new message that is mapped to the specified ApplicationResources
	 * key value using an list of parameters.
	 *
	 * @param key The ApplicationResources key that corresponds to the message
	 *  to display.
	 * @param paramList An list of parameters that correspond to the {0}, {1}, etc
	 *  params in the specified message key value.  Note that these parameters are
	 *  automatically HTML escaped to prevent erorrs in display.
	 */
	public WikiMessage(String key, List<String> paramList) {
		this.key = key;
		if (paramList != null && !paramList.isEmpty()) {
			this.params = new WikiMessageParam[paramList.size()];
			int i = 0;
			for (String param : paramList) {
				this.params[i++] = new WikiMessageParam(this.escapeHtml4(param));
			}
		}
	}

	/**
	 * Add a string param to this WikiMessage object.  The param will be HTML-escaped
	 * to prevent formatting issues or XSS issues.
	 *
	 * @param param A string that will be processed with the fmt:param tag
	 *  when this wiki message is rendered.
	 */
	public void addParam(String param) {
		WikiMessageParam wikiMessageParam = new WikiMessageParam(this.escapeHtml4(param));
		this.addParam(wikiMessageParam);
	}

	/**
	 * Add a wiki link param to this WikiMessage object.
	 *
	 * @param wikiLinkParam A string that will be processed with the jamwiki:link tag
	 *  when this wiki message is rendered.
	 */
	public void addWikiLinkParam(String wikiLinkParam) {
		WikiMessageParam wikiMessageParam = new WikiMessageParam(wikiLinkParam, true);
		this.addParam(wikiMessageParam);
	}

	/**
	 * Add a wiki link param to this WikiMessage object.
	 *
	 * @param wikiLinkParam A string that will be processed with the jamwiki:link tag
	 *  when this wiki message is rendered.
	 * @param wikiLinkParamText A string that will be used as text with the jamwiki:link
	 *  tag when this wiki message is rendered.
	 */
	public void addWikiLinkParam(String wikiLinkParam, String wikiLinkParamText) {
		WikiMessageParam wikiMessageParam = new WikiMessageParam(wikiLinkParam, wikiLinkParamText);
		this.addParam(wikiMessageParam);
	}

	/**
	 * Update the list of params for this WikiMessage, adding the new
	 * param to the end of the parameter array.
	 */
	private void addParam(WikiMessageParam wikiMessageParam) {
		WikiMessageParam[] newParams = new WikiMessageParam[this.getParamsLength() + 1];
		int i = 0;
		if (this.params != null) {
			for (WikiMessageParam param : this.params) {
				newParams[i++] = param;
			}
		}
		newParams[i] = wikiMessageParam;
		this.params = newParams;
	}

	/**
	 * Return the ApplicationResources message key associated with this message.
	 *
	 * @return The ApplicationResources message key associated with this message.
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Return the array of parameter objects associated with this message.
	 *
	 * @return The array of parameter objects associated with this message.
	 */
	public WikiMessageParam[] getParams() {
		return this.params;
	}

	/**
	 * Return the number of params assigned to this WikiMessage.
	 *
	 * @return The number of params assigned to this WikiMessage.
	 */
	public int getParamsLength() {
		return ((this.params == null) ? 0 : this.params.length);
	}

	/**
	 * This set method allows message parameters to be set without being escaped.
	 * Note that this can be a gaping security hole as it opens the site up to
	 * cross-site scripting attacks.  USE THIS METHOD ONLY IF YOU KNOW WHAT YOU ARE
	 * DOING!
	 *
	 * @param params The array of parameter objects to associate with this message.
	 */
	public void setParamsWithoutEscaping(String[] params) {
		this.params = new WikiMessageParam[params.length];
		for (int i = 0; i < params.length; i++) {
			this.params[i] = new WikiMessageParam(params[i]);
		}
	}
	
	/**
	 * Escape HTML.  StringEscapeUtils.escapeHtml should be used for this functionality,
	 * but the current version escapes unicode characters as well as HTML entities
	 * which breaks some wiki functionality.
	 */
	private String escapeHtml4(String param) {
		// this could be optimized should performance become an issue
		param = StringUtils.replace(param, "&", "&amp;");
		param = StringUtils.replace(param, "<", "&lt;");
		param = StringUtils.replace(param, ">", "&gt;");
		param = StringUtils.replace(param, "\"", "&quot;");
		return param;
	}

	/**
	 * Utility method for replacing a specified WikiMessage paramter.  If no
	 * parameter exists at the specified index then an error will be thrown.
	 *
	 * @param index The message parameter to replace, starting with zero.
	 * @param parameter The value to replace the current parameter with.
	 * @throws IllegalArgumentException Thrown if the existing message parameter
	 *  array is shorter than the specified index.
	 */
	public void replaceParameter(int index, String parameter) {
		if (this.params == null || this.params.length <= index) {
			throw new IllegalArgumentException("Attempt to replace index " + index + " for an array that has " + ((this.params == null) ? "0" : this.params.length) + " parameters");
		}
		this.params[index] = new WikiMessageParam(this.escapeHtml4(parameter));
	}

	/**
	 * Override the toString method to return the key and params used by this message
	 * object.  This capability is useful mainly for logging purposes when formatting
	 * by locale is not required.
	 *
	 * @return A string representation of this WikiMessage including the message key
	 *  and any params.
	 */
	public String toString() {
		String result = this.key;
		if (this.params != null) {
			for (WikiMessageParam param : this.params) {
				result += " | " + param.toString();
			}
		}
		return result;
	}
}
