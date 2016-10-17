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

/**
 * Provides an object representing a Wiki group.
 */
public class WikiGroup implements Serializable {

	private String description;
	private int groupId;
	private final String name;

	public static final String GROUP_ANONYMOUS = "GROUP_ANONYMOUS";
	public static final String GROUP_REGISTERED_USER = "GROUP_REGISTERED_USER";

	/**
	 * @param name The group name.  Group names will be forced to uppercase.
	 */
	public WikiGroup(String name) {
		this.name = ((name == null) ? null : name.toUpperCase());
	}

	/**
	 *
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 *
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 *
	 */
	public int getGroupId() {
		return this.groupId;
	}

	/**
	 *
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	/**
	 * Retrieve the group name.  Group names are returned in uppercase.
	 */
	public String getName() {
		return (this.name == null) ? null : this.name.toUpperCase();
	}
}