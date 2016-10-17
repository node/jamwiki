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
package org.jamwiki.utils;

/**
 * This class is a utility class used with the {@link org.jamwiki.WikiMessage} class
 * to hold message params.
 */
public class WikiMessageParam {

	private String param = null;
	private String paramText = null;
	private boolean isWikiLink = false;

	/**
	 * Create a WikiMessageParam that represents a string value.  Note that this
	 * value should be properly escaped prior to being stored as a parameter.
	 */
	public WikiMessageParam(String param) {
		this.param = param;
	}

	/**
	 * Create a WikiMessageParam and optionally set that param to represent a
	 * wiki link object.  Note that if the parameter is not a wiki link then the
	 * value should be properly escaped prior to being stored as a parameter.
	 */
	public WikiMessageParam(String param, boolean isWikiLink) {
		this.param = param;
		this.isWikiLink = isWikiLink;
	}

	/**
	 * Create a WikiMessageParam that represents a wiki link, specifying both
	 * the link target and the link text.
	 */
	public WikiMessageParam(String linkValue, String linkText) {
		this.param = linkValue;
		this.paramText = linkText;
		this.isWikiLink = true;
	}

	/**
	 * Return a flag indicating whether this message param should be formatted
	 * as a wiki link.
	 */
	public boolean isWikiLink() {
		return this.isWikiLink;
	}

	/**
	 * Return the param value.
	 */
	public String getParam() {
		return this.param;
	}

	/**
	 * Return the param text (if set) or the param value if there is no param
	 * text.
	 */
	public String getParamText() {
		return (this.paramText == null) ? this.param : this.paramText;
	}

	/**
	 * Return the param value for this object when converting to a string.
	 */
	public String toString() {
		return this.param;
	}
}
