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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.jamwiki.Environment;
import org.jamwiki.model.TopicType;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;

/**
 * DB2/400-specific implementation of the QueryHandler interface.  This class implements
 * DB2/400-specific methods for instances where DB2/400 does not support the default
 * ASCII SQL syntax.
 */
public class DB2400QueryHandler extends AnsiQueryHandler {

	private static final WikiLogger logger = WikiLogger.getLogger(DB2400QueryHandler.class.getName());
	private static final String SQL_PROPERTY_FILE_NAME = "sql/sql.db2400.properties";

	/**
	 *
	 */
	public DB2400QueryHandler() {
		Properties defaults = Environment.loadProperties(AnsiQueryHandler.SQL_PROPERTY_FILE_NAME);
		Properties props = Environment.loadProperties(SQL_PROPERTY_FILE_NAME, defaults);
		super.init(props);
	}

	/**
	 * DB2/400 will not allow query parameters such as "fetch ? rows only", so
	 * this method provides a way of formatting the query limits without using
	 * query parameters.
	 *
	 * @param sql The SQL statement, with the last result parameter specified as
	 *  {0} and the total number of rows parameter specified as {1}.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @return A formatted SQL string.
	 */
	private String formatStatement(String sql, Pagination pagination) {
		Object[] objects = {pagination.getEnd(), pagination.getNumResults()};
		return this.formatStatement(sql, objects);
	}

	/**
	 * DB2/400 will not allow query parameters such as "fetch ? rows only", so
	 * this method provides a way of formatting the query limits without using
	 * query parameters.
	 *
	 * @param sql The SQL statement, with the last result parameter specified as
	 *  {0} and the total number of rows parameter specified as {1}.
	 * @param pagination A Pagination object that specifies the number of results
	 *  and starting result offset for the result set to be retrieved.
	 * @return A formatted SQL string.
	 */
	private String formatStatement(String sql, int limit) {
		Object[] objects = {limit};
		return this.formatStatement(sql, objects);
	}

	/**
	 *
	 */
	protected PreparedStatement getCategoriesStatement(Connection conn, int virtualWikiId, String virtualWikiName, Pagination pagination) throws SQLException {
		String sql = this.formatStatement(STATEMENT_SELECT_CATEGORIES, pagination);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, virtualWikiId);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement getLogItemsStatement(Connection conn, int virtualWikiId, String virtualWikiName, int logType, Pagination pagination, boolean descending) throws SQLException {
		int index = 1;
		PreparedStatement stmt = null;
		String sql = null;
		if (logType == -1) {
			sql = this.formatStatement(STATEMENT_SELECT_LOG_ITEMS, pagination);
			stmt = conn.prepareStatement(sql);
		} else {
			sql = this.formatStatement(STATEMENT_SELECT_LOG_ITEMS_BY_TYPE, pagination);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(index++, logType);
		}
		stmt.setInt(index++, virtualWikiId);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement getRecentChangesStatement(Connection conn, String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
		String sql = this.formatStatement(STATEMENT_SELECT_RECENT_CHANGES, pagination);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, virtualWiki);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement getTopicHistoryStatement(Connection conn, int topicId, Pagination pagination, boolean descending, boolean selectDeleted) throws SQLException {
		Object[] params = {pagination.getEnd(), pagination.getNumResults(), ""};
		if (selectDeleted) {
			params[2] = "not";
		}
		String sql = this.formatStatement(STATEMENT_SELECT_TOPIC_HISTORY, params);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, topicId);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement getTopicsAdminStatement(Connection conn, int virtualWikiId, Pagination pagination) throws SQLException {
		String sql = this.formatStatement(STATEMENT_SELECT_TOPICS_ADMIN, pagination);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, virtualWikiId);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement getUserContributionsByLoginStatement(Connection conn, String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
		String sql = this.formatStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN, pagination);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, virtualWiki);
		stmt.setString(2, login);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement getUserContributionsByUserDisplayStatement(Connection conn, String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
		String sql = this.formatStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS, pagination);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, virtualWiki);
		stmt.setString(2, userDisplay);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement getWatchlistStatement(Connection conn, int virtualWikiId, int userId, Pagination pagination) throws SQLException {
		String sql = this.formatStatement(STATEMENT_SELECT_WATCHLIST_CHANGES, pagination);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, userId);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement lookupTopicByTypeStatement(Connection conn, int virtualWikiId, TopicType topicType1, TopicType topicType2, int namespaceStart, int namespaceEnd, Pagination pagination) throws SQLException {
		String sql = this.formatStatement(STATEMENT_SELECT_TOPIC_BY_TYPE, pagination);
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, topicType1.id());
		stmt.setInt(3, topicType2.id());
		stmt.setInt(4, namespaceStart);
		stmt.setInt(5, namespaceEnd);
		return stmt;
	}

	/**
	 *
	 */
	protected PreparedStatement lookupWikiUsersStatement(Connection conn, Pagination pagination) throws SQLException {
		String sql = this.formatStatement(STATEMENT_SELECT_WIKI_USERS, pagination);
		PreparedStatement stmt = conn.prepareStatement(sql);
		return stmt;
	}

	/**
	 *
	 */
	public void reloadRecentChanges(Connection conn, int limit) throws SQLException {
		PreparedStatement stmt = null;
		String sql = this.formatStatement(STATEMENT_INSERT_RECENT_CHANGES_VERSIONS, limit);
		try {
			DatabaseConnection.executeUpdate(STATEMENT_DELETE_RECENT_CHANGES, conn);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, limit);
			stmt.executeUpdate();
			DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES_LOGS, conn);
		} finally {
			DatabaseConnection.closeStatement(stmt);
		}
	}
}
