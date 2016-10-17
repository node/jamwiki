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
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.validator.ReCaptchaUtil;

/**
 * Utility tag for displaying reCAPTCHA input fields.  See
 * http://code.google.com/apis/recaptcha/docs/java.html.
 */
public class ReCaptchaTag extends BodyTagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(ReCaptchaTag.class.getName());
	private static final String RECAPTCHA_PROP_LANGUAGE = "lang";
	private static final String RECAPTCHA_PROP_TAB_INDEX = "tabindex";
	private static final String RECAPTCHA_PROP_THEME = "theme";
	private int tabIndex = 0;
	private String theme = "clean";

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		Properties props = new Properties();
		props.put(RECAPTCHA_PROP_LANGUAGE, request.getLocale().getLanguage());
		props.put(RECAPTCHA_PROP_TAB_INDEX, Integer.toString(this.getTabIndex()));
		props.put(RECAPTCHA_PROP_THEME, this.getTheme());
		try {
			this.pageContext.getOut().print(ReCaptchaUtil.recaptchaInstance().createRecaptchaHtml(null, props));
		} catch (IOException e) {
			logger.error("Failure while generating reCAPTCHA input", e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 * Return the form tabIndex property for the captcha input.
	 */
	public int getTabIndex() {
		return this.tabIndex;
	}

	/**
	 * Set the form tabIndex property for the captcha input.
	 */
	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	/**
	 * Return the reCAPTCHA theme to use for the captcha input.  See
	 * http://code.google.com/apis/recaptcha/docs/customization.html#Standard_Themes.
	 */
	public String getTheme() {
		return this.theme;
	}

	/**
	 * Set the reCAPTCHA theme to use for the captcha input.  See
	 * http://code.google.com/apis/recaptcha/docs/customization.html#Standard_Themes.
	 */
	public void setTheme(String theme) {
		this.theme = theme;
	}
}
