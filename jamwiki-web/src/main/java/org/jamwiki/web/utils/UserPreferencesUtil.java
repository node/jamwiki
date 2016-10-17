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

package org.jamwiki.web.utils;

import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiConfiguration;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.DateUtil;
import org.jamwiki.utils.WikiLogger;

public class UserPreferencesUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(UserPreferencesUtil.class.getName());
	private final WikiUser user;
	/** List of default preferences organized by group. */
	private Map<String, Map<String, String>> defaults;
	// This is a workaround. It should be possible to get the signature preview directly
	// from a method...
	public static String signaturePreview = null;

	/**
	 * Initialize an instance of this utility class for a given user.
	 *
	 * @param user The user whose preferences will be retrieved, or
	 *  <code>null</code> if default preferences should be retrieved.
	 */
	public UserPreferencesUtil(WikiUser user) {
		this.user = user;
	}

	/**
	 * Convenience method to retrieve the UserPreferenceItem for a single preference.
	 *
	 * @param preferenceKey
	 * @return The user preference corresponding to the specified key.
	 */
	public UserPreferenceItem getPreference(String preferenceKey) {
		Map<String, Map<String, UserPreferenceItem>> map = this.getGroups();
		for (String group : map.keySet()) {
			for (String key : map.get(group).keySet()) {
				if (preferenceKey.equals(key)) {
					return map.get(group).get(key);
				}
			}
		}
		return null;
	}

	/**
	 * The method return a map with the following structure:
	 * pref_group_key -> Map(pref_key -> Instance of UserPreferenceItem for pref_key)
	 * UserPreferenceItems implements the getters necessary to automate the display of
	 * user preferences choices in JSTL.
	 *
	 * @return A mapping of user preferences (key-value) grouped by preference group.
	 */
	public Map<String, Map<String, UserPreferenceItem>> getGroups() {
		Map<String, Map<String, UserPreferenceItem>> groups = new LinkedHashMap<String, Map<String, UserPreferenceItem>>();
		String lastKey = null;
		LinkedHashMap<String, UserPreferenceItem> items = null;
		for (String group : this.getDefaults().keySet()) {
			if (lastKey == null || !lastKey.equals(group)) {
				items = new LinkedHashMap<String, UserPreferenceItem>();
			}
			for (String item : this.getDefaults().get(group).keySet()) {
				items.put(item, new UserPreferenceItem(item));
			}
			groups.put(group, items);
		}
		return groups;
	}

	/**
	 * Return a map of date pattern-name for all date formats available for user selection.
	 */
	public Map<String, String> getAvailableDateFormats() {
		return DateUtil.getDateFormats(user);
	}

	/**
	 * Return a map of locale ID-name for all locales available for user selection.
	 */
	public Map<String, String> getAvailableLocales() {
		Map<String, String> locales = new TreeMap<String, String>();
		// first get any locales with explicit JAMWiki configuration
		locales.putAll(WikiConfiguration.getInstance().getTranslations());
		// now get the system locales
		Locale[] localeArray = Locale.getAvailableLocales();
		for (Locale locale : Locale.getAvailableLocales()) {
			String key = locale.toString();
			String value = key + " - " + locale.getDisplayName(locale);
			locales.put(key, value);
		}
		return locales;
	}

	/**
	 * Return a map of time pattern-name for all time formats available for user selection.
	 */
	public Map<String, String> getAvailableTimeFormats() {
		return DateUtil.getTimeFormats(user);
	}

	/**
	 * Return a map of time zone ID-name for all time zones available for user selection.
	 */
	public Map<String, String> getAvailableTimeZones() {
		return DateUtil.getTimeZoneMap();
	}

	/**
	 * Return the default date format pattern
	 */
	public String getDefaultDatePattern() {
		String pattern = this.retrieveDefaultValue(WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, WikiUser.USER_PREFERENCE_DATE_FORMAT);
		return (pattern != null) ? pattern : WikiConfiguration.getInstance().getDateFormats().get(0);
	}

	/**
	 * Return the ID for the default locale.
	 */
	public String getDefaultLocale() {
		String localeString = this.retrieveDefaultValue(WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, WikiUser.USER_PREFERENCE_DEFAULT_LOCALE);
		return DateUtil.stringToLocale(localeString).toString();
	}

	/**
	 * Return the default time format pattern
	 */
	public String getDefaultTimePattern() {
		String pattern = this.retrieveDefaultValue(WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, WikiUser.USER_PREFERENCE_TIME_FORMAT);
		return (pattern != null) ? pattern : WikiConfiguration.getInstance().getTimeFormats().get(0);
	}

	/**
	 * Return the ID for the default time zone.
	 */
	public String getDefaultTimeZone() {
		String timeZoneString = this.retrieveDefaultValue(WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, WikiUser.USER_PREFERENCE_TIMEZONE);
		return DateUtil.stringToTimeZone(timeZoneString).getID();
	}

	/**
	 * Retrieve a default value given its group and preference key.
	 */
	private String retrieveDefaultValue(String groupKey, String preferenceKey) {
		Map<String, String> groupMap = this.getDefaults().get(groupKey);
		return (groupMap != null) ? groupMap.get(preferenceKey) : null;
	}

	/**
	 * Return a list of default preferences organized by group.
	 */
	private Map<String, Map<String, String>> getDefaults() {
		if (this.defaults == null) {
			try {
				this.defaults = (Map<String, Map<String, String>>) WikiBase.getDataHandler().getUserPreferencesDefaults();
			} catch (DataAccessException e) {
				throw new IllegalStateException("Failure while retrieving default user preferences", e);
			}
		}
		return this.defaults;
	}

	/**
	 *
	 */
	public void setSignaturePreview(String signature) {
		// This is a workaround. It should be possible to get the signature preview directly
		// from a method...
		signaturePreview = signature;
	}

	/**
	 *
	 */
	public class UserPreferenceItem {
		String prefName = null;

		/**
		 *
		 */
		public UserPreferenceItem(String prefName) {
			this.prefName = prefName;
		}

		/**
		 *
		 */
		public String getKey() {
			return this.prefName;
		}

		/**
		 * This must match an entry in the ApplicationResources language file.
		 */
		public String getLabel() {
			return prefName + ".label";
		}

		/**
		 * This must match an entry in the ApplicationResources language file.
		 */
		public String getHelp() {
			return prefName + ".help";
		}

		/**
		 * Add an if statement if a new property must fill a drop down box with
		 * a list of key/value pairs. The key is the value stored in the database for
		 * the user, while value is used to display the content in the dropdown box.
		 */
		public Map getMap() {
			if (prefName.equals(WikiUser.USER_PREFERENCE_TIMEZONE)) {
				return getAvailableTimeZones();
			} else if (prefName.equals(WikiUser.USER_PREFERENCE_DEFAULT_LOCALE)) {
				return getAvailableLocales();
			} else if (prefName.equals(WikiUser.USER_PREFERENCE_PREFERRED_EDITOR)) {
				return WikiConfiguration.getInstance().getEditors();
			} else if (prefName.equals(WikiUser.USER_PREFERENCE_DATE_FORMAT)) {
				return getAvailableDateFormats();
			} else if (prefName.equals(WikiUser.USER_PREFERENCE_TIME_FORMAT)) {
				return getAvailableTimeFormats();
			}
			return null;
		}

		/**
		 * Add an if statement if a property must display a checkbox.
		 */
		public boolean getCheckbox() {
			return false;
		}

		/**
		 * Add an if statement if a property has a preview to display on screen.
		 */
		public String getPreview() {
			return (prefName.equals(WikiUser.USER_PREFERENCE_SIGNATURE)) ? UserPreferencesUtil.signaturePreview : null;
		}
	}
}
