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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jamwiki.Environment;
import org.jamwiki.model.Category;
import org.jamwiki.model.LogItem;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.TopicType;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;

/**
 * Caché-specific implementation of the QueryHandler interface.  This class implements
 * Caché-specific methods for instances where Caché does not support the default
 * ASCII SQL syntax.
 * Most of these changes have to do with creating a pagination scheme that will work
 * Caché does not support the limit and offset functionality
 * Also it needs the content to be stored and passed as a clob to avoid default string size limitations
 * 
 * in sql.cache.properties there are 3 changes to upgrade sql statements due to the way
 * Caché handles alter statements.   alter statements are required to do no more than one 
 * operation at a time, and specifying the data type (ie,  VARCHAR(200) NOT NULL) is 
 * considered to be an operation.   Since the datatype and size for the fields in question
 * had not changed,  i removed the datatype declaration to leave the actual "not null" change
 */
public class CacheQueryHandler extends AnsiQueryHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(AnsiQueryHandler.class.getName());
	protected static final String SQL_PROPERTY_FILE_NAME = "sql/sql.cache.properties";

	/**
	 *
	 */
	public CacheQueryHandler() {
		Properties defaults = Environment.loadProperties(AnsiQueryHandler.SQL_PROPERTY_FILE_NAME);
		Properties props = Environment.loadProperties(SQL_PROPERTY_FILE_NAME, defaults);
		super.init(props);
	}


	/**
	 *
	 */
	public List<Category> getCategories(int virtualWikiId, String virtualWikiName, Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = this.getCategoriesStatement(conn, virtualWikiId, virtualWikiName, pagination);
			rs = stmt.executeQuery();
			List<Category> results = new ArrayList<Category>();
			while (rs.next()) {
				Category category = new Category();
				category.setName(rs.getString("category_name"));
				// child topic name not initialized since it is not needed
				category.setVirtualWiki(virtualWikiName);
				category.setSortKey(rs.getString("sort_key"));
				// topic type not initialized since it is not needed
				results.add(category);
			}
			return results;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement getCategoriesStatement(Connection conn, int virtualWikiId, String virtualWikiName, Pagination pagination) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_CATEGORIES);
		stmt.setInt(1, pagination.getNumResults());
		stmt.setInt(2, virtualWikiId);
		stmt.setInt(3, pagination.getOffset());
		return stmt;
	}

	/**
	 *
	 */
	public List<LogItem> getLogItems(int virtualWikiId, String virtualWikiName, int logType, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<LogItem> logItems = new ArrayList<LogItem>();
		try {
			conn = DatabaseConnection.getConnection();
			stmt = this.getLogItemsStatement(conn, virtualWikiId, virtualWikiName, logType, pagination, descending);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			while (rs.next()) {
				logItems.add(this.initLogItem(rs, virtualWikiName));
			}
			return logItems;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement getLogItemsStatement(Connection conn, int virtualWikiId, String virtualWikiName, int logType, Pagination pagination, boolean descending) throws SQLException {
		int index = 1;
		PreparedStatement stmt = null;
		if (logType == -1) {
			stmt = conn.prepareStatement(STATEMENT_SELECT_LOG_ITEMS);
		} else {
			stmt = conn.prepareStatement(STATEMENT_SELECT_LOG_ITEMS_BY_TYPE);
			stmt.setInt(index++, logType);
		}
		stmt.setInt(index++, pagination.getNumResults());
		stmt.setInt(index++, virtualWikiId);
		stmt.setInt(index++, pagination.getOffset());
		return stmt;
	}

	/**
	 *
	 */
	public List<RecentChange> getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = this.getRecentChangesStatement(conn, virtualWiki, pagination, descending);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement getRecentChangesStatement(Connection conn, String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_RECENT_CHANGES);
		stmt.setInt(1, pagination.getNumResults());
		stmt.setString(2, virtualWiki);
		stmt.setInt(3, pagination.getOffset());
		return stmt;
	}

	/**
	 *
	 */
	public List<RecentChange> getTopicHistory(int topicId, Pagination pagination, boolean descending, boolean selectDeleted) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = getTopicHistoryStatement(conn, topicId, pagination, descending, selectDeleted);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement getTopicHistoryStatement(Connection conn, int topicId, Pagination pagination, boolean descending, boolean selectDeleted) throws SQLException {
		// the SQL contains the syntax "is {0} null", which needs to be formatted as a message.
		Object[] params = {""};
		if (selectDeleted) {
			params[0] = "not";
		}
		String sql = this.formatStatement(STATEMENT_SELECT_TOPIC_HISTORY, params);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, pagination.getNumResults());
		stmt.setInt(2, topicId);
		stmt.setInt(3, pagination.getOffset());
		return stmt;
	}

	/**
	 *
	 */
	public List<String> getTopicsAdmin(int virtualWikiId, Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = this.getTopicsAdminStatement(conn, virtualWikiId, pagination);
			rs = stmt.executeQuery();
			List<String> results = new ArrayList<String>();
			while (rs.next()) {
				results.add(rs.getString("topic_name"));
			}
			return results;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement getTopicsAdminStatement(Connection conn, int virtualWikiId, Pagination pagination) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_TOPICS_ADMIN);
		stmt.setInt(1, pagination.getNumResults());
		stmt.setInt(2, virtualWikiId);
		stmt.setInt(3, pagination.getOffset());
		return stmt;
	}

	/**
	 *
	 */
	public List<RecentChange> getUserContributionsByLogin(String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = this.getUserContributionsByLoginStatement(conn, virtualWiki, login, pagination, descending);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement getUserContributionsByLoginStatement(Connection conn, String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN);
		stmt.setInt(1, pagination.getNumResults());
		stmt.setString(2, virtualWiki);
		stmt.setString(3, login);
		stmt.setInt(4, pagination.getOffset());
		return stmt;
	}

	/**
	 *
	 */
	public List<RecentChange> getUserContributionsByUserDisplay(String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = this.getUserContributionsByUserDisplayStatement(conn, virtualWiki, userDisplay, pagination, descending);
			// FIXME - sort order ignored
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement getUserContributionsByUserDisplayStatement(Connection conn, String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS);
		stmt.setInt(1, pagination.getNumResults());
		stmt.setString(2, virtualWiki);
		stmt.setString(3, userDisplay);
		stmt.setInt(4, pagination.getOffset());
		return stmt;
	}

	/**
	 *
	 */
	public List<String> getWatchlist(int virtualWikiId, int userId) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = conn.prepareStatement(STATEMENT_SELECT_WATCHLIST);
			stmt.setInt(1, virtualWikiId);
			stmt.setInt(2, userId);
			rs = stmt.executeQuery();
			List<String> watchedTopicNames = new ArrayList<String>();
			while (rs.next()) {
				watchedTopicNames.add(rs.getString("topic_name"));
			}
			return watchedTopicNames;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	public List<RecentChange> getWatchlist(int virtualWikiId, int userId, Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = this.getWatchlistStatement(conn, virtualWikiId, userId, pagination);
			rs = stmt.executeQuery();
			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
			while (rs.next()) {
				recentChanges.add(this.initRecentChange(rs));
			}
			return recentChanges;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement getWatchlistStatement(Connection conn, int virtualWikiId, int userId, Pagination pagination) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_WATCHLIST_CHANGES);
		stmt.setInt(1, pagination.getNumResults());
		stmt.setInt(2, virtualWikiId);
		stmt.setInt(3, userId);
		stmt.setInt(4, pagination.getOffset());
		return stmt;
	}

	/**
	 *
	 */
	public Map<Integer, String> lookupTopicByType(int virtualWikiId, TopicType topicType1, TopicType topicType2, int namespaceStart, int namespaceEnd, Pagination pagination) throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DatabaseConnection.getConnection();
			stmt = this.lookupTopicByTypeStatement(conn, virtualWikiId, topicType1, topicType2, namespaceStart, namespaceEnd, pagination);
			rs = stmt.executeQuery();
			Map<Integer, String> results = new LinkedHashMap<Integer, String>();
			while (rs.next()) {
				results.put(rs.getInt("topic_id"), rs.getString("topic_name"));
			}
			return results;
		} finally {
			DatabaseConnection.closeConnection(conn, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected PreparedStatement lookupTopicByTypeStatement(Connection conn, int virtualWikiId, TopicType topicType1, TopicType topicType2, int namespaceStart, int namespaceEnd, Pagination pagination) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_BY_TYPE);
		stmt.setInt(1, pagination.getNumResults());
		stmt.setInt(2, virtualWikiId);
		stmt.setInt(3, topicType1.id());
		stmt.setInt(4, topicType2.id());
		stmt.setInt(5, namespaceStart);
		stmt.setInt(6, namespaceEnd);
		stmt.setInt(7, pagination.getOffset());
		return stmt;
	}
	
	/**
	 * 
	 */
	protected void prepareTopicVersionStatement(TopicVersion topicVersion, PreparedStatement stmt) throws SQLException {
		StringReader sr = null;
		try {
			int index = 1;
			stmt.setInt(index++, topicVersion.getTopicVersionId());
			if (topicVersion.getEditDate() == null) {
				topicVersion.setEditDate(new Timestamp(System.currentTimeMillis()));
			}
			stmt.setInt(index++, topicVersion.getTopicId());
			stmt.setString(index++, topicVersion.getEditComment());
			//pass the content into a stream to be passed to Caché
			sr = new StringReader(topicVersion.getVersionContent());
			stmt.setCharacterStream(index++, sr, topicVersion.getVersionContent().length());
			if (topicVersion.getAuthorId() == null) {
				stmt.setNull(index++, Types.INTEGER);
			} else {
				stmt.setInt(index++, topicVersion.getAuthorId());
			}
			stmt.setInt(index++, topicVersion.getEditType());
			stmt.setString(index++, topicVersion.getAuthorDisplay());
			stmt.setTimestamp(index++, topicVersion.getEditDate());
			if (topicVersion.getPreviousTopicVersionId() == null) {
				stmt.setNull(index++, Types.INTEGER);
			} else {
				stmt.setInt(index++, topicVersion.getPreviousTopicVersionId());
			}
			stmt.setInt(index++, topicVersion.getCharactersChanged());
			stmt.setString(index++, topicVersion.getVersionParamString());
		} finally {
			if (sr != null) {
				sr.close();
			}
		}
	}
}
