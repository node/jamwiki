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
package org.jamwiki.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides an object representing a configuration value as used by
 * {@link org.jamwiki.WikiConfiguration}.
 */
public class WikiConfigurationObject {

	private String clazz;
	private String key;
	private String key2;
	private String name;
	private String state;
	private Map<String, String> initParams;

	/**
	 *
	 */
	public String getClazz() {
		return this.clazz;
	}

	/**
	 *
	 */
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	/**
	 *
	 */
	public boolean isExperimental() {
		return (this.state != null && this.state.equalsIgnoreCase("experimental"));
	}

	/**
	 *
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 *
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 *
	 */
	public String getKey2() {
		return this.key2;
	}

	/**
	 *
	 */
	public void setKey2(String key2) {
		this.key2 = key2;
	}

	/**
	 *
	 */
	public String getName() {
		return this.name;
	}

	/**
	 *
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 */
	public String getState() {
		return this.state;
	}

	/**
	 *
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Return a mapping of of key-value pairs representing initialization parameters.
	 * This method will never return <code>null</code>.
	 */
	public Map<String, String> getInitParams() {
		if (this.initParams == null) {
			this.initParams = new HashMap<String, String>();
		}
		return this.initParams;
	}

	/**
	 * Add a key-value pair to the mapping of of key-value pairs representing
	 * initialization parameters.
	 */
	public void addInitParam(String key, String value) {
		this.getInitParams().put(key, value);
	}
}
