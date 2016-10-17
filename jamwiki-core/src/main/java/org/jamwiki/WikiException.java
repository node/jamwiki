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

/**
 * Custom exception class for JAMWiki errors.  This exception type allows an error
 * message key to be passed back to the front end for display to the user.
 */
public class WikiException extends Exception {

	private final WikiMessage wikiMessage;

	/**
	 * Constructor for a WikiException containing a Wiki message value.
	 *
	 * @param wikiMessage The message information for the exception.
	 */
	public WikiException(WikiMessage wikiMessage) {
		super();
		this.wikiMessage = wikiMessage;
	}

	/**
	 * Constructor for a WikiException containing a Wiki message value.
	 *
	 * @param wikiMessage The message information for the exception.
	 * @param t The exception that is the cause of this WikiException.
	 */
	public WikiException(WikiMessage wikiMessage, Throwable t) {
		super(t);
		this.wikiMessage = wikiMessage;
	}

	/**
	 * Return the WikiMessage object associated with this exception.
	 *
	 * @return The WikiMessage object assocated with this exception.
	 */
	public WikiMessage getWikiMessage() {
		return this.wikiMessage;
	}
}
