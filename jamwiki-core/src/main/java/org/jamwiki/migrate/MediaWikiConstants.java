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
package org.jamwiki.migrate;

import java.util.Map;
import java.util.TreeMap;
import org.jamwiki.model.Namespace;

/**
 * Constants needed for Mediawiki import/export functionality.
 */
public final class MediaWikiConstants {

	static final String MEDIAWIKI_ELEMENT_NAMESPACE = "namespace";
	static final String MEDIAWIKI_ELEMENT_TOPIC = "page";
	static final String MEDIAWIKI_ELEMENT_TOPIC_CONTENT = "text";
	static final String MEDIAWIKI_ELEMENT_TOPIC_NAME = "title";
	static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION = "revision";
	static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_COMMENT = "comment";
	static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_MINOR_EDIT = "minor";
	static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_EDIT_DATE = "timestamp";
	static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_IP = "ip";
	static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_USERNAME = "username";
	// the Mediawiki XML file uses ISO 8601 format for dates
	static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	static final Map<Integer, String> MEDIAWIKI_NAMESPACE_MAP = new TreeMap<Integer, String>();
	static {
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.MEDIA_ID, "Media");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.SPECIAL_ID, "Special");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.MAIN_ID, "");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.COMMENTS_ID, "Talk");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.USER_ID, "User");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.USER_COMMENTS_ID, "User talk");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.FILE_ID, "File");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.FILE_COMMENTS_ID, "File talk");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.JAMWIKI_ID, "Mediawiki");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.JAMWIKI_COMMENTS_ID, "Mediawiki talk");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.TEMPLATE_ID, "Template");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.TEMPLATE_COMMENTS_ID, "Template talk");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.HELP_ID, "Help");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.HELP_COMMENTS_ID, "Help talk");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.CATEGORY_ID, "Category");
		MEDIAWIKI_NAMESPACE_MAP.put(Namespace.CATEGORY_COMMENTS_ID, "Category talk");
	}

	/**
	 * Private constructor to prevent any instance of this class from ever being instantiated.
	 */
	private MediaWikiConstants() {
	}
}
