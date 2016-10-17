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
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.ResourceUtil;

/**
 * Extension of the Commons DBCP BasicDataSource class that
 * initializes itself from the JAMWiki Environment.
 * <p>
 * Note that we do not directly use the "BasicDataSource" supplied by the commons-dbcp package,
 * because as of version 1.2.2 it does not allow to modify the "WhenExhaustedAction" of the 
 * underlying commons-pool GenericObjectPool instance.
 */
public class LocalDataSource extends BasicDataSource {

	/**
	 * Constructs a new WikiDataSource
	 */
	public LocalDataSource() throws SQLException, ClassNotFoundException {
		super();
		if (!StringUtils.isBlank(Environment.getValue(Environment.PROP_DB_DRIVER))) {
			ResourceUtil.forName(Environment.getValue(Environment.PROP_DB_DRIVER));
		}
		setUrl(Environment.getValue(Environment.PROP_DB_URL));
		setUsername(Environment.getValue(Environment.PROP_DB_USERNAME));
		setPassword(Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, null));
		setDefaultReadOnly(false);
		// AutoCommit should NOT be set to true 
		// set pool properties
		setMaxActive(Environment.getIntValue(Environment.PROP_DBCP_MAX_ACTIVE));
		setMaxIdle(Environment.getIntValue(Environment.PROP_DBCP_MAX_IDLE));
		setMinEvictableIdleTimeMillis(Environment.getIntValue(Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME) * 1000);
		setTestOnBorrow(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_ON_BORROW));
		setTestOnReturn(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_ON_RETURN));
		setTestWhileIdle(Environment.getBooleanValue(Environment.PROP_DBCP_TEST_WHILE_IDLE));
		setTimeBetweenEvictionRunsMillis(Environment.getIntValue(Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS) * 1000);
		setNumTestsPerEvictionRun(Environment.getIntValue(Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN));
		setValidationQuery(WikiDatabase.getConnectionValidationQuery());
		setPoolPreparedStatements(Environment.getBooleanValue(Environment.PROP_DBCP_POOL_PREPARED_STATEMENTS));
		setMaxOpenPreparedStatements(Environment.getIntValue(Environment.PROP_DBCP_MAX_OPEN_PREPARED_STATEMENTS));
		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(QueryHandler.QUERY_HANDLER_ORACLE)) {
			// handle clobs as strings, Oracle 10g and higher drivers (ojdbc14.jar)
			addConnectionProperty("SetBigStringTryClob", "true");
		}
		if (url.startsWith("jdbc:hsqldb:mem")) {
			addConnectionProperty("shutdown", "true");
		}
		// Test the connection (this will also initialize the connection pool)
		Connection testConnection = null;
		try {
			// try to get a test connection
			testConnection = getConnection();
		} catch (SQLException ex) {
			if (connectionPool != null) {
				try {
					connectionPool.close();
				} catch (Exception e) {
				} // ignore any exception during cleanup
			}
			throw ex;
		} finally {
			if (testConnection != null) {
				testConnection.close();
			}
		}
		// the ConnectionPool is now initialised, so we can set the dbcp-when-exhausted-action
		connectionPool.setWhenExhaustedAction((byte) Environment.getIntValue(Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION));
	}
}
