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
package org.jamwiki.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interface for all JAMWiki servlets.
 */
interface JAMWikiController {

	/**
	 * Method that will be called by all implementing servlets to handle the
	 * servlet request.
	 *
	 * @param request The servlet request object.
	 * @param response The servlet response object.
	 * @param next A ModelAndView object that has been initialized to the view
	 *  specified by the <code>displayJSP</code> member variable.
	 * @param pageInfo A WikiPageInfo object that will hold output parameters
	 *  to be passed to the output JSP.
	 * @return A ModelAndView object corresponding to the information to be
	 *  rendered, or <code>null</code> if the method directly handles its own
	 *  output, for example by writing directly to the output response.
	 */
	ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception;
}
