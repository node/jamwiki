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
import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;

/**
 * Utility methods for working with the reCAPTCHA library.  See
 * http://code.google.com/apis/recaptcha/docs/java.html.
 */
public class ReCaptchaUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(ReCaptchaUtil.class.getName());
	public static final int RECAPTCHA_NEVER = 0;
	public static final int RECAPTCHA_UNREGISTERED_ONLY = 1;
	public static final int RECAPTCHA_ALWAYS = 2;
	private static ReCaptcha RECAPTCHA = null;

	/**
	 * Utility method for determining if Captcha is enabled for editing.
	 *
	 * @param user The current user, which may be either a logged-in user
	 *  or an unregistered user.
	 * @return Returns <code>true</code> if Captcha is enabled for editing for
	 *  the current user, <code>false</code> otherwise.
	 */
	public static boolean isEditEnabled(WikiUser user) {
		int minCaptchaLevel = (user.getUserId() < 1) ? RECAPTCHA_UNREGISTERED_ONLY : RECAPTCHA_ALWAYS;
		return (Environment.getIntValue(Environment.PROP_RECAPTCHA_EDIT) >= minCaptchaLevel);
	}

	/**
	 * Utility method for determining if Captcha is enabled for user registration.
	 *
	 * @return Returns <code>true</code> if Captcha is enabled for user registration,
	 *  <code>false</code> otherwise.
	 */
	public static boolean isRegistrationEnabled() {
		return (Environment.getIntValue(Environment.PROP_RECAPTCHA_REGISTER) > RECAPTCHA_NEVER);
	}

	/**
	 * Utility method for determining if Captcha is successful during editing.
	 *
	 * @param request The current request that contains the Captcha response.
	 * @param user The current user, which may be either a logged-in user
	 *  or an unregistered user.
	 * @return Returns <code>true</code> if Captcha is successful OR if Captcha
	 *  is not enabled for editing, <code>false</code> otherwise.
	 */
	public static boolean isValidForEdit(HttpServletRequest request, WikiUser user) {
		if (!ReCaptchaUtil.isEditEnabled(user)) {
			// recaptcha not enabled
			return true;
		}
		return ReCaptchaUtil.validateCaptcha(request);
	}

	/**
	 * Utility method for determining if Captcha is successful during user
	 * registration.
	 *
	 * @param request The current request that contains the Captcha response.
	 * @return Returns <code>true</code> if Captcha is successful OR if Captcha
	 *  is not enabled for registration, <code>false</code> otherwise.
	 */
	public static boolean isValidForRegistration(HttpServletRequest request) {
		if (!ReCaptchaUtil.isRegistrationEnabled()) {
			// recaptcha not enabled
			return true;
		}
		return ReCaptchaUtil.validateCaptcha(request);
	}

	/**
	 * Return an instance of the ReCaptcha object, initializing it if it is not already
	 * initialized.
	 *
	 * @return Returns an instance of the CAPTCHA object.
	 */
	public static ReCaptcha recaptchaInstance() {
		String publicKey = Environment.getValue(Environment.PROP_RECAPTCHA_PUBLIC_KEY);
		String privateKey = Environment.getValue(Environment.PROP_RECAPTCHA_PRIVATE_KEY);
		if (RECAPTCHA == null) {
			RECAPTCHA = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);
		}
		return RECAPTCHA;
	}

	/**
	 *
	 */
	private static boolean validateCaptcha(HttpServletRequest request) {
		ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
		String privateKey = Environment.getValue(Environment.PROP_RECAPTCHA_PRIVATE_KEY);
		if (StringUtils.isBlank(privateKey)) {
			logger.warn("Attempt to use CAPTCHA without a private key.  Please configure this value from using the Special:Admin tools.");
			return true;
		}
		reCaptcha.setPrivateKey(privateKey);
		String challenge = request.getParameter("recaptcha_challenge_field");
		String response = request.getParameter("recaptcha_response_field");
		boolean result = false;
		if (!StringUtils.isBlank(challenge) && !StringUtils.isBlank(response)) {
			// spambots sometimes fail to submit these fields, and checkAnswer
			// throws a NPE without them
			result = reCaptcha.checkAnswer(request.getRemoteAddr(), challenge, response).isValid();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Captcha validation " + (result ? "successful" : "failed") + " for " + request.getRemoteAddr());
		}
		return result;
	}
}
