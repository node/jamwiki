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
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Role;
import org.jamwiki.utils.WikiUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Loads user data from JAMWiki database.
 *
 * @author Rainer Schmitz
 * @version $Id: $
 * @since 28.11.2006
 */
public class JAMWikiDaoImpl implements UserDetailsService {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		if (StringUtils.isBlank(username)) {
			throw new UsernameNotFoundException("Cannot retrieve user without a valid username");
		}
		String encryptedPassword = null;
		try {
			encryptedPassword = WikiBase.getDataHandler().lookupWikiUserEncryptedPassword(username);
		} catch (org.jamwiki.DataAccessException e) {
			throw new DataAccessResourceFailureException("Unable to retrieve authorities for user: " + username, e);
		}
		if (encryptedPassword == null) {
			throw new UsernameNotFoundException("Failure retrieving user information for " + username);
		}
		Collection<GrantedAuthority> authorities = this.retrieveUserAuthorities(username);
		return new WikiUserDetailsImpl(username, encryptedPassword, true, true, true, true, authorities);
	}

	/**
	 *
	 */
	private Collection<GrantedAuthority> retrieveUserAuthorities(String username) throws DataAccessException {
		if (WikiUtil.isFirstUse()) {
			return new ArrayList<GrantedAuthority>();
		}
		// add authorities given to all users
		Collection<GrantedAuthority> results = new ArrayList<GrantedAuthority>();
		if (JAMWikiAuthenticationConfiguration.getDefaultGroupRoles() != null) {
			results.addAll(JAMWikiAuthenticationConfiguration.getDefaultGroupRoles());
		}
		// add authorities specific to this user
		if (!StringUtils.isBlank(username)) {
			// FIXME - log error for blank username?  RegisterServlet will trigger that.
			try {
				List<Role> roles = WikiBase.getDataHandler().getRoleMapUser(username);
				if (roles != null) {
					for (Role role : roles) {
						results.add(new SimpleGrantedAuthority(role.getAuthority()));
					}
				}
			} catch (org.jamwiki.DataAccessException e) {
				throw new DataAccessResourceFailureException("Unable to retrieve authorities for user: " + username, e);
			}
		}
		return results;
	}
}
