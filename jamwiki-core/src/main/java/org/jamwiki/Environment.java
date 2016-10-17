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
package org.jamwiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
// FIXME - remove this import
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jamwiki.db.QueryHandler;
import org.jamwiki.utils.ResourceUtil;
import org.jamwiki.utils.SortedProperties;
import org.jamwiki.utils.WikiLogger;

/**
 * The <code>Environment</code> class is instantiated as a singleton to
 * provides access to JAMWiki property values stored in the
 * <code>jamwiki.properties</code> file.
 */
public class Environment {
	private static final WikiLogger logger = WikiLogger.getLogger(Environment.class.getName());

	public static final String PROP_BASE_COOKIE_EXPIRE = "cookie-expire";
	public static final String PROP_BASE_DEFAULT_TOPIC = "default-topic";
	public static final String PROP_BASE_FILE_DIR = "homeDir";
	public static final String PROP_BASE_INITIALIZED = "props-initialized";
	public static final String PROP_BASE_LOGO_IMAGE = "logo-image";
	public static final String PROP_BASE_META_DESCRIPTION = "meta-description";
	public static final String PROP_BASE_PERSISTENCE_TYPE = "persistenceType";
	public static final String PROP_BASE_SEARCH_ENGINE = "search-engine";
	public static final String PROP_BASE_WIKI_VERSION = "wiki-version";
	public static final String PROP_DB_DRIVER = "driver";
	public static final String PROP_DB_PASSWORD = "db-password";
	public static final String PROP_DB_TYPE = "database-type";
	public static final String PROP_DB_URL = "url";
	public static final String PROP_DB_USERNAME = "db-user";
	public static final String PROP_DBCP_MAX_ACTIVE = "dbcp-max-active";
	public static final String PROP_DBCP_MAX_IDLE = "dbcp-max-idle";
	public static final String PROP_DBCP_MAX_OPEN_PREPARED_STATEMENTS = "dbcp-max-open-prepared-statements";
	public static final String PROP_DBCP_MIN_EVICTABLE_IDLE_TIME = "dbcp-min-evictable-idle-time";
	public static final String PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN = "dbcp-num-tests-per-eviction-run";
	public static final String PROP_DBCP_POOL_PREPARED_STATEMENTS = "dbcp-pool-prepared-statements";
	public static final String PROP_DBCP_TEST_ON_BORROW = "dbcp-test-on-borrow";
	public static final String PROP_DBCP_TEST_ON_RETURN = "dbcp-test-on-return";
	public static final String PROP_DBCP_TEST_WHILE_IDLE = "dbcp-test-while-idle";
	public static final String PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS = "dbcp-time-between-eviction-runs";
	public static final String PROP_DBCP_WHEN_EXHAUSTED_ACTION = "dbcp-when-exhausted-action";
	public static final String PROP_EMAIL_SMTP_ENABLE = "smtp-enable";
	public static final String PROP_EMAIL_SMTP_REQUIRES_AUTH = "smtp-authentication";
	public static final String PROP_EMAIL_SMTP_USERNAME = "smtp-username";
	public static final String PROP_EMAIL_SMTP_PASSWORD = "smtp-userpass";
	public static final String PROP_EMAIL_SMTP_USE_SSL = "smtp-use-ssl";
	public static final String PROP_EMAIL_REPLY_ADDRESS = "smtp-reply-to";
	public static final String PROP_EMAIL_SMTP_HOST = "smtp-host";
	public static final String PROP_EMAIL_SMTP_PORT = "smtp-port";
	public static final String PROP_EMAIL_ADDRESS_SEPARATOR = "smtp-address-separator";
	public static final String PROP_EMAIL_DEFAULT_CONTENT_TYPE = "smtp-content-type";
	public static final String PROP_EMAIL_SERVICE_FORGOT_PASSWORD = "smtp-service-forgot-password-enable";
	public static final String PROP_EMAIL_SERVICE_FORGOT_PASSWORD_CHALLENGE_TIMEOUT = "smtp-service-forgot-password-challenge-timeout";
	public static final String PROP_EMAIL_SERVICE_FORGOT_PASSWORD_CHALLENGE_RETRIES = "smtp-service-forgot-password-challenge-retries";
	public static final String PROP_EMAIL_SERVICE_FORGOT_PASSWORD_IP_LOCK_DURATION = "smtp-service-forgot-password-ip-lock-duration";
	public static final String PROP_ENCRYPTION_ALGORITHM = "encryption-algorithm";
	public static final String PROP_EXTERNAL_LINK_NEW_WINDOW = "external-link-new-window";
	public static final String PROP_FILE_BLACKLIST = "file-blacklist";
	public static final String PROP_FILE_BLACKLIST_TYPE = "file-blacklist-type";
	public static final String PROP_FILE_DIR_FULL_PATH = "file-dir-full-path";
	public static final String PROP_FILE_DIR_RELATIVE_PATH = "file-dir-relative-path";
	public static final String PROP_FILE_MAX_FILE_SIZE = "max-file-size";
	public static final String PROP_FILE_SERVER_URL = "file-server-url";
	public static final String PROP_FILE_UPLOAD_STORAGE = "file-upload-storage";
	public static final String PROP_FILE_WHITELIST = "file-whitelist";
	public static final String PROP_HONEYPOT_ACCESS_KEY = "honeypot-access-key";
	public static final String PROP_HONEYPOT_FILTER_ENABLED = "honeypot-enabled";
	public static final String PROP_IMAGE_RESIZE_INCREMENT = "image-resize-increment";
	public static final String PROP_MAX_RECENT_CHANGES = "max-recent-changes";
	public static final String PROP_MAX_TOPIC_VERSION_EXPORT = "max-topic-version-export";
	public static final String PROP_PARSER_ALLOW_CAPITALIZATION = "allow-capitalization";
	public static final String PROP_PARSER_ALLOW_HTML = "allowHTML";
	public static final String PROP_PARSER_ALLOW_JAVASCRIPT = "allow-javascript";
	public static final String PROP_PARSER_ALLOW_TEMPLATES = "allow-templates";
	public static final String PROP_PARSER_CLASS = "parser";
	public static final String PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE = "parser-interwiki-links-inline";
	public static final String PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS = "display-special-virtual-wiki";
	public static final String PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE = "parser-virtualwiki-links-inline";
	/** Maximum number of template inclusions allowed on a page. */
	public static final String PROP_PARSER_MAX_INCLUSIONS = "parser-max-inclusions";
	/** This constant controls how many infinite loops a topic can hold before parsing aborts. */
	public static final String PROP_PARSER_MAXIMUM_INFINITE_LOOP_LIMIT = "parser-infinite-loop-limit";
	/** Maximum number of parser iterations allowed for a single parsing run. */
	public static final String PROP_PARSER_MAX_PARSER_ITERATIONS = "parser-max-iterations";
	/** Maximum depth to which templates can be included for a single parsing run. */
	public static final String PROP_PARSER_MAX_TEMPLATE_DEPTH = "parser-max-template-depth";
	public static final String PROP_PARSER_SIGNATURE_DATE_PATTERN = "signature-date";
	public static final String PROP_PARSER_SIGNATURE_USER_PATTERN = "signature-user";
	public static final String PROP_PARSER_TOC = "allow-toc";
	public static final String PROP_PARSER_TOC_DEPTH = "toc-depth";
	public static final String PROP_PARSER_USE_NUMBERED_HTML_LINKS = "use-numbered-html-links";
	public static final String PROP_PATTERN_INVALID_NAMESPACE_NAME = "pattern-namespace-name-invalid";
	public static final String PROP_PATTERN_INVALID_ROLE_NAME = "pattern-role-name-invalid";
	public static final String PROP_PATTERN_INVALID_TOPIC_PATTERN = "pattern-topic-name-invalid";
	public static final String PROP_PATTERN_VALID_USER_LOGIN = "pattern-login-valid";
	public static final String PROP_PATTERN_VALID_VIRTUAL_WIKI = "pattern-virtualwiki-valid";
	public static final String PROP_PRINT_NEW_WINDOW = "print-new-window";
	public static final String PROP_RECAPTCHA_EDIT = "recaptcha-edit";
	public static final String PROP_RECAPTCHA_PRIVATE_KEY = "recaptcha-private-key";
	public static final String PROP_RECAPTCHA_PUBLIC_KEY = "recaptcha-public-key";
	public static final String PROP_RECAPTCHA_REGISTER = "recaptcha-register";
	public static final String PROP_RECENT_CHANGES_NUM = "recent-changes-days";
	public static final String PROP_RSS_ALLOWED = "rss-allowed";
	public static final String PROP_RSS_TITLE = "rss-title";
	public static final String PROP_SERVER_URL = "server-url";
	public static final String PROP_SHARED_UPLOAD_VIRTUAL_WIKI = "shared-upload-virtual-wiki";
	public static final String PROP_SITE_NAME = "site-name";
	public static final String PROP_TOPIC_EDITOR = "default-editor";
	public static final String PROP_TOPIC_SPAM_FILTER = "use-spam-filter";
	public static final String PROP_TOPIC_USE_PREVIEW = "use-preview";
	public static final String PROP_TOPIC_USE_SHOW_CHANGES = "use-show-changes";
	public static final String PROP_VIRTUAL_WIKI_DEFAULT = "virtual-wiki-default";
	// Lookup properties file location from system properties first.
	private static final String PROPERTY_FILE_NAME = System.getProperty("jamwiki.property.file", "jamwiki.properties");

