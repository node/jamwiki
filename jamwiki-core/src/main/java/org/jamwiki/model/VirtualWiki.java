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

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;

/**
 * Provides an object representing a virtual wiki.
 */
public class VirtualWiki implements Serializable {

	private String rootTopicName;
	private String logoImageUrl;
	private String metaDescription;
	private final String name;
	private String siteName;
	private int virtualWikiId = -1;

	/**
	 *
	 */
	public VirtualWiki(String name) {
		this.name = name;
	}

	/**
	 * Temporary utility method for returning a virtual wiki containing only default values.
	 */
	public static VirtualWiki defaultVirtualWiki() {
		return new VirtualWiki(Environment.getValue(Environment.PROP_VIRTUAL_WIKI_DEFAULT));
	}

	/**
	 *
	 */
	public String getLogoImageUrl() {
		return (StringUtils.isBlank(this.logoImageUrl) ? Environment.getValue(Environment.PROP_BASE_LOGO_IMAGE) : this.logoImageUrl);
	}

	/**
	 *
	 */
	public void setLogoImageUrl(String logoImageUrl) {
		this.logoImageUrl = logoImageUrl;
	}

	/**
	 *
	 */
	public boolean isDefaultLogoImageUrl() {
		return (StringUtils.isBlank(this.logoImageUrl) || StringUtils.equals(this.logoImageUrl, Environment.getValue(Environment.PROP_BASE_LOGO_IMAGE)));
	}

	/**
	 *
	 */
	public String getMetaDescription() {
		return (StringUtils.isBlank(this.metaDescription) ? Environment.getValue(Environment.PROP_BASE_META_DESCRIPTION) : this.metaDescription);
	}

	/**
	 *
	 */
	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	/**
	 *
	 */
	public boolean isDefaultMetaDescription() {
		return (StringUtils.isBlank(this.metaDescription) || StringUtils.equals(this.metaDescription, Environment.getValue(Environment.PROP_BASE_META_DESCRIPTION)));
	}

	/**
	 * Determine if this virtual wiki matches the system default.
	 */
	public boolean isDefaultVirtualWiki() {
		return (this.name != null && this.name.equals(Environment.getValue(Environment.PROP_VIRTUAL_WIKI_DEFAULT)));
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
	public String getRootTopicName() {
		return (StringUtils.isBlank(this.rootTopicName) ? Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC) : this.rootTopicName);
	}

	/**
	 *
	 */
	public void setRootTopicName(String rootTopicName) {
		this.rootTopicName = rootTopicName;
	}

	/**
	 *
	 */
	public boolean isDefaultRootTopicName() {
		return (StringUtils.isBlank(this.rootTopicName) || StringUtils.equals(this.rootTopicName, Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC)));
	}

	/**
	 *
	 */
	public String getSiteName() {
		return (StringUtils.isBlank(this.siteName) ? Environment.getValue(Environment.PROP_SITE_NAME) : this.siteName);
	}

	/**
	 *
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	/**
	 *
	 */
	public boolean isDefaultSiteName() {
		return (StringUtils.isBlank(this.siteName) || StringUtils.equals(this.siteName, Environment.getValue(Environment.PROP_SITE_NAME)));
	}

	/**
	 *
	 */
	public int getVirtualWikiId() {
		return this.virtualWikiId;
	}

	/**
	 *
	 */
	public void setVirtualWikiId(int virtualWikiId) {
		this.virtualWikiId = virtualWikiId;
	}
}