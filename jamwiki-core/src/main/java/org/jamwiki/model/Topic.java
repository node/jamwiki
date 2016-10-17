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
import java.sql.Timestamp;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.parser.WikiLink;

/**
 * Provides an object representing a Wiki topic.
 */
public class Topic implements Serializable {

	// FIXME - consider making this an ACL (more flexible)
	private boolean adminOnly = false;
	private Integer currentVersionId;
	private Timestamp deleteDate;
	private Namespace namespace;
	/** Page name is the topic name without the namespace.  For example, if the topic name is "Help:Help Page" the page name is "Help Page". */
	private String pageName;
	private boolean readOnly = false;
	private String redirectTo;
	private String topicContent;
	private int topicId = -1;
	private TopicType topicType = TopicType.ARTICLE;
	private String virtualWiki;

	/**
	 * Initialize a topic, passing in the virtual wiki, namespace and page name.  Note
	 * that page name does NOT include namespace, so for a topic of "Help:Help Page"
	 * the page name is "Help Page".
	 */
	public Topic(String virtualWiki, Namespace namespace, String pageName) {
		this.virtualWiki = virtualWiki;
		this.namespace = namespace;
		this.pageName = pageName;
	}

	/**
	 *
	 */
	public Topic(Topic topic) {
		this.adminOnly = topic.adminOnly;
		this.currentVersionId = topic.currentVersionId;
		this.deleteDate = topic.deleteDate;
		this.namespace = topic.namespace;
		this.pageName = topic.pageName;
		this.readOnly = topic.readOnly;
		this.redirectTo = topic.redirectTo;
		this.topicContent = topic.topicContent;
		this.topicId = topic.topicId;
		this.topicType = topic.topicType;
		this.virtualWiki = topic.virtualWiki;
	}

	/**
	 *
	 */
	public boolean getAdminOnly() {
		return this.adminOnly;
	}

	/**
	 *
	 */
	public void setAdminOnly(boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

	/**
	 *
	 */
	public Integer getCurrentVersionId() {
		return this.currentVersionId;
	}

	/**
	 *
	 */
	public void setCurrentVersionId(Integer currentVersionId) {
		this.currentVersionId = currentVersionId;
	}

	/**
	 *
	 */
	public boolean getDeleted() {
		return (this.deleteDate != null);
	}

	/**
	 *
	 */
	public Timestamp getDeleteDate() {
		return this.deleteDate;
	}

	/**
	 *
	 */
	public void setDeleteDate(Timestamp deleteDate) {
		this.deleteDate = deleteDate;
	}

	/**
	 * Return the full topic name, including namespace.  Example: "Help:Help Page".
	 */
	public String getName() {
		String name = this.pageName;
		if (!StringUtils.isBlank(this.namespace.getLabel(this.virtualWiki))) {
			name = this.namespace.getLabel(this.virtualWiki) + Namespace.SEPARATOR + this.pageName;
		}
		return name;
	}

	/**
	 * Set the full topic name, including namespace.  Example: "Help:Help Page".
	 */
	public void setName(String name) {
		WikiLink wikiLink = new WikiLink(null, this.virtualWiki, name);
		this.namespace = wikiLink.getNamespace();
		this.pageName = wikiLink.getArticle();
	}

	/**
	 *
	 */
	public Namespace getNamespace() {
		return this.namespace;
	}

	/**
	 *
	 */
	public String getPageName() {
		return this.pageName;
	}

	/**
	 *
	 */
	public boolean getReadOnly() {
		return this.readOnly;
	}

	/**
	 *
	 */
	public String getRedirectTo() {
		return this.redirectTo;
	}

	/**
	 *
	 */
	public void setRedirectTo(String redirectTo) {
		this.redirectTo = redirectTo;
	}

	/**
	 *
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 *
	 */
	public String getTopicContent() {
		return this.topicContent;
	}

	/**
	 *
	 */
	public void setTopicContent(String topicContent) {
		this.topicContent = topicContent;
	}

	/**
	 *
	 */
	public int getTopicId() {
		return this.topicId;
	}

	/**
	 *
	 */
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	/**
	 *
	 */
	public TopicType getTopicType() {
		return this.topicType;
	}

	/**
	 *
	 */
	public void setTopicType(TopicType topicType) {
		this.topicType = topicType;
	}

	/**
	 *
	 */
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 * Utility method for generating a topic name from a namespace and a page name.
	 */
	public static String buildTopicName(String virtualWiki, Namespace namespace, String pageName) {
		String topicName = namespace.getLabel(virtualWiki);
		if (topicName.length() != 0) {
			topicName += Namespace.SEPARATOR;
		}
		topicName += pageName;
		return topicName;
	}
}