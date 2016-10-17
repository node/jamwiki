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
package org.jamwiki.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiConfiguration;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Interwiki;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Role;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.ResourceUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.transaction.TransactionStatus;

/**
 * This class contains general database utility methods that are useful for a
 * variety of JAMWiki database functions, including setup and upgrades.
 */
public class WikiDatabase {

	private static String CONNECTION_VALIDATION_QUERY = null;
	private static final WikiLogger logger = WikiLogger.getLogger(WikiDatabase.class.getName());
	/** Root directory within the WAR distribution that contains the default topic pages. */
	public static final String SPECIAL_PAGE_DIR = "pages";
	// array used in database migration - elements are table name and, if elements within the
	// table have dependencies (such as jam_namespace dependending on main_namespace_id), the
	// column to sort results in order to avoid foreign key constrain violations
	private static final String[][] JAMWIKI_DB_TABLE_INFO = {
		{"jam_virtual_wiki", "virtual_wiki_id"},
		{"jam_users", null},
		{"jam_wiki_user", "wiki_user_id"},
		{"jam_namespace", "namespace_id"},
		{"jam_namespace_translation", "namespace_id"},
		{"jam_topic", "topic_id"},
		{"jam_topic_version", "topic_version_id"},
		{"jam_file", "file_id"},
		{"jam_file_version", "file_version_id"},
		{"jam_category", null},
		{"jam_group", "group_id"},
		{"jam_group_members", "id"},
		{"jam_role", null},
		{"jam_authorities", null},
		{"jam_group_authorities", null},
		{"jam_recent_change", null},
		{"jam_log", null},
		{"jam_watchlist", null},
		{"jam_topic_links", null},
		{"jam_interwiki", null},
		{"jam_configuration", null},
		{"jam_user_block", "user_block_id"},
		{"jam_file_data", "file_version_id"}
	};

	/**
	 *
	 */
	private WikiDatabase() {
	}

	/**
	 *
	 */
	private static QueryHandler findNewQueryHandler(Properties props) {
		// find the QueryHandler appropriate to the NEW database
		String handlerClassName = props.getProperty(Environment.PROP_DB_TYPE);
		return (QueryHandler) ResourceUtil.instantiateClass(handlerClassName);
	}

	/**
	 * Utility method for validating that all topics are currently pointing to the correct
	 * namespace ID.  This method is required for updating data when upgrading to JAMWiki 0.9.0,
	 * and is also available for use to resolve data issues after creating or updating
	 * namespace names.
	 */
	public static int fixIncorrectTopicNamespaces() throws DataAccessException {
		int count = 0;
		Map<Integer, String> topicNames;
		List<Topic> topics;
		WikiLink wikiLink;
		List<VirtualWiki> virtualWikis = WikiBase.getDataHandler().getVirtualWikiList();
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			for (VirtualWiki virtualWiki : virtualWikis) {
				topicNames = WikiBase.getDataHandler().queryHandler().lookupTopicNames(virtualWiki.getVirtualWikiId(), true, conn);
				if (topicNames.isEmpty()) {
					continue;
				}
				topics = new ArrayList<Topic>();
				for (Map.Entry<Integer, String> entry : topicNames.entrySet()) {
					wikiLink = new WikiLink(null, virtualWiki.getName(), entry.getValue());
					Topic topic = new Topic(virtualWiki.getName(), wikiLink.getNamespace(), wikiLink.getArticle());
					topic.setTopicId(entry.getKey());
					topics.add(topic);
				}
				WikiBase.getDataHandler().queryHandler().updateTopicNamespaces(topics, conn);
				count += topicNames.size();
			}
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
		return count;
	}

