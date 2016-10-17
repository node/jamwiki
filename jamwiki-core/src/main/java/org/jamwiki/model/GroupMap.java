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
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is a container for managing groups and their members. It can be
 * instantiated in two different modes:
 * <ul>
 * <li>As a group containing members.</li>
 * <li>As a member with a list of the groups he belongs to.</li>
 * </ul>
 *  * @author cclavadetscher
 */
public class GroupMap implements Serializable {

	/**
	 * Constant for a GroupMap representing a group
	 */
	public static final int GROUP_MAP_GROUP = 1;
	/**
	 * Constant for a GroupMap representing a user
	 */
	public static final int GROUP_MAP_USER  = 2;

	/**
	 * The type of the GroupMap. Valid values are GroupMap.GROUP_MAP_GROUP
	 * and GroupMap.GROUP_MAP_USER. The value is set by the costructor depending
	 * on which instance type of GroupMap is created.
	 */
	private int groupMapType = -1;

	/**
	 * Attributes when acting as a group containing users
	 */
	private int groupId = -1;
	private List<String> groupMembers = null;

	/**
	 * Attributes when acting as a user belonging to groups
	 */
	private String userLogin = null;
	private List<Integer> groupIds = null;

	private List<String> roleNames = null;

	/**
	 * Constructor when acting as a group containing users
	 * @param groupId The id of the group
	 */
	public GroupMap(int groupId) {
		this.groupMapType = this.GROUP_MAP_GROUP;
		this.groupId = groupId;
		this.groupMembers = new ArrayList<String>();
	}

	/**
	 * Constructor when acting as a user contained in groups
	 * @param userLogin The login String of the user
	 */
	public GroupMap(String userLogin) {
		this.groupMapType = this.GROUP_MAP_USER;
		this.userLogin = userLogin;
		this.groupIds = new ArrayList<Integer>();
	}

	/**
	 *
	 */
	public int getGroupMapType() {
		return groupMapType;
	}

	/**
	 *
	 */
	public void setGroupMapType(int groupMapType) {
		this.groupMapType = groupMapType;
	}

	/**
	 *
	 */
	public int getGroupId() {
		return groupId;
	}

	/**
	 *
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	/**
	 *
	 */
	public List<String> getGroupMembers() {
		return groupMembers;
	}

	/**
	 *
	 */
	public void setGroupMembers(List<String> groupMembers) {
		this.groupMembers = groupMembers;
	}

	/**
	 *
	 */
	public String getUserLogin() {
		return userLogin;
	}

	/**
	 *
	 */
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	/**
	 *
	 */
	public List<Integer> getGroupIds() {
		if (this.groupIds == null) {
			return new ArrayList<Integer>();
		}
		return groupIds;
	}

	/**
	 *
	 */
	public void setGroupIds(List<Integer> groupIds) {
		this.groupIds = groupIds;
	}

	/**
	 *
	 */
	public void setRoleNames(List<String> roleNames) {
		this.roleNames = roleNames;
	}

	/**
	 *
	 */
	 public List<String> getRoleNames() {
		if (this.roleNames == null) {
			return new ArrayList<String>();
		}
		return this.roleNames;
	}

	/**
	 *
	 */
	public HashMap getGroupIdMap() {
		HashMap<Integer, String> hMap = new HashMap<Integer, String>();
		for (Integer groupId : groupIds) {
			hMap.put(groupId, groupId.toString());
		}
		return hMap;
	}

	/**
	 *
	 */
	public HashMap getGroupMembersMap() {
		HashMap<String, String> hMap = new HashMap<String, String>();
		for (String userName : groupMembers) {
			hMap.put(userName, userName);
		}
		return hMap;
	}

	/**
	 *
	 */
	public HashMap<String, String> getGroupMapRoles() {
		HashMap<String, String> hMap = new HashMap<String, String>();
		if (roleNames != null) {
			for (String roleName : roleNames) {
				hMap.put(roleName, roleName);
			}
		}
		return hMap;
	}

	/**
	 * Returns a String representation of this GroupMap.
	 * @return The String representation of this GroupMap
	 */
	public String toString() {
		switch (groupMapType) {
			case GROUP_MAP_GROUP: {
				return new String("GroupType: GROUP; GroupId: " + groupId + "; GroupMembers: " + groupMembers + "; groupMapRoles: " + roleNames);
			}
			case GROUP_MAP_USER: {
				return new String("GroupType: USER; userLogin: " + userLogin + "; Groups: " + groupIds);
			}
			default: {
				return new String("GroupType: UNDEFINED");
			}
		}
	}
}