	public static final String PROP_ROLE_ADMIN = "role-admin";
	public static final String PROP_ROLE_ANONYMOUS = "role-anonymous";
	public static final String PROP_ROLE_EDIT_EXISTING = "role-edit-existing";
	public static final String PROP_ROLE_EDIT_NEW = "role-edit-new";
	public static final String PROP_ROLE_EMBEDDED = "role-embedded";
	public static final String PROP_ROLE_IMPORT = "role-import";
	public static final String PROP_ROLE_MOVE = "role-move";
	public static final String PROP_ROLE_NO_ACCOUNT = "role-no-account";
	public static final String PROP_ROLE_SYSADMIN = "role-sysadmin";
	public static final String PROP_ROLE_TRANSLATE = "role-translate";
	public static final String PROP_ROLE_UPLOAD = "role-upload";
	public static final String PROP_ROLE_VIEW = "role-view";
	public static final String PROP_ROLE_REGISTER = "role-register";

	private static Environment ENVIRONMENT_INSTANCE = null;
	private Properties defaults = null;
	private SortedProperties props = null;

	/**
	 * The constructor loads property values from the property file.
	 */
	private Environment() {
		this.initDefaultProperties();
		logger.debug("Default properties initialized: " + this.defaults.toString());
		this.props = loadProperties(PROPERTY_FILE_NAME, this.defaults);
		if ("true".equals(System.getProperty("jamwiki.override.file.properties"))) {
			overrideFromSystemProperties();
		}
		logger.debug("JAMWiki properties initialized: " + this.props.toString());
	}