	/**
	 * Migrate from the current database to a new database.
	 * Tables are created in the new database, and then the contents
	 * of the existing database are transferred across.
	 *
	 * @param props Properties object containing the new database properties
	 * @param errors List to add error messages to
	 */
	public static void migrateDatabase(Properties props, List<WikiMessage> errors) throws DataAccessException {
		// verify that new database is different from the old database
		if (StringUtils.equalsIgnoreCase(Environment.getValue(Environment.PROP_DB_URL), props.getProperty(Environment.PROP_DB_URL))) {
			errors.add(new WikiMessage("error.databaseconnection", "Cannot migrate to the same database"));
			return;
		}
		// find the QueryHandler appropriate to the NEW database
		QueryHandler newQueryHandler = WikiDatabase.findNewQueryHandler(props);
		logger.debug("Using NEW query handler: " + newQueryHandler.getClass().getName());
		Connection conn = null;
		Connection from = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			// create the tables in the NEW database
			conn = WikiDatabase.initializeNewDatabase(props, errors, newQueryHandler);
			if (conn == null) {
				return;
			}
			// since this is a new database setting autocommit to true is ok.  in addition,
			// since a potentially huge amount of data might be getting committed it prevents
			// locking issues when loading the database.
			conn.setAutoCommit(true);
			// copy the existing table content from the CURRENT database across to the NEW database
			from = DatabaseConnection.getConnection();
			from.setReadOnly(true);
			from.setAutoCommit(true);
			// used to track current_version_id for each jam_topic row inserted
			Map<Integer, Integer> topicVersions = new HashMap<Integer, Integer>();
			for (int i = 0; i < JAMWIKI_DB_TABLE_INFO.length; i++) {
				// these 3 variables are for special handling of the jam_topic.current_version_id field
				// which cannot be loaded on initial insert due to the jam_f_topic_topicv constraint
				boolean isTopicTable = "jam_topic".equals(JAMWIKI_DB_TABLE_INFO[i][0]);
				int topicIdColumn = 0;
				int currentVersionColumn = 0;
				int maxIndex = WikiDatabase.retrieveMaximumTableId(JAMWIKI_DB_TABLE_INFO[i][0], JAMWIKI_DB_TABLE_INFO[i][1]);
				StringBuilder insert;
				ResultSetMetaData md;
				StringBuilder values;
				StringBuilder select;
				String columnName;
				Integer topicId;
				Integer currentVersionId;
				Object o;
				// cycle through at most RECORDS_PER_CYCLE records at a time to avoid blowing up the system
				int RECORDS_PER_CYCLE = 25;
				for (int j = 0; j <= maxIndex; j += RECORDS_PER_CYCLE) {
					select = new StringBuilder("SELECT * FROM ").append(JAMWIKI_DB_TABLE_INFO[i][0]);
					if (!StringUtils.isBlank(JAMWIKI_DB_TABLE_INFO[i][1])) {
						if (j == 0) {
							// for the first record do not set a lower limit in case there is an ID less
							// than zero
							select.append(" WHERE ");
						} else {
							select.append(" WHERE ").append(JAMWIKI_DB_TABLE_INFO[i][1]).append(" > ").append(j);
							select.append(" AND ");
						}
						select.append(JAMWIKI_DB_TABLE_INFO[i][1]).append(" <= ").append(j + RECORDS_PER_CYCLE);
						select.append(" ORDER BY ").append(JAMWIKI_DB_TABLE_INFO[i][1]);
					}
					insert = new StringBuilder();
					stmt = from.createStatement();
					logger.info(select.toString());
					rs = stmt.executeQuery(select.toString());
					md = rs.getMetaData();
					insert.append("INSERT INTO ").append(JAMWIKI_DB_TABLE_INFO[i][0]).append('(');
					values = new StringBuilder();
					for (int k = 1; k <= md.getColumnCount(); k++) {
						if (k > 1) {
							insert.append(',');
							values.append(',');
						}
						columnName = md.getColumnLabel(k);
						if (isTopicTable) {
							if ("topic_id".equalsIgnoreCase(columnName)) {
								topicIdColumn = k;
							} else if ("current_version_id".equalsIgnoreCase(columnName)) {
								currentVersionColumn = k;
							}
						}
						// special handling for Sybase ASA, which requires the "login" column name to be quoted
						if (newQueryHandler instanceof org.jamwiki.db.SybaseASAQueryHandler && "login".equalsIgnoreCase(columnName)) {
							columnName = "\"" + columnName + "\"";
						}
						insert.append(columnName);
						values.append('?');
					}
					insert.append(") VALUES (").append(values).append(')');
					logger.info(insert.toString());
					PreparedStatement insertStmt = conn.prepareStatement(insert.toString());
					while (rs.next()) {
						topicId = null;
						currentVersionId = null;
						for (int k = 1; k <= md.getColumnCount(); k++) {
							o = rs.getObject(k);
							if (isTopicTable) {
								if (k == topicIdColumn) {
									topicId = (Integer)o;
								} else if (k == currentVersionColumn) {
									currentVersionId = (Integer)o;
								}
							}
							if (rs.wasNull() || (isTopicTable && k == currentVersionColumn)) {
								insertStmt.setNull(k, md.getColumnType(k));
							} else {
								insertStmt.setObject(k, rs.getObject(k));
							}
						}
						insertStmt.executeUpdate();
						if (topicId != null && currentVersionId != null) {
							// store current topic version for later update.  since topic id is the
							// map key, any older (obsolete) topic version IDs will be overwritten
							// as later records are processed.
							topicVersions.put(topicId, currentVersionId);
						}
					}
					rs.close();
					DatabaseConnection.closeStatement(stmt);
					DatabaseConnection.closeStatement(insertStmt);
				}
			}
			// update the jam_topic.current_version_id field that we had to leave blank on initial insert
			String updateSql = "UPDATE jam_topic SET current_version_id = ? WHERE topic_id = ?";
			logger.info(updateSql);
			PreparedStatement update = conn.prepareStatement(updateSql);
			for (Integer topicId : topicVersions.keySet()) {
				Integer topicVersionId = topicVersions.get(topicId);
				update.setObject(1, topicVersionId);
				update.setObject(2, topicId);
				update.executeUpdate();
			}
		} catch (Exception e) {
			logger.error("Error attempting to migrate the database", e);
			errors.add(new WikiMessage("error.unknown", e.getMessage()));
			try {
				newQueryHandler.dropTables(conn);
			} catch (Exception ex) {
				logger.warn("Unable to drop tables in NEW database following failed migration", ex);
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
			if (from != null) {
				DatabaseConnection.closeConnection(from, stmt, rs);
			}
		}
	}

