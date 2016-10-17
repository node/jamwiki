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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a wrapper around the <a href="http://www.slf4j.org">SFL4J</a>
 * logging facade framework. A logging framework can be chosen at runtime.
 */
public class WikiLogger {

	private final Logger logger;

	/** Logging framework configuration file path. */
	public final static String LOGGING_CONFIGURATION_FILE_PATH = "/WEB-INF/classes/logback.xml";

	/**
	 *
	 */
	private WikiLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Retrieve a named <code>WikiLogger</code> object.
	 *
	 * @param name The name of the log object to retrieve or create.
	 * @return A logger instance for the given name.
	 */
	public static WikiLogger getLogger(String name) {
		Logger logger = LoggerFactory.getLogger(name);
		return new WikiLogger(logger);
	}

	/**
	 * Log a message at the {@link org.slf4j.Logger#DEBUG} level,
	 * provided that the current log level is {@link org.slf4j.Logger#DEBUG}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void debug(String msg) {
		this.logger.debug(msg);
	}

	/**
	 * Log a message and an exception at the {@link org.slf4j.Logger#DEBUG}
	 * level, provided that the current log level is {@link org.slf4j.Logger#DEBUG}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void debug(String msg, Throwable thrown) {
		this.logger.debug(msg, thrown);
	}

	/**
	 * Log a message at the {@link org.slf4j.Logger#ERROR} level,
	 * provided that the current log level is {@link org.slf4j.Logger#ERROR}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void error(String msg) {
		this.logger.error(msg);
	}

	/**
	 * Log a message and an exception at the {@link org.slf4j.Logger#ERROR}
	 * level, provided that the current log level is {@link org.slf4j.Logger#ERROR}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void error(String msg, Throwable thrown) {
		this.logger.error(msg, thrown);
	}

	/**
	 * Log a message at the {@link org.slf4j.Logger#INFO} level,
	 * provided that the current log level is {@link org.slf4j.Logger#INFO}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void info(String msg) {
		this.logger.info(msg);
	}

	/**
	 * Log a message and an exception at the {@link org.slf4j.Logger#INFO}
	 * level, provided that the current log level is {@link org.slf4j.Logger#INFO}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void info(String msg, Throwable thrown) {
		this.logger.info(msg, thrown);
	}

	/**
	 * Return <code>true</code> if a log message of level {@link org.slf4j.Logger#DEBUG}
	 * can be logged.
	 */
	public boolean isDebugEnabled() {
		return this.logger.isDebugEnabled();
	}

	/**
	 * Return <code>true</code> if a log message of level {@link org.slf4j.Logger#INFO}
	 * can be logged.
	 */
	public boolean isInfoEnabled() {
		return this.logger.isInfoEnabled();
	}

	/**
	 * Return <code>true</code> if a log message of level {@link org.slf4j.Logger#TRACE}
	 * can be logged.
	 */
	public boolean isTraceEnabled() {
		return this.logger.isTraceEnabled();
	}

	/**
	 * Return <code>true</code> if a log message of level {@link org.slf4j.Logger#WARN}
	 * can be logged.
	 */
	public boolean isWarnEnabled() {
		return this.logger.isWarnEnabled();
	}

	/**
	 * Log a message at the {@link org.slf4j.Logger#TRACE} level,
	 * provided that the current log level is {@link org.slf4j.Logger#TRACE}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void trace(String msg) {
		this.logger.trace(msg);
	}

	/**
	 * Log a message and an exception at the {@link org.slf4j.Logger#TRACE}
	 * level, provided that the current log level is {@link org.slf4j.Logger#TRACE}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void trace(String msg, Throwable thrown) {
		this.logger.trace(msg, thrown);
	}

	/**
	 * Log a message at the {@link org.slf4j.Logger#WARN} level,
	 * provided that the current log level is {@link org.slf4j.Logger#WARN}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 */
	public void warn(String msg) {
		this.logger.warn(msg);
	}

	/**
	 * Log a message and an exception at the {@link org.slf4j.Logger#WARN}
	 * level, provided that the current log level is {@link org.slf4j.Logger#WARN}
	 * or greater.
	 *
	 * @param msg The message to be written to the log.
	 * @param thrown An exception to be written to the log.
	 */
	public void warn(String msg, Throwable thrown) {
		this.logger.warn(msg, thrown);
	}
}
