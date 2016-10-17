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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.jamwiki.Environment;
import org.jamwiki.utils.WikiLogger;

/**
 * JSP tag that executes its tag content only if the specified property has
 * a value of <code>true</code>.
 */
public class EnabledTag extends BodyTagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(EnabledTag.class.getName());
	private String property = null;

	/**
	 *
	 */
	public int doStartTag() throws JspException {
		String propertyName = null;
		try {
			propertyName = (String)Environment.class.getField(this.property).get(null);
		} catch (NoSuchFieldException e) {
			logger.error("Failure in enabled tag for " + this.property, e);
			throw new JspException(e);
		} catch (IllegalAccessException e) {
			logger.error("Failure in enabled tag for " + this.property, e);
			throw new JspException(e);
		}
		if (Environment.getBooleanValue(propertyName)) {
			return EVAL_BODY_INCLUDE;
		}
		return SKIP_BODY;
	}

	/**
	 *
	 */
	public String getProperty() {
		return this.property;
	}

	/**
	 *
	 */
	public void setProperty(String property) {
		this.property = property;
	}
}
