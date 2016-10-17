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

import java.sql.SQLException;

/**
 * Custom exception class for JAMWiki data errors.  This class will typically
 * wrap <code>SQLException</code> or other exception types.
 */
public class DataAccessException extends Exception {

	/** SQLException can bury useful info in getNextException(), so add special handling. */
	private SQLException nextException;

	/**
	 * Constructor for an exception containing a message.
	 *
	 * @param message The message information for the exception.
	 */
	public DataAccessException(String message) {
		super(message);
		this.init(null);
	}

	/**
	 * Constructor for an exception containing a message and wrapping another
	 * exception.
	 *
	 * @param message The message information for the exception.
	 * @param t The exception that is the cause of this exception.
	 */
	public DataAccessException(String message, Throwable t) {
		super(message, t);
		this.init(t);
	}

	/**
	 * Constructor for an exception that wraps another exception.
	 *
	 * @param t The exception that is the cause of this exception.
	 */
	public DataAccessException(Throwable t) {
		super(t);
		this.init(t);
	}

	/**
	 *
	 */
	private void init(Throwable t) {
		if (t instanceof SQLException) {
			this.nextException = ((SQLException)t).getNextException();
		}
	}

	/**
	 * Override the parent method to add special handling for SQLException.getNextException().
	 */
	public String getMessage() {
		String message = super.getMessage();
		if (this.nextException != null) {
			message = (message == null) ? "" : message;
			message += " [ " + this.nextException.toString() + " ]";
		}
		return message;
	}
}
