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
package org.jamwiki.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.utils.WikiLogger;

/**
 * This abstract class implements functionality for both the "radio" tag and
 * "checkbox" tag as they are very similar.
 *
 * @see CheckboxTag
 * @see RadioTag
 */
public abstract class AbstractButtonTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(AbstractButtonTag.class.getName());
	private String checked = null;
	private String id = null;
	private String name = null;
	private String onchange = null;
	private String onclick = null;
	private String style = null;
	private String value = null;

	/**
	 *
	 */
	public AbstractButtonTag() {
		super();
	}

	/**
	 * Generate the tag HTML output.
	 */
	public int doEndTag() throws JspException {
		StringBuilder output = new StringBuilder();
		output.append("<input type=\"").append(this.getButtonType()).append('\"');
		output.append(" value=\"").append(this.value).append('\"');
		output.append(" name=\"").append(this.name).append('\"');
		if (!StringUtils.isBlank(this.id)) {
			output.append(" id=\"").append(this.id).append('\"');
		}
		if (!StringUtils.isBlank(this.style)) {
			output.append(" style=\"").append(this.style).append('\"');
		}
		if (!StringUtils.isBlank(this.onchange)) {
			output.append(" onchange=\"").append(this.onchange).append('\"');
		}
		if (!StringUtils.isBlank(this.onclick)) {
			output.append(" onclick=\"").append(this.onclick).append('\"');
		}
		if (!StringUtils.isBlank(this.checked) && this.checked.equals(this.value)) {
			output.append(" checked=\"checked\"");
		}
		output.append(" />");
		try {
			this.pageContext.getOut().print(output.toString());
		} catch (IOException e) {
			logger.error("Failure in " + getButtonType() + " tag for " + this.id + " / " + this.name + " / " + this.style + " / " + this.value, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 * Return the form tag checked value.
	 */
	public String getChecked() {
		return this.checked;
	}

	/**
	 * Set the form tag checked value.
	 */
	public void setChecked(String checked) {
		this.checked = checked;
	}

	/**
	 * Return the form tag ID value.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Set the form tag ID value.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Return the form tag name value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the form tag name value.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Return the form tag onchange value.
	 */
	public String getOnchange() {
		return this.onchange;
	}

	/**
	 * Set the form tag onchange value.
	 */
	public void setOnchange(String onchange) {
		this.onchange = onchange;
	}

	/**
	 * Return the form tag onclick value.
	 */
	public String getOnclick() {
		return this.onclick;
	}

	/**
	 * Set the form tag onclick value.
	 */
	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	/**
	 * Return the form tag CSS style value.
	 */
	public String getStyle() {
		return this.style;
	}

	/**
	 * Set the form tag CSS style value.
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * Set the form tag value (if any).
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Returns the form tag value (if any).
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the type of button, eg. "checkbox" or "radio".
	 *
	 * @return The type of button, eg. "checkbox" or "radio".
	 */
	public abstract String getButtonType() ;
}