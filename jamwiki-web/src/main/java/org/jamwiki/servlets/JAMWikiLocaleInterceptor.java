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
package org.jamwiki.servlets;

import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.LocaleUtils;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * This method extends the Spring <code>LocaleChangeInterceptor</code> to
 * search for and use a locale key in either the session or the request if one
 * can be found.
 */
public class JAMWikiLocaleInterceptor extends LocaleChangeInterceptor {

	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiServlet.class.getName());

	/**
	 * Ensure that the session locale value is set.
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
		Locale locale = (Locale)request.getSession().getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
		if (locale == null) {
			locale = this.setUserLocale(request, response);
		}
		if (locale != null) {
			LocaleResolver resolver = RequestContextUtils.getLocaleResolver(request);
			if (resolver != null) {
				resolver.setLocale(request, response, locale);
			}
		}
		return super.preHandle(request, response, handler);
	}

	/**
	 *
	 */
	private Locale retrieveUserLocale(HttpServletRequest request) throws ServletException {
		Locale locale = null;
		try {
			WikiUser user = ServletUtil.currentWikiUser();
			locale = LocaleUtils.toLocale(user.getDefaultLocale());
		} catch (AuthenticationCredentialsNotFoundException e) {
			// do nothing, just use a default locale
		}
		return locale;
	}

	/**
	 *
	 */
	private Locale setUserLocale(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		Locale locale = this.retrieveUserLocale(request);
		request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
		return locale;
	}
}
