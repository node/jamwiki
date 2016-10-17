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

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Interwiki;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class AdminVirtualWikiServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(AdminVirtualWikiServlet.class.getName());
	/** The name of the JSP file used to render the servlet output when searching. */
	protected static final String JSP_ADMIN_VIRTUAL_WIKI = "admin-virtual-wiki.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String function = request.getParameter("function");
		next.addObject("function", function);
		if (StringUtils.isBlank(function)) {
			view(request, next, pageInfo);
		} else if (function.equals("addnamespace")) {
			addNamespace(request, next, pageInfo);
		} else if (function.equals("addInterwiki")) {
			addInterwiki(request, next, pageInfo);
		} else if (function.equals("updateInterwiki")) {
			updateInterwiki(request, next, pageInfo);
		} else if (function.equals("namespaces")) {
			namespaces(request, next, pageInfo);
		} else if (function.equals("search")) {
			search(request, next, pageInfo);
		} else if (function.equals("virtualwiki")) {
			virtualWiki(request, next, pageInfo);
		} else if (function.equals("commonvwiki")) {
			commonVirtualWiki(request, next, pageInfo);
		} else if (function.equals("commoniwiki")) {
			commonInterwiki(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void addInterwiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String interwikiPrefix = request.getParameter("interwikiPrefix");
		String interwikiPattern = request.getParameter("interwikiPattern");
		String interwikiDisplay = request.getParameter("interwikiDisplay");
		try {
			Interwiki interwiki = new Interwiki(interwikiPrefix, interwikiPattern, interwikiDisplay);
			// write to the database.  this will also perform required validation.
			WikiBase.getDataHandler().writeInterwiki(interwiki);
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		} catch (DataAccessException e) {
			logger.error("Failure while adding interwiki record", e);
			pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
		}
		if (!pageInfo.getErrors().isEmpty()) {
			next.addObject("interwikiPrefix", interwikiPrefix);
			next.addObject("interwikiPattern", interwikiPattern);
			next.addObject("interwikiDisplay", interwikiDisplay);
		} else {
			pageInfo.addMessage(new WikiMessage("admin.vwiki.message.interwiki.addsuccess", interwikiPrefix));
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void addNamespace(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String mainNamespace = request.getParameter("mainNamespace");
		String commentsNamespace = request.getParameter("commentsNamespace");
		// validate that the namespace values are acceptable
		try {
			WikiUtil.validateNamespaceName(mainNamespace);
			if (mainNamespace.equals(commentsNamespace)) {
				throw new WikiException(new WikiMessage("admin.vwiki.error.namespace.unique", mainNamespace));
			}
			WikiUtil.validateNamespaceName(commentsNamespace);
			// write namespaces to the database
			Namespace mainNamespaceObj = new Namespace(null, mainNamespace);
			WikiBase.getDataHandler().writeNamespace(mainNamespaceObj);
			if (!StringUtils.isBlank(commentsNamespace)) {
				Namespace commentsNamespaceObj = new Namespace(null, commentsNamespace);
				commentsNamespaceObj.setMainNamespaceId(mainNamespaceObj.getId());
				WikiBase.getDataHandler().writeNamespace(commentsNamespaceObj);
			}
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		} catch (DataAccessException e) {
			logger.error("Failure while creating new namespace", e);
			pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
		}
		if (!pageInfo.getErrors().isEmpty()) {
			next.addObject("mainNamespace", mainNamespace);
			next.addObject("commentsNamespace", commentsNamespace);
		} else {
			pageInfo.addMessage(new WikiMessage("admin.vwiki.message.addnamespacesuccess", mainNamespace));
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void commonInterwiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		Environment.setBooleanValue(Environment.PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE, !StringUtils.isBlank(request.getParameter(Environment.PROP_PARSER_DISPLAY_INTERWIKI_LINKS_INLINE)));
		try {
			Environment.saveConfiguration();
			pageInfo.addMessage(new WikiMessage("admin.vwiki.message.interwiki.common"));
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		}
		view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void commonVirtualWiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		String defaultVirtualWiki = request.getParameter("defaultVirtualWiki");
		if (!StringUtils.isBlank(defaultVirtualWiki)) {
			Environment.setValue(Environment.PROP_VIRTUAL_WIKI_DEFAULT, defaultVirtualWiki);
		}
		Environment.setBooleanValue(Environment.PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS, !StringUtils.isBlank(request.getParameter(Environment.PROP_PARSER_DISPLAY_SPECIAL_PAGE_VIRTUAL_WIKI_LINKS)));
		Environment.setBooleanValue(Environment.PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE, !StringUtils.isBlank(request.getParameter(Environment.PROP_PARSER_DISPLAY_VIRTUALWIKI_LINKS_INLINE)));
		try {
			Environment.saveConfiguration();
			pageInfo.addMessage(new WikiMessage("admin.vwiki.message.commonupdated"));
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		}
		view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void namespaces(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = request.getParameter("selected");
		List<Namespace> namespaces = new ArrayList<Namespace>();
		String[] namespaceIds = request.getParameterValues("namespace_id");
		String defaultLabel;
		String translatedLabel;
		try {
			for (String namespaceId : namespaceIds) {
				defaultLabel = request.getParameter(namespaceId + "_label");
				Namespace namespace = WikiBase.getDataHandler().lookupNamespace(null, defaultLabel);
				translatedLabel = request.getParameter(namespaceId + "_vwiki");
				if (StringUtils.equals(defaultLabel, translatedLabel) || StringUtils.isBlank(translatedLabel)) {
					namespace.getNamespaceTranslations().remove(virtualWiki);
				} else {
					namespace.getNamespaceTranslations().put(virtualWiki, translatedLabel);
				}
				namespaces.add(namespace);
			}
			WikiBase.getDataHandler().writeNamespaceTranslations(namespaces, virtualWiki);
			pageInfo.addMessage(new WikiMessage("admin.vwiki.message.namespacesuccess", virtualWiki));
		} catch (DataAccessException e) {
			logger.error("Failure while retrieving adding/updating namespace translations", e);
			pageInfo.addError(new WikiMessage("admin.vwiki.error.addnamespacefail", e.getMessage()));
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void search(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void updateInterwiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String[] interwikiPrefixes = request.getParameterValues("interwikiPrefix");
		if (interwikiPrefixes != null) {
			for (String interwikiPrefix : interwikiPrefixes) {
				String interwikiPattern = request.getParameter("pattern-" + interwikiPrefix);
				String interwikiDisplay = request.getParameter("display-" + interwikiPrefix);
				try {
					Interwiki interwiki = new Interwiki(interwikiPrefix, interwikiPattern, interwikiDisplay);
					// write to the database.  this will also perform required validation.
					if (!StringUtils.equals(request.getParameter("delete-" + interwikiPrefix), "true")) {
						WikiBase.getDataHandler().writeInterwiki(interwiki);
					} else {
						WikiBase.getDataHandler().deleteInterwiki(interwiki);
					}
				} catch (WikiException e) {
					pageInfo.addError(e.getWikiMessage());
				} catch (DataAccessException e) {
					logger.error("Failure while adding interwiki record", e);
					pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
				}
			}
		}
		if (pageInfo.getErrors().isEmpty()) {
			pageInfo.addMessage(new WikiMessage("admin.vwiki.message.interwiki.updatesuccess"));
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		// find the current virtual wiki
		String selected = request.getParameter("selected");
		if (!StringUtils.isBlank(selected)) {
			VirtualWiki virtualWiki = null;
			try {
				virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(selected);
			} catch (DataAccessException e) {
				logger.error("Failure while retrieving virtual wiki", e);
				pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
			}
			if (virtualWiki != null) {
				next.addObject("selected", virtualWiki);
			}
		}
		next.addObject("defaultVirtualWiki", VirtualWiki.defaultVirtualWiki());
		next.addObject("props", Environment.getInstance());
		// initialize page defaults
		pageInfo.setAdmin(true);
		try {
			List<VirtualWiki> virtualWikiList = WikiBase.getDataHandler().getVirtualWikiList();
			next.addObject("wikis", virtualWikiList);
			List<Namespace> namespaces = WikiBase.getDataHandler().lookupNamespaces();
			next.addObject("namespaces", namespaces);
			List<Interwiki> interwikis = WikiBase.getDataHandler().lookupInterwikis();
			next.addObject("interwikis", interwikis);
		} catch (DataAccessException e) {
			logger.error("Failure while retrieving database records", e);
			pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
		}
		pageInfo.setContentJsp(JSP_ADMIN_VIRTUAL_WIKI);
		pageInfo.setPageTitle(new WikiMessage("admin.vwiki.title"));
	}

	/**
	 *
	 */
	private void virtualWiki(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		WikiUser user = ServletUtil.currentWikiUser();
		VirtualWiki virtualWiki = new VirtualWiki(request.getParameter("name"));
		if (!StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
			virtualWiki.setVirtualWikiId(Integer.valueOf(request.getParameter("virtualWikiId")));
		}
		if (StringUtils.isBlank(request.getParameter("defaultRootTopicName"))) {
			virtualWiki.setRootTopicName(WikiUtil.getParameterFromRequest(request, "rootTopicName", true));
		}
		if (StringUtils.isBlank(request.getParameter("defaultVirtualWikiLogoImageUrl"))) {
			virtualWiki.setLogoImageUrl(request.getParameter("virtualWikiLogoImageUrl"));
		}
		if (StringUtils.isBlank(request.getParameter("defaultVirtualWikiMetaDescription"))) {
			virtualWiki.setMetaDescription(request.getParameter("virtualWikiMetaDescription"));
		}
		if (StringUtils.isBlank(request.getParameter("defaultVirtualWikiSiteName"))) {
			virtualWiki.setSiteName(request.getParameter("virtualWikiSiteName"));
		}
		try {
			WikiBase.getDataHandler().writeVirtualWiki(virtualWiki);
			if (StringUtils.isBlank(request.getParameter("virtualWikiId"))) {
				// add
				WikiBase.getDataHandler().setupSpecialPages(request.getLocale(), user, virtualWiki);
				pageInfo.addMessage(new WikiMessage("admin.message.virtualwikiadded", virtualWiki.getName()));
			} else {
				// update
				pageInfo.addMessage(new WikiMessage("admin.message.virtualwikiupdated", virtualWiki.getName()));
			}
		} catch (DataAccessException e) {
			logger.error("Failure while adding virtual wiki", e);
			pageInfo.addError(new WikiMessage("admin.message.virtualwikifail", e.getMessage()));
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		}
		next.addObject("selected", virtualWiki);
		view(request, next, pageInfo);
	}
}
