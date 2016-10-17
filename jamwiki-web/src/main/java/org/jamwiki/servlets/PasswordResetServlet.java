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
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMessage;
import org.jamwiki.mail.WikiMail;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Encryption;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to handle requests or redirects to the login page, as well as requests to logout.
 */
public class PasswordResetServlet extends JAMWikiServlet {

	// Remove after testing
	// challenge: ?loginUsername=charles&rcode=VWY3HyfcgLNTOoOVy3EyV6uLaAkkf73BvFC82mNURQpHKiD8NJCtkMYXfOrQHpN8dJde5HPXpvLX3LjehWu1bDdEvUzqWnQo
	
	/** Logger */
	private static final WikiLogger logger = WikiLogger.getLogger(PasswordResetServlet.class.getName());
	/** The name of the JSP file used to render the servlet output when searching. */
	protected static final String JSP_LOGIN_RESET = "password-reset.jsp";

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setSpecial(true);
		pageInfo.setContentJsp(JSP_LOGIN_RESET);
		pageInfo.setPageTitle(new WikiMessage("password.reset.password"));
		boolean mailEnabled = Environment.getBooleanValue(Environment.PROP_EMAIL_SMTP_ENABLE) && Environment.getBooleanValue(Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD);
		String function = request.getParameter("function");
		String challenge = request.getParameter("rcode");
		if(mailEnabled) {
			if(!StringUtils.isBlank(function)) {
				if(function.equals("sendEmail")) {
					sendEmail(request, response, next, pageInfo);
				}
				else if(function.equals("resetPassword")) {
					resetPassword(request, response, next, pageInfo);
				}
			}
			else {
				if(!StringUtils.isBlank(challenge)) {
					getNewPassword(request, response, next, pageInfo);
				}
				else {
					sendEmail(request, response, next, pageInfo);
				}
			}
		}
		else {
			pageInfo.addError(new WikiMessage("password.reset.password.error.noservice"));
		}
		next.addObject("mailEnabled", mailEnabled);
		next.addObject("function", function);
		return next;
	}
	
	private void sendEmail(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String function = request.getParameter("function");
		String username = request.getParameter("loginUsername");
		String mailAddress = null;
		String challenge = null;
		boolean success = false;
		// If second call, function is not null
		if(StringUtils.isBlank(username) && !StringUtils.isBlank(function)) {
			pageInfo.addError(new WikiMessage("password.reset.password.error.nousername"));
			success = true;
		}
		else {
			WikiUser user = WikiBase.getDataHandler().lookupPwResetChallengeData(username);
			if(user != null) {
				mailAddress = user.getEmail();
				if(StringUtils.isBlank(mailAddress)) {
					mailAddress = null;
					pageInfo.addError(new WikiMessage("password.reset.password.error.noemail"));
				}
				else {
					challenge = getChallenge(request, pageInfo, user);
					if(challenge != null) {
						user.setChallengeValue(challenge);
						int requests = user.getChallengeTries();
						user.setChallengeDate(new Timestamp(new GregorianCalendar().getTimeInMillis()));
						user.setChallengeIp(request.getRemoteAddr());
						user.setChallengeTries(++requests);
						WikiBase.getDataHandler().updatePwResetChallengeData(user);
						try {
							// Note: add toString to WikiMessage to retrieve text...
							// new WikiMessage("password.reset.password.email");
							StringBuffer mailLink = request.getRequestURL();
							mailLink.append("?loginUsername=");
							mailLink.append(username);
							mailLink.append("&rcode=");
							mailLink.append(challenge);
							WikiMail sender = new WikiMail();
							Locale language = Locale.getDefault();
							String[] localeData = user.getDefaultLocale().split("_");
							if(localeData.length > 0) {
								language = new Locale(localeData[0]);
							}
							String mailSubject = Utilities.formatMessage("password.reset.password.email.subject", language);
							String mailBody = null;
							mailBody = Utilities.formatMessage("password.reset.password.email.body", language, new Object[]{mailLink});
							sender.postMail(mailAddress,mailSubject, mailBody);
							pageInfo.addMessage(new WikiMessage("password.reset.password.message.sendmail.success"));
						}
						catch(Exception ex) {
							pageInfo.addError(new WikiMessage("password.reset.password.message.sendmail.failed"));
							logger.error("Unable to send E-Mail", ex);
						}
					}
				}
				success = true;
			}
			else {
				if(!StringUtils.isBlank(function)) {
					pageInfo.addError(new WikiMessage("password.reset.password.error.notregistered"));
					success = true;
				}
			}
			next.addObject("username", username);
			next.addObject("success", success);
		}
	}
	
	private void getNewPassword(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String username = request.getParameter("loginUsername");
		String challenge = request.getParameter("rcode");
		boolean challengeOk = true;
		WikiUser user = WikiBase.getDataHandler().lookupPwResetChallengeData(username);
		if(user == null) {
			pageInfo.addError(new WikiMessage("password.reset.password.error.notregistered"));
			challengeOk = false;
			/** The only possible cause of this to happen is that somebody entered fake
			    data to try to access the page. Returning success will disable the display
			    of the password reset entry fields */
			next.addObject("success", Boolean.TRUE);
		}
		else {
			try {
				challengeOk = isChallengeOk(request, pageInfo, user, challenge);
				if(!challengeOk) {
					challengeOk = false;
					/** The only possible cause of this to happen is that somebody entered fake
					    data to try to access the page. Returning success will disable the display
					    of the password reset entry fields */
					next.addObject("success", Boolean.TRUE);
				}
			}
			catch(Exception ex) {
				pageInfo.addError(new WikiMessage("password.reset.password.error.novalidation"));
				challengeOk = false;
			}
		}
		next.addObject("username", username);
		next.addObject("challengeOk", challengeOk);
		// Add a fake rcode for filtering in jstl
		next.addObject("rcode", "1");
	}
	
	private void resetPassword(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		boolean result = false;
		
		String username = request.getParameter("loginUsername");
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");
		if(StringUtils.isBlank(newPassword) || StringUtils.isBlank(confirmPassword)) {
			pageInfo.addError(new WikiMessage("password.reset.password.error.nullpassword"));
		}
		else if((!newPassword.equals(confirmPassword))) {
			pageInfo.addError(new WikiMessage("password.reset.password.error.nomatch"));
		}
		else {
			// store new password
			WikiUser user = WikiBase.getDataHandler().lookupWikiUser(username);
			try {
				WikiBase.getDataHandler().writeWikiUser(user, username, Encryption.encrypt(newPassword));
				pageInfo.addMessage(new WikiMessage("password.reset.password.success"));
				result = true; 
			} catch(Exception ex) {
				pageInfo.addError(new WikiMessage("password.reset.password.failed"));
			}
			next.addObject("showLoginLink", Boolean.TRUE);
		}
		next.addObject("username", username);
		next.addObject("success", result);
	}
	
	private String getChallenge(HttpServletRequest request, WikiPageInfo pageInfo, WikiUser user) throws Exception {
		int numOfTries = Environment.getIntValue(Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD_CHALLENGE_RETRIES);
		int lockDuration = Environment.getIntValue(Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD_IP_LOCK_DURATION);
		if(user.getChallengeDate() != null && user.getChallengeIp() != null) {
			// compute some deadlines
			GregorianCalendar currentDate = new GregorianCalendar();
			GregorianCalendar lockExpires = new GregorianCalendar();
			lockExpires.setTimeInMillis(user.getChallengeDate().getTime());
			lockExpires.add(Calendar.MINUTE, lockDuration);

			if(request.getRemoteAddr().equals(user.getChallengeIp()) && 
			   user.getChallengeTries() >= numOfTries &&
			   currentDate.before(lockExpires)) {
				pageInfo.addError(new WikiMessage("password.reset.password.error.ip.locked"));
				return null;
			}
			// reset retries after lock timeout
			if(user.getChallengeTries() >= numOfTries &&
			   currentDate.after(lockExpires)) {
				user.setChallengeTries(0);
				WikiBase.getDataHandler().updatePwResetChallengeData(user);
			}
		}
		return new Integer((int)(Math.random() * Integer.MAX_VALUE)).toString();
	}
	
	private boolean isChallengeOk(HttpServletRequest request, WikiPageInfo pageInfo, WikiUser user, String challenge) throws Exception {
		int timeout = Environment.getIntValue(Environment.PROP_EMAIL_SERVICE_FORGOT_PASSWORD_CHALLENGE_TIMEOUT);
		if(user.getChallengeValue() != null && user.getChallengeDate() != null && user.getChallengeIp() != null) {
			// compute some deadlines
			GregorianCalendar currentDate = new GregorianCalendar();
			GregorianCalendar challengeExpires = new GregorianCalendar();
			challengeExpires.setTimeInMillis(user.getChallengeDate().getTime());
			challengeExpires.add(Calendar.MINUTE, timeout);
			if(!user.getChallengeValue().equals(challenge)) {
				pageInfo.addError(new WikiMessage("password.reset.password.error.challenge.nok"));
			}
			else if(currentDate.after(challengeExpires)) {
				pageInfo.addError(new WikiMessage("password.reset.password.error.challenge.expired"));
				resetChallengeData(user);
			}
			else {
				resetChallengeData(user);
				return true;
			}
		}
		else {
			pageInfo.addError(new WikiMessage("password.reset.password.error.challenge.nok"));
		}
		return false;
	}
	
	private void resetChallengeData(WikiUser user) throws Exception {
		user.setChallengeValue(null);
		user.setChallengeDate(null);
		user.setChallengeIp(null);
		user.setChallengeTries(0);
		WikiBase.getDataHandler().updatePwResetChallengeData(user);
	}
}
