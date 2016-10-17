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

/**
 * Interface used when determining whether or not to allow or disallow
 * a request.
 */
public interface RequestValidator {

	/**
	 * Determine if the specified request should be considered valid or not.
	 * This method will attempt to perform whatever validation is required,
	 * and will then return a non-null {@link RequestValidatorInfo} object
	 * that encapsulates the validation result.
	 *
	 * @param request The current servlet request.
	 * @return Returns a non-null {@link RequestValidatorInfo} object that
	 * encapsulates the validation result.
	 */
	public <T extends RequestValidatorInfo> T validate(HttpServletRequest request);
}
