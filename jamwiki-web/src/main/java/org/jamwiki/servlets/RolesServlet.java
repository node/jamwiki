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
package org.jamwiki.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Role;
import org.jamwiki.model.RoleMap;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.model.GroupMap;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class RolesServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(RolesServlet.class.getName());
	/** The name of the JSP file used to render the servlet output when searching. */
	protected static final String JSP_ADMIN_ROLES = "admin-roles.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String function = request.getParameter("function");
		if (StringUtils.isBlank(function)) {
			view(request, next, pageInfo);
		} else if (function.equals("modifyRole")) {
			modifyRole(request, next, pageInfo);
		} else if (function.equals("searchRole")) {
			searchRole(request, next, pageInfo);
		} else if (function.equals("assignRole")) {
			assignRole(request, next, pageInfo);
		} else if (function.equals("modifyGroup")) {
			modifyGroup(request, next, pageInfo);
		}
		return next;
	}
	
	/**
	 * Utility method for converting a processing an array of "userid|groupid|role" values
	 * into a List of roles for a specific id value.
	 *
	 * @return A List of role names for the given id, or an empty
	 *  List if no matching values are found.
	 */
	private static List<String> buildRoleArray(int userId, int groupId, String[] valueArray) {
		List<String> results = new ArrayList<String>();
		if (valueArray == null) {
			return results;
		}
		for (int i = 0; i < valueArray.length; i++) {
			String[] tokens = valueArray[i].split("\\|");
			String parsedUserId = tokens[0];
			int userInt = -1;
			try {
				userInt = Integer.parseInt(parsedUserId);
			} catch (Exception ignore) {}
			String parsedGroupId = tokens[1];
			int groupInt = -1;
			try {
				groupInt = Integer.parseInt(parsedGroupId);
			} catch (Exception ignore) {}
			String valueRole = tokens[2];
			if ((userId > 0 && userId == userInt) || (groupId > 0 && groupId == groupInt)) {
				results.add(valueRole);
			}
		}
		return results;
	}
	
	/**
	 * Utility method to create a GroupMap from a userLogin and the array of groups
	 * the user belongs to. The format of the array elements is "userLogin|groupId"
	 * 
	 * @param userLogin The login String of the user
	 * @param groupIds The array of groups the user belongs to
	 */
	private GroupMap buildGroupMap(String userLogin,String[] groupIds) {
		GroupMap groupMap = new GroupMap(userLogin);
		List<Integer> groupIdsList = new ArrayList<Integer>();
		for (int i=0;i<groupIds.length;i++) {
			String[] tokens = groupIds[i].split("\\|");
			if (tokens[0].equals(userLogin)) {
				groupIdsList.add(new Integer(tokens[1]));
			}
		}
		groupMap.setGroupIds(groupIdsList);
		return groupMap;
	}

	/**
	 *
	 */
	private void assignRole(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// the way this works is that there will be an array of candidate
		// groups - these are all groups that could have been modified.  there
		// will also be a groupRole array containing values of the form
		// "userid|groupid|role".  process both, deleting all old roles for the
		// candidate group array and adding the new roles in the groupRole
		// array.
		try {
			String[] candidateGroups = request.getParameterValues("candidateGroup");
			String[] groupRoles = request.getParameterValues("groupRole");
			if (candidateGroups != null) {
				for (int i = 0; i < candidateGroups.length; i++) {
					int groupId = Integer.parseInt(candidateGroups[i]);
					List<String> roles = buildRoleArray(-1, groupId, groupRoles);
					WikiBase.getDataHandler().writeRoleMapGroup(groupId, roles);
				}
				pageInfo.addMessage(new WikiMessage("roles.message.grouproleupdate"));
			}
			// now do the same for user roles.
			String[] candidateUsers = request.getParameterValues("candidateUser");
			String[] candidateUsernames = request.getParameterValues("candidateUsername");
			String[] userRoles = request.getParameterValues("userRole");
			String[] userGroups = request.getParameterValues("userGroup");
			if (candidateUsers != null) {
				for (int i = 0; i < candidateUsers.length; i++) {
					int userId = Integer.parseInt(candidateUsers[i]);
					String username = candidateUsernames[i];
					List<String> roles = buildRoleArray(userId, -1, userRoles);
					if (userId == ServletUtil.currentWikiUser().getUserId() && !roles.contains(Role.ROLE_SYSADMIN.getAuthority())) {
						pageInfo.addError(new WikiMessage("roles.message.sysadminremove"));
						roles.add(Role.ROLE_SYSADMIN.getAuthority());
					}
					WikiBase.getDataHandler().writeRoleMapUser(username, roles);
					// handle group assignments
					GroupMap groupMap = null;
					if (userGroups != null) {
						groupMap = buildGroupMap(username,userGroups);
						// We must delete the groups GROUP_ANONYMOUS and GROUP_REGISTERED_USER
						// if available to avoid duplicates in the datase
						groupMap.getGroupIds().remove(new Integer(1));
						groupMap.getGroupIds().remove(new Integer(2));
					}
					else {
						groupMap = new GroupMap(username); 
					}
					WikiBase.getDataHandler().writeGroupMap(groupMap);
				}
				pageInfo.addMessage(new WikiMessage("roles.message.userroleupdate"));
			}
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		} catch (Exception e) {
			logger.error("Failure while adding role", e);
			pageInfo.addError(new WikiMessage("roles.message.rolefail", e.getMessage()));
		}
		this.searchRole(request, next, pageInfo);
		// this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void modifyRole(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String updateRole = request.getParameter("updateRole");
		Role role = null;
		if (!StringUtils.isBlank(request.getParameter("Submit"))) {
			try {
				// once created a role name cannot be modified, so the text field
				// will be disabled in the form.
				boolean update = StringUtils.isBlank(request.getParameter("roleName"));
				String roleName = (update) ? updateRole : request.getParameter("roleName");
				role = new Role(roleName);
				role.setDescription(request.getParameter("roleDescription"));
				WikiUtil.validateRole(role);
				WikiBase.getDataHandler().writeRole(role, update);
				if (!StringUtils.isBlank(updateRole) && updateRole.equals(role.getAuthority())) {
					pageInfo.addMessage(new WikiMessage("roles.message.roleupdated", role.getAuthority()));
				} else {
					pageInfo.addMessage(new WikiMessage("roles.message.roleadded", role.getAuthority()));
				}
			} catch (WikiException e) {
				pageInfo.addError(e.getWikiMessage());
				role = null;
			} catch (Exception e) {
				role = null;
				logger.error("Failure while adding role", e);
				pageInfo.addError(new WikiMessage("roles.message.rolefail", e.getMessage()));
			}
		} else if (!StringUtils.isBlank(updateRole)) {
			// FIXME - use a cached list of roles instead of iterating
			// load details for the selected role
			List<Role> roles = WikiBase.getDataHandler().getAllRoles();
			for (Role tempRole : roles) {
				if (tempRole.getAuthority().equals(updateRole)) {
					role = tempRole;
				}
			}
		}
		if (role != null) {
			next.addObject("roleName", role.getAuthority());
			next.addObject("roleDescription", role.getDescription());
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void searchRole(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		try {
			String includeInheritedRoles = request.getParameter("includeInheritedRoles");
			Boolean test = new Boolean(includeInheritedRoles == null?false:true);
			next.addObject("includeInheritedRoles",test);
			String searchLogin = request.getParameter("searchLogin");
			String searchRole  = request.getParameter("searchRole");
			String searchGroup = request.getParameter("searchGroup");
			List<RoleMap> roleMapUsers = null;
			HashMap<String,GroupMap> groupMaps = new HashMap<String,GroupMap>();
			if (!StringUtils.isBlank(searchLogin)) {
				roleMapUsers = WikiBase.getDataHandler().getRoleMapByLogin(searchLogin);
				next.addObject("searchLogin", searchLogin);
			} else if (!StringUtils.isBlank(searchRole)) {
				roleMapUsers = WikiBase.getDataHandler().getRoleMapByRole(searchRole,test.booleanValue());
				next.addObject("searchRole", searchRole);
			} else if (!StringUtils.isBlank(searchGroup)) {
				GroupMap groupMap = WikiBase.getDataHandler().getGroupMapGroup(Integer.valueOf(searchGroup));
				roleMapUsers = new ArrayList<RoleMap>();
				List<String> groupMembers = groupMap.getGroupMembers();
				for (String userLogin : groupMembers) {
					List<RoleMap> roleMapUser = WikiBase.getDataHandler().getRoleMapByLogin(userLogin);
					// The userLogin is unique. We can assume that there is only one item in the list.
					roleMapUsers.add(roleMapUser.get(0));
				}
				next.addObject("searchGroup",searchGroup);
			}
			// Add group lists of users
			if (roleMapUsers != null) {
				for (RoleMap roleMap : roleMapUsers) {
					groupMaps.put(roleMap.getUserLogin(),WikiBase.getDataHandler().getGroupMapUser(roleMap.getUserLogin()));
				}
			}
			next.addObject("groupMaps",groupMaps);
			next.addObject("roleMapUsers", roleMapUsers);
		} catch (Exception e) {
			logger.error("Failure while retrieving role", e);
			pageInfo.addMessage(new WikiMessage("roles.message.rolesearchfail", e.getMessage()));
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void modifyGroup(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String updateGroup = request.getParameter("updateGroup");
		WikiGroup group = null;
		if (!StringUtils.isBlank(request.getParameter("Submit"))) {
			try {
				// once created a group name cannot be modified, so the text field
				// will be disabled in the form.
				boolean update = StringUtils.isBlank(request.getParameter("groupName"));
				String groupName = (update) ? updateGroup : request.getParameter("groupName");
				if (!StringUtils.isBlank(groupName)) {
					group = WikiBase.getDataHandler().lookupWikiGroup(groupName);
				}
				if (group == null) {
					group = new WikiGroup(groupName);
				}
				group.setDescription(request.getParameter("groupDescription"));
				WikiUtil.validateWikiGroup(group);
				WikiBase.getDataHandler().writeWikiGroup(group);
				if (update) {
					pageInfo.addMessage(new WikiMessage("group.message.groupupdated", group.getName()));
				} else {
					pageInfo.addMessage(new WikiMessage("group.message.groupadded", group.getName()));
				}
			} catch (WikiException e) {
				pageInfo.addError(e.getWikiMessage());
				group = null;
			} catch (Exception e) {
				group = null;
				logger.error("Failure while adding or modifying group", e);
				pageInfo.addError(new WikiMessage("group.message.groupfail", e.getMessage()));
			}
		}
		else if (!StringUtils.isBlank("updateGroup")) group = WikiBase.getDataHandler().lookupWikiGroup(updateGroup);
		if (group != null) {
			next.addObject("groupName", group.getName());
			next.addObject("groupDescription", group.getDescription());
		}
		this.view(request, next, pageInfo);
	}
	
	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		List<Role> roles = WikiBase.getDataHandler().getAllRoles();
		next.addObject("roles", roles);
		next.addObject("roleCount", roles.size());
		List<RoleMap> roleMapGroups = WikiBase.getDataHandler().getRoleMapGroups();
		next.addObject("roleMapGroups", roleMapGroups);
		List<WikiGroup> groups = WikiBase.getDataHandler().getAllWikiGroups();
		next.addObject("groups", groups);
		pageInfo.setAdmin(true);
		pageInfo.setContentJsp(JSP_ADMIN_ROLES);
		pageInfo.setPageTitle(new WikiMessage("roles.title"));
	}
}