	/**
	* Overrides file properties from system properties. Iterates over all properties
	* and checks if application server has defined overriding property. System wide
	* properties are prefixed with "jamwiki". These properties may be used to define
	* dynamic runtime properties (eg. upload path depends on environment).
	*/
	private void overrideFromSystemProperties() {
		logger.info("Overriding file properties with system properties.");
		Map<String, String> properties = propertiesToMap(this.props);
		for (String key : properties.keySet()) {
			String value = System.getProperty("jamwiki." + key);
			if (value != null) {
				this.props.setProperty(key, value);
				logger.info("Replaced property " + key + " with value: " + value);
			}
		}
	}

	/**
	 * Load a property file.  First check for the file in the path from which
	 * the application was started, then check other classpath locations.
	 *
	 * @param filename The name of the property file to be loaded.  This name can be
	 *  either absolute or relative; if relative then the file will be loaded from
	 *  the class path or from the directory from which the JVM was loaded.
	 * @return A File object containing the properties file instance.
	 * @throws IOException Thrown if the specified property file cannot
	 *  be located.
	 */
	private static File findProperties(String filename) throws IOException {
		// read in properties file
		File file = new File(filename);
		if (file.exists()) {
			return file; //NOPMD
		}
		// search for file in class loader path
		return Environment.retrievePropertyFile(filename);
	}

