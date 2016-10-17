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

import java.util.EnumSet;

/**
 * Allowable type values for topics.
 */
public enum TopicType {

	/* Standard topic type. */
	ARTICLE(1),
	/* Topic redirects to another topic. */
	REDIRECT(2),
	/* Topic is an image. */
	IMAGE(4),
	/* Topic is a category. */
	CATEGORY(5),
	/* Topic is a non-image file. */
	FILE(6),
	/* Internal files, do not display on Special:AllPages */
	SYSTEM_FILE(7),
	/* Wiki templates. */
	TEMPLATE(8);

	private final int id;

	/**
	 *
	 */
	private TopicType(int id) {
		this.id = id;
	}

	/**
	 *
	 */
	public int id() {
		return this.id;
	}

	/**
	 *
	 */
	public static TopicType findTopicType(int id) {
		for (TopicType topicType : EnumSet.allOf(TopicType.class)) {
			if (topicType.id == id) {
				return topicType;
			}
		}
		return null;
	}
}