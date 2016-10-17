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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.UserBlock;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to provide capability for blocking a user by login or IP address.
 */
public class BlockServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(BlockServlet.class.getName());
	/** The name of the JSP file used to render the block page servlet output. */
	protected static final String JSP_ADMIN_BLOCK = "admin-block.jsp";
	/** The name of the JSP file used to render the unblock page servlet output. */
	protected static final String JSP_ADMIN_UNBLOCK = "admin-unblock.jsp";

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (ServletUtil.isTopic(request, "Special:Block")) {
			if (!StringUtils.isBlank(request.getParameter("block"))) {
				block(request, next, pageInfo);
			} else {
				viewBlock(request, next, pageInfo);
			}
		} else {
			if (!StringUtils.isBlank(request.getParameter("unblock"))) {
				unblock(request, next, pageInfo);
			} else {
				viewUnblock(request, next, pageInfo);
			}
		}
		return next;
	}

	/**
	 *
	 */
	private void block(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException, WikiException {
		UserBlock userBlock = this.initializeBlock(request, next, pageInfo);
		if (userBlock != null) {
			WikiBase.getDataHandler().writeUserBlock(userBlock);
			String dateString = "infinite";
			if (userBlock.getBlockEndDate() != null) {
				// FIXME - handle date formatting in a way that is locale-specific
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("d MMM yyyy hh:mm aaa");
				dateString = sdf.format(userBlock.getBlockEndDate().getTime());
			}
			String username = (userBlock.getWikiUserId() == null) ? userBlock.getIpAddress() : WikiBase.getDataHandler().lookupWikiUser(userBlock.getWikiUserId()).getUsername();
			pageInfo.addMessage(new WikiMessage("block.message.success", username, dateString));
		} else {
			next.addObject("durationNumber", request.getParameter("durationNumber"));
			next.addObject("durationUnit", request.getParameter("durationUnit"));
			next.addObject("reason", request.getParameter("reason"));
		}
		this.viewBlock(request, next, pageInfo);
	}

	/**
	 * Initialize a UserBlock object from the form parameters submitted on the
	 * block page and return it, or return <code>null</code> if there are errors
	 * encountered while building the block object.
	 */
	private UserBlock initializeBlock(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException {
		String username = StringUtils.trim(request.getParameter("user"));
		String ipAddress = Utilities.isIpAddress(username) ? username : null;
		WikiUser wikiUser = this.initializeWikiUser(request, pageInfo);
		int durationUnit = NumberUtils.toInt(request.getParameter("durationUnit"), -1);
		int durationNumber = NumberUtils.toInt(request.getParameter("durationNumber"), -1);
		Timestamp blockEndDate = null;
		if (durationUnit > 0 && durationNumber < 1) {
			pageInfo.addError(new WikiMessage("block.error.duration", request.getParameter("durationNumber")));
		} else if (durationUnit > 0) {
			Calendar blockEndCal = new GregorianCalendar();
			blockEndCal.add(durationUnit, durationNumber);
			blockEndDate = new Timestamp(blockEndCal.getTimeInMillis());
		}
		Integer wikiUserId = (wikiUser != null) ? wikiUser.getUserId() : null;
		if ((wikiUserId != null || ipAddress != null) && WikiBase.getDataHandler().lookupUserBlock(wikiUserId, ipAddress) != null) {
			// user is already blocked
			pageInfo.addError(new WikiMessage("block.error.alreadyblocked", username));
		}
		UserBlock userBlock = null;
		if (pageInfo.getErrors().isEmpty()) {
			int blockedByUserId = ServletUtil.currentWikiUser().getUserId();
			if (blockedByUserId < 1) {
				logger.warn("This wiki seems to have been configured to allow anonymous users to access the Special:Block page.  The JAMWiki software requires that only logged-in users be given the ability to apply user blocks.");
				pageInfo.addError(new WikiMessage("error.unknown", "Invalid configuration"));
				return null;
			}
			userBlock = new UserBlock(wikiUserId, ipAddress, blockEndDate, blockedByUserId);
			userBlock.setBlockReason(request.getParameter("reason"));
		}
		return userBlock;
	}

	/**
	 * Initialize a UserBlock object from the form parameters submitted on the
	 * unblock page and return it, or return <code>null</code> if there are errors
	 * encountered while building the block object.
	 */
	private UserBlock initializeUnblock(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException {
		String username = StringUtils.trim(request.getParameter("user"));
		String ipAddress = Utilities.isIpAddress(username) ? username : null;
		WikiUser wikiUser = this.initializeWikiUser(request, pageInfo);
		Integer wikiUserId = (wikiUser != null) ? wikiUser.getUserId() : null;
		UserBlock userBlock = null;
		if (wikiUserId != null || ipAddress != null) {
			userBlock = WikiBase.getDataHandler().lookupUserBlock(wikiUserId, ipAddress);
			if (userBlock == null) {
				// user is not blocked
				pageInfo.addError(new WikiMessage("unblock.error.notblocked", username));
			}
		}
		if (pageInfo.getErrors().isEmpty()) {
			int unblockedByUserId = ServletUtil.currentWikiUser().getUserId();
			if (unblockedByUserId < 1) {
				logger.warn("This wiki seems to have been configured to allow anonymous users to access the Special:Block page.  The JAMWiki software requires that only logged-in users be given the ability to apply user blocks.");
				pageInfo.addError(new WikiMessage("error.unknown", "Invalid configuration"));
				return null;
			}
			userBlock.setUnblockedByUserId(unblockedByUserId);
			userBlock.setUnblockReason(request.getParameter("reason"));
			userBlock.setUnblockDate(new Timestamp(System.currentTimeMillis()));
		}
		return userBlock;
	}

	/**
	 * Shared code for the block & unblock pages used to retrieve a WikiUser from the
	 * username submitted in the form.  This method returns <code>null</code> if no
	 * matching user is found and requires that the two pages use the same form field
	 * names.
	 */
	private WikiUser initializeWikiUser(HttpServletRequest request, WikiPageInfo pageInfo) throws DataAccessException {
		String username = StringUtils.trim(request.getParameter("user"));
		WikiUser wikiUser = null;
		if (StringUtils.isBlank(username)) {
			pageInfo.addError(new WikiMessage("block.error.user"));
		} else if (!Utilities.isIpAddress(username)) {
			wikiUser = WikiBase.getDataHandler().lookupWikiUser(username);
			if (wikiUser == null) {
				pageInfo.addError(new WikiMessage("block.error.invaliduser", username));
			}
		}
		return wikiUser;
	}

	/**
	 *
	 */
	private void unblock(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException, WikiException {
		UserBlock userBlock = this.initializeUnblock(request, next, pageInfo);
		if (userBlock != null) {
			WikiBase.getDataHandler().writeUserBlock(userBlock);
			String username = (userBlock.getWikiUserId() == null) ? userBlock.getIpAddress() : WikiBase.getDataHandler().lookupWikiUser(userBlock.getWikiUserId()).getUsername();
			pageInfo.addMessage(new WikiMessage("unblock.message.success", username));
		} else {
			next.addObject("reason", request.getParameter("reason"));
		}
		this.viewUnblock(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void viewBlock(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws DataAccessException {
		next.addObject("user", request.getParameter("user"));
		WikiUser user = this.initializeWikiUser(request, pageInfo);
		if (user != null) {
			next.addObject("lastLoginIpAddress", user.getLastLoginIpAddress());
		}
		pageInfo.setContentJsp(JSP_ADMIN_BLOCK);
		pageInfo.setPageTitle(new WikiMessage("block.title"));
		pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private void viewUnblock(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		next.addObject("user", request.getParameter("user"));
		pageInfo.setContentJsp(JSP_ADMIN_UNBLOCK);
		pageInfo.setPageTitle(new WikiMessage("unblock.title"));
		pageInfo.setSpecial(true);
	}
}
