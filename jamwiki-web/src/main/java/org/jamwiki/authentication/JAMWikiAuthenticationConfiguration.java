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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Role;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * This class acts as a utility class for holding information used by the authentication
 * and authorization code.
 */
public class JAMWikiAuthenticationConfiguration {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAuthenticationConfiguration.class.getName());

	/**
	 *
	 */
	public static Collection<GrantedAuthority> getDefaultGroupRoles() {
		if (WikiUtil.isFirstUse() || WikiUtil.isUpgrade()) {
			// only query for authorities if wiki is fully setup
			return null;
		}
		try {
			return JAMWikiAuthenticationConfiguration.roleToGrantedAuthority(WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_REGISTERED_USER));
		} catch (DataAccessException e) {
			// FIXME - without default roles bad things happen, so should this throw the
			// error to the calling method?
			logger.error("Unable to retrieve default roles for " + WikiGroup.GROUP_REGISTERED_USER, e);
		}
		return null;
	}

	/**
	 *
	 */
	public static Collection<GrantedAuthority> getJamwikiAnonymousAuthorities() {
		if (WikiUtil.isFirstUse() || WikiUtil.isUpgrade()) {
			// only query for authorities if wiki is fully setup
			return null;
		}
		try {
			return JAMWikiAuthenticationConfiguration.roleToGrantedAuthority(WikiBase.getDataHandler().getRoleMapGroup(WikiGroup.GROUP_ANONYMOUS));
		} catch (DataAccessException e) {
			logger.error("Failure while initializing JAMWiki anonymous user authorities", e);
		}
		return null;
	}

	/**
	 * The DataHandler interface returns Role objects, but the AnsiDataHandler returns Role
	 * so cast the results appropriately.
	 */
	private static Collection<GrantedAuthority> roleToGrantedAuthority(List<Role> roles) {
		if (roles == null) {
			return null;
		}
		Collection<GrantedAuthority> results = new ArrayList<GrantedAuthority>();
		for (Role role : roles) {
			results.add(new SimpleGrantedAuthority(role.getAuthority()));
		}
		return results;
	}
}