	/**
	 * Initialize the default property values.
	 */
	private void initDefaultProperties() {
		this.defaults = new Properties();
		this.defaults.setProperty(PROP_BASE_COOKIE_EXPIRE, "31104000");
		this.defaults.setProperty(PROP_BASE_DEFAULT_TOPIC, "StartingPoints");
		this.defaults.setProperty(PROP_BASE_FILE_DIR, "");
		this.defaults.setProperty(PROP_BASE_INITIALIZED, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_BASE_LOGO_IMAGE, "logo_oliver.gif");
		this.defaults.setProperty(PROP_BASE_META_DESCRIPTION, "");
		this.defaults.setProperty(PROP_BASE_PERSISTENCE_TYPE, WikiBase.PERSISTENCE_INTERNAL);
		this.defaults.setProperty(PROP_BASE_SEARCH_ENGINE, SearchEngine.SEARCH_ENGINE_LUCENE);
		this.defaults.setProperty(PROP_BASE_WIKI_VERSION, "0.0.0");
		this.defaults.setProperty(PROP_DB_DRIVER, "");
		this.defaults.setProperty(PROP_DB_PASSWORD, "");
		this.defaults.setProperty(PROP_DB_TYPE, QueryHandler.QUERY_HANDLER_HSQL);
		this.defaults.setProperty(PROP_DB_URL, "");
		this.defaults.setProperty(PROP_DB_USERNAME, "");
		this.defaults.setProperty(PROP_DBCP_MAX_ACTIVE, "15");
		this.defaults.setProperty(PROP_DBCP_MAX_IDLE, "15");
		this.defaults.setProperty(PROP_DBCP_MAX_OPEN_PREPARED_STATEMENTS, "20");
		this.defaults.setProperty(PROP_DBCP_MIN_EVICTABLE_IDLE_TIME, "600");
		this.defaults.setProperty(PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN, "5");
		this.defaults.setProperty(PROP_DBCP_POOL_PREPARED_STATEMENTS, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_DBCP_TEST_ON_BORROW, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_DBCP_TEST_ON_RETURN, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_DBCP_TEST_WHILE_IDLE, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS, "120");
		this.defaults.setProperty(PROP_DBCP_WHEN_EXHAUSTED_ACTION, String.valueOf(GenericObjectPool.WHEN_EXHAUSTED_GROW));
		this.defaults.setProperty(PROP_EMAIL_SMTP_ENABLE,Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_EMAIL_SMTP_REQUIRES_AUTH,Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_EMAIL_SMTP_USERNAME,"");
		this.defaults.setProperty(PROP_EMAIL_SMTP_PASSWORD,"");
		this.defaults.setProperty(PROP_EMAIL_SMTP_USE_SSL, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_EMAIL_REPLY_ADDRESS,"");
		this.defaults.setProperty(PROP_EMAIL_SMTP_HOST,"");
		this.defaults.setProperty(PROP_EMAIL_SMTP_PORT,"25");
		this.defaults.setProperty(PROP_EMAIL_ADDRESS_SEPARATOR,";");
		this.defaults.setProperty(PROP_EMAIL_DEFAULT_CONTENT_TYPE,"text/plain");
		this.defaults.setProperty(PROP_EMAIL_SERVICE_FORGOT_PASSWORD, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_EMAIL_SERVICE_FORGOT_PASSWORD_CHALLENGE_TIMEOUT, "60"); // minutes
		this.defaults.setProperty(PROP_EMAIL_SERVICE_FORGOT_PASSWORD_CHALLENGE_RETRIES, "3");
		this.defaults.setProperty(PROP_EMAIL_SERVICE_FORGOT_PASSWORD_IP_LOCK_DURATION, "1440"); // minutes = 24h
		this.defaults.setProperty(PROP_ENCRYPTION_ALGORITHM, "SHA-512");
		this.defaults.setProperty(PROP_EXTERNAL_LINK_NEW_WINDOW, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_FILE_BLACKLIST, "bat,bin,exe,htm,html,js,jsp,php,sh");
		this.defaults.setProperty(PROP_FILE_BLACKLIST_TYPE, String.valueOf(WikiBase.UPLOAD_BLACKLIST));
		this.defaults.setProperty(PROP_FILE_DIR_FULL_PATH, "");
		this.defaults.setProperty(PROP_FILE_DIR_RELATIVE_PATH, "");
		// size is in bytes
		this.defaults.setProperty(PROP_FILE_MAX_FILE_SIZE, "5000000");
		this.defaults.setProperty(PROP_FILE_SERVER_URL, "");
		this.defaults.setProperty(PROP_FILE_UPLOAD_STORAGE, WikiBase.UPLOAD_STORAGE.JAMWIKI.toString());
		this.defaults.setProperty(PROP_FILE_WHITELIST, "bmp,gif,jpeg,jpg,pdf,png,properties,svg,txt,zip");
		this.defaults.setProperty(PROP_HONEYPOT_ACCESS_KEY, "");
		this.defaults.setProperty(PROP_HONEYPOT_FILTER_ENABLED, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_IMAGE_RESIZE_INCREMENT, "100");
		this.defaults.setProperty(PROP_MAX_RECENT_CHANGES, "10000");
		this.defaults.setProperty(PROP_MAX_TOPIC_VERSION_EXPORT, "1000");
		this.defaults.setProperty(PROP_PARSER_ALLOW_CAPITALIZATION, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_PARSER_ALLOW_HTML, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_PARSER_ALLOW_JAVASCRIPT, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_PARSER_ALLOW_TEMPLATES, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_PARSER_CLASS, "org.jamwiki.parser.jflex.JFlexParser");
		this.defaults.setProperty(PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_PARSER_MAX_INCLUSIONS, "250");
		this.defaults.setProperty(PROP_PARSER_MAXIMUM_INFINITE_LOOP_LIMIT, "5");
		this.defaults.setProperty(PROP_PARSER_MAX_PARSER_ITERATIONS, "100");
		this.defaults.setProperty(PROP_PARSER_MAX_TEMPLATE_DEPTH, "100");
		this.defaults.setProperty(PROP_PARSER_SIGNATURE_DATE_PATTERN, "HH:mm, dd MMMM yyyy (z)");
		this.defaults.setProperty(PROP_PARSER_SIGNATURE_USER_PATTERN, "[[{0}|{4}]]");
		this.defaults.setProperty(PROP_PARSER_TOC, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_PARSER_TOC_DEPTH, "5");
		this.defaults.setProperty(PROP_PARSER_USE_NUMBERED_HTML_LINKS, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_PATTERN_INVALID_NAMESPACE_NAME, "([\\n\\r\\\\<>\\[\\]\\:_%/?&#]+)");
		this.defaults.setProperty(PROP_PATTERN_INVALID_ROLE_NAME, "([A-Za-z0-9_]+)");
		this.defaults.setProperty(PROP_PATTERN_INVALID_TOPIC_PATTERN, "[\\n\\r\\\\<>\\[\\]?#]");
		this.defaults.setProperty(PROP_PATTERN_VALID_USER_LOGIN, "([A-Za-z0-9_]+)");
		this.defaults.setProperty(PROP_PATTERN_VALID_VIRTUAL_WIKI, "([A-Za-z0-9_]+)");
		this.defaults.setProperty(PROP_PRINT_NEW_WINDOW, Boolean.FALSE.toString());
		this.defaults.setProperty(PROP_RECAPTCHA_EDIT, "0");
		this.defaults.setProperty(PROP_RECAPTCHA_PRIVATE_KEY, "");
		this.defaults.setProperty(PROP_RECAPTCHA_PUBLIC_KEY, "");
		this.defaults.setProperty(PROP_RECAPTCHA_REGISTER, "0");
		this.defaults.setProperty(PROP_RECENT_CHANGES_NUM, "100");
		this.defaults.setProperty(PROP_RSS_ALLOWED, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_RSS_TITLE, "Wiki Recent Changes");
		this.defaults.setProperty(PROP_SERVER_URL, "");
		this.defaults.setProperty(PROP_SHARED_UPLOAD_VIRTUAL_WIKI, "");
		this.defaults.setProperty(PROP_SITE_NAME, "JAMWiki");
		// FIXME - hard coding
		this.defaults.setProperty(PROP_TOPIC_EDITOR, "toolbar");
		this.defaults.setProperty(PROP_TOPIC_SPAM_FILTER, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_TOPIC_USE_PREVIEW, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_TOPIC_USE_SHOW_CHANGES, Boolean.TRUE.toString());
		this.defaults.setProperty(PROP_VIRTUAL_WIKI_DEFAULT, "en");
		this.defaults.setProperty(PROP_ROLE_ADMIN, "ROLE_ADMIN");
		this.defaults.setProperty(PROP_ROLE_ANONYMOUS, "ROLE_ANONYMOUS");
		this.defaults.setProperty(PROP_ROLE_EDIT_EXISTING, "ROLE_EDIT_EXISTING");
		this.defaults.setProperty(PROP_ROLE_EDIT_NEW, "ROLE_EDIT_NEW");
		this.defaults.setProperty(PROP_ROLE_EMBEDDED, "ROLE_EMBEDDED");
		this.defaults.setProperty(PROP_ROLE_IMPORT, "ROLE_IMPORT");
		this.defaults.setProperty(PROP_ROLE_MOVE, "ROLE_MOVE");
		this.defaults.setProperty(PROP_ROLE_NO_ACCOUNT, "ROLE_NO_ACCOUNT");
		this.defaults.setProperty(PROP_ROLE_SYSADMIN, "ROLE_SYSADMIN");
		this.defaults.setProperty(PROP_ROLE_TRANSLATE, "ROLE_TRANSLATE");
		this.defaults.setProperty(PROP_ROLE_UPLOAD, "ROLE_UPLOAD");
		this.defaults.setProperty(PROP_ROLE_VIEW, "ROLE_VIEW");
		this.defaults.setProperty(PROP_ROLE_REGISTER, "ROLE_REGISTER");
	}

