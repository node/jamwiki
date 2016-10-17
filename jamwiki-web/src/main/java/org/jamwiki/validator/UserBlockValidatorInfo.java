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

import org.jamwiki.model.UserBlock;

/**
 * Determine whether a request should be blocked based on the wiki blocklist.
 */
public class UserBlockValidatorInfo extends RequestValidatorInfo {

	/** The {@link org.jamwiki.model.UserBlock} object associated with this validation. */
	private final UserBlock userBlock;

	/**
	 * Standard constructor.
	 *
	 * @param userBlock If the UserBlock object is <code>null</code> then the
	 *  request that generated this validation object will be considered valid,
	 *  otherwise it will be considered invalid.
	 */
	public UserBlockValidatorInfo(UserBlock userBlock) {
		super(userBlock == null);
		this.userBlock = userBlock;
	}

	/**
	 * Return the {@link org.jamwiki.model.UserBlock} object associated with
	 * this validation.
	 *
	 * @return Returns the {@link org.jamwiki.model.UserBlock} object
	 *  associated with this validation.
	 */
	public UserBlock getUserBlock() {
		return this.userBlock;
	}
}
