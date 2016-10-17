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

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.authentication.JAMWikiAuthenticationConfiguration;
import org.jamwiki.authentication.WikiUserDetailsImpl;
import org.jamwiki.model.Role;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.jamwiki.validator.ReCaptchaUtil;
import org.jamwiki.web.utils.UserPreferencesUtil;
import org.jamwiki.web.utils.UserPreferencesUtil.UserPreferenceItem;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/**
 * Used to process new user account setup.
 */
public class RegisterServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(RegisterServlet.class.getName());
	/** The name of the JSP file used to render the servlet output when searching. */
	protected static final String JSP_REGISTER = "register.jsp";

	/**
	 * Override defaults to enable user blocking.
	 */
	public RegisterServlet() {
		this.blockable = true;
	}

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (request.getParameter("function") == null) {
			view(request, next, pageInfo);
		} else {
			register(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void loadDefaults(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, WikiUser user) throws Exception {
		UserPreferencesUtil userPreferences = new UserPreferencesUtil(user);
		if (StringUtils.isBlank(user.getDefaultLocale())) {
			user.setDefaultLocale(userPreferences.getDefaultLocale());
		}
		next.addObject("newuser", user);
		// Note: adding the signature preview this way is a workaround. Better would be
		// if the preview can be generated in UserPreferencesUtil inner class
		// UserPreferenceItem directly...
		userPreferences.setSignaturePreview(this.signaturePreview(request, pageInfo, user));
		next.addObject("userPreferences", userPreferences);
		next.addObject("recaptchaEnabled", ReCaptchaUtil.isRegistrationEnabled());
		pageInfo.setSpecial(true);
		pageInfo.setContentJsp(JSP_REGISTER);
		pageInfo.setPageTitle(new WikiMessage("register.title"));
	}

	/**
	 *
	 */
	private void login(HttpServletRequest request, String username, String password) {
		WikiUserDetailsImpl userDetails = new WikiUserDetailsImpl(username, password, true, true, true, true, JAMWikiAuthenticationConfiguration.getDefaultGroupRoles());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 *
	 */
	private void register(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWikiName = pageInfo.getVirtualWikiName();
		WikiUser user = this.setWikiUser(request);
		boolean isUpdate = (user.getUserId() != -1);
		next.addObject("newuser", user);
		this.validate(request, pageInfo, user);
		if (!pageInfo.getErrors().isEmpty()) {
			String oldPassword = request.getParameter("oldPassword");
			String newPassword = request.getParameter("newPassword");
			String confirmPassword = request.getParameter("confirmPassword");
			if (oldPassword != null) {
				next.addObject("oldPassword", oldPassword);
			}
			if (newPassword != null) {
				next.addObject("newPassword", newPassword);
			}
			if (confirmPassword != null) {
				next.addObject("confirmPassword", confirmPassword);
			}
			this.loadDefaults(request, next, pageInfo, user);
		} else {
			String username = request.getParameter("login");
			String newPassword = request.getParameter("newPassword");
			String encryptedPassword = null;
			if (!StringUtils.isBlank(newPassword)) {
				encryptedPassword = Encryption.encrypt(newPassword);
			}
			WikiBase.getDataHandler().writeWikiUser(user, username, encryptedPassword);
			if (!StringUtils.isBlank(newPassword)) {
				// login the user
				this.login(request, user.getUsername(), newPassword);
			}
			// update the locale key since the user may have changed default locale
			if (!StringUtils.isBlank(user.getDefaultLocale())) {
				Locale locale = LocaleUtils.toLocale(user.getDefaultLocale());
				request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
			}
			if (isUpdate) {
				pageInfo.addMessage(new WikiMessage("register.caption.updatesuccess"));
				this.view(request, next, pageInfo);
			} else {
				VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
				String topic = virtualWiki.getRootTopicName();
				ServletUtil.redirect(next, virtualWikiName, topic);
			}
		}
	}

	/**
	 *
	 */
	private WikiUser setWikiUser(HttpServletRequest request) throws Exception {
		String username = request.getParameter("login");
		WikiUser user = new WikiUser(username);
		String userIdString = request.getParameter("userId");
		if (!StringUtils.isBlank(userIdString)) {
			int userId = Integer.valueOf(userIdString);
			if (userId > 0) {
				user = WikiBase.getDataHandler().lookupWikiUser(userId);
			}
		}
		user.setEmail(request.getParameter("email"));
		// FIXME - need to distinguish between add & update
		user.setCreateIpAddress(ServletUtil.getIpAddress(request));
		user.setLastLoginIpAddress(ServletUtil.getIpAddress(request));
		user.setDisplayName(request.getParameter("displayName"));
		LinkedHashMap<String, Map<String, UserPreferenceItem>> preferences = (LinkedHashMap<String, Map<String, UserPreferenceItem>>)new UserPreferencesUtil(user).getGroups();
		for (String group : preferences.keySet()) {
			for (String key : preferences.get(group).keySet()) {
				user.setPreference(key, request.getParameter(key));
			}
		}
		return user;
	}

	/**
	 *
	 */
	private String signaturePreview(HttpServletRequest request, WikiPageInfo pageInfo, WikiUser user) {
		String signature = request.getParameter("signature");
		if (StringUtils.isBlank(signature)) {
			signature = user.getSignature();
		}
		ParserInput parserInput = new ParserInput(pageInfo.getVirtualWikiName(), "");
		parserInput.setContext(request.getContextPath());
		parserInput.setLocale(request.getLocale());
		parserInput.setWikiUser(user);
		parserInput.setUserDisplay(ServletUtil.getIpAddress(request));
		ParserOutput parserOutput = new ParserOutput();
		try {
			// FIXME - should not need to specify mode
			return WikiBase.getParserInstance().parseFragment(parserInput, parserOutput, signature, JFlexParser.MODE_PROCESS);
		} catch (ParserException e) {
			logger.error("Failure while parsing user signature " + signature, e);
		}
		return "";
	}

	/**
	 *
	 */
	private void validate(HttpServletRequest request, WikiPageInfo pageInfo, WikiUser user) throws Exception {
		try {
			WikiUtil.validateUserName(user.getUsername());
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		}
		String oldPassword = request.getParameter("oldPassword");
		if (user.getUserId() > 0 && !StringUtils.isBlank(oldPassword) && !WikiBase.getDataHandler().authenticate(user.getUsername(), oldPassword)) {
			pageInfo.addError(new WikiMessage("register.error.oldpasswordinvalid"));
		}
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");
		if (user.getUserId() < 1 && StringUtils.isBlank(newPassword)) {
			pageInfo.addError(new WikiMessage("register.error.passwordempty"));
		}
		if (!StringUtils.isBlank(newPassword) || !StringUtils.isBlank(confirmPassword)) {
			if (user.getUserId() > 0 && StringUtils.isBlank(oldPassword)) {
				pageInfo.addError(new WikiMessage("register.error.oldpasswordinvalid"));
			}
			try {
				WikiUtil.validatePassword(newPassword, confirmPassword);
			} catch (WikiException e) {
				pageInfo.addError(e.getWikiMessage());
			}
		}
		if (user.getUserId() < 1 && WikiBase.getDataHandler().lookupWikiUser(user.getUsername()) != null) {
			pageInfo.addError(new WikiMessage("register.error.logininvalid", user.getUsername()));
		}
		if (user.getUserId() < 1 && !ReCaptchaUtil.isValidForRegistration(request)) {
			pageInfo.addError(new WikiMessage("common.exception.recaptcha"));
		}
		String result = ServletUtil.checkForSpam(request, user.getUsername());
		if (result != null) {
			pageInfo.addError(new WikiMessage("edit.exception.spam", result));
		}
		// These fields are only available for update
		if(user.getUserId() != -1) {
			try {
				new SimpleDateFormat(request.getParameter(WikiUser.USER_PREFERENCE_DATE_FORMAT));
				new SimpleDateFormat(request.getParameter(WikiUser.USER_PREFERENCE_TIME_FORMAT));
			} catch (IllegalArgumentException e) {
				// the format will never be null in a properly configured wiki, so this
				// test is mostly for administrators
				logger.error("Invalid date format configured, please check wiki configuration", e);
			}
		}
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// FIXME - i suspect initializing with a null login is bad
		WikiUser user = new WikiUser(null);
		if (!ServletUtil.currentUserDetails().hasRole(Role.ROLE_ANONYMOUS)) {
			user = ServletUtil.currentWikiUser();
		}
		this.loadDefaults(request, next, pageInfo, user);
	}
}