	/**
	 * Get the value of a boolean property.
	 * Returns <code>true</code> if the property is equal, ignoring case,
	 * to the string "true".
	 * Returns false in all other cases (eg: "false", "yes", "1")
	 *
	 * @param name The name of the property whose value is to be retrieved.
	 * @return The value of the property.
	 */
	public static boolean getBooleanValue(String name) {
		return Boolean.valueOf(getValue(name));
	}

	/**
	 * Return an instance of the current properties object.  The property instance
	 * returned should not be directly modified.
	 *
	 * @return Returns an instance of the current system properties.
	 */
	public static Properties getInstance() {
		if (ENVIRONMENT_INSTANCE == null) {
			// initialize the singleton instance
			ENVIRONMENT_INSTANCE = new Environment();
		}
		return ENVIRONMENT_INSTANCE.props;
	}

	/**
	 * Get the value of an integer property.
	 *
	 * @param name The name of the property whose value is to be retrieved.
	 * @return The value of the property.
	 */
	public static int getIntValue(String name) {
		int value = NumberUtils.toInt(getValue(name), -1);
		if (value == -1) {
			logger.warn("Invalid integer property " + name + " with value " + value);
		}
		// FIXME - should this otherwise indicate an invalid property?
		return value;
	}

	/**
	 * Get the value of a long property.
	 *
	 * @param name The name of the property whose value is to be retrieved.
	 * @return The value of the property.
	 */
	public static long getLongValue(String name) {
		long value = NumberUtils.toLong(getValue(name), -1);
		if (value == -1) {
			logger.warn("Invalid long property " + name + " with value " + value);
		}
		// FIXME - should this otherwise indicate an invalid property?
		return value;
	}

