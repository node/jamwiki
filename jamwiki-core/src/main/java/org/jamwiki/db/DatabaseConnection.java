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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.utils.ResourceUtil;
import org.jamwiki.utils.WikiLogger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class provides methods for retrieving database connections, executing queries,
 * and setting up connection pools.
 */
public class DatabaseConnection {

	private static final WikiLogger logger = WikiLogger.getLogger(DatabaseConnection.class.getName());
	private static DataSource dataSource = null;
	private static DataSourceTransactionManager transactionManager = null;

	/**
	 * This class has only static methods and is never instantiated.
	 */
	private DatabaseConnection() {
	}

	/**
	 * Utility method for closing a database connection, a statement and a result set.
	 * This method must ALWAYS be called for any connection retrieved by the
	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
	 * connection SHOULD NOT have already been closed.
	 *
	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
	 * @param stmt A statement object that is to be closed.  May be <code>null</code>.
	 * @param rs A result set object that is to be closed.  May be <code>null</code>.
	 */
	protected static void closeConnection(Connection conn, Statement stmt, ResultSet rs) {
		DatabaseConnection.closeResultSet(rs);
		DatabaseConnection.closeConnection(conn, stmt);
	}

	/**
	 * Utility method for closing a database connection and a statement.  This method
	 * must ALWAYS be called for any connection retrieved by the
	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
	 * connection SHOULD NOT have already been closed.
	 *
	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
	 * @param stmt A statement object that is to be closed.  May be <code>null</code>.
	 */
	protected static void closeConnection(Connection conn, Statement stmt) {
		DatabaseConnection.closeStatement(stmt);
		DatabaseConnection.closeConnection(conn);
	}

	/**
	 * Utility method for closing a database connection.  This method must ALWAYS be
	 * called for any connection retrieved by the
	 * {@link DatabaseConnection#getConnection getConnection()} method, and the
	 * connection SHOULD NOT have already been closed.
	 *
	 * @param conn A database connection, retrieved using DatabaseConnection.getConnection(),
	 *  that is to be closed.  This connection SHOULD NOT have been previously closed.
	 */
	protected static void closeConnection(Connection conn) {
		if (conn == null) {
			return;
		}
		DataSourceUtils.releaseConnection(conn, dataSource);
	}

	/**
	 * Close the connection pool, to be called for example during Servlet shutdown.
	 * <p>
	 * Note that this only applies if the DataSource was created by JAMWiki;
	 * in the case of a container DataSource obtained via JNDI this method does nothing
	 * except clear the static reference to the DataSource.
	 */
	protected static void closeConnectionPool() throws SQLException {
		try {
			DataSource testDataSource = dataSource;
			while (testDataSource instanceof DelegatingDataSource) {
				testDataSource = ((DelegatingDataSource) testDataSource).getTargetDataSource();
			}
			if (testDataSource instanceof BasicDataSource) {
				// required to release any connections e.g. in case of servlet shutdown
				((BasicDataSource) testDataSource).close();
			}
		} catch (SQLException e) {
			logger.error("Unable to close connection pool", e);
			throw e;
		}
		// clear references to prevent them being reused (& allow garbage collection)
		dataSource = null;
		transactionManager = null;
	}

