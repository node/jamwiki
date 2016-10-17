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

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.AntPathRequestMatcher;
import org.springframework.security.web.util.RequestMatcher;
import org.jamwiki.utils.WikiLogger;

/**
 * This class provides a configurable bean object that can be used with the
 * JAMWikiAccessDeniedHandler to retrieve URL-specific error messages to
 * present to the user in the case of authorization or authentication failures.
 */
public class JAMWikiErrorMessageProvider {

	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiErrorMessageProvider.class.getName());
	private LinkedHashMap<RequestMatcher, String> matcherToKeyMap;
	private LinkedHashMap<String, String> urlPatterns;

	/**
	 *
	 */
	public String getErrorMessageKey(HttpServletRequest request) {
		return this.retrieveErrorKey(request);
	}

	/**
	 *
	 */
	public LinkedHashMap<String, String> getUrlPatterns() {
		return this.urlPatterns;
	}

	/**
	 *
	 */
	private String retrieveErrorKey(HttpServletRequest request) {
		if (this.matcherToKeyMap == null) {
			this.matcherToKeyMap = new LinkedHashMap<RequestMatcher, String>();
			for (Map.Entry<String, String> entry : this.getUrlPatterns().entrySet()) {
				this.matcherToKeyMap.put(new AntPathRequestMatcher(entry.getKey()), entry.getValue());
			}
		}
		for (Map.Entry<RequestMatcher, String> entry : this.matcherToKeyMap.entrySet()) {
			if (entry.getKey().matches(request)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 *
	 */
	public void setUrlPatterns(LinkedHashMap<String, String> urlPatterns) {
		this.urlPatterns = urlPatterns;
	}
}
