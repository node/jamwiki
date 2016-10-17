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

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiConfiguration;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.authentication.WikiUserDetailsImpl;
import org.jamwiki.db.WikiDatabase;
import org.jamwiki.mail.WikiMail;
import org.jamwiki.model.Role;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiConfigurationObject;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.WikiCache;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.jamwiki.web.utils.SpamFilter;
import org.jamwiki.web.utils.UserPreferencesUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to provide administrative functions including changing Wiki
 * configuration settings and refreshing internal Wiki objects.
 */
public class AdminServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(AdminServlet.class.getName());
	/** The name of the JSP file used to render the servlet output for the admin maintenance configuration. */
	protected static final String JSP_ADMIN = "admin.jsp";
	/** The name of the JSP file used to render the servlet output for the admin maintenance functionality. */
	protected static final String JSP_ADMIN_SYSTEM = "admin-maintenance.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String function = request.getParameter("function");
		next.addObject("function", function);
		if (StringUtils.isBlank(function) && ServletUtil.isTopic(request, "Special:Maintenance")) {
			viewAdminSystem(request, next, pageInfo);
		} else if (StringUtils.isBlank(function)) {
			viewAdmin(request, next, pageInfo, null);
		} else if (function.equals("cache")) {
			cache(request, next, pageInfo);
		} else if (function.equals("search")) {
			refreshIndex(request, next, pageInfo);
		} else if (function.equals("properties")) {
			properties(request, next, pageInfo);
		} else if (function.equals("logitems")) {
			logItems(request, next, pageInfo);
		} else if (function.equals("recentchanges")) {
			recentChanges(request, next, pageInfo);
		} else if (function.equals("spam")) {
			spam(request, next, pageInfo);
		} else if (function.equals("migrate")) {
			migrateDatabase(request, next, pageInfo);
		} else if (function.equals("password")) {
			password(request, next, pageInfo);
		} else if (function.equals("adduser")) {
			adduser(request, next, pageInfo);
		} else if (function.equals("namespaces")) {
			namespaces(request, next, pageInfo);
		} else if (function.equals("links")) {
			links(request, next, pageInfo);
		}
		return next;
	}

	/**
	 * add new user account
	 */
	private void adduser(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String userLogin = request.getParameter("adduserLogin");
		String password = request.getParameter("adduserPassword");
		String confirmPassword = request.getParameter("adduserPasswordConfirm");
		String email = request.getParameter("adduserEmail");
		String displayName = request.getParameter("adduserdisplayName");
		try {
			WikiUser user = WikiBase.getDataHandler().lookupWikiUser(userLogin);
			if (user != null) {
				throw new WikiException(new WikiMessage("admin.adduser.message.uidexists", userLogin));
			}
			WikiUtil.validatePassword(password, confirmPassword);
			String encryptedPassword = Encryption.encrypt(password);
			user = new WikiUser(userLogin);
			user.setDisplayName(displayName);
			user.setEmail(email);
			user.setCreateIpAddress(ServletUtil.getIpAddress(request));
			user.setLastLoginIpAddress(ServletUtil.getIpAddress(request));
			WikiBase.getDataHandler().writeWikiUser(user, userLogin, encryptedPassword);
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		} catch (Exception e) {
			logger.error("Failure while create new user account", e);
			pageInfo.addError(new WikiMessage("admin.message.adduserfail", e.getMessage()));
		}
		if (!pageInfo.getErrors().isEmpty()) {
			next.addObject("adduserLogin", userLogin);
			next.addObject("adduserPassword", password);
			next.addObject("adduserPasswordConfirm", confirmPassword);
			next.addObject("adduserEmail", email);
			next.addObject("adduserdisplayName", displayName);
		} else {
			pageInfo.addMessage(new WikiMessage("admin.adduser.message.success", userLogin));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void cache(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			WikiCache.initialize();
			pageInfo.addMessage(new WikiMessage("admin.message.cache"));
		} catch (Exception e) {
			logger.error("Failure while clearing cache", e);
			pageInfo.addError(new WikiMessage("admin.cache.message.clearfailed", e.getMessage()));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void links(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException {
		int[] resultArray = WikiDatabase.rebuildTopicMetadata();
		pageInfo.addMessage(new WikiMessage("admin.maintenance.message.metadata", Integer.toString(resultArray[0])));
		if (resultArray[1] != 0) {
			pageInfo.addError(new WikiMessage("admin.maintenance.error.metadata", Integer.toString(resultArray[1])));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void logItems(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			WikiBase.getDataHandler().reloadLogItems();
			pageInfo.addMessage(new WikiMessage("admin.message.logitems"));
		} catch (Exception e) {
			logger.error("Failure while loading log items", e);
			pageInfo.addError(new WikiMessage("admin.message.logitemsfail", e.getMessage()));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void migrateDatabase(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		Properties props = new Properties();
		try {
			setProperty(props, request, Environment.PROP_BASE_PERSISTENCE_TYPE);
			if (props.getProperty(Environment.PROP_BASE_PERSISTENCE_TYPE).equals(WikiBase.PERSISTENCE_EXTERNAL)) {
				setProperty(props, request, Environment.PROP_DB_DRIVER);
				setProperty(props, request, Environment.PROP_DB_TYPE);
				setProperty(props, request, Environment.PROP_DB_URL);
				setProperty(props, request, Environment.PROP_DB_USERNAME);
				setPassword(props, request, next, Environment.PROP_DB_PASSWORD, "dbPassword");
			} else {
				// reverting from external database to an internal database
				props.setProperty(Environment.PROP_BASE_FILE_DIR, Environment.getValue(Environment.PROP_BASE_FILE_DIR));
				WikiDatabase.setupDefaultDatabase(props);
			}
			// migrate from the current database to the new database
			// identified by the properties
			// Will return errors if the new database cannot be connected to,
			// if it is already populated, or an error occurs copying the contents
			WikiDatabase.migrateDatabase(props, pageInfo.getErrors());
			if (this.saveProperties(request, next, pageInfo, props)) {
				pageInfo.addMessage(new WikiMessage("admin.message.migratedatabase", Environment.getValue(Environment.PROP_DB_URL)));
			}
		} catch (Exception e) {
			logger.error("Failure while migrating to a new database", e);
			pageInfo.addError(new WikiMessage("admin.message.migrationfailure", e.getMessage()));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void namespaces(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		try {
			int numUpdated = WikiDatabase.fixIncorrectTopicNamespaces();
			pageInfo.addMessage(new WikiMessage("admin.maintenance.message.topicsUpdated", Integer.toString(numUpdated)));
		} catch (DataAccessException e) {
			logger.error("Failure while fixing incorrect topic namespaces", e);
			pageInfo.addError(new WikiMessage("admin.maintenance.error.namespacefail", e.getMessage()));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void password(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String userLogin = request.getParameter("passwordLogin");
		String newPassword = request.getParameter("passwordPassword");
		String confirmPassword = request.getParameter("passwordPasswordConfirm");
		try {
			WikiUser user = WikiBase.getDataHandler().lookupWikiUser(userLogin);
			if (user == null) {
				throw new WikiException(new WikiMessage("admin.password.message.invalidlogin", userLogin));
			}
			WikiUtil.validatePassword(newPassword, confirmPassword);
			String encryptedPassword = Encryption.encrypt(newPassword);
			WikiBase.getDataHandler().writeWikiUser(user, userLogin, encryptedPassword);
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		} catch (Exception e) {
			logger.error("Failure while updating user password", e);
			pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
		}
		if (!pageInfo.getErrors().isEmpty()) {
			next.addObject("passwordLogin", userLogin);
			next.addObject("passwordPassword", newPassword);
			next.addObject("passwordPasswordConfirm", confirmPassword);
		} else {
			pageInfo.addMessage(new WikiMessage("admin.password.message.success", userLogin));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	private void properties(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// Load current properties to avoid losing those that are outside of the specific section being modified
		Properties props = Environment.getInstance();
		try {
			// general
			setProperty(props, request, Environment.PROP_BASE_FILE_DIR);
			setProperty(props, request, Environment.PROP_SERVER_URL);
			setProperty(props, request, Environment.PROP_SITE_NAME);
			setProperty(props, request, Environment.PROP_BASE_LOGO_IMAGE);
			setProperty(props, request, Environment.PROP_BASE_DEFAULT_TOPIC);
			setNumericProperty(props, request, Environment.PROP_RECENT_CHANGES_NUM, pageInfo.getErrors());
			setBooleanProperty(props, request, Environment.PROP_TOPIC_USE_PREVIEW);
			setBooleanProperty(props, request, Environment.PROP_TOPIC_USE_SHOW_CHANGES);
			setProperty(props, request, Environment.PROP_BASE_META_DESCRIPTION);
			setProperty(props, request, Environment.PROP_BASE_SEARCH_ENGINE);
			setProperty(props, request, Environment.PROP_TOPIC_EDITOR);
			setNumericProperty(props, request, Environment.PROP_MAX_TOPIC_VERSION_EXPORT, pageInfo.getErrors());
			// parser
			setProperty(props, request, Environment.PROP_PARSER_CLASS);
			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_HTML);
			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_JAVASCRIPT);
			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_TEMPLATES);
			setBooleanProperty(props, request, Environment.PROP_PRINT_NEW_WINDOW);
			setBooleanProperty(props, request, Environment.PROP_EXTERNAL_LINK_NEW_WINDOW);
			setBooleanProperty(props, request, Environment.PROP_PARSER_USE_NUMBERED_HTML_LINKS);
			setBooleanProperty(props, request, Environment.PROP_PARSER_TOC);
			setNumericProperty(props, request, Environment.PROP_PARSER_TOC_DEPTH, pageInfo.getErrors());
			setBooleanProperty(props, request, Environment.PROP_PARSER_ALLOW_CAPITALIZATION);
			setBooleanProperty(props, request, Environment.PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE);
			setBooleanProperty(props, request, Environment.PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE);
			setBooleanProperty(props, request, Environment.PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS);
			setNumericProperty(props, request, Environment.PROP_IMAGE_RESIZE_INCREMENT, pageInfo.getErrors());
			setProperty(props, request, Environment.PROP_PARSER_SIGNATURE_USER_PATTERN);
			setDatePatternProperty(props, request, Environment.PROP_PARSER_SIGNATURE_DATE_PATTERN, pageInfo.getErrors());
			// database
			setProperty(props, request, Environment.PROP_BASE_PERSISTENCE_TYPE);
			if (props.getProperty(Environment.PROP_BASE_PERSISTENCE_TYPE).equals(WikiBase.PERSISTENCE_EXTERNAL)) {
				setProperty(props, request, Environment.PROP_DB_DRIVER);
				setProperty(props, request, Environment.PROP_DB_TYPE);
				setProperty(props, request, Environment.PROP_DB_URL);
				setProperty(props, request, Environment.PROP_DB_USERNAME);
				setPassword(props, request, next, Environment.PROP_DB_PASSWORD, "dbPassword");
			} else {
				WikiDatabase.setupDefaultDatabase(props);
			}
			setNumericProperty(props, request, Environment.PROP_DBCP_MAX_ACTIVE, pageInfo.getErrors());
			setNumericProperty(props, request, Environment.PROP_DBCP_MAX_IDLE, pageInfo.getErrors());
			setNumericProperty(props, request, Environment.PROP_DBCP_MAX_OPEN_PREPARED_STATEMENTS, pageInfo.getErrors());
			setBooleanProperty(props, request, Environment.PROP_DBCP_POOL_PREPARED_STATEMENTS);
			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_ON_BORROW);
			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_ON_RETURN);
			setBooleanProperty(props, request, Environment.PROP_DBCP_TEST_WHILE_IDLE);
			setNumericProperty(props, request, Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME, pageInfo.getErrors());
			setNumericProperty(props, request, Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS, pageInfo.getErrors());
			setNumericProperty(props, request, Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN, pageInfo.getErrors());
			setProperty(props, request, Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION);
			// upload
			setProperty(props, request, Environment.PROP_FILE_UPLOAD_STORAGE);
			String maxFileSizeString = request.getParameter(Environment.PROP_FILE_MAX_FILE_SIZE);
			if (StringUtils.isBlank(maxFileSizeString) || !StringUtils.isNumeric(maxFileSizeString)) {
				pageInfo.addError(new WikiMessage("admin.message.nonnumeric", Environment.PROP_FILE_MAX_FILE_SIZE, maxFileSizeString));
			} else {
				long maxFileSizeInKB = Long.parseLong(maxFileSizeString);
				props.setProperty(Environment.PROP_FILE_MAX_FILE_SIZE, Long.toString(maxFileSizeInKB * 1000));
			}
			setProperty(props, request, Environment.PROP_FILE_DIR_FULL_PATH);
			setProperty(props, request, Environment.PROP_FILE_DIR_RELATIVE_PATH);
			setProperty(props, request, Environment.PROP_FILE_SERVER_URL);
			setProperty(props, request, Environment.PROP_SHARED_UPLOAD_VIRTUAL_WIKI);
			setProperty(props, request, Environment.PROP_FILE_BLACKLIST_TYPE);
			setProperty(props, request, Environment.PROP_FILE_BLACKLIST);
			setProperty(props, request, Environment.PROP_FILE_WHITELIST);
			// email
			setBooleanProperty(props, request, Environment.PROP_EMAIL_SMTP_ENABLE);
			if (props.getProperty(Environment.PROP_EMAIL_SMTP_ENABLE).equals("true")) {
				setBooleanProperty(props, request, Environment.PROP_EMAIL_SMTP_REQUIRES_AUTH);
				setProperty(props, request, Environment.PROP_EMAIL_SMTP_USERNAME);
				setPassword(props, request, next, Environment.PROP_EMAIL_SMTP_PASSWORD, "smtpPassword");
				setBooleanProperty(props, request, Environment.PROP_EMAIL_SMTP_USE_SSL);
				setProperty(props, request, Environment.PROP_EMAIL_REPLY_ADDRESS);
				setProperty(props, request, Environment.PROP_EMAIL_SMTP_HOST);
				setNumericProperty(props, request, Environment.PROP_EMAIL_SMTP_PORT, pageInfo.getErrors());
				setProperty(props, request, Environment.PROP_EMAIL_ADDRESS_SEPARATOR);
				setProperty(props, request, Environment.PROP_EMAIL_DEFAULT_CONTENT_TYPE);
				setBooleanProperty(props, request, Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD);
				if (props.getProperty(Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD).equals("true")) {
					setNumericProperty(props, request, Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD_CHALLENGE_TIMEOUT, pageInfo.getErrors());
					setNumericProperty(props, request, Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD_CHALLENGE_RETRIES, pageInfo.getErrors());
					setNumericProperty(props, request, Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD_IP_LOCK_DURATION, pageInfo.getErrors());
				}
				// Check if we have to test the configuration
				String command = request.getParameter("testMail");
				if (!StringUtils.isBlank(command) && command.equalsIgnoreCase("send test mail")) {
					String mailAddress = null;
					try {
						WikiMail sender = new WikiMail(props);
						logger.error(sender.toString());
						mailAddress = ServletUtil.currentWikiUser().getEmail();
						if (StringUtils.isBlank(mailAddress)) {
							pageInfo.addError(new WikiMessage("admin.smtp.error.nomailaddress"));
						} else {
							sender.postMail(mailAddress,"JAMWiki mail test","The configuration of your mail is correct");
							pageInfo.addMessage(new WikiMessage("admin.smtp.message.sentmail",mailAddress));
						}
					} catch(Exception e) {
						pageInfo.addError(new WikiMessage("admin.smtp.error.couldnotsend", mailAddress, e.getMessage()));
					}
				}
			}
			// spam
			setBooleanProperty(props, request, Environment.PROP_TOPIC_SPAM_FILTER);
			setNumericProperty(props, request, Environment.PROP_RECAPTCHA_EDIT, pageInfo.getErrors());
			setNumericProperty(props, request, Environment.PROP_RECAPTCHA_REGISTER, pageInfo.getErrors());
			setProperty(props, request, Environment.PROP_RECAPTCHA_PUBLIC_KEY);
			setProperty(props, request, Environment.PROP_RECAPTCHA_PRIVATE_KEY);
			if (StringUtils.isBlank(props.getProperty(Environment.PROP_RECAPTCHA_PUBLIC_KEY)) || StringUtils.isBlank(props.getProperty(Environment.PROP_RECAPTCHA_PRIVATE_KEY))) {
				if (NumberUtils.toInt(props.getProperty(Environment.PROP_RECAPTCHA_EDIT)) > 0 || NumberUtils.toInt(props.getProperty(Environment.PROP_RECAPTCHA_REGISTER)) > 0) {
					pageInfo.addError(new WikiMessage("admin.spam.message.invalidkeys"));
				}
			}
			setBooleanProperty(props, request, Environment.PROP_HONEYPOT_FILTER_ENABLED);
			setProperty(props, request, Environment.PROP_HONEYPOT_ACCESS_KEY);
			if (Boolean.parseBoolean(props.getProperty(Environment.PROP_HONEYPOT_FILTER_ENABLED)) && StringUtils.isBlank(props.getProperty(Environment.PROP_HONEYPOT_ACCESS_KEY))) {
				pageInfo.addError(new WikiMessage("admin.spam.message.invalidhoneypotkey"));
			}
			// other
			setBooleanProperty(props, request, Environment.PROP_RSS_ALLOWED);
			setProperty(props, request, Environment.PROP_RSS_TITLE);
			pageInfo.getErrors().addAll(WikiUtil.validateSystemSettings(props));
			if (pageInfo.getErrors().isEmpty()) {
				this.saveUserPreferenceDefaults(request, pageInfo);
			}
			if (this.saveProperties(request, next, pageInfo, props)) {
				pageInfo.addMessage(new WikiMessage("admin.message.changessaved"));
			}
		} catch (Exception e) {
			logger.error("Failure while processing property values", e);
			pageInfo.addError(new WikiMessage("admin.message.propertyfailure", e.getMessage()));
		}
		viewAdmin(request, next, pageInfo, props);
	}

	/**
	 *
	 */
	private void recentChanges(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			WikiBase.getDataHandler().reloadRecentChanges();
			pageInfo.addMessage(new WikiMessage("admin.message.recentchanges"));
		} catch (Exception e) {
			logger.error("Failure while loading recent changes", e);
			pageInfo.addError(new WikiMessage("admin.message.recentchangesfail", e.getMessage()));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void refreshIndex(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			WikiBase.getSearchEngine().refreshIndex();
			pageInfo.addMessage(new WikiMessage("admin.message.indexrefreshed"));
		} catch (Exception e) {
			logger.error("Failure while refreshing search index", e);
			pageInfo.addError(new WikiMessage("admin.message.searchrefresh", e.getMessage()));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private boolean saveProperties(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, Properties props) throws Exception {
		if (!pageInfo.getErrors().isEmpty()) {
			// insert a general warning about changes not being saved as the first error
			pageInfo.getErrors().add(0, new WikiMessage("admin.message.changesnotsaved"));
			return false;
		}
		// all is well, save the properties
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			Environment.setValue(entry.getKey().toString(), entry.getValue().toString());
		}
		Environment.saveConfiguration();
		// re-initialize to reset database settings (if needed)
		WikiUserDetailsImpl userDetails = ServletUtil.currentUserDetails();
		if (userDetails.hasRole(Role.ROLE_ANONYMOUS)) {
			throw new IllegalArgumentException("Cannot pass null or anonymous WikiUser object to setupAdminUser");
		}
		WikiUser user = ServletUtil.currentWikiUser();
		WikiBase.reset(request.getLocale(), user, user.getUsername(), null);
		return true;
	}

	/**
	 * Default user preferences are stored in the database rather than a
	 * properties file and must be handled differently.  Only call this method
	 * when preferences should actually be processed and saved.
	 */
	private static void saveUserPreferenceDefaults(HttpServletRequest request, WikiPageInfo pageInfo) {
		try {
			setUserPreferenceDefault(request, WikiUser.USER_PREFERENCE_DEFAULT_LOCALE, WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, 1);
			setUserPreferenceDefault(request, WikiUser.USER_PREFERENCE_TIMEZONE, WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, 2);
			setUserPreferenceDefault(request, WikiUser.USER_PREFERENCE_DATE_FORMAT, WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, 3);
			setUserPreferenceDefault(request, WikiUser.USER_PREFERENCE_TIME_FORMAT, WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, 4);
			setUserPreferenceDefault(request, WikiUser.USER_PREFERENCE_PREFERRED_EDITOR, WikiUser.USER_PREFERENCES_GROUP_EDITING, 1);
			setUserPreferenceDefault(request, WikiUser.USER_PREFERENCE_SIGNATURE, WikiUser.USER_PREFERENCES_GROUP_EDITING, 2);
		} catch (DataAccessException e) {
			logger.error("Failure while updating default user preferences", e);
			pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
		}
	}

	/**
	 *
	 */
	private static void setBooleanProperty(Properties props, HttpServletRequest request, String parameter) {
		boolean value = (request.getParameter(parameter) != null);
		props.setProperty(parameter, Boolean.toString(value));
	}

	/**
	 * Utility method for setting a property that represents a SimpleDateFormat pattern.
	 * If the pattern is invalid or <code>null</code> then an error is generated.
	 */
	private static void setDatePatternProperty(Properties props, HttpServletRequest request, String parameter, List<WikiMessage> errors) {
		String value = request.getParameter(parameter);
		if (!StringUtils.equalsIgnoreCase(value, "SHORT") && !StringUtils.equalsIgnoreCase(value, "MEDIUM") && !StringUtils.equalsIgnoreCase(value, "LONG") && !StringUtils.equalsIgnoreCase(value, "FULL")) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(value);
			} catch (Exception e) {
				errors.add(new WikiMessage("admin.message.date.error", parameter, value));
			}
		}
		props.setProperty(parameter, value);
	}

	/**
	 *
	 */
	private static void setPassword(Properties props, HttpServletRequest request, ModelAndView next, String parameter, String passwordParam) throws Exception {
		String value = request.getParameter(parameter);
		if (!StringUtils.isBlank(value)) {
			Encryption.setEncryptedProperty(parameter, value, props);
			next.addObject(passwordParam, request.getParameter(parameter));
		} else {
			props.setProperty(parameter, Environment.getValue(parameter));
		}
	}

	/**
	 *
	 */
	private static void setNumericProperty(Properties props, HttpServletRequest request, String parameter, List<WikiMessage> errors) {
		String value = request.getParameter(parameter);
		if (StringUtils.isBlank(value) || !StringUtils.isNumeric(value)) {
			errors.add(new WikiMessage("admin.message.nonnumeric", parameter, value));
		}
		props.setProperty(parameter, value);
	}

	/**
	 *
	 */
	private static void setProperty(Properties props, HttpServletRequest request, String parameter) {
		String value = request.getParameter(parameter);
		if (value == null) {
			value = "";
		}
		props.setProperty(parameter, value);
	}

	/**
	 *
	 */
	private static void setUserPreferenceDefault(HttpServletRequest request, String parameter, String group, int sequence) throws DataAccessException {
		String value = request.getParameter(parameter);
		if (!StringUtils.isBlank(value)) {
			WikiBase.getDataHandler().writeUserPreferenceDefault(parameter, value, group, sequence);
		}
	}

	/**
	 *
	 */
	private void spam(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			SpamFilter.reload();
			pageInfo.addMessage(new WikiMessage("admin.message.spamfilter"));
		} catch (Exception e) {
			logger.error("Failure while reloading spam filter patterns", e);
			pageInfo.addError(new WikiMessage("admin.message.spamfilterfail", e.getMessage()));
		}
		viewAdminSystem(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void viewAdmin(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, Properties props) {
		pageInfo.setContentJsp(JSP_ADMIN);
		pageInfo.setAdmin(true);
		pageInfo.setPageTitle(new WikiMessage("admin.title"));
		UserPreferencesUtil userPreferences = new UserPreferencesUtil(null);
		next.addObject("userPreferences", userPreferences);
		Map<String, String> editors = WikiConfiguration.getInstance().getEditors();
		next.addObject("editors", editors);
		List<WikiConfigurationObject> queryHandlers = WikiConfiguration.getInstance().getQueryHandlers();
		next.addObject("queryHandlers", queryHandlers);
		List<WikiConfigurationObject> searchEngines = WikiConfiguration.getInstance().getSearchEngines();
		next.addObject("searchEngines", searchEngines);
		List<WikiConfigurationObject> parsers = WikiConfiguration.getInstance().getParsers();
		next.addObject("parsers", parsers);
		List<String> smtpContentTypes = WikiConfiguration.getInstance().getSmtpContentTypes();
		next.addObject("smptContentTypes", smtpContentTypes);
		LinkedHashMap<Integer, String> poolExhaustedMap = new LinkedHashMap<Integer, String>();
		poolExhaustedMap.put(Integer.valueOf(GenericObjectPool.WHEN_EXHAUSTED_FAIL), "admin.persistence.caption.whenexhaustedaction.fail");
		poolExhaustedMap.put(Integer.valueOf(GenericObjectPool.WHEN_EXHAUSTED_BLOCK), "admin.persistence.caption.whenexhaustedaction.block");
		poolExhaustedMap.put(Integer.valueOf(GenericObjectPool.WHEN_EXHAUSTED_GROW), "admin.persistence.caption.whenexhaustedaction.grow");
		next.addObject("poolExhaustedMap", poolExhaustedMap);
		LinkedHashMap<Integer, String> blacklistTypesMap = new LinkedHashMap<Integer, String>();
		blacklistTypesMap.put(Integer.valueOf(WikiBase.UPLOAD_ALL), "admin.upload.caption.allowall");
		blacklistTypesMap.put(Integer.valueOf(WikiBase.UPLOAD_NONE), "admin.upload.caption.allownone");
		blacklistTypesMap.put(Integer.valueOf(WikiBase.UPLOAD_BLACKLIST), "admin.upload.caption.useblacklist");
		blacklistTypesMap.put(Integer.valueOf(WikiBase.UPLOAD_WHITELIST), "admin.upload.caption.usewhitelist");
		next.addObject("blacklistTypes", blacklistTypesMap);
		if (props == null) {
			props = Environment.getInstance();
		}
		long maximumFileSize = Long.valueOf(props.getProperty(Environment.PROP_FILE_MAX_FILE_SIZE))/1000;
		next.addObject("maximumFileSize", maximumFileSize);
		next.addObject("props", props);
		try {
			List<VirtualWiki> virtualWikiList = WikiBase.getDataHandler().getVirtualWikiList();
			next.addObject("virtualwikis", virtualWikiList);
		} catch (DataAccessException e) {
			logger.error("Failure while retrieving database records", e);
			pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
		}
	}

	/**
	 *
	 */
	private void viewAdminSystem(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		pageInfo.setContentJsp(JSP_ADMIN_SYSTEM);
		pageInfo.setAdmin(true);
		pageInfo.setPageTitle(new WikiMessage("admin.maintenance.title"));
		boolean allowExport = Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals(WikiBase.PERSISTENCE_INTERNAL);
		next.addObject("allowExport", allowExport);
		List<WikiConfigurationObject> queryHandlers = WikiConfiguration.getInstance().getQueryHandlers();
		next.addObject("queryHandlers", queryHandlers);
	}
}
