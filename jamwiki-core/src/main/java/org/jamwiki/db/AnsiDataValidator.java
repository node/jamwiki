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
package org.jamwiki.db;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Category;
import org.jamwiki.model.LogItem;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Role;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.UserBlock;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiGroup;
import org.jamwiki.model.WikiUser;
import org.jamwiki.model.WikiUserDetails;

/**
 * Utility methods for validating data prior to committing it to the
 * database.
 */
public class AnsiDataValidator {

	/**
	 * Verify that a string does not exceed a specified maximum, throwing
	 * an exception if it does.
	 */
	private void checkLength(String value, int maxLength) throws WikiException {
		if (value != null && value.length() > maxLength) {
			throw new WikiException(new WikiMessage("error.fieldlength", value, Integer.valueOf(maxLength).toString()));
		}
	}

	/**
	 * Validate that all fields of a role object are valid for the
	 * database.
	 */
	protected void validateAuthority(String role) throws WikiException {
		checkLength(role, 30);
	}

	/**
	 * Validate that all fields of a Category object are valid for the
	 * database.
	 */
	protected void validateCategory(Category category) throws WikiException {
		checkLength(category.getName(), 200);
		checkLength(category.getSortKey(), 200);
	}

	/**
	 * Validate that all fields of a configuration object are valid for the
	 * database.
	 */
	protected void validateConfiguration(Map<String, String> configuration) throws WikiException {
		for (Map.Entry<String, String> entry : configuration.entrySet()) {
			checkLength(entry.getKey(), 50);
			checkLength(entry.getValue(), 500);
		}
	}

	/**
	 * Validate that all fields of a LogItem object are valid for the
	 * database.
	 */
	protected void validateLogItem(LogItem logItem) throws WikiException {
		checkLength(logItem.getUserDisplayName(), 200);
		checkLength(logItem.getLogParamString(), 500);
		logItem.setLogComment(StringUtils.substring(logItem.getLogComment(), 0, 200));
	}

	/**
	 * Validate that all fields of a Namespace object are valid for the
	 * database.
	 */
	protected void validateNamespace(Namespace namespace) throws WikiException {
		checkLength(namespace.getDefaultLabel(), 200);
	}

	/**
	 * Validate that all fields of a namespace translation object are valid
	 * for the database.
	 */
	protected void validateNamespaceTranslation(Namespace namespace, String virtualWiki) throws WikiException {
		checkLength(namespace.getLabel(virtualWiki), 200);
	}

	/**
	 * Validate that all fields of a RecentChange object are valid for the
	 * database.
	 */
	protected void validateRecentChange(RecentChange change) throws WikiException {
		checkLength(change.getTopicName(), 200);
		checkLength(change.getAuthorName(), 200);
		checkLength(change.getVirtualWiki(), 100);
		change.setChangeComment(StringUtils.substring(change.getChangeComment(), 0, 200));
		checkLength(change.getParamString(), 500);
	}

	/**
	 * Validate that all fields of a Role object are valid for the
	 * database.
	 */
	protected void validateRole(Role role) throws WikiException {
		checkLength(role.getAuthority(), 30);
		role.setDescription(StringUtils.substring(role.getDescription(), 0, 200));
	}

	/**
	 * Validate that all fields of a Topic object are valid for the
	 * database.
	 */
	protected void validateTopic(Topic topic) throws WikiException {
		checkLength(topic.getName(), 200);
		checkLength(topic.getRedirectTo(), 200);
	}

	/**
	 * Validate that all fields of a TopicVersion object are valid for the
	 * database.
	 */
	protected void validateTopicVersion(TopicVersion topicVersion) throws WikiException {
		checkLength(topicVersion.getAuthorDisplay(), 100);
		checkLength(topicVersion.getVersionParamString(), 500);
		topicVersion.setEditComment(StringUtils.substring(topicVersion.getEditComment(), 0, 200));
	}

	/**
	 * Validate that all fields of a UserBlock object are valid for the
	 * database.
	 */
	protected void validateUserBlock(UserBlock userBlock) throws WikiException {
		checkLength(userBlock.getBlockReason(), 200);
		checkLength(userBlock.getUnblockReason(), 200);
	}

	/**
	 * Validate that all fields of a WikiUserDetails object are valid for
	 * the database.
	 */
	protected void validateUserDetails(WikiUserDetails userDetails) throws WikiException {
		checkLength(userDetails.getUsername(), 100);
		// do not throw exception containing password info
		if (userDetails.getPassword() != null && userDetails.getPassword().length() > 100) {
			throw new WikiException(new WikiMessage("error.fieldlength", "-", "100"));
		}
	}

	/**
	 * Validate that all fields of a VirtualWiki object are valid for the
	 * database.
	 */
	protected void validateVirtualWiki(VirtualWiki virtualWiki) throws WikiException {
		checkLength(virtualWiki.getName(), 100);
		checkLength(virtualWiki.getRootTopicName(), 200);
		checkLength(virtualWiki.getLogoImageUrl(), 200);
		checkLength(virtualWiki.getMetaDescription(), 500);
		checkLength(virtualWiki.getSiteName(), 200);
	}

	/**
	 * Validate that all fields of a watchlist object are valid for the
	 * database.
	 */
	protected void validateWatchlistEntry(String topicName) throws WikiException {
		checkLength(topicName, 200);
	}

	/**
	 * Validate that all fields of a WikiFile object are valid for the
	 * database.
	 */
	protected void validateWikiFile(WikiFile wikiFile) throws WikiException {
		checkLength(wikiFile.getFileName(), 200);
		checkLength(wikiFile.getUrl(), 200);
		checkLength(wikiFile.getMimeType(), 100);
	}

	/**
	 * Validate that all fields of a WikiFileVersion object are valid for
	 * the database.
	 */
	protected void validateWikiFileVersion(WikiFileVersion wikiFileVersion) throws WikiException {
		checkLength(wikiFileVersion.getUrl(), 200);
		checkLength(wikiFileVersion.getMimeType(), 100);
		checkLength(wikiFileVersion.getAuthorDisplay(), 100);
		wikiFileVersion.setUploadComment(StringUtils.substring(wikiFileVersion.getUploadComment(), 0, 200));
	}

	/**
	 * Validate that all fields of a WikiGroup object are valid for the
	 * database.
	 */
	protected void validateWikiGroup(WikiGroup group) throws WikiException {
		checkLength(group.getName(), 30);
		group.setDescription(StringUtils.substring(group.getDescription(), 0, 200));
	}

	/**
	 * Validate that all fields of a WikiUser object are valid for the
	 * database.
	 */
	protected void validateWikiUser(WikiUser user) throws WikiException {
		checkLength(user.getUsername(), 100);
		checkLength(user.getDisplayName(), 100);
		checkLength(user.getCreateIpAddress(), 39);
		checkLength(user.getLastLoginIpAddress(), 39);
		checkLength(user.getDefaultLocale(), 8);
		checkLength(user.getEmail(), 100);
		// loop through preferences and check length
		for (String preference : user.getPreferences().values()) {
			checkLength(preference, 250);
		}
	}
}
