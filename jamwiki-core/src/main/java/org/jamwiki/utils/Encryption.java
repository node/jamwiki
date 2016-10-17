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
package org.jamwiki.utils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiException;

/**
 * Provide capability for encrypting and decrypting values.  Inspired by an
 * example from http://www.devx.com/assets/sourcecode/10387.zip.
 */
public class Encryption {

	private static final WikiLogger logger = WikiLogger.getLogger(Encryption.class.getName());
	public static final String DES_ALGORITHM = "DES";
	public static final String ENCRYPTION_KEY = "JAMWiki Key 12345";

	/**
	 * Hide the constructor by making it private.
	 */
	private Encryption() {
	}

	/**
	 * Encrypt a String value using the DES encryption algorithm.
	 *
	 * @param unencryptedBytes The unencrypted String value that is to be encrypted.
	 * @return An encrypted version of the String that was passed to this method.
	 */
	private static String encrypt64(byte[] unencryptedBytes) throws GeneralSecurityException, UnsupportedEncodingException {
		if (unencryptedBytes == null || unencryptedBytes.length == 0) {
			throw new IllegalArgumentException("Cannot encrypt a null or empty byte array");
		}
		SecretKey key = createKey();
		Cipher cipher = Cipher.getInstance(key.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encryptedBytes = Base64.encodeBase64(cipher.doFinal(unencryptedBytes));
		return bytes2String(encryptedBytes);
	}

	/**
	 *
	 */
	public static String encrypt(String unencryptedString) {
		if (StringUtils.isBlank(unencryptedString)) {
			throw new IllegalArgumentException("Cannot encrypt a null or empty string");
		}
		MessageDigest md = null;
		String encryptionAlgorithm = Environment.getValue(Environment.PROP_ENCRYPTION_ALGORITHM);
		try {
			md = MessageDigest.getInstance(encryptionAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			logger.warn("JDK does not support the " + encryptionAlgorithm + " encryption algorithm.  Weaker encryption will be attempted.");
		}
		if (md == null) {
			// fallback to weaker encryption algorithm if nothing better is available
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				throw new UnsupportedOperationException("JDK does not support the SHA-1 or SHA-512 encryption algorithms");
			}
			// save the algorithm so that if the user upgrades the JDK they can
			// still use passwords encrypted with the weaker algorithm
			Environment.setValue(Environment.PROP_ENCRYPTION_ALGORITHM, "SHA-1");
			try {
				Environment.saveConfiguration();
			} catch (WikiException e) {
				// FIXME - shouldn't this be better handled ???
				logger.info("Failure while saving encryption algorithm property", e);
			}
		}
		try {
			md.update(unencryptedString.getBytes("UTF-8"));
			byte raw[] = md.digest();
			return encrypt64(raw);
		} catch (GeneralSecurityException e) {
			logger.error("Encryption failure", e);
			throw new IllegalStateException("Failure while encrypting value");
		} catch (UnsupportedEncodingException e) {
			// this should never happen
			throw new IllegalStateException("Unsupporting encoding UTF-8");
		}
	}

	/**
	 * Unencrypt a String value using the DES encryption algorithm.
	 *
	 * @param encryptedString The encrypted String value that is to be unencrypted.
	 * @return An unencrypted version of the String that was passed to this method.
	 */
	private static String decrypt64(String encryptedString) throws GeneralSecurityException, UnsupportedEncodingException {
		if (StringUtils.isBlank(encryptedString)) {
			return encryptedString;
		}
		SecretKey key = createKey();
		Cipher cipher = Cipher.getInstance(key.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] encryptedBytes = encryptedString.getBytes("UTF8");
		byte[] unencryptedBytes = cipher.doFinal(Base64.decodeBase64(encryptedBytes));
		return bytes2String(unencryptedBytes);
	}

	/**
	 * Convert a byte array to a String value.
	 *
	 * @param bytes The byte array that is to be converted.
	 * @return A String value created from the byte array that was passed to this method.
	 */
	private static String bytes2String(byte[] bytes) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			buffer.append((char)bytes[i]);
		}
		return buffer.toString();
	}

	/**
	 * Create the encryption key value.
	 *
	 * @return An encryption key value implementing the DES encryption algorithm.
	 */
	private static SecretKey createKey() throws GeneralSecurityException, UnsupportedEncodingException {
		byte[] bytes = ENCRYPTION_KEY.getBytes("UTF8");
		DESKeySpec spec = new DESKeySpec(bytes);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES_ALGORITHM);
		return keyFactory.generateSecret(spec);
	}

	/**
	 * If a property value is encrypted, return the unencrypted value.  Note that if this
	 * method finds an un-encrypted value it will automatically encrypt it and re-save it to
	 * the property file.
	 *
	 * @param name The name of the encrypted property being retrieved.
	 * @return The unencrypted value of the property.
	 */
	public static String getEncryptedProperty(String name, Properties props) {
		try {
			if (props != null) {
				return Encryption.decrypt64(props.getProperty(name));
			}
			return Encryption.decrypt64(Environment.getValue(name));
		} catch (GeneralSecurityException e) {
			String value = Environment.getValue(name);
			if (props != null || StringUtils.isBlank(value)) {
				logger.error("Encryption failure or no value available for property: " + name, e);
				throw new IllegalStateException("Failure while retrieving encrypted property: " + name);
			}
			// the property might have been unencrypted in the property file, so encrypt, save, and return the value
			logger.warn("Found unencrypted property file value: " + name + ".  Assuming that this value manually un-encrypted in the property file so re-encrypting and re-saving.");
			Encryption.setEncryptedProperty(name, value, null);
			try {
				Environment.saveConfiguration();
			} catch (WikiException ex) {
				logger.error("Failure while saving properties", ex);
				throw new IllegalStateException("Failure while saving properties");
			}
			return value;
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Unsupporting encoding UTF-8");
		}
	}

	/**
	 * Encrypt and set a property value.
	 *
	 * @param name The name of the encrypted property being retrieved.
	 * @param value The unenencrypted value of the property.
	 * @param props The property object in which the property is being set.
	 */
	public static void setEncryptedProperty(String name, String value, Properties props) {
		String encrypted = "";
		if (!StringUtils.isBlank(value)) {
			byte[] unencryptedBytes = null;
			try {
				unencryptedBytes = value.getBytes("UTF8");
				encrypted = Encryption.encrypt64(unencryptedBytes);
			} catch (GeneralSecurityException e) {
				logger.error("Encryption failure", e);
				throw new IllegalStateException("Failure while encrypting value");
			} catch (UnsupportedEncodingException e) {
				// this should never happen
				throw new IllegalStateException("Unsupporting encoding UTF-8");
			}
		}
		if (props == null) {
			Environment.setValue(name, encrypted);
		} else {
			props.setProperty(name, encrypted);
		}
	}
}
