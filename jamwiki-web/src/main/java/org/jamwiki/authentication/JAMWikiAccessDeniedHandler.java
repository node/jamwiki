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
import org.jamwiki.utils.WikiUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

/**
 * This class is a hack implemented to support virtual wikis and Spring Security.
 */
public class JAMWikiAccessDeniedHandler extends AccessDeniedHandlerImpl {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAccessDeniedHandler.class.getName());
	private JAMWikiErrorMessageProvider errorMessageProvider;

	/**
	 *
	 */
	public JAMWikiErrorMessageProvider getErrorMessageProvider() {
		return this.errorMessageProvider;
	}

	/**
	 *
	 */
	public void setErrorMessageProvider(JAMWikiErrorMessageProvider errorMessageProvider) {
		this.errorMessageProvider = errorMessageProvider;
	}

	/**
	 *
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
		String accessDeniedRedirectUri = "/" + virtualWiki + "/Special:Login";
		this.setErrorPage(accessDeniedRedirectUri);
		request.getSession().setAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_ERROR_KEY, this.getErrorMessageProvider().getErrorMessageKey(request));
		request.getSession().setAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_URI_KEY, WikiUtil.getTopicFromURI(request));
		super.handle(request, response, accessDeniedException);
	}
}
