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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiMessage;
import org.jamwiki.authentication.JAMWikiAuthenticationConstants;
import org.jamwiki.servlets.ServletUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility tag for creating HTML checkboxes.
 */
public class AuthMsgTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(AuthMsgTag.class.getName());
	private String css = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		String output = this.processSpringSecurityException();
		if (output != null) {
			try {
				this.pageContext.getOut().print(output);
			} catch (IOException e) {
				logger.error("Failure in authmsg tag", e);
				throw new JspException(e);
			}
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	private String formatMessage(String message) throws JspException {
		if (message == null) {
			return null;
		}
		StringBuilder output = new StringBuilder("<div");
		if (!StringUtils.isBlank(this.css)) {
			output.append(" class=\"").append(this.css).append('\"');
		}
		output.append('>');
		output.append(message);
		output.append("</div>");
		return output.toString();
	}

	/**
	 *
	 */
	private String processSpringSecurityException() throws JspException {
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		if (request.getSession().getAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_ERROR_KEY) != null) {
			return this.processAccessDeniedException(request);
		}
		if (request.getSession().getAttribute(JAMWikiAuthenticationConstants.JAMWIKI_AUTHENTICATION_REQUIRED_KEY) != null) {
			return this.processAuthenticationRequiredException(request);
		}
		if (request.getParameter("message") != null) {
			return this.processAuthorizationException(request);
		}
		if (request.getAttribute("messageObject") != null) {
			return this.processLegacyLogin(request);
		}
		return null;
	}

	/**
	 *
	 */
	private String processAccessDeniedException(HttpServletRequest request) throws JspException {
		String key = (String)request.getSession().getAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_ERROR_KEY);
		String uri = (String)request.getSession().getAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_URI_KEY);
		if (key == null) {
			return null;
		}
		Object[] params = {uri};
		String message = Utilities.formatMessage(key, ServletUtil.retrieveUserLocale(request), params);
		request.getSession().removeAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_ERROR_KEY);
		request.getSession().removeAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_URI_KEY);
		return formatMessage(message);
	}

	/**
	 *
	 */
	private String processAuthenticationRequiredException(HttpServletRequest request) throws JspException {
		String key = (String)request.getSession().getAttribute(JAMWikiAuthenticationConstants.JAMWIKI_AUTHENTICATION_REQUIRED_KEY);
		String uri = (String)request.getSession().getAttribute(JAMWikiAuthenticationConstants.JAMWIKI_AUTHENTICATION_REQUIRED_URI_KEY);
		if (key == null) {
			return null;
		}
		Object[] params = {uri};
		String message = Utilities.formatMessage(key, ServletUtil.retrieveUserLocale(request), params);
		request.getSession().removeAttribute(JAMWikiAuthenticationConstants.JAMWIKI_AUTHENTICATION_REQUIRED_KEY);
		request.getSession().removeAttribute(JAMWikiAuthenticationConstants.JAMWIKI_AUTHENTICATION_REQUIRED_URI_KEY);
		return formatMessage(message);
	}

	/**
	 *
	 */
	private String processAuthorizationException(HttpServletRequest request) throws JspException {
		String key = (String)request.getParameter("message");
		if (key == null) {
			return null;
		}
		String message = Utilities.formatMessage(key, ServletUtil.retrieveUserLocale(request));
		return formatMessage(message);
	}

	/**
	 *
	 */
	private String processLegacyLogin(HttpServletRequest request) throws JspException {
		WikiMessage messageObject = (WikiMessage)request.getAttribute("messageObject");
		if (messageObject == null) {
			return null;
		}
		String message = Utilities.formatMessage(messageObject.getKey(), ServletUtil.retrieveUserLocale(request), messageObject.getParams());
		return formatMessage(message);
	}

	/**
	 *
	 */
	public String getCss() {
		return this.css;
	}

	/**
	 *
	 */
	public void setCss(String css) {
		this.css = css;
	}
}
