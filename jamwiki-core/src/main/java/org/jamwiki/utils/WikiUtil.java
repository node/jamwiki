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
package org.jamwiki.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.JAMWikiParser;
import org.jamwiki.SearchEngine;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.WikiVersion;
import org.jamwiki.db.DatabaseConnection;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Role;
import org.jamwiki.model.TopicType;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiGroup;

/**
 * This class provides a variety of general utility methods for handling
 * wiki-specific functionality such as retrieving topics from the URL.
 */
public class WikiUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiUtil.class.getName());

	/** webapp context path, initialized from JAMWikiFilter. */
	public static String WEBAPP_CONTEXT_PATH = null;
	private static final WikiCache<String, MessageFormat> CACHE_TEMPLATE_MESSAGE_FORMATTER = new WikiCache<String, MessageFormat>("org.jamwiki.util.WikiUtil.CACHE_TEMPLATE_MESSAGE_FORMATTER");
	private static final Pattern HTML_COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
	private static final Pattern INVALID_NAMESPACE_NAME_PATTERN = Pattern.compile(Environment.getValue(Environment.PROP_PATTERN_INVALID_NAMESPACE_NAME));
	private static final Pattern INVALID_ROLE_NAME_PATTERN = Pattern.compile(Environment.getValue(Environment.PROP_PATTERN_INVALID_ROLE_NAME));
	private static final Pattern VALID_USER_LOGIN_PATTERN = Pattern.compile(Environment.getValue(Environment.PROP_PATTERN_VALID_USER_LOGIN));
	private static final Pattern VALID_VIRTUAL_WIKI_PATTERN = Pattern.compile(Environment.getValue(Environment.PROP_PATTERN_VALID_VIRTUAL_WIKI));
	private static final Pattern XSS_PATTERN = Pattern.compile("[\\\"><]");
	public static final String PARAMETER_TOPIC = "topic";
	public static final String PARAMETER_VIRTUAL_WIKI = "virtualWiki";
	public static final String PARAMETER_WATCHLIST = "watchlist";

	/**
	 * Create a pagination object based on parameters found in the current
	 * request.
	 *
	 * @param request The servlet request object.
	 * @return A Pagination object constructed from parameters found in the
	 *  request object.
	 */
	public static Pagination buildPagination(HttpServletRequest request) {
		int num = Environment.getIntValue(Environment.PROP_RECENT_CHANGES_NUM);
		if (request.getParameter("num") != null) {
			try {
				num = Integer.parseInt(request.getParameter("num"));
			} catch (NumberFormatException e) {
				// invalid number
			}
		}
		int offset = 0;
		if (request.getParameter("offset") != null) {
			try {
				offset = Integer.parseInt(request.getParameter("offset"));
			} catch (NumberFormatException e) {
				// invalid number
			}
		}
		return new Pagination(num, offset);
	}

	/**
	 * Convert a topic name or other value into a value suitable for use as a
	 * file name.  This method replaces spaces with underscores, and then URL
	 * encodes the value.
	 *
	 * @param name The value that is to be encoded for use as a file name.
	 * @return The encoded value.
	 */
	public static String encodeForFilename(String name) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("File name not specified in encodeForFilename");
		}
		// replace spaces with underscores
		String result = Utilities.encodeTopicName(name);
		// URL encode the rest of the name
		try {
			result = URLEncoder.encode(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// this should never happen
			throw new IllegalStateException("Unsupporting encoding UTF-8");
		}
		return result;
	}

	/**
	 * Determine the URL for the default virtual wiki topic, not including the application server context.
	 */
	public static String findDefaultVirtualWikiUrl(String virtualWikiName) {
		VirtualWiki virtualWiki = VirtualWiki.defaultVirtualWiki();
		if (!StringUtils.isBlank(virtualWikiName)) {
			try {
				virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
			} catch (DataAccessException e) {
				logger.warn("Unable to retrieve default topic for virtual wiki", e);
			}
		}
		return "/" + virtualWiki.getName() + "/" + virtualWiki.getRootTopicName();
	}

	/**
	 * Given a namespace name, determine the topic type.
	 *
	 * @param namespace The namespace.
	 * @return The topic type that matches the namespace.
	 */
	public static TopicType findTopicTypeForNamespace(Namespace namespace) {
		if (namespace != null) {
			if (namespace.getId().equals(Namespace.CATEGORY_ID)) {
				return TopicType.CATEGORY;
			}
			if (namespace.getId().equals(Namespace.TEMPLATE_ID)) {
				return TopicType.TEMPLATE;
			}
			if (namespace.getId().equals(Namespace.JAMWIKI_ID)) {
				return TopicType.SYSTEM_FILE;
			}
			if (namespace.getId().equals(Namespace.FILE_ID)) {
				// FIXME - handle TYPE_FILE
				return TopicType.IMAGE;
			}
		}
		return TopicType.ARTICLE;
	}

	/**
	 * Load a template file, which is a <code>java.text.MessageFormat</code>
	 * pattern, and parse it with the specified arguments.
	 *
	 * @param template A template file path that is either absolute or
	 *  relative to the classpath.
	 * @param args Zero or more arguments to use when parsing the template.
	 */
	public static String formatFromTemplate(String template, Object... args) throws IOException {
		// cache the message formatter since this code is likely to be invoked often
		MessageFormat mf = CACHE_TEMPLATE_MESSAGE_FORMATTER.retrieveFromCache(template);
		if (mf == null) {
			String templateString = ResourceUtil.readFile(template);
			// strip HTML comments
			Matcher m = WikiUtil.HTML_COMMENT_PATTERN.matcher(templateString);
			templateString = m.replaceAll("");
			mf = new MessageFormat(templateString.trim());
			CACHE_TEMPLATE_MESSAGE_FORMATTER.addToCache(template, mf);
		}
		return mf.format(args);
	}

	/**
	 * Retrieve a parameter from the servlet request.  This method works around
	 * some issues encountered when retrieving non-ASCII values from URL
	 * parameters.
	 *
	 * @param request The servlet request object.
	 * @param name The parameter name to be retrieved.
	 * @param decodeUnderlines Set to <code>true</code> if underlines should
	 *  be automatically converted to spaces.
	 * @return The decoded parameter value retrieved from the request.
	 */
	public static String getParameterFromRequest(HttpServletRequest request, String name, boolean decodeUnderlines) {
		String value = null;
		if (request.getMethod().equalsIgnoreCase("GET")) {
			// parameters passed via the URL are URL encoded, so request.getParameter may
			// not interpret non-ASCII characters properly.  This code attempts to work
			// around that issue by manually decoding.  yes, this is ugly and it would be
			// great if someone could eventually make it unnecessary.
			String query = Utilities.getQueryString(request);
			if (StringUtils.isBlank(query)) {
				return null;
			}
			String prefix = name + "=";
			int pos = query.indexOf(prefix);
			if (pos != -1 && (pos + prefix.length()) < query.length()) {
				value = query.substring(pos + prefix.length());
				if (value.indexOf('&') != -1) {
					value = value.substring(0, value.indexOf('&'));
				}
			}
			return Utilities.decodeAndEscapeTopicName(value, decodeUnderlines);
		}
		value = request.getParameter(name);
		if (value == null) {
			value = (String)request.getAttribute(name);
		}
		if (value == null) {
			return null;
		}
		return Utilities.decodeTopicName(value, decodeUnderlines);
	}

	/**
	 * Retrieve a file that represents a "tmp" directory within the wiki system directory.
	 * The caller should test directory.exists() to verify that the directory is available.
	 */
	public static File getTempDirectory() {
		String subdirectory = "tmp";
		File directory = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), subdirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

	/**
	 * Retrieve a topic name from the servlet request.  This method will
	 * retrieve a request parameter matching the PARAMETER_TOPIC value,
	 * and will decode it appropriately.
	 *
	 * @param request The servlet request object.
	 * @return The decoded topic name retrieved from the request.
	 * @throws WikiException If the topic name is invalid, such as values
	 *  used in cross-site scripting attacks.
	 */
	public static String getTopicFromRequest(HttpServletRequest request) throws WikiException {
		String topic = WikiUtil.getParameterFromRequest(request, WikiUtil.PARAMETER_TOPIC, true);
		if (StringUtils.isBlank(topic)) {
			return topic;
		}
		// check for XSS
		Matcher m = WikiUtil.XSS_PATTERN.matcher(topic);
		if (m.find()) {
			throw new WikiException(new WikiMessage("common.exception.name", topic));
		}
		return topic;
	}

	/**
	 * Retrieve a topic name from the request URI.  This method will retrieve
	 * the portion of the URI that follows the virtual wiki and decode it
	 * appropriately.
	 *
	 * @param request The servlet request object.
	 * @return The decoded topic name retrieved from the URI.
	 */
	public static String getTopicFromURI(HttpServletRequest request) {
		// skip one directory, which is the virutal wiki
		String topic = retrieveDirectoriesFromURI(request, 1);
		if (topic == null) {
			// may be the root URL, ie: http://example.com/wiki/en/
			if (logger.isDebugEnabled()) {
				logger.debug("No topic in URL: " + request.getRequestURI());
			}
			return null;
		}
		int pos = topic.indexOf('#');
		if (pos != -1) {
			// strip everything after and including '#'
			if (pos == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("No topic in URL: " + request.getRequestURI());
				}
				return null;
			}
			topic = topic.substring(0, pos);
		}
		pos = topic.indexOf('?');
		if (pos != -1) {
			// strip everything after and including '?'
			if (pos == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("No topic in URL: " + request.getRequestURI());
				}
				return null;
			}
			topic = topic.substring(0, pos);
		}
		pos = topic.indexOf(';');
		if (pos != -1) {
			// some servlet containers return parameters of the form ";jsessionid=1234" when getRequestURI is called.
			if (pos == 0) {
				if (logger.isDebugEnabled()) {
					logger.debug("No topic in URL: " + request.getRequestURI());
				}
				return null;
			}
			topic = topic.substring(0, pos);
		}
		if (!StringUtils.isBlank(topic)) {
			topic = Utilities.decodeAndEscapeTopicName(topic, true);
		}
		return topic;
	}

	/**
	 * Retrieve a virtual wiki name from the servlet request.  This method
	 * will retrieve a request parameter matching the PARAMETER_VIRTUAL_WIKI
	 * value, and will decode it appropriately.
	 *
	 * @param request The servlet request object.
	 * @return The decoded virtual wiki name retrieved from the request.
	 */
	public static String getVirtualWikiFromRequest(HttpServletRequest request) {
		String virtualWiki = request.getParameter(WikiUtil.PARAMETER_VIRTUAL_WIKI);
		if (virtualWiki == null) {
			virtualWiki = (String)request.getAttribute(WikiUtil.PARAMETER_VIRTUAL_WIKI);
		}
		if (virtualWiki == null) {
			return null;
		}
		return Utilities.decodeTopicName(virtualWiki, true);
	}

	/**
	 * Retrieve a virtual wiki name from the request URI.  This method will
	 * retrieve the portion of the URI that immediately follows the servlet
	 * context and decode it appropriately.
	 *
	 * @param request The servlet request object.
	 * @return The decoded virtual wiki name retrieved from the URI.
	 */
	public static String getVirtualWikiFromURI(HttpServletRequest request) {
		String uri = retrieveDirectoriesFromURI(request, 0);
		if (StringUtils.isBlank(uri)) {
			logger.info("No virtual wiki found in URL: " + request.getRequestURI());
			return null;
		}
		// default the virtual wiki to the URI since the user may have accessed a URL of
		// the form /context/virtualwiki with no trailing slash
		String virtualWiki = uri;
		int slashIndex = uri.indexOf('/');
		if (slashIndex != -1) {
			virtualWiki = uri.substring(0, slashIndex);
		}
		return Utilities.decodeAndEscapeTopicName(virtualWiki, true);
	}

	/**
	 * Determine if the system properties file exists and has been initialized.
	 * This method is primarily used to determine whether or not to display
	 * the system setup page or not.
	 *
	 * @return <code>true</code> if the properties file has NOT been initialized,
	 *  <code>false</code> otherwise.
	 */
	public static boolean isFirstUse() {
		return !Environment.isInitialized();
	}

	/**
	 * Determine if the system code has been upgraded from the configured system
	 * version.  Thus if the system is upgraded, this method returns <code>true</code>
	 *
	 * @return <code>true</code> if the system has been upgraded, <code>false</code>
	 *  otherwise.
	 */
	public static boolean isUpgrade() {
		if (WikiUtil.isFirstUse()) {
			return false;
		}
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		WikiVersion currentVersion = new WikiVersion(WikiVersion.CURRENT_WIKI_VERSION);
		return oldVersion.before(currentVersion);
	}

	/**
	 * Utility method to retrieve an instance of the current system parser.
	 *
	 * @return An instance of the system parser.
	 */
	public static JAMWikiParser parserInstance() {
		String parserInstanceClass = Environment.getValue(Environment.PROP_PARSER_CLASS);
		try {
			return (JAMWikiParser)ResourceUtil.instantiateClass(parserInstanceClass);
		} catch (ClassCastException e) {
			throw new IllegalStateException("Parser instance specified in jamwiki.properties does not implement org.jamwiki.JAMWikiParser: " + parserInstanceClass);
		}
	}

	/**
	 * Utility method for retrieving values from the URI.  This method
	 * will attempt to properly convert the URI encoding, and then offers a way
	 * to return directories after the initial context directory.  For example,
	 * if the URI is "/context/first/second/third" and this method is called
	 * with a skipCount of 1, the return value is "second/third".
	 *
	 * @param request The servlet request object.
	 * @param skipCount The number of directories to skip.
	 * @return A UTF-8 encoded portion of the URL that skips the web application
	 *  context and skipCount directories, or <code>null</code> if the number of
	 *  directories is less than skipCount.
	 */
	private static String retrieveDirectoriesFromURI(HttpServletRequest request, int skipCount) {
		String uri = request.getRequestURI().trim();
		// FIXME - needs testing on other platforms
		uri = Utilities.convertEncoding(uri, "ISO-8859-1", "UTF-8");
		String contextPath = request.getContextPath().trim();
		if (StringUtils.isBlank(uri) || contextPath == null) {
			return null;
		}
		// make sure there are no instances of "//" in the URL
		uri = uri.replaceAll("(/){2,}", "/");
		if (uri.length() <= contextPath.length()) {
			return null;
		}
		uri = uri.substring(contextPath.length() + 1);
		int i = 0;
		while (i < skipCount) {
			int slashIndex = uri.indexOf('/');
			if (slashIndex == -1) {
				return null;
			}
			uri = uri.substring(slashIndex + 1);
			i++;
		}
		return uri;
	}

	/**
	 * If a blacklist or whitelist of allowed file upload types is being used,
	 * retrieve the list from the properties file and return as a List object.
	 * If no such list is being used then return an empty List object.
	 *
	 * @return A list consisting of lowercase versions of all file extensions
	 *  for the whitelist/blacklist.  Entries in the list are of the form
	 *  "txt", not ".txt".
	 */
	public static List retrieveUploadFileList() {
		List<String> list = new ArrayList<String>();
		int blacklistType = Environment.getIntValue(Environment.PROP_FILE_BLACKLIST_TYPE);
		String listString = "";
		if (blacklistType == WikiBase.UPLOAD_BLACKLIST) {
			listString = Environment.getValue(Environment.PROP_FILE_BLACKLIST);
		} else if (blacklistType == WikiBase.UPLOAD_WHITELIST) {
			listString = Environment.getValue(Environment.PROP_FILE_WHITELIST);
		}
		String[] tokens = listString.split("[\\s,\\.]");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (StringUtils.isBlank(token)) {
				continue;
			}
			list.add(token.toLowerCase());
		}
		return list;
	}

	/**
	 * Utility method to retrieve an instance of the current search engine.
	 *
	 * @return An instance of the current search engine.
	 */
	public static SearchEngine searchEngineInstance() {
		String searchEngineClass = Environment.getValue(Environment.PROP_BASE_SEARCH_ENGINE);
		try {
			return (SearchEngine)ResourceUtil.instantiateClass(searchEngineClass);
		} catch (ClassCastException e) {
			throw new IllegalStateException("Search engine specified in jamwiki.properties does not implement org.jamwiki.SearchEngine: " + searchEngineClass);
		}
	}

	/**
	 * Verify that a directory exists and is writable.
	 *
	 * @param name The full name (including the path) for the directory being tested.
	 * @return A WikiMessage object containing any error encountered, otherwise
	 *  <code>null</code>.
	 */
	public static WikiMessage validateDirectory(String name) {
		File directory = new File(name);
		if (!directory.exists() || !directory.isDirectory()) {
			return new WikiMessage("error.directoryinvalid", name);
		}
		String filename = "jamwiki-test-" + System.currentTimeMillis() + ".txt";
		File file = new File(name, filename);
		String text = "Testing";
		String read = null;
		try {
			// attempt to write a temp file to the directory
			FileUtils.writeStringToFile(file, text, "UTF-8");
		} catch (IOException e) {
			return new WikiMessage("error.directorywrite", name, e.getMessage());
		}
		try {
			// verify that the file was correctly written
			read = FileUtils.readFileToString(file, "UTF-8");
			if (read == null || !text.equals(read)) {
				throw new IOException();
			}
		} catch (IOException e) {
			return new WikiMessage("error.directoryread", name, e.getMessage());
		}
		try {
			// attempt to delete the file
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			return new WikiMessage("error.directorydelete", name, e.getMessage());
		}
		return null;
	}

	/**
	 * Utility method for determining if a namespace name is valid for use on the Wiki,
	 * meaning that it is not empty and does not contain any invalid characters.
	 *
	 * @param name The namespace name to validate.
	 * @throws WikiException Thrown if the user name is invalid.
	 */
	public static void validateNamespaceName(String name) throws WikiException {
		if (name == null || (name.length() != 0 && StringUtils.isBlank(name)) || name.length() != name.trim().length()) {
			// name cannot be null, contain only whitespace, or have trailing whitespace
			throw new WikiException(new WikiMessage("admin.vwiki.error.namespace.whitespace", name));
		}
		Matcher m = WikiUtil.INVALID_NAMESPACE_NAME_PATTERN.matcher(name);
		if (m.find()) {
			throw new WikiException(new WikiMessage("admin.vwiki.error.namespace.characters", name));
		}
		List<Namespace> namespaces = null;
		try {
			namespaces = WikiBase.getDataHandler().lookupNamespaces();
		} catch (DataAccessException e) {
			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()));
		}
		for (Namespace namespace : namespaces) {
			// verify that the namespace name is unique
			if (name.equals(namespace.getDefaultLabel())) {
				throw new WikiException(new WikiMessage("admin.vwiki.error.namespace.unique", name));
			}
			// verify that there are no translated namespaces with the same name
			for (String namespaceTranslation : namespace.getNamespaceTranslations().values()) {
				if (name.equals(namespaceTranslation)) {
					throw new WikiException(new WikiMessage("admin.vwiki.error.namespace.unique", name));
				}
			}
		}
	}

	/**
	 * Utility method for determining if the parameters of a Role are valid
	 * or not.
	 *
	 * @param role The Role to validate.
	 * @throws WikiException Thrown if the role is invalid.
	 */
	public static void validateRole(Role role) throws WikiException {
		Matcher m = WikiUtil.INVALID_ROLE_NAME_PATTERN.matcher(role.getAuthority());
		if (!m.matches()) {
			throw new WikiException(new WikiMessage("roles.error.name", role.getAuthority()));
		}
		if (!StringUtils.isBlank(role.getDescription()) && role.getDescription().length() > 200) {
			throw new WikiException(new WikiMessage("roles.error.description"));
		}
		// FIXME - throw a user-friendly error if the role name is already in use
	}

	/**
	 * Utility method for determining if the parameters of a group are valid
	 * or not.
	 *
	 * @param group The WikiGroup to validate.
	 * @throws WikiException Thrown if the role is invalid.
	 */
	public static void validateWikiGroup(WikiGroup group) throws WikiException {
		String groupName = group.getName();
		if (groupName == null || groupName.length() == 0 || groupName.length() > 30) {
			throw new WikiException(new WikiMessage("group.error.name", groupName));
		}
		Matcher m = WikiUtil.INVALID_ROLE_NAME_PATTERN.matcher(groupName);
		if (!m.matches()) {
			throw new WikiException(new WikiMessage("group.error.name", groupName));
		}
		if (!StringUtils.isBlank(group.getDescription()) && group.getDescription().length() > 200) {
			throw new WikiException(new WikiMessage("group.error.description"));
		}
	}

	/**
	 * Utility method for determining if a password is valid for use on the wiki.
	 *
	 * @param password The password value.
	 * @param confirmPassword Passwords must be entered twice to avoid tying errors.
	 *  This field represents the confirmed password entry.
	 */
	public static void validatePassword(String password, String confirmPassword) throws WikiException {
		if (StringUtils.isBlank(password)) {
			throw new WikiException(new WikiMessage("error.newpasswordempty"));
		}
		if (StringUtils.isBlank(confirmPassword)) {
			throw new WikiException(new WikiMessage("error.passwordconfirm"));
		}
		if (!password.equals(confirmPassword)) {
			throw new WikiException(new WikiMessage("admin.message.passwordsnomatch"));
		}
	}

	/**
	 * Validate that vital system properties, such as database connection settings,
	 * have been specified properly.
	 *
	 * @param props The property object to validate against.
	 * @return A list of WikiMessage objects containing any errors encountered,
	 *  or an empty list if no errors are encountered.
	 */
	public static List<WikiMessage> validateSystemSettings(Properties props) {
		List<WikiMessage> errors = new ArrayList<WikiMessage>();
		// test directory permissions & existence
		WikiMessage baseDirError = WikiUtil.validateDirectory(props.getProperty(Environment.PROP_BASE_FILE_DIR));
		if (baseDirError != null) {
			errors.add(baseDirError);
		}
		if (props.getProperty(Environment.PROP_FILE_UPLOAD_STORAGE).equals(WikiBase.UPLOAD_STORAGE.DOCROOT.toString())) {
			WikiMessage fullDirError = WikiUtil.validateDirectory(props.getProperty(Environment.PROP_FILE_DIR_FULL_PATH));
			if (fullDirError != null) {
				errors.add(fullDirError);
			}
		}
		String classesDir = null;
		try {
			classesDir = ResourceUtil.getClassLoaderRoot().getPath();
			WikiMessage classesDirError = WikiUtil.validateDirectory(classesDir);
			if (classesDirError != null) {
				errors.add(classesDirError);
			}
		} catch (IOException e) {
			errors.add(new WikiMessage("error.directorywrite", classesDir, e.getMessage()));
		}
		// test database
		String driver = props.getProperty(Environment.PROP_DB_DRIVER);
		String url = props.getProperty(Environment.PROP_DB_URL);
		String userName = props.getProperty(Environment.PROP_DB_USERNAME);
		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, props);
		try {
			DatabaseConnection.testDatabase(driver, url, userName, password, false);
		} catch (ClassNotFoundException e) {
			logger.error("Invalid database settings", e);
			errors.add(new WikiMessage("error.databaseconnection", e.getMessage()));
		} catch (SQLException e) {
			logger.error("Invalid database settings", e);
			errors.add(new WikiMessage("error.databaseconnection", e.getMessage()));
		}
		// verify valid parser class
		String parserClass = props.getProperty(Environment.PROP_PARSER_CLASS);
		String abstractParserClass = JAMWikiParser.class.getName();
		boolean validParser = (parserClass != null && !parserClass.equals(abstractParserClass));
		if (validParser) {
			try {
				Class parent = ClassUtils.getClass(parserClass);
				Class child = ClassUtils.getClass(abstractParserClass);
				if (!child.isAssignableFrom(parent)) {
					validParser = false;
				}
			} catch (ClassNotFoundException e) {
				validParser = false;
			}
		}
		if (!validParser) {
			errors.add(new WikiMessage("error.parserclass", parserClass));
		}
		return errors;
	}

	/**
	 * Utility method for determining if a username is valid for use on the Wiki,
	 * meaning that it is not empty and does not contain any invalid characters.
	 *
	 * @param name The username to validate.
	 * @throws WikiException Thrown if the user name is invalid.
	 */
	public static void validateUserName(String name) throws WikiException {
		if (StringUtils.isBlank(name)) {
			throw new WikiException(new WikiMessage("error.loginempty"));
		}
		Matcher m = WikiUtil.VALID_USER_LOGIN_PATTERN.matcher(name);
		if (!m.matches()) {
			throw new WikiException(new WikiMessage("common.exception.name", name));
		}
	}

	/**
	 * Utility method for determining if a virtual wiki name is valid for use on the Wiki,
	 * meaning that it is not empty and does not contain any invalid characters.
	 *
	 * @param virtualWikiName The virtual wiki name to validate.
	 * @throws WikiException Thrown if the user name is invalid.
	 */
	public static void validateVirtualWikiName(String virtualWikiName) throws WikiException {
		if (StringUtils.isBlank(virtualWikiName)) {
			throw new WikiException(new WikiMessage("common.exception.novirtualwiki"));
		}
		Matcher m = WikiUtil.VALID_VIRTUAL_WIKI_PATTERN.matcher(virtualWikiName);
		if (!m.matches()) {
			throw new WikiException(new WikiMessage("common.exception.name", virtualWikiName));
		}
	}
}
