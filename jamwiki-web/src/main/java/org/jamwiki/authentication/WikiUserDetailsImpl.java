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
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.model.Role;
import org.jamwiki.utils.WikiLogger;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JAMWiki implementation of the Spring Security <code>UserDetails</code> interface.
 * This object will be populated on successful login and made available as a way of
 * verifying user credentials.  Spring Security will use this object to determine
 * access rights.
 */
public class WikiUserDetailsImpl implements UserDetails {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiUserDetailsImpl.class.getName());
	/** The default anonymous username.  This value should match the Spring Security <code>AnonymousBeanDefinitionParser.DEF_USERNAME</code> value, which unfortunately is not public. */
	public static final String ANONYMOUS_USER_USERNAME = "roleAnonymous";
	private static final long serialVersionUID = -2818435399240684581L;
	private String username = null;
	private String password = null;
	/**
	 * GrantedAuthority is used by Spring Security to support several authorities
	 * (roles). Anonymous users are assigned ROLE_ANONYMOUS by the Spring Security
	 * filters.
	 */
	private Collection<GrantedAuthority> authorities;
	private boolean accountNonExpired = true;
	private boolean accountNonLocked = true;
	private boolean credentialsNonExpired = true;
	private boolean enabled = true;

	/**
	 * Construct the <code>User</code> with the details required by Spring Security
	 * <code>org.springframework.security.providers.dao.DaoAuthenticationProvider</code>.  This
	 * method should be used by systems that do NOT use the default JAMWiki
	 * user and group roles.  This method will NOT assign default roles to the
	 * user, and as a result the Special:Roles functionality will be ignored.
	 *
	 * @param username the username presented to the
	 *  <code>DaoAuthenticationProvider</code>
	 * @param password the password that should be presented to the
	 *  <code>DaoAuthenticationProvider</code>
	 * @param enabled set to <code>true</code> if the user is enabled
	 * @param accountNonExpired set to <code>true</code> if the account has not
	 *  expired
	 * @param credentialsNonExpired set to <code>true</code> if the credentials
	 *  have not expired
	 * @param accountNonLocked set to <code>true</code> if the account is not
	 *  locked
	 * @param authorities the authorities that should be granted to the caller
	 *  if they presented the correct username and password and the user
	 *  is enabled
	 * @throws IllegalArgumentException if a <code>null</code> value was passed
	 *  either as a parameter or as an element in the <code>authorities</code>
	 *  collection.
	 */
	public WikiUserDetailsImpl(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
		if (StringUtils.isBlank(username) || password == null) {
			throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
		}
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.accountNonExpired = accountNonExpired;
		this.credentialsNonExpired = credentialsNonExpired;
		this.accountNonLocked = accountNonLocked;
		if (authorities == null) {
			authorities = new ArrayList<GrantedAuthority>();
		}
		this.setAuthorities(authorities);
	}

	/**
	 * Private copy constructor.
	 */
	private WikiUserDetailsImpl(UserDetails userDetails) {
		this.username = userDetails.getUsername();
		this.password = userDetails.getPassword();
		this.enabled = userDetails.isEnabled();
		this.accountNonExpired = userDetails.isAccountNonExpired();
		this.credentialsNonExpired = userDetails.isCredentialsNonExpired();
		this.accountNonLocked = userDetails.isAccountNonLocked();
		this.setAuthorities(userDetails.getAuthorities());
	}

	/**
	 * Standard get method for determining the user's granted authorites (permissions).
	 *
	 * @return authorites, never null.
	 */
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * Standard set method for setting an array of granted authorities (permissions) for
	 * the user.  This method will overwrite any existing authorities.
	 */
	private void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		if (authorities == null) {
			throw new IllegalArgumentException("Cannot pass a null authorities array");
		}
		this.authorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority auth : authorities) {
			if (auth == null) {
				throw new IllegalArgumentException("Granted authorities cannot contain null elements");
			}
			this.authorities.add(auth);
		}
	}

	/**
	 * Standard get method to determine if the user account has expired.
	 */
	public boolean isAccountNonExpired() {
		return this.accountNonExpired;
	}

	/**
	 * Standard get method to determine if the user account has been locked.
	 */
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	/**
	 * Standard get method to determine if the user is still valid or if
	 * re-validation is required.
	 */
	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	/**
	 * Standard get method to determine if the current user account is enabled.
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 *
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Standard get method for the username (login).
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Convenience method for determining if a user has been assigned a role
	 * without the need to examine an array of Role objects.
	 *
	 * @param role If the user has been assigned this role then the method will
	 *  return <code>true</code>.
	 * @return <code>true</code> if the user has been assigned the specified
	 *  role, <code>false</code> otherwise.
	 */
	public boolean hasRole(Role role) {
		return this.hasRole(new SimpleGrantedAuthority(role.getAuthority()));
	}

	/**
	 * Convenience method for determining if a user has been assigned a role
	 * without the need to examine an array of Role objects.
	 *
	 * @param authority If the user has been assigned this role then the method will
	 *  return <code>true</code>.
	 * @return <code>true</code> if the user has been assigned the specified
	 *  role, <code>false</code> otherwise.
	 */
	public boolean hasRole(GrantedAuthority authority) {
		if (this.authorities == null) {
			logger.warn("No roles assigned for user " + this.getUsername());
			return false;
		}
		return authorities.contains(authority);
	}

	/**
	 * Utility method for converting a Spring Security <code>Authentication</code>
	 * object into a <code>WikiUserDetailsImpl</code>.  If the user is logged-in then the
	 * <code>Authentication</code> object will have the <code>WikiUserDetailsImpl</code>
	 * as its principal.  If the user is not logged in then create an empty
	 * <code>WikiUserDetailsImpl</code> object and assign it the same authorities as the
	 * <code>Authentication</code> object.
	 *
	 * @param auth The Spring Security <code>Authentication</code> object that is being
	 *  converted into a <code>WikiUserDetailsImpl</code> object.
	 * @return Returns a <code>WikiUserDetailsImpl</code> object that corresponds to the
	 *  Spring Security <code>Authentication</code> object.  If the user is not currently
	 *  logged-in then an empty <code>WikiUserDetailsImpl</code> with the same authorities
	 *  as the <code>Authentication</code> object is returned.  This method
	 *  will never return <code>null</code>.
	 * @throws AuthenticationCredentialsNotFoundException If authentication
	 *  credentials are unavailable.
	 */
	public static WikiUserDetailsImpl initWikiUserDetailsImpl(Authentication auth) throws AuthenticationCredentialsNotFoundException {
		if (auth == null) {
			throw new AuthenticationCredentialsNotFoundException("No authentication credential available");
		}
		if (auth instanceof AnonymousAuthenticationToken || !(auth.getPrincipal() instanceof UserDetails)) {
			// anonymous user
			return new WikiUserDetailsImpl(ANONYMOUS_USER_USERNAME, "", true, true, true, true, auth.getAuthorities());
		}
		// logged-in (or remembered) user
		if (auth.getPrincipal() instanceof WikiUserDetailsImpl) {
			return (WikiUserDetailsImpl)auth.getPrincipal();
		}
		return new WikiUserDetailsImpl((UserDetails)auth.getPrincipal());
	}
}
