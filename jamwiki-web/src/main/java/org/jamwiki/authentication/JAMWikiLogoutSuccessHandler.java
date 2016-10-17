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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.utils.WikiLogger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

/**
 *
 */
public class JAMWikiLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiLogoutSuccessHandler.class.getName());

	/**
	 *
	 */
	public JAMWikiLogoutSuccessHandler() {
		this.setTargetUrlParameter(JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGIN_TARGET_URL_FIELD_NAME);
	}

	/**
	 *
	 */
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		// Special:Logout adds a proper redirection URL as a parameter in the
		// Spring logout URL.
		String targetUrl = request.getParameter(JAMWikiAuthenticationConstants.SPRING_SECURITY_LOGOUT_REDIRECT_QUERY_PARAM);
		logger.info("Redirecting to: " + targetUrl);
		response.sendRedirect(targetUrl);
	}
}
