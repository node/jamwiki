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

import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Category;
import org.jamwiki.model.Namespace;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used for display a list of all categories that are currently in use for a
 * virtual wiki.
 */
public class CategoryServlet extends JAMWikiServlet {

	/** Logger for this class and subclasses. */
	private static final WikiLogger logger = WikiLogger.getLogger(CategoryServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_CATEGORIES = "categories.jsp";

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		this.viewCategories(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private void viewCategories(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		Pagination pagination = ServletUtil.loadPagination(request, next);
		List<Category> categoryObjects = WikiBase.getDataHandler().getAllCategories(virtualWiki, pagination);
		LinkedHashMap<String, String> categories = new LinkedHashMap<String, String>();
		for (Category category : categoryObjects) {
			String key = category.getName();
			String value = key.substring(Namespace.namespace(Namespace.CATEGORY_ID).getLabel(virtualWiki).length() + Namespace.SEPARATOR.length());
			categories.put(key, value);
		}
		next.addObject("categoryCount", categories.size());
		next.addObject("categories", categories);
		if (categories.isEmpty()) {
			pageInfo.addMessage(new WikiMessage("allcategories.message.none"));
		}
		pageInfo.setPageTitle(new WikiMessage("allcategories.title"));
		pageInfo.setContentJsp(JSP_CATEGORIES);
		pageInfo.setSpecial(true);
	}
}