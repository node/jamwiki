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

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiConfiguration;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.WikiVersion;
import org.jamwiki.db.DatabaseConnection;
import org.jamwiki.db.WikiDatabase;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiConfigurationObject;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to handle JAMWiki setup, including setting and validating JAMWiki
 * configuration values.
 *
 * @see org.jamwiki.servlets.UpgradeServlet
 */
public class SetupServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(SetupServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_SETUP = "setup.jsp";
	private static final JavaVersion MINIMUM_JDK_VERSION = JavaVersion.JAVA_1_5;

	/**
	 * This servlet requires slightly different initialization parameters from most
	 * servlets.
	 */
	public SetupServlet() {
		this.layout = false;
		this.displayJSP = "setup";
	}

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		if (!WikiUtil.isFirstUse()) {
			throw new WikiException(new WikiMessage("setup.error.notrequired"));
		}
		try {
			if (!SystemUtils.isJavaVersionAtLeast(MINIMUM_JDK_VERSION)) {
				throw new WikiException(new WikiMessage("setup.error.jdk", MINIMUM_JDK_VERSION.toString(), System.getProperty("java.version")));
			}
			VirtualWiki virtualWiki = VirtualWiki.defaultVirtualWiki();
			if (!StringUtils.isBlank(request.getParameter("override")) && this.restoreProperties(pageInfo)) {
				if (WikiUtil.isUpgrade()) {
					ServletUtil.redirect(next, virtualWiki.getName(), "Special:Upgrade");
				} else {
					ServletUtil.redirect(next, virtualWiki.getName(), virtualWiki.getRootTopicName());
				}
			} else if (!StringUtils.isBlank(request.getParameter("function")) && initialize(request, next, pageInfo)) {
				ServletUtil.redirect(next, virtualWiki.getName(), virtualWiki.getRootTopicName());
			} else {
				view(request, next, pageInfo);
			}
		} catch (Exception e) {
			handleSetupError(request, next, pageInfo, e);
		}
		return next;
	}

	/**
	 *
	 */
	private void handleSetupError(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, Exception e) {
		// reset properties
		Environment.setBooleanValue(Environment.PROP_BASE_INITIALIZED, false);
		if (!(e instanceof WikiException)) {
			logger.error("Setup error", e);
		}
		try {
			this.view(request, next, pageInfo);
		} catch (Exception ex) {
			logger.error("Unable to set up page view object for setup.jsp", ex);
		}
		if (e instanceof WikiException) {
			WikiException we = (WikiException)e;
			next.addObject("messageObject", we.getWikiMessage());
		} else {
			next.addObject("messageObject", new WikiMessage("error.unknown", e.getMessage()));
		}
	}

	/**
	 *
	 */
	private boolean initialize(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		setProperties(request, next);
		WikiUser user = setAdminUser(request);
		this.validate(request, pageInfo, user);
		if (!pageInfo.getErrors().isEmpty()) {
			next.addObject("username", user.getUsername());
			next.addObject("newPassword", request.getParameter("newPassword"));
			next.addObject("confirmPassword", request.getParameter("confirmPassword"));
			return false;
		}
		if (previousInstall()) {
			// user is trying to do a new install when a previous installation exists
			next.addObject("installExists", "true");
			next.addObject("username", user.getUsername());
			next.addObject("newPassword", request.getParameter("newPassword"));
			next.addObject("confirmPassword", request.getParameter("confirmPassword"));
			return false;
		}
		Environment.setBooleanValue(Environment.PROP_BASE_INITIALIZED, true);
		Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiVersion.CURRENT_WIKI_VERSION);
		String username = request.getParameter("username");
		String newPassword = request.getParameter("newPassword");
		String encryptedPassword = Encryption.encrypt(newPassword);
		WikiBase.reset(request.getLocale(), user, username, encryptedPassword);
		Environment.saveConfiguration();
		// the setup process does not add new topics to the index (currently)
		// TODO - remove this once setup uses safe connection handling
		WikiBase.getSearchEngine().refreshIndex();
		// force current user credentials to be removed and re-validated.
		SecurityContextHolder.clearContext();
		return true;
	}

	/**
	 *
	 */
	private boolean previousInstall() {
		String driver = Environment.getValue(Environment.PROP_DB_DRIVER);
		String url = Environment.getValue(Environment.PROP_DB_URL);
		String userName = Environment.getValue(Environment.PROP_DB_USERNAME);
		String password = Encryption.getEncryptedProperty(Environment.PROP_DB_PASSWORD, null);
		try {
			DatabaseConnection.testDatabase(driver, url, userName, password, true);
		} catch (Exception e) {
			// no previous database, all good
			return false;
		}
		return true;
	}

	/**
	 * Call this method only when an existing JAMWiki database is found and
	 * 
	 */
	private boolean restoreProperties(WikiPageInfo pageInfo) {
		// read the old configuration from the database
		boolean result = false;
		try {
			Map<String, String> configuration = WikiBase.getDataHandler().lookupConfiguration();
			for (Map.Entry<String, String> entry : configuration.entrySet()) {
				Environment.setValue(entry.getKey(), entry.getValue());
			}
			Environment.saveConfiguration();
			result = true;
		} catch (DataAccessException e) {
			pageInfo.addError(new WikiMessage("error.unknown", e.getMessage()));
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		}
		return result;
	}

	/**
	 *
	 */
	private WikiUser setAdminUser(HttpServletRequest request) throws Exception {
		String username = request.getParameter("username");
		WikiUser user = new WikiUser(username);
		user.setCreateIpAddress(ServletUtil.getIpAddress(request));
		user.setLastLoginIpAddress(ServletUtil.getIpAddress(request));
		return user;
	}

	/**
	 *
	 */
	private void setProperties(HttpServletRequest request, ModelAndView next) throws Exception {
		Environment.setValue(Environment.PROP_BASE_FILE_DIR, request.getParameter(Environment.PROP_BASE_FILE_DIR));
		Environment.setValue(Environment.PROP_FILE_UPLOAD_STORAGE, request.getParameter(Environment.PROP_FILE_UPLOAD_STORAGE));
		Environment.setValue(Environment.PROP_FILE_DIR_FULL_PATH, request.getParameter(Environment.PROP_FILE_DIR_FULL_PATH));
		Environment.setValue(Environment.PROP_FILE_DIR_RELATIVE_PATH, request.getParameter(Environment.PROP_FILE_DIR_RELATIVE_PATH));
		Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, request.getParameter(Environment.PROP_BASE_PERSISTENCE_TYPE));
		if (Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals(WikiBase.PERSISTENCE_EXTERNAL)) {
			Environment.setValue(Environment.PROP_DB_DRIVER, request.getParameter(Environment.PROP_DB_DRIVER));
			Environment.setValue(Environment.PROP_DB_TYPE, request.getParameter(Environment.PROP_DB_TYPE));
			Environment.setValue(Environment.PROP_DB_URL, request.getParameter(Environment.PROP_DB_URL));
			Environment.setValue(Environment.PROP_DB_USERNAME, request.getParameter(Environment.PROP_DB_USERNAME));
			Encryption.setEncryptedProperty(Environment.PROP_DB_PASSWORD, request.getParameter(Environment.PROP_DB_PASSWORD), null);
			next.addObject("dbPassword", request.getParameter(Environment.PROP_DB_PASSWORD));
		} else {
			WikiDatabase.setupDefaultDatabase(Environment.getInstance());
		}
		Environment.setValue(Environment.PROP_SERVER_URL, Utilities.getServerUrl(request));
	}

	/**
	 *
	 */
	private void validate(HttpServletRequest request, WikiPageInfo pageInfo, WikiUser user) throws Exception {
		pageInfo.getErrors().addAll(WikiUtil.validateSystemSettings(Environment.getInstance()));
		if (StringUtils.isBlank(user.getUsername())) {
			pageInfo.addError(new WikiMessage("error.loginempty"));
		}
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");
		if (newPassword != null || confirmPassword != null) {
			if (newPassword == null) {
				pageInfo.addError(new WikiMessage("error.newpasswordempty"));
			} else if (confirmPassword == null) {
				pageInfo.addError(new WikiMessage("error.passwordconfirm"));
			} else if (!newPassword.equals(confirmPassword)) {
				pageInfo.addError(new WikiMessage("admin.message.passwordsnomatch"));
			}
		}
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setContentJsp(JSP_SETUP);
		pageInfo.setSpecial(true);
		pageInfo.setPageTitle(new WikiMessage("setup.title", WikiVersion.CURRENT_WIKI_VERSION));
		List<WikiConfigurationObject> queryHandlers = WikiConfiguration.getInstance().getQueryHandlers();
		next.addObject("queryHandlers", queryHandlers);
		WikiMessage logMessage = new WikiMessage("setup.help.logfile", WikiLogger.LOGGING_CONFIGURATION_FILE_PATH);
		next.addObject("logMessage", logMessage);
	}
}
