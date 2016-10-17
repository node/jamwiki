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
import org.springframework.security.core.context.SecurityContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.WikiVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.UpgradeUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to automatically handle JAMWiki upgrades, including configuration and
 * data modifications.
 *
 * @see org.jamwiki.servlets.SetupServlet
 */
public class UpgradeServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(UpgradeServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_UPGRADE = "upgrade.jsp";

	/**
	 * This servlet requires slightly different initialization parameters from most
	 * servlets.
	 */
	public UpgradeServlet() {
		this.layout = false;
		this.displayJSP = "upgrade";
	}

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (!WikiUtil.isUpgrade()) {
			throw new WikiException(new WikiMessage("upgrade.error.notrequired"));
		}
		String function = request.getParameter("function");
		pageInfo.setPageTitle(new WikiMessage("upgrade.title", Environment.getValue(Environment.PROP_BASE_WIKI_VERSION), WikiVersion.CURRENT_WIKI_VERSION));
		boolean performUpgrade = (!StringUtils.isBlank(function) && function.equals("upgrade"));
		try {
			UpgradeUtil upgradeUtil = new UpgradeUtil(pageInfo.getMessages(), performUpgrade);
			upgradeUtil.upgrade(request.getLocale(), ServletUtil.getIpAddress(request));
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		}
		if (performUpgrade) {
			upgrade(request, next, pageInfo);
		} else {
			next.addObject("viewOnly", "true");
			view(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void upgrade(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		if (pageInfo.getErrors().isEmpty()) {
			// success
			WikiMessage wm = new WikiMessage("upgrade.caption.upgradecomplete");
			VirtualWiki virtualWiki = VirtualWiki.defaultVirtualWiki();
			WikiLink wikiLink = new WikiLink(request.getContextPath(), virtualWiki.getName(), virtualWiki.getRootTopicName());
			try {
				String htmlLink = LinkUtil.buildInternalLinkHtml(wikiLink, virtualWiki.getRootTopicName(), null, null, true);
				// do not escape the HTML link
				wm.setParamsWithoutEscaping(new String[]{htmlLink});
			} catch (DataAccessException e) {
				// building a link to the start page shouldn't fail, but if it does display a message
				wm = new WikiMessage("upgrade.error.nonfatal", e.toString());
				logger.warn("Upgrade complete, but unable to build redirect link to the start page.", e);
			}
			next.addObject("successMessage", wm);
			// force logout to ensure current user will be re-validated.  this is
			// necessary because the upgrade may have changed underlying data structures.
			SecurityContextHolder.clearContext();
		} else {
			// failure
			pageInfo.addError(new WikiMessage("upgrade.caption.upgradefailed"));
			next.addObject("failure", "true");
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		pageInfo.getMessages().add(new WikiMessage("upgrade.caption.releasenotes"));
		pageInfo.getMessages().add(new WikiMessage("upgrade.caption.manual"));
		pageInfo.setContentJsp(JSP_UPGRADE);
		pageInfo.setSpecial(true);
	}
}
