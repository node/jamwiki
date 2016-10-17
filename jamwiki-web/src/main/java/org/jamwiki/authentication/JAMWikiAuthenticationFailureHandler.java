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
package org.jamwiki.authentication;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * This class is a hack implemented to support virtual wikis and Spring Security.
 */
public class JAMWikiAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAuthenticationFailureHandler.class.getName());
	private String authenticationFailureUrl;

	/**
	 *
	 */
	public String getAuthenticationFailureUrl() {
		return this.authenticationFailureUrl;
	}

	/**
	 *
	 */
	public void setAuthenticationFailureUrl(String authenticationFailureUrl) {
		this.authenticationFailureUrl = authenticationFailureUrl;
	}

	/**
	 *
	 */
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws java.io.IOException, ServletException {
		String virtualWikiName = WikiUtil.getVirtualWikiFromURI(request);
		if (StringUtils.isBlank(virtualWikiName)) {
			virtualWikiName = VirtualWiki.defaultVirtualWiki().getName();
		}
		String targetUrl = "/" + virtualWikiName + this.getAuthenticationFailureUrl();
		// set the original target in the request for later use
		String target = request.getParameter(JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_TARGET_URL_FIELD_NAME);
		if (!StringUtils.isBlank(target)) {
			targetUrl += (targetUrl.indexOf('?') == -1) ? "?" : "&";
			try {
				targetUrl += JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_TARGET_URL_FIELD_NAME + "=" + URLEncoder.encode(target, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// this should never happen
				throw new IllegalStateException("Unsupporting encoding UTF-8");
			}
		}
		this.setDefaultFailureUrl(targetUrl);
		super.onAuthenticationFailure(request, response, exception);
	}
}
