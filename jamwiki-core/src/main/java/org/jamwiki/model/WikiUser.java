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
package org.jamwiki.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an object representing Wiki-specific information about a user of
 * the Wiki.
 */
public class WikiUser implements Serializable {

	private Timestamp createDate = new Timestamp(System.currentTimeMillis());
	private String createIpAddress = "0.0.0.0";
	private String email;
	private Timestamp lastLoginDate = new Timestamp(System.currentTimeMillis());
	private String lastLoginIpAddress = "0.0.0.0";
	private final String username;
	private int userId = -1;
	private String displayName = null;
	private Map<String, String> preferences;
	private String challengeValue = null;
	private Timestamp challengeDate = null;
	private String challengeIp = null;
	private int challengeTries = 0;

	// Constants for user preference groups and preferences
	public static final String USER_PREFERENCES_GROUP_EDITING = "user.preferences.group.editing";
	public static final String USER_PREFERENCE_SIGNATURE = "user.signature";
	public static final String USER_PREFERENCE_PREFERRED_EDITOR = "user.preferred.editor";
	public static final String USER_PREFERENCES_GROUP_INTERNATIONALIZATION = "user.preferences.group.internationalization";
	public static final String USER_PREFERENCE_DEFAULT_LOCALE = "user.default.locale";
	public static final String USER_PREFERENCE_TIMEZONE = "user.timezone";
	public static final String USER_PREFERENCE_DATE_FORMAT = "user.date.format";
	public static final String USER_PREFERENCE_TIME_FORMAT = "user.time.format";

	/**
	 *
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 *
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 *
	 */
	public String getDefaultLocale() {
		return this.getPreferences().get(USER_PREFERENCE_DEFAULT_LOCALE);
	}

	/**
	 *
	 */
	public void setDefaultLocale(String defaultLocale) {
		this.getPreferences().put(USER_PREFERENCE_DEFAULT_LOCALE, defaultLocale);
	}

	/**
	 *
	 */
	public WikiUser(String username) {
		this.username = username;
	}

	/**
	 *
	 */
	public Timestamp getCreateDate() {
		return this.createDate;
	}

	/**
	 *
	 */
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	/**
	 *
	 */
	public String getCreateIpAddress() {
		return this.createIpAddress;
	}

	/**
	 *
	 */
	public void setCreateIpAddress(String createIpAddress) {
		this.createIpAddress = createIpAddress;
	}

	/**
	 *
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 *
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 *
	 */
	public Timestamp getLastLoginDate() {
		return this.lastLoginDate;
	}

	/**
	 *
	 */
	public void setLastLoginDate(Timestamp lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	/**
	 *
	 */
	public String getLastLoginIpAddress() {
		return this.lastLoginIpAddress;
	}

	/**
	 *
	 */
	public void setLastLoginIpAddress(String lastLoginIpAddress) {
		this.lastLoginIpAddress = lastLoginIpAddress;
	}

	/**
	 *
	 */
	public int getUserId() {
		return this.userId;
	}

	/**
	 *
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 *
	 */
	public String getUsername() {
		return username;
	}

	/**
	 *
	 */
	public Map<String, String> getPreferences() {
		if (this.preferences == null) {
			this.preferences = new HashMap<String, String>();
		}
		return preferences;
	}

	/**
	 *
	 */
	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	/**
	 * Shortcut to get the user signature
	 */
	public String getSignature() {
		return this.getPreferences().get(USER_PREFERENCE_SIGNATURE);
	}

	/**
	 *
	 */
	public void setSignature(String signature) {
		this.getPreferences().put(USER_PREFERENCE_SIGNATURE, signature);
	}

	/**
	 * Helper method to get a specific preference
	 */
	public String getPreference(String preferenceKey) {
		return this.getPreferences().get(preferenceKey);
	}

	/**
	 * Helper method to set a specific preference
	 */
	public void setPreference(String preferenceKey, String preferenceValue) {
		this.getPreferences().put(preferenceKey, preferenceValue);
	}

	/**
	 * Return the challenge key used when resetting password.
	 */
	public String getChallengeValue() {
		return challengeValue;
	}

	/**
	 * Set the challenge key used when resetting password.
	 */
	public void setChallengeValue(String challengeValue) {
		this.challengeValue = challengeValue;
	}

	/**
	 * Return the date for which a password reset was last requested.
	 */
	public Timestamp getChallengeDate() {
		return challengeDate;
	}

	/**
	 * Set the date for which a password reset was last requested.
	 */
	public void setChallengeDate(Timestamp challengeDate) {
		this.challengeDate = challengeDate;
	}

	/**
	 * Return the IP address from which a password reset was last made.
	 */
	public String getChallengeIp() {
		return challengeIp;
	}

	/**
	 * Set the IP address from which a password reset was last made.
	 */
	public void setChallengeIp(String challengeIp) {
		this.challengeIp = challengeIp;
	}

	/**
	 * Return the number of password reset requests that have been recently made.
	 */
	public int getChallengeTries() {
		return challengeTries;
	}

	/**
	 * Set the number of password reset requests that have been recently made.
	 */
	public void setChallengeTries(int challengeTries) {
		this.challengeTries = challengeTries;
	}

	/**
	 *
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("WikiUser ID " + userId  + ": " + username + "; displayName: " + displayName);
		sb.append("; Password reset challenge Data: challenge=" + challengeValue + "; challenge date=" + challengeDate + "; challenge IP=" + challengeIp + "; challenge request count=" + challengeTries);
		sb.append("; preferences: ");
		for (String key : preferences.keySet()) {
			sb.append(key + "=" + this.getPreferences().get(key) + "; ");
		}
		return sb.toString();
	}
}