	/**
	 * Returns the value of a property.
	 *
	 * @param name The name of the property whose value is to be retrieved.
	 * @return The value of the property.
	 */
	public static String getValue(String name) {
		return getInstance().getProperty(name);
	}

	/**
	 * Return <code>true</code> if wiki properties have been initialized,
	 * <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if wiki properties have been initialized,
	 * <code>false</code> otherwise.
	 */
	public static boolean isInitialized() {
		return Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED);
	}

	/**
	 * Given a property file name, load the property file and return an object
	 * representing the property values.
	 *
	 * @param propertyFile The name of the property file to load.
	 * @return The loaded SortedProperties object.
	 */
	public static SortedProperties loadProperties(String propertyFile) {
		return loadProperties(propertyFile, null);
	}

	/**
	 * Given a property file name, load the property file and return an object
	 * representing the property values.
	 *
	 * @param propertyFile The name of the property file to load.
	 * @param def Default property values, or <code>null</code> if there are no defaults.
	 * @return The loaded SortedProperties object.
	 */
	public static SortedProperties loadProperties(String propertyFile, Properties def) {
		SortedProperties properties = new SortedProperties();
		if (def != null) {
			properties = new SortedProperties(def);
		}
		File file = null;
		FileInputStream fis = null;
		try {
			file = findProperties(propertyFile);
			if (file == null) {
				logger.warn("Property file " + propertyFile + " does not exist");
			} else if (!file.exists()) {
				logger.warn("Property file " + file.getPath() + " does not exist");
			} else {
				logger.info("Loading properties from " + file.getPath());
				fis = new FileInputStream(file);
				properties.load(fis);
			}
		} catch (IOException e) {
			logger.error("Failure while trying to load properties file " + file.getPath(), e);
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return properties;
	}

	/**
	 * Convert a Properties object to a Map object.
	 */
	private static Map<String, String> propertiesToMap(Properties properties) {
		Map<String, String> map = new HashMap<String, String>();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			map.put(entry.getKey().toString(), entry.getValue().toString());
		}
		return map;
	}

	/**
	 * Utility methods for retrieving property files from the class path, based on
	 * code from the org.apache.log4j.helpers.Loader class.
	 *
	 * @param filename Given a filename return a File object for the file.  The filename
	 *  may be relative to the class path or the directory from which the JVM was
	 *  initialized.
	 * @return Returns a file representing the filename, or <code>null</code> if
	 *  the file cannot be found.
	 */
	private static File retrievePropertyFile(String filename) {
		try {
			return ResourceUtil.getClassLoaderFile(filename);
		} catch (IOException e) {
			// NOPMD file might not exist
		}
		try {
			return new File(ResourceUtil.getClassLoaderRoot(), filename);
		} catch (IOException e) {
			logger.error("Error while searching for resource " + filename, e);
		}
		return null;
	}

	/**
	 * Persist the current wiki system configuration and reload all values.
	 *
	 * @throws WikiException Thrown if a failure occurs while saving the
	 *  configuration values.
	 */
	public static void saveConfiguration() throws WikiException {
		try {
			Environment.saveProperties(PROPERTY_FILE_NAME, getInstance(), null);
			// do not use WikiBase.getDataHandler() directly since properties are
			// being changed
			WikiBase.getDataHandler().writeConfiguration(propertiesToMap(getInstance()));
		} catch (IOException e) {
			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()));
		} catch (DataAccessException e) {
			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()));
		}
	}

	/**
	 * Save the specified property values to the filesystem.
	 *
	 * @param propertyFile The name of the property file to save.
	 * @param properties The properties object that is to be saved.
	 * @param comments A comment to save in the properties file.
	 * @throws IOException Thrown if the file cannot be found or if an I/O
	 *  error occurs.
	 */
	public static void saveProperties(String propertyFile, Properties properties, String comments) throws IOException {
		File file = findProperties(propertyFile);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			properties.store(out, comments);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Set a new boolean value for the given property name.
	 *
	 * @param name The name of the property whose value is to be set.
	 * @param value The value of the property being set.
	 */
	public static void setBooleanValue(String name, boolean value) {
		getInstance().setProperty(name, Boolean.toString(value));
	}

	/**
	 * Sets a new integer value for the given property name.
	 *
	 * @param name The name of the property whose value is to be set.
	 * @param value The value of the property being set.
	 */
	public static void setIntValue(String name, int value) {
		getInstance().setProperty(name, Integer.toString(value));
	}

	/**
	 * Sets a new value for the given property name.
	 *
	 * @param name The name of the property whose value is to be set.
	 * @param value The value of the property being set.
	 */
	public static void setValue(String name, String value) {
		// it is invalid to set a property value null, so convert to empty string
		if (value == null) {
			value = "";
		}
		getInstance().setProperty(name, value);
	}
}
