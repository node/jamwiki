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
import org.jamwiki.Environment;
import org.jamwiki.model.Role;
import org.jamwiki.servlets.ServletUtil;
import org.jamwiki.utils.WikiLogger;
import org.springframework.security.remoting.dns.DnsEntryNotFoundException;
import org.springframework.security.remoting.dns.JndiDnsResolver;

/**
 * Utility methods for working with the Project Honeypot API.  See
 * http://www.projecthoneypot.org/httpbl_api.php.
 */
public class HoneypotValidator implements RequestValidator {

	private static final WikiLogger logger = WikiLogger.getLogger(HoneypotValidator.class.getName());
	/** DNS lookup address for Honeypot lookups.  IP and access key will be prepended to this value. */
	private static final String PROJECT_HONEYPOT_DOMAIN = "dnsbl.httpbl.org";
	/** Use the Spring DNS resolver as the Java InetAddress methods take excessively long on Windows machines. */
	private static JndiDnsResolver JNDI_DNS_RESOLVER = new JndiDnsResolver();
	/** Project Honeypot Octet for search engines. */
	private static final int HONEYPOT_OCTET_SEARCH_ENGINE = 0;
	/** Project Honeypot Octet for suspicious addresses. */
	private static final int HONEYPOT_OCTET_SUSPICIOUS = 1;
	/** Project Honeypot Octet for address harvesters. */
	private static final int HONEYPOT_OCTET_HARVESTER = 2;
	/** Project Honeypot Octet for comment spammers. */
	private static final int HONEYPOT_OCTET_COMMENT_SPAMMER = 4;
	/** Minimum number of days since malicious activity was detected in order for the block to be imposed. */
	// TODO: make this configurable
	private static final int MAX_DAYS_SINCE_LAST_ACTIVE_FOR_BLOCK = 60;

	/**
	 * Generate the DNS lookup address for the current user request.
	 */
	private String buildDnsLookupAddress(String ipAddress) {
		String dnsAddress = Environment.getValue(Environment.PROP_HONEYPOT_ACCESS_KEY) + '.';
		// per Honeypot rules, reverse the IP address (10.20.30.40 becomes 40.30.20.10)
		String[] octets = ipAddress.split("\\.");
		for (int i = (octets.length - 1); i >= 0; i--) {
			dnsAddress += octets[i] + '.';
		}
		dnsAddress += PROJECT_HONEYPOT_DOMAIN;
		return dnsAddress;
	}

	/**
	 * Determine if the specified request is blacklisted by Project Honeypot.  Note
	 * that only anonymous users are validated as it is assumed that a user who
	 * has registered successfully has already passed Honeypot validation.
	 *
	 * @param request The current user request.
	 * @return Returns a non-null {@link RequestValidatorInfo} object that
	 * encapsulates the validation result.
	 */
	public RequestValidatorInfo validate(HttpServletRequest request) {
		if (!ServletUtil.currentUserDetails().hasRole(Role.ROLE_ANONYMOUS)) {
			// do not validate logged-in users
			return new RequestValidatorInfo(true);
		}
		long start = System.currentTimeMillis();
		String ipAddress = ServletUtil.getIpAddress(request);
		String dnsLookupAddress = this.buildDnsLookupAddress(ipAddress);
		HoneypotResult honeypotResult = this.retrieveHoneypotResult(dnsLookupAddress);
		boolean result = this.isValidHoneypotResult(honeypotResult, ipAddress);
		if (logger.isDebugEnabled()) {
			logger.debug("Honeypot filter execution time for " + ipAddress + " (score: " + ((honeypotResult != null) ? honeypotResult.ipAddress : "null") + "): " + ((System.currentTimeMillis() - start) / 1000.000) + " s.");
		}
		return new RequestValidatorInfo(result);
	}

	/**
	 * Return <code>true</code> if the honeypot result indicates a valid user,
	 * or <code>false</code> if it indicates a user that should be blocked.
	 */
	private boolean isValidHoneypotResult(HoneypotResult honeypotResult, String ipAddress) {
		if (honeypotResult == null) {
			return true;
		}
		if (honeypotResult.threatTypeOctet >= HONEYPOT_OCTET_COMMENT_SPAMMER && honeypotResult.lastActivityOctet <= MAX_DAYS_SINCE_LAST_ACTIVE_FOR_BLOCK) {
			logger.info("Blocking malicious IP address identified by Project Honeypot blacklist: " + ipAddress + " (score: " + honeypotResult.ipAddress + ").");
			return false;
		}
		return true;
	}

	/**
	 * Query the Honeypot DNS server to get the result.  The result will be of
	 * the form "127.3.5.1", where the first octet is always "127", the second
	 * octet is the number of days since the queried address last triggered a
	 * Honeypot server, the third octet is the threat score of the address from
	 * 0-255, and the fourth octet is a bitset indicating the type of threat.
	 *
	 * If no result is returned then there is no Honeypot record for the address
	 * in question.
	 */
	private HoneypotResult retrieveHoneypotResult(String dnsLookupAddress) {
		try {
			HoneypotResult honeypotResult = new HoneypotResult(JNDI_DNS_RESOLVER.resolveIpAddress(dnsLookupAddress));
			if (honeypotResult.errorCheckOctet != 127 && honeypotResult.errorCheckOctet != -1) {
				logger.info("Invalid result returned for DNS lookup of " + dnsLookupAddress + ": " + honeypotResult.ipAddress);
				return null;
			}
			return honeypotResult;
		} catch (DnsEntryNotFoundException e) {
			return null;
		}
	}

	/**
	 * Utility class for modeling Honeypot results.
	 */
	class HoneypotResult {

		/** The IP address represent by this honeypot result. */
		String ipAddress = null;
		/** First octet of the result, always 127 unless an error occurs. */
		int errorCheckOctet = -1;
		/** Second octet is days since last activity. */
		int lastActivityOctet = -1;
		/** Third octet is threat score. */
		int threatScoreOctet = -1;
		/** Last octet is a bitset of threat type. */
		int threatTypeOctet = -1;

		HoneypotResult(String ipAddress) {
			this.ipAddress = ipAddress;
			if (this.ipAddress == null) {
				return;
			}
			String[] octets = this.ipAddress.split("\\.");
			if (octets.length != 4) {
				return;
			}
			this.errorCheckOctet = Integer.parseInt(octets[0]);
			this.lastActivityOctet = Integer.parseInt(octets[1]);
			this.threatScoreOctet = Integer.parseInt(octets[2]);
			this.threatTypeOctet = Integer.parseInt(octets[3]);
		}
	}
}
