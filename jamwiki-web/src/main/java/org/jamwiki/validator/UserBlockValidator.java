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

import javax.servlet.http.HttpServletRequest;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.model.UserBlock;
import org.jamwiki.model.WikiUser;
import org.jamwiki.servlets.ServletUtil;
import org.jamwiki.utils.WikiLogger;

/**
 * Validator used when determining if a request represents a user or IP
 * address block using the wiki block list.
 */
public class UserBlockValidator implements RequestValidator {

	private static final WikiLogger logger = WikiLogger.getLogger(UserBlockValidator.class.getName());

	/**
	 * Determine if the specified request represents a user or IP
	 * address block using the wiki block list.
	 *
	 * @param request The current servlet request.
	 * @return Returns a non-null {@link UserBlockValidatorInfo} object that
	 * encapsulates the validation result.
	 */
	public UserBlockValidatorInfo validate(HttpServletRequest request) {
		WikiUser wikiUser = ServletUtil.currentWikiUser();
		Integer wikiUserId = (wikiUser.getUserId() > 0) ? wikiUser.getUserId() : null;
		UserBlock userBlock = null;
		try {
			userBlock = WikiBase.getDataHandler().lookupUserBlock(wikiUserId, request.getRemoteAddr());
		} catch (DataAccessException e) {
			logger.error("Data access exception while retrieving user block status, e");
		}
		return new UserBlockValidatorInfo(userBlock);
	}
}