	/**
	 *
	 */
	protected static String getConnectionValidationQuery() {
		return (!StringUtils.isBlank(CONNECTION_VALIDATION_QUERY)) ? CONNECTION_VALIDATION_QUERY : null;
	}

	/**
	 *
	 */
	public synchronized static void initialize() {
		try {
			WikiDatabase.CONNECTION_VALIDATION_QUERY = WikiBase.getDataHandler().queryHandler().connectionValidationQuery();
			// initialize connection pool in its own try-catch to avoid an error
			// causing property values not to be saved.
			// this clears out any existing connection pool, so that a new one will be created on first access
			DatabaseConnection.closeConnectionPool();
		} catch (Exception e) {
			logger.error("Unable to initialize database", e);
		}
	}

	/**
	 *
	 */
	private static Connection initializeNewDatabase(Properties props, List<WikiMessage> errors, QueryHandler newQueryHandler) {
		String driver = props.getProperty(Environment.PROP_DB_DRIVER);
		String url = props.getProperty(Environment.PROP_DB_URL);
		String userName = props.getProperty(Environment.PROP_DB_USERNAME);
		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, props);
		Connection conn = null;
		try {
			// test to see if we can connect to the new database
			conn = DatabaseConnection.getTestConnection(driver, url, userName, password);
			conn.setAutoCommit(true);
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {}
			}
			errors.add(new WikiMessage("error.databaseconnection", e.getMessage()));
			return null;
		}
		// test to see if JAMWiki tables already exist (if they do, we can't continue this migration process
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery(newQueryHandler.existenceValidationQuery());
			errors.add(new WikiMessage("setup.error.migrate"));
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {}
			}
			return null;
		} catch (Exception ex) {
			// we expect this exception as the JAMWiki tables don't exist
			logger.debug("NEW Database does not contain any JAMWiki instance");
		} finally {
			DatabaseConnection.closeStatement(stmt);
		}
		try {
			newQueryHandler.createTables(conn);
		} catch (Exception e) {
			logger.error("Error attempting to migrate the database", e);
			errors.add(new WikiMessage("error.unknown", e.getMessage()));
			try {
				newQueryHandler.dropTables(conn);
			} catch (Exception ex) {
				logger.warn("Unable to drop tables in NEW database following failed migration", ex);
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {}
			}
		}
		return conn;
	}

	public synchronized static void shutdown() {
		try {
			DatabaseConnection.closeConnectionPool();
		} catch (Exception e) {
			logger.error("Unable to close the connection pool on shutdown", e);
		}
	}

	/**
	 * This method causes all existing data to be deleted from the Wiki.  Use only
	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
	 * DELETE ALL WIKI DATA!
	 */
	protected static void purgeData(Connection conn) throws DataAccessException {
		// BOOM!  Everything gone...
		WikiBase.getDataHandler().queryHandler().dropTables(conn);
		try {
			// re-create empty tables
			WikiBase.getDataHandler().queryHandler().createTables(conn);
		} catch (Exception e) {
			// creation failure, don't leave tables half-committed
			WikiBase.getDataHandler().queryHandler().dropTables(conn);
		}
	}

	/**
	 * Utility method for reading special topic values from files and returning
	 * the file contents.
	 *
	 * @param locale The locale for the user viewing the special page.
	 * @param pageName The name of the special page being retrieved.
	 */
	protected static String readSpecialPage(Locale locale, String pageName) throws IOException {
		String contents = null;
		String filename = null;
		String language = null;
		String country = null;
		if (locale != null) {
			language = locale.getLanguage();
			country = locale.getCountry();
		}
		String subdirectory = "";
		if (!StringUtils.isBlank(language) && !StringUtils.isBlank(country)) {
			try {
				subdirectory = WikiDatabase.SPECIAL_PAGE_DIR + File.separator + language + "_" + country;
				filename = subdirectory + File.separator + WikiUtil.encodeForFilename(pageName) + ".txt";
				contents = ResourceUtil.readFile(filename);
			} catch (IOException e) {
				logger.info("No locale-specific file is available for " + filename + ", checking for a language-specific version.");
			}
		}
		if (contents == null && !StringUtils.isBlank(language)) {
			try {
				subdirectory = WikiDatabase.SPECIAL_PAGE_DIR + File.separator + language;
				filename = subdirectory + File.separator + WikiUtil.encodeForFilename(pageName) + ".txt";
				contents = ResourceUtil.readFile(filename);
			} catch (IOException e) {
				logger.info("No language-specific file is available for " + filename + " so the default will be used.");
			}
		}
		if (contents == null) {
			try {
				subdirectory = WikiDatabase.SPECIAL_PAGE_DIR;
				filename = subdirectory + File.separator + WikiUtil.encodeForFilename(pageName) + ".txt";
				contents = ResourceUtil.readFile(filename);
			} catch (IOException e) {
				logger.warn("Default topic initialization file " + filename + " could not be read", e);
				throw e;
			}
		}
		return contents;
	}

	/**
	 * Utility method for regenerating categories, "link to" records and other metadata
	 * for all wiki topics.
	 *
	 * @return An array of two numerical values, the first one is the number of records
	 *  updated successfully, the second is the number of records that failed.
	 */
	public static int[] rebuildTopicMetadata() throws DataAccessException {
		int numErrors = 0;
		int numUpdated = 0;
		List<String> topicNames;
		Topic topic;
		ParserOutput parserOutput;
		List<VirtualWiki> virtualWikis = WikiBase.getDataHandler().getVirtualWikiList();
		for (VirtualWiki virtualWiki : virtualWikis) {
			topicNames = WikiBase.getDataHandler().getAllTopicNames(virtualWiki.getName(), false);
			if (topicNames.isEmpty()) {
				continue;
			}
			for (String topicName : topicNames) {
				try {
					topic = WikiBase.getDataHandler().lookupTopic(virtualWiki.getName(), topicName, false);
					if (topic == null) {
						logger.warn("Invalid topic record found, possible database integrity issue: " + virtualWiki.getName() + " / " + topicName);
						numErrors++;
						continue;
					}
					parserOutput = ParserUtil.parserOutput(topic.getTopicContent(), virtualWiki.getName(), topicName);
					WikiBase.getDataHandler().writeTopic(topic, null, parserOutput.getCategories(), parserOutput.getLinks());
					numUpdated++;
				} catch (ParserException e) {
					logger.error("Failure while regenerating topic metadata for " + virtualWiki.getName() + " / " + topicName + ": " + e.getMessage());
					numErrors++;
				} catch (DataAccessException e) {
					logger.error("Failure while regenerating topic metadata for " + virtualWiki.getName() + " / " + topicName + ": " + e.getMessage());
					numErrors++;
				} catch (WikiException e) {
					logger.error("Failure while regenerating topic metadata for " + virtualWiki.getName() + " / " + topicName + ": " + e.getMessage());
					numErrors++;
				}
			}
		}
		int[] resultArray = new int[2];
		resultArray[0] = numUpdated;
		resultArray[1] = numErrors;
		return resultArray;
	}

	/**
	 *
	 */
	protected static void releaseConnection(Connection conn, Object transactionObject) throws SQLException {
		if (transactionObject instanceof Connection) {
			// transaction objects will be released elsewhere
			return;
		}
		WikiDatabase.releaseConnection(conn);
	}

	/**
	 *
	 */
	private static void releaseConnection(Connection conn) throws SQLException {
		if (conn == null) {
			return;
		}
		try {
			conn.commit();
		} finally {
			DatabaseConnection.closeConnection(conn);
		}
	}

	/**
	 * Return the largest primary key ID for the specified table, or 1 if the table does
	 * not have a numeric primary key value.
	 */
	private static int retrieveMaximumTableId(String tableName, String primaryIdColumnName) throws SQLException {
		if (StringUtils.isBlank(tableName) || StringUtils.isBlank(primaryIdColumnName)) {
			return 1;
		}
		String sql = "select max(" + primaryIdColumnName + ") as max_table_id from " + tableName;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			return (rs.next()) ? rs.getInt("max_table_id") : 0;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected static void setup(Locale locale, WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
		TransactionStatus status = null;
		try {
			status = DatabaseConnection.startTransaction();
			Connection conn = DatabaseConnection.getConnection();
			// set up tables
			WikiBase.getDataHandler().queryHandler().createTables(conn);
			WikiDatabase.setupDefaultVirtualWiki();
			WikiDatabase.setupDefaultNamespaces();
			WikiDatabase.setupDefaultInterwikis();
			WikiDatabase.setupRoles();
			WikiDatabase.setupGroups();
			WikiDatabase.setupUserPreferencesDefaults();
			WikiDatabase.setupAdminUser(user, username, encryptedPassword);
			WikiDatabase.setupSpecialPages(locale, user);
		} catch (SQLException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.error("Unable to set up database tables", e);
			// clean up anything that might have been created
			try {
				Connection conn = DatabaseConnection.getConnection();
				WikiBase.getDataHandler().queryHandler().dropTables(conn);
			} catch (Exception e2) {}
			throw new DataAccessException(e);
		} catch (DataAccessException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.error("Unable to set up database tables", e);
			// clean up anything that might have been created
			try {
				Connection conn = DatabaseConnection.getConnection();
				WikiBase.getDataHandler().queryHandler().dropTables(conn);
			} catch (Exception e2) {}
			throw e;
		} catch (WikiException e) {
			DatabaseConnection.rollbackOnException(status, e);
			logger.error("Unable to set up database tables", e);
			// clean up anything that might have been created
			try {
				Connection conn = DatabaseConnection.getConnection();
				WikiBase.getDataHandler().queryHandler().dropTables(conn);
			} catch (Exception e2) {}
			throw e;
		}
		DatabaseConnection.commit(status);
	}

	/**
	 *
	 */
	private static void setupAdminUser(WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
		logger.info("Creating wiki admin user");
		if (user == null) {
			throw new IllegalArgumentException("Cannot pass null or anonymous WikiUser object to setupAdminUser");
		}
		if (WikiBase.getDataHandler().lookupWikiUser(user.getUserId()) != null) {
			logger.warn("Admin user already exists");
		}
		WikiBase.getDataHandler().writeWikiUser(user, username, encryptedPassword);
		List<String> roles = new ArrayList<String>();
		roles.add(Role.ROLE_ADMIN.getAuthority());
		roles.add(Role.ROLE_IMPORT.getAuthority());
		roles.add(Role.ROLE_SYSADMIN.getAuthority());
		roles.add(Role.ROLE_TRANSLATE.getAuthority());
		WikiBase.getDataHandler().writeRoleMapUser(user.getUsername(), roles);
	}

	/**
	 *
	 */
	public static void setupDefaultDatabase(Properties props) {
		props.setProperty(Environment.PROP_DB_DRIVER, "org.hsqldb.jdbcDriver");
		props.setProperty(Environment.PROP_DB_TYPE, QueryHandler.QUERY_HANDLER_HSQL);
		props.setProperty(Environment.PROP_DB_USERNAME, "sa");
		props.setProperty(Environment.PROP_DB_PASSWORD, "");
		File file = new File(props.getProperty(Environment.PROP_BASE_FILE_DIR), "database");
		if (!file.exists()) {
			file.mkdirs();
		}
		String url = "jdbc:hsqldb:file:" + new File(file.getPath(), "jamwiki").getPath() + ";shutdown=true";
		props.setProperty(Environment.PROP_DB_URL, url);
	}

	/**
	 *
	 */
	// FIXME - make this private once the ability to upgrade to 1.0.0 is removed
	protected static void setupDefaultInterwikis() throws DataAccessException, WikiException {
		logger.info("Creating default interwiki records");
		Interwiki jamwiki = new Interwiki("jamwikiorg", "http://jamwiki.org/wiki/en/{0}", "JAMWiki");
		WikiBase.getDataHandler().writeInterwiki(jamwiki);
		Interwiki mediawiki = new Interwiki("mediawiki", "http://www.mediawiki.org/wiki/{0}", "MediaWiki");
		WikiBase.getDataHandler().writeInterwiki(mediawiki);
		Interwiki metawikipedia = new Interwiki("metawikipedia", "http://meta.wikimedia.org/wiki/{0}", "Wikimedia Meta-Wiki");
		WikiBase.getDataHandler().writeInterwiki(metawikipedia);
		Interwiki wiki = new Interwiki("wiki", "http://c2.com/cgi/wiki?{0}", "WikiWiki");
		WikiBase.getDataHandler().writeInterwiki(wiki);
		Interwiki wikia = new Interwiki("wikia", "http://www.wikia.com/wiki/index.php/{0}", "Wikia");
		WikiBase.getDataHandler().writeInterwiki(wikia);
		Interwiki wikipedia = new Interwiki("wikipedia", "http://en.wikipedia.org/wiki/{0}", "Wikipedia");
		WikiBase.getDataHandler().writeInterwiki(wikipedia);
		Interwiki wikiquote = new Interwiki("wikiquote", "http://en.wikiquote.org/wiki/{0}", "Wikiquote");
		WikiBase.getDataHandler().writeInterwiki(wikiquote);
		Interwiki wikinews = new Interwiki("wikinews", "http://en.wikinews.org/wiki/{0}", "Wikinews");
		WikiBase.getDataHandler().writeInterwiki(wikinews);
	}

	/**
	 *
	 */
	private static void setupDefaultNamespaces() throws DataAccessException, WikiException {
		logger.info("Creating default wiki namespaces");
		Namespace[] defaultNamespaces = Namespace.retrieveDefaultNamespacesForSetup();
		// namespaces are ordered with main first, then comments, so loop through and get each
		for (int i = 0; i < defaultNamespaces.length; i++) {
			Namespace mainNamespace = defaultNamespaces[i];
			WikiBase.getDataHandler().writeNamespace(mainNamespace);
			// some namespaces do not have a comments namespace, so verify one is present
			if (defaultNamespaces.length > (i + 1)) {
				Namespace commentsNamespace = defaultNamespaces[i + 1];
				if (mainNamespace.getId().equals(commentsNamespace.getMainNamespaceId())) {
					WikiBase.getDataHandler().writeNamespace(commentsNamespace);
					i++;
				}
			}
		}
	}

	/**
	 *
	 */
	private static void setupDefaultVirtualWiki() throws DataAccessException, WikiException {
		logger.info("Creating default virtual wiki");
		VirtualWiki virtualWiki = VirtualWiki.defaultVirtualWiki();
		WikiBase.getDataHandler().writeVirtualWiki(virtualWiki);
	}

	/**
	 *
	 */
	protected static void setupGroups() throws DataAccessException, WikiException {
		logger.info("Creating default wiki groups");
		WikiGroup group = new WikiGroup(WikiGroup.GROUP_ANONYMOUS);
		// FIXME - use message key
		group.setDescription("All non-logged in users are automatically assigned to the anonymous group.");
		WikiBase.getDataHandler().writeWikiGroup(group);
		List<String> anonymousRoles = new ArrayList<String>();
		anonymousRoles.add(Role.ROLE_EDIT_EXISTING.getAuthority());
		anonymousRoles.add(Role.ROLE_EDIT_NEW.getAuthority());
		anonymousRoles.add(Role.ROLE_REGISTER.getAuthority());
		anonymousRoles.add(Role.ROLE_UPLOAD.getAuthority());
		anonymousRoles.add(Role.ROLE_VIEW.getAuthority());
		WikiBase.getDataHandler().writeRoleMapGroup(group.getGroupId(), anonymousRoles);
		group = new WikiGroup(WikiGroup.GROUP_REGISTERED_USER);
		// FIXME - use message key
		group.setDescription("All logged in users are automatically assigned to the registered user group.");
		WikiBase.getDataHandler().writeWikiGroup(group);
		List<String> userRoles = new ArrayList<String>();
		userRoles.add(Role.ROLE_EDIT_EXISTING.getAuthority());
		userRoles.add(Role.ROLE_EDIT_NEW.getAuthority());
		userRoles.add(Role.ROLE_MOVE.getAuthority());
		userRoles.add(Role.ROLE_UPLOAD.getAuthority());
		userRoles.add(Role.ROLE_VIEW.getAuthority());
		WikiBase.getDataHandler().writeRoleMapGroup(group.getGroupId(), userRoles);
	}

	/**
	 *
	 */
	protected static void setupRoles() throws DataAccessException, WikiException {
		logger.info("Creating default wiki roles");
		Role role = Role.ROLE_ADMIN;
		// FIXME - use message key
		role.setDescription("Provides the ability to perform wiki maintenance tasks not available to normal users.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_EDIT_EXISTING;
		// FIXME - use message key
		role.setDescription("Allows a user to edit an existing topic.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_EDIT_NEW;
		// FIXME - use message key
		role.setDescription("Allows a user to create a new topic.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_IMPORT;
		// FIXME - use message key
		role.setDescription("Allows a user to import data from a file.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_MOVE;
		// FIXME - use message key
		role.setDescription("Allows a user to move a topic to a different name.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_SYSADMIN;
		// FIXME - use message key
		role.setDescription("Allows access to set database parameters, modify parser settings, and set other wiki system settings.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_TRANSLATE;
		// FIXME - use message key
		role.setDescription("Allows access to the translation tool used for modifying the values of message keys used to display text on the wiki.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_UPLOAD;
		// FIXME - use message key
		role.setDescription("Allows a user to upload a file to the wiki.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_VIEW;
		// FIXME - use message key
		role.setDescription("Allows a user to view topics on the wiki.");
		WikiBase.getDataHandler().writeRole(role, false);
		role = Role.ROLE_REGISTER;
		// FIXME - use message key
		role.setDescription("Allows anonymous users to self-register.");
		WikiBase.getDataHandler().writeRole(role, false);
	}

	/**
	 *
	 */
	protected static void setupSpecialPage(Locale locale, String virtualWiki, String topicName, WikiUser user, boolean adminOnly, boolean readOnly) throws DataAccessException, WikiException {
		logger.info("Setting up special page " + virtualWiki + " / " + topicName);
		if (user == null) {
			throw new IllegalArgumentException("Cannot pass null WikiUser object to setupSpecialPage");
		}
		String contents = null;
		try {
			contents = WikiDatabase.readSpecialPage(locale, topicName);
		} catch (IOException e) {
			throw new DataAccessException(e);
		}
		WikiLink wikiLink = new WikiLink(null, virtualWiki, topicName);
		Topic topic = new Topic(virtualWiki, wikiLink.getNamespace(), wikiLink.getArticle());
		topic.setTopicContent(contents);
		topic.setAdminOnly(adminOnly);
		topic.setReadOnly(readOnly);
		int charactersChanged = StringUtils.length(contents);
		// FIXME - hard coding
		TopicVersion topicVersion = new TopicVersion(user, user.getLastLoginIpAddress(), "Automatically created by system setup", contents, charactersChanged);
		// FIXME - it is not connection-safe to parse for metadata since we are already holding a connection
		// ParserOutput parserOutput = ParserUtil.parserOutput(topic.getTopicContent(), virtualWiki, topicName);
		// WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
		WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, null);
	}

	/**
	 *
	 */
	private static void setupSpecialPages(Locale locale, WikiUser user) throws DataAccessException, WikiException {
		List<VirtualWiki> all = WikiBase.getDataHandler().getVirtualWikiList();
		for (VirtualWiki virtualWiki : all) {
			// create the default topics
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STARTING_POINTS, user, false, false);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_SIDEBAR, user, true, false);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_FOOTER, user, true, false);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_HEADER, user, true, false);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_SYSTEM_CSS, user, true, true);
			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_CUSTOM_CSS, user, true, false);
		}
	}

	/**
	 *
	 */
	// TODO - make this method private once the ability to upgrade to 1.3.0 has been removed.
	protected static void setupUserPreferencesDefaults() throws DataAccessException, WikiException {
		WikiBase.getDataHandler().writeUserPreferenceDefault(WikiUser.USER_PREFERENCE_DEFAULT_LOCALE, Locale.getDefault().toString(), WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, 1);
		WikiBase.getDataHandler().writeUserPreferenceDefault(WikiUser.USER_PREFERENCE_TIMEZONE, TimeZone.getDefault().getID(), WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, 2);
		WikiBase.getDataHandler().writeUserPreferenceDefault(WikiUser.USER_PREFERENCE_DATE_FORMAT, WikiConfiguration.getInstance().getDateFormats().get(0), WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, 3);
		WikiBase.getDataHandler().writeUserPreferenceDefault(WikiUser.USER_PREFERENCE_TIME_FORMAT, WikiConfiguration.getInstance().getTimeFormats().get(0), WikiUser.USER_PREFERENCES_GROUP_INTERNATIONALIZATION, 4);
		WikiBase.getDataHandler().writeUserPreferenceDefault(WikiUser.USER_PREFERENCE_PREFERRED_EDITOR, "toolbar", WikiUser.USER_PREFERENCES_GROUP_EDITING, 1);
		WikiBase.getDataHandler().writeUserPreferenceDefault(WikiUser.USER_PREFERENCE_SIGNATURE, null, WikiUser.USER_PREFERENCES_GROUP_EDITING, 2);
	}
}
