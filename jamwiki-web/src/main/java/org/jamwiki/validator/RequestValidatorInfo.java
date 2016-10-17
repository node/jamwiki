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
package org.jamwiki.validator;

/**
 * Utility class used to return information about a validation request.
 */
public class RequestValidatorInfo {

	/**
	 * Flag indicating whether the request that led to the creation of this
	 * object should be considered valid or not.
	 */
	private final boolean valid;

	/**
	 * Standard constructor.
	 *
	 * @param valid <code>true</code> if the request should be considered
	 *  acceptable, or <code>false</code> if the request should be considered
	 *  invalid.
	 */
	public RequestValidatorInfo(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Return a boolean value indicating whether or not the request that led
	 * to the creation of this object should be considered valid or not.
	 *
	 * @return Returns <code>true</code> if the request should be considered
	 *  acceptable, or <code>false</code> if the request should be considered
	 *  invalid.
	 */
	public boolean isValid() {
		return this.valid;
	}
}