	/**
	 * Utility method for closing a result set that may or may not be <code>null</code>.
	 * The result set SHOULD NOT have already been closed.
	 *
	 * @param rs A statement object that is to be closed.  May be <code>null</code>.
	 */
	protected static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {}
		}
	}

	/**
	 * Utility method for closing a statement that may or may not be <code>null</code>.
	 * The statement SHOULD NOT have already been closed.
	 *
	 * @param stmt A statement object that is to be closed.  May be <code>null</code>.
	 */
	protected static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {}
		}
	}

	/**
	 * Execute a query to retrieve a single integer value, generally the result of SQL such
	 * as "select max(id) from table".
	 *
	 * @param sql The SQL to execute.
	 * @param field The field that is returned containing the integer value.
	 * @param conn The database connection to use when querying.
	 * @return Returns the result of the query or 0 if no result is found.
	 */
	protected static int executeSequenceQuery(String sql, String field, Connection conn) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			return (rs.next()) ? rs.getInt(field) : 0;
		} finally {
			DatabaseConnection.closeConnection(null, stmt, rs);
		}
	}

	/**
	 *
	 */
	protected static int executeUpdate(String sql, Connection conn) throws SQLException {
		Statement stmt = null;
		try {
			long start = System.currentTimeMillis();
			stmt = conn.createStatement();
			if (logger.isInfoEnabled()) {
				logger.info("Executing SQL: " + sql);
			}
			int result = stmt.executeUpdate(sql);
			if (logger.isDebugEnabled()) {
				long execution = System.currentTimeMillis() - start;
				logger.debug("Executed " + sql + " (" + (execution / 1000.000) + " s.)");
			}
			return result;
		} catch (SQLException e) {
			logger.error("Failure while executing " + sql, e);
			throw e;
		} finally {
			DatabaseConnection.closeStatement(stmt);
		}
	}

	/**
	 * Execute a string representing a SQL statement, suppressing any exceptions.
	 */
	protected static void executeUpdateNoException(String sql, Connection conn) {
		try {
			DatabaseConnection.executeUpdate(sql, conn);
		} catch (SQLException e) {
			// suppress
		}
	}

	/**
	 *
	 */
	protected static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			// DataSource has not yet been created, obtain it now
			configDataSource();
		}
		return DataSourceUtils.getConnection(dataSource);
	}

	/**
	 * Static method that will configure a DataSource based on the Environment setup.
	 */
	private synchronized static void configDataSource() throws SQLException {
		if (dataSource != null) {
			closeConnectionPool(); // DataSource has already been created so remove it
		}
		String url = Environment.getValue(Environment.PROP_DB_URL);
		DataSource targetDataSource = null;
		if (url.startsWith("jdbc:")) {
			try {
				// Use an internal "LocalDataSource" configured from the Environment
				targetDataSource = new LocalDataSource();
			} catch (ClassNotFoundException e) {
				logger.error("Failure while configuring local data source", e);
				throw new SQLException("Failure while configuring local data source: " + e.toString());
			}
		} else {
			try {
				// Use a container DataSource obtained via JNDI lookup
				// TODO: Should try prefix java:comp/env/ if not already part of the JNDI name?
				Context ctx = new InitialContext();
				targetDataSource = (DataSource)ctx.lookup(url);
			} catch (NamingException e) {
				logger.error("Failure while configuring JNDI data source with URL: " + url, e);
				throw new SQLException("Unable to configure JNDI data source with URL " + url + ": " + e.toString());
			}
		}
		dataSource = new LazyConnectionDataSourceProxy(targetDataSource);
		transactionManager = new DataSourceTransactionManager(targetDataSource);
	}

	/**
	 * Test whether the database identified by the given parameters can be connected to.
	 *
	 * @param driver A String indicating the full path for the database driver class.
	 * @param url The JDBC driver URL.
	 * @param user The database user.
	 * @param password The database user password.
	 * @param existence Set to <code>true</code> if a test query should be executed.
	 * @throws SQLException Thrown if any failure occurs while creating a test connection.
	 */
	public static void testDatabase(String driver, String url, String user, String password, boolean existence) throws SQLException, ClassNotFoundException {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getTestConnection(driver, url, user, password);
			if (existence) {
				stmt = conn.createStatement();
				// test to see if database exists
				AnsiQueryHandler queryHandler = new AnsiQueryHandler();
				stmt.executeQuery(queryHandler.existenceValidationQuery());
			}
		} finally {
			DatabaseConnection.closeConnection(conn, stmt);
			// explicitly null the variable to improve garbage collection.
			// with very large loops this can help avoid OOM "GC overhead
			// limit exceeded" errors.
			stmt = null;
			conn = null;
		}
	}

	/**
	 * Return a connection to the database with the specified parameters.
	 * The caller <b>must</b> close this connection when finished!
	 *
	 * @param driver A String indicating the full path for the database driver class.
	 * @param url The JDBC driver URL.
	 * @param user The database user.
	 * @param password The database user password.
	 * @throws SQLException Thrown if any failure occurs while getting the test connection.
	 */
	protected static Connection getTestConnection(String driver, String url, String user, String password) throws SQLException {
		if (url.startsWith("jdbc:")) {
			if (!StringUtils.isBlank(driver)) {
				try {
					// ensure that the Driver class has been loaded
					ResourceUtil.forName(driver);
				} catch (ClassNotFoundException e) {
					throw new SQLException("Unable to instantiate class with name: " + driver);
				}
			}
			return DriverManager.getConnection(url, user, password);
		} else {
			DataSource testDataSource = null;
			try {
				Context ctx = new InitialContext();
				// TODO: Try appending "java:comp/env/" to the JNDI Name if it is missing?
				testDataSource = (DataSource) ctx.lookup(url);
			} catch (NamingException e) {
				logger.error("Failure while configuring JNDI data source with URL: " + url, e);
				throw new SQLException("Unable to configure JNDI data source with URL " + url + ": " + e.toString());
			}
			return testDataSource.getConnection();
		}
	}

	/**
	 * Starts a transaction using the default settings.
	 *
	 * @return TransactionStatus representing the status of the Transaction
	 * @throws SQLException
	 */
	public static TransactionStatus startTransaction() throws SQLException {
		return startTransaction(new DefaultTransactionDefinition());
	}

	/**
	 * Starts a transaction, using the given TransactionDefinition
	 *
	 * @param definition TransactionDefinition
	 * @return TransactionStatus
	 * @throws SQLException
	 */
	protected static TransactionStatus startTransaction(TransactionDefinition definition) throws SQLException {
		if (transactionManager == null || dataSource == null) {
			configDataSource(); // this will create both the DataSource and a TransactionManager
		}
		return transactionManager.getTransaction(definition);
	}

	/**
	 * Perform a rollback, handling rollback exceptions properly.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error
	 * @throws TransactionException in case of a rollback error
	 */
	protected static void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException {
		logger.debug("Initiating transaction rollback on application exception", ex);
		if (status == null) {
			logger.info("TransactionStatus is null, unable to rollback");
			return;
		}
		try {
			transactionManager.rollback(status);
		} catch (TransactionSystemException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			ex2.initApplicationException(ex);
			throw ex2;
		} catch (RuntimeException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw ex2;
		} catch (Error err) {
			logger.error("Application exception overridden by rollback error", ex);
			throw err;
		}
	}

	/**
	 * Commit the current transaction.
	 * Note if the transaction has been programmatically marked for rollback then
	 * a rollback will occur instead.
	 *
	 * @param status TransactionStatus representing the status of the transaction
	 */
	protected static void commit(TransactionStatus status) {
		if (status == null) {
			logger.info("TransactionStatus is null, unable to commit");
			return;
		}
		transactionManager.commit(status);
	}
}