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
package org.jamwiki.parser;

import org.apache.commons.lang3.StringUtils;
import org.jamwiki.model.Interwiki;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.utils.Utilities;

/**
 * Utility method used in processing Wiki links.
 */
public class WikiLink {

	/**
	 * Virtual wiki link prefix, used when this link is to a virtual wiki other than the
	 * current virtual wiki.
	 */
	private VirtualWiki altVirtualWiki;
	/** Indicator that the link requires special handling, such as links starting with a colon. */
	private boolean colon = false;
	/** The servlet context path. */
	private final String contextPath;
	/** Article name, not including namespace. */
	private String article;
	/** Link destination, including namespace. */
	private String destination;
	/** Interwiki link prefix. */
	private Interwiki interwiki;
	/** Namespace prefix for the link. */
	private Namespace namespace;
	/** Link query paramters. */
	private String query;
	/** Link section (ie #section). */
	private String section;
	/** Link text. */
	private String text;
	/** The current link virtual wiki. */
	private final String virtualWiki;

	/**
	 * Standard constructor which initializes the destination field of
	 * the wiki link.
	 *
	 * @param contextPath The servlet context path.  If this value is
	 *  <code>null</code> then calling {@link #toRelativeUrl} will generate a
	 *  a URL without a context path, which breaks HTML links but is useful
	 *  for servlet redirection URLs.
	 * @param virtualWiki The current link virtual wiki.  This is the virtual
	 *  wiki that will be used when converting this WikiLink object to a
	 *  relative URL UNLESS there is an altVirtualWiki specified.
	 * @param destination The topic that this link points to, including
	 *  the namespace.  May  be <code>null</code> if (for example) this
	 *  link is to a section within the current article of the form
	 *  "#Section".  If this value is not <code>null</code> then it must be
	 *  clean - if it is prefixed with a virtual wiki, has a query string,
	 *  is encoded, or otherwise requires additional processing then this
	 *  method will not perform as expected.
	 */
	public WikiLink(String contextPath, String virtualWiki, String destination) {
		this.contextPath = contextPath;
		this.virtualWiki = virtualWiki;
		this.destination = destination;
		this.initialize(this.getDestination());
	}

	/**
	 * Constructor used by LinkUtil.parseWikiLink that allows that
	 * method to manually initialize most fields.
	 */
	protected WikiLink(String contextPath, String virtualWiki) {
		this.contextPath = contextPath;
		this.virtualWiki = virtualWiki;
	}

	/**
	 * Copy constructor.
	 */
	protected WikiLink(WikiLink wikiLink) {
		this.altVirtualWiki = wikiLink.altVirtualWiki;
		this.colon = wikiLink.colon;
		this.contextPath = wikiLink.contextPath;
		this.article = wikiLink.article;
		this.destination = wikiLink.destination;
		this.interwiki = wikiLink.interwiki;
		this.namespace = wikiLink.namespace;
		this.query = wikiLink.query;
		this.section = wikiLink.section;
		this.text = wikiLink.text;
		this.virtualWiki = wikiLink.virtualWiki;
	}

	/**
	 * Set the namespace and article fields for this wiki link based on the destination
	 * value.  Note that the destination must be clean - if it is prefixed with a
	 * virtual wiki, has a query string, is encoded, or otherwise requires additional
	 * processing then this method will not perform as expected.
	 *
	 * @throws IllegalStateException Thrown if a data access error occurs, which could
	 *  indicate a database failure or other system problem.
	 * @throws IllegalArgumentException Thrown if this method is called with a null
	 *  destination or virtual wiki.
	 */
	protected void initialize(String destination) throws IllegalStateException, IllegalArgumentException {
		String virtualWiki = (this.getAltVirtualWiki() == null) ? this.getVirtualWiki() : this.getAltVirtualWiki().getName();
		if (destination == null || virtualWiki == null) {
			throw new IllegalArgumentException("Cannot call WikiLink.initialize() with null destination or virtual wiki");
		}
		Namespace namespace = LinkUtil.retrieveTopicNamespace(virtualWiki, destination);
		String article = destination;
		if (namespace.getId() != Namespace.MAIN_ID) {
			int prefixPosition = LinkUtil.prefixPosition(destination);
			article = destination.substring(prefixPosition + Namespace.SEPARATOR.length()).trim();
		}
		this.setNamespace(namespace);
		this.setArticle(article);
	}

	/**
	 * Return the internal virtual wiki that this wiki link is linking to.  Note
	 * that this parameter is used when linking to a different virtual wiki and
	 * thus may be <code>null</code> when the link is to a topic in the same
	 * virtual wiki.
	 */
	public VirtualWiki getAltVirtualWiki() {
		return this.altVirtualWiki;
	}

	/**
	 * Set the internal virtual wiki that this wiki link is linking to.  Note
	 * that this parameter is used when linking to a different virtual wiki and
	 * thus may be <code>null</code> when the link is to a topic in the same
	 * virtual wiki.
	 */
	public void setAltVirtualWiki(VirtualWiki altVirtualWiki) {
		this.altVirtualWiki = altVirtualWiki;
	}

	/**
	 * Return the topic that this link points to, not including the namespace.
	 * May be <code>null</code> if (for example) this link is to a section within
	 * the current article of the form "#Section".
	 */
	public String getArticle() {
		return this.article;
	}

	/**
	 * Set the topic that this link points to, not including the namespace.
	 * May be <code>null</code> if (for example) this link is to a section within
	 * the current article of the form "#Section".
	 */
	public void setArticle(String article) {
		this.article = article;
	}

	/**
	 *
	 */
	public boolean getColon() {
		return this.colon;
	}

	/**
	 *
	 */
	public void setColon(boolean colon) {
		this.colon = colon;
	}

	/**
	 * Return the servlet context path for this wiki link.  In some cases this
	 * value may be initialized to <code>null</code>, if for example a redirection
	 * URL is being generated relative to the context root.
	 */
	public String getContextPath() {
		return this.contextPath;
	}

	/**
	 * Return the topic that this link points to, including the namespace.  May
	 * be <code>null</code> if (for example) this link is to a section within
	 * the current article of the form "#Section".
	 */
	public String getDestination() {
		return this.destination;
	}

	/**
	 * Set the topic that this link points to, including the namespace.  May
	 * be <code>null</code> if (for example) this link is to a section within
	 * the current article of the form "#Section".
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * Return the interwiki link object if this wiki link references is to
	 * a configured interwiki.
	 */
	public Interwiki getInterwiki() {
		return this.interwiki;
	}

	/**
	 * Set the interwiki link object if this wiki link references is to
	 * a configured interwiki.
	 */
	public void setInterwiki(Interwiki interwiki) {
		this.interwiki = interwiki;
	}

	/**
	 * Return the namespace object for this wiki link's target topic, or
	 * <code>null</code> if there is no target topic.
	 */
	public Namespace getNamespace() {
		if (this.namespace == null) {
			this.namespace = Namespace.namespace(Namespace.MAIN_ID);
		}
		return this.namespace;
	}

	/**
	 * Set the namespace object for this wiki link's target topic.
	 */
	public void setNamespace(Namespace namespace) {
		if (namespace == null) {
			throw new IllegalArgumentException("Namespace cannot be null");
		}
		this.namespace = namespace;
	}

	/**
	 * Return the query string that this link refers to ("?param=value").
	 * This method returns the query string without the opening "?", or
	 * <code>null</code> if there is no query string for this link.
	 */
	public String getQuery() {
		return this.query;
	}

	/**
	 * Set the query string that this link refers to ("?param=value").
	 * Minimal processing is done by this method to strip any opening "?"
	 * and to ensure that the supplied value has non-whitespace content.
	 */
	public void setQuery(String query) {
		if (!StringUtils.isBlank(query)) {
			if (query.trim().startsWith("?")) {
				query = query.trim().substring(1);
			}
			this.query = query;
		}
	}

	/**
	 * Return the section within a page that this link refers to ("#Section").
	 * This method returns the section name without the opening "#", or
	 * <code>null</code> if there is no section for this link.
	 */
	public String getSection() {
		return this.section;
	}

	/**
	 * Set the section within a page that this link refers to ("#Section").
	 * Minimal processing is done by this method to strip any opening "#"
	 * and to ensure that the supplied value has non-whitespace content.
	 */
	public void setSection(String section) {
		if (!StringUtils.isBlank(section)) {
			if (section.trim().startsWith("#")) {
				section = section.trim().substring(1);
			}
			this.section = section.trim();
		}
	}

	/**
	 * Utility method for determining if this wiki link is in the Special:
	 * namespace.
	 */
	public boolean isSpecial() {
		return this.getNamespace().getId().equals(Namespace.SPECIAL_ID);
	}

	/**
	 *
	 */
	public String getText() {
		return this.text;
	}

	/**
	 *
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * The current link virtual wiki.  This is the virtual wiki that will be used
	 * when converting this WikiLink object to a relative URL UNLESS there is an
	 * altVirtualWiki specified.
	 */
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 * Utility method for converting a WikiLink to a relative URL.  This method
	 * does NOT verify if the topic exists or if it is a "Special:" page, but
	 * simply returns a relative URL for the topic and virtual wiki.
	 *
	 * @return A relative URL for the wiki link.  Sample return values might be
	 *  of the form "/context/virtualwiki/Topic?param=value#Section", "#Section",
	 *  "/context/virtualwiki/Namespace:Topic", etc.
	 */
	public String toRelativeUrl() {
		if (StringUtils.isBlank(this.getDestination()) && !StringUtils.isBlank(this.getSection())) {
			return "#" + LinkUtil.buildAnchorText(this.getSection());
		}
		String linkVirtualWiki = ((this.getAltVirtualWiki() != null) ? this.getAltVirtualWiki().getName() : this.getVirtualWiki());
		StringBuilder url = new StringBuilder();
		if (this.getContextPath() != null) {
			url.append(this.getContextPath());
		}
		// context never ends with a "/" per servlet specification
		url.append('/');
		url.append(Utilities.encodeAndEscapeTopicName(linkVirtualWiki));
		url.append('/');
		url.append(Utilities.encodeAndEscapeTopicName(this.getDestination()));
		if (!StringUtils.isBlank(this.getQuery())) {
			url.append('?');
			url.append(this.getQuery());
		}
		if (!StringUtils.isBlank(this.getSection())) {
			url.append('#');
			url.append(LinkUtil.buildAnchorText(this.getSection()));
		}
		return url.toString();
	}
}
