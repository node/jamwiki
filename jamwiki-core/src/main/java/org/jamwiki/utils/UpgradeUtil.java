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
package org.jamwiki.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiConfiguration;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.WikiVersion;
import org.jamwiki.db.DatabaseUpgrades;
import org.jamwiki.db.WikiDatabase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiCache;
import org.jamwiki.utils.WikiLogger;

/**
 * This class contains logic for performing upgrades of the existing wiki
 * configuration to the current code's wiki version.
 */
public class UpgradeUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(UpgradeUtil.class.getName());
	/**
	 * When a search index update is needed it will run during the upgrade process
	 * unless the total number of wiki topics exceeds a this threshold.
	 */
	private static final int MAX_TOPICS_FOR_AUTOMATIC_UPDATE = 1000;

	/** List that will have messages appended to it to reflect upgrade actions. */
	private final List<WikiMessage> messages;
	/**
	 * Flag indicating whether an upgrade should actually be performed or whether
	 * messages about what would be upgraded are all that are needed.
	 */
	private final boolean performUpgrade;

	/**
	 * Initialize an instace of this class with a list that will have messages
	 * appended to it as the class is executed.
	 */
	public UpgradeUtil(List<WikiMessage> messages, boolean performUpgrade) {
		this.messages = messages;
		this.performUpgrade = performUpgrade;
	}

	/**
	 *
	 */
	public void upgrade(Locale locale, String ipAddress) throws WikiException {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		if (oldVersion.before(1, 2, 0)) {
			throw new WikiException(new WikiMessage("upgrade.error.oldversion", WikiVersion.CURRENT_WIKI_VERSION, "1.0.0"));
		}
		// first perform database upgrades
		if (this.upgradeDatabase() && !this.performUpgrade) {
			this.messages.add(new WikiMessage("upgrade.caption.database"));
		}
		// upgrade the search index if required & possible
		if (this.upgradeSearchIndex() && !this.performUpgrade) {
			WikiMessage searchWikiMessage = new WikiMessage("upgrade.caption.search");
			searchWikiMessage.addParam(Integer.toString(MAX_TOPICS_FOR_AUTOMATIC_UPDATE));
			searchWikiMessage.addWikiLinkParam("Special:Maintenance");
			this.messages.add(searchWikiMessage);
		}
		// upgrade stylesheet
		if (this.upgradeStyleSheet(locale, ipAddress) && !this.performUpgrade) {
			this.messages.add(new WikiMessage("upgrade.caption.stylesheet"));
		}
		this.messages.addAll(WikiUtil.validateSystemSettings(Environment.getInstance()));
		if (this.upgradeConfigXmlRequired() && !this.performUpgrade) {
			File file = null;
			try {
				file = WikiConfiguration.getInstance().retrieveConfigFile();
				this.messages.add(new WikiMessage("upgrade.caption.config", file.getAbsolutePath()));
			} catch (IOException e) {
				logger.warn("Unable to retrieve configuration file location", e);
			}
		}
		if (this.performUpgrade) {
			try {
				Environment.setValue(Environment.PROP_BASE_WIKI_VERSION, WikiVersion.CURRENT_WIKI_VERSION);
				Environment.saveConfiguration();
				// reset data handler and other instances.  this probably hides a bug
				// elsewhere since no reset should be needed, but it's anyone's guess
				// where that might be...
				WikiBase.reload();
			} catch (Exception e) {
				logger.error("Failure during upgrade while saving properties and executing WikiBase.reload()", e);
				throw new WikiException(new WikiMessage("upgrade.error.nonfatal", e.toString()));
			}
		}
	}

	/**
	 *
	 */
	private boolean upgradeConfigXmlRequired() {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		return (oldVersion.before(1, 3, 0));
	}

	/**
	 * Determine whether or not a database upgrade is required, and if one is
	 * required and the flag to perform it is true, upgrade the database.
	 *
	 * @return <code>true</code> if a database upgrade is needed, <code>false</code>
	 *  if no upgrade is required.
	 */
	private boolean upgradeDatabase() throws WikiException {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		boolean upgradeRequired = (oldVersion.before(1, 3, 0));
		if (upgradeRequired && this.performUpgrade) {
			DatabaseUpgrades.upgrade130(this.messages);
			// Flush connection pool to manage database schema change
			WikiDatabase.initialize();
			WikiCache.initialize();
		}
		return upgradeRequired;
	}

	/**
	 * Determine whether or not a search index upgrade is required, and if one
	 * is required and the flag to perform it is true, upgrade the search index.
	 *
	 * @return <code>true</code> if a search index upgrade is needed,
	 *  <code>false</code> if no upgrade is required.
	 */
	private boolean upgradeSearchIndex() {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		boolean upgradeRequired = (oldVersion.before(2, 0, 0));
		if (upgradeRequired && this.performUpgrade) {
			try {
				int topicCount = WikiBase.getDataHandler().lookupTopicCount(VirtualWiki.defaultVirtualWiki().getName(), null);
				if (topicCount < MAX_TOPICS_FOR_AUTOMATIC_UPDATE) {
					// refresh search engine
					WikiBase.getSearchEngine().refreshIndex();
					this.messages.add(new WikiMessage("upgrade.message.search.refresh"));
				} else {
					// print a message telling the user to do this step manually
					WikiMessage searchWikiMessage = new WikiMessage("upgrade.error.search.refresh");
					searchWikiMessage.addWikiLinkParam("Special:Maintenance");
					this.messages.add(searchWikiMessage);
				}
			} catch (Exception e) {
				logger.warn("Failure during upgrade while rebuilding search index.  Please use the tools on the Special:Maintenance page to complete this step.", e);
				this.messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
			}
		}
		return upgradeRequired;
	}

	/**
	 * Determine whether or not the system stylesheet needs to be upgrade, and
	 * if an upgrade is required and the flag to perform it is true, upgrade the
	 * stylesheet.
	 *
	 * @param locale The upgrading user's locale.
	 * @param ipAddress The upgrading users's IP address.
	 * @return <code>true</code> if a stylesheet upgrade is needed,
	 *  <code>false</code> if no upgrade is required.
	 */
	private boolean upgradeStyleSheet(Locale locale, String ipAddress) {
		WikiVersion oldVersion = new WikiVersion(Environment.getValue(Environment.PROP_BASE_WIKI_VERSION));
		boolean upgradeRequired = (oldVersion.before(1, 3, 0));
		if (upgradeRequired && this.performUpgrade) {
			try {
				List<VirtualWiki> virtualWikis = WikiBase.getDataHandler().getVirtualWikiList();
				for (VirtualWiki virtualWiki : virtualWikis) {
					WikiBase.getDataHandler().updateSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_SYSTEM_CSS, ipAddress);
					this.messages.add(new WikiMessage("upgrade.message.stylesheet.success", virtualWiki.getName()));
				}
			} catch (WikiException e) {
				logger.warn("Failure while updating JAMWiki stylesheet", e);
				this.messages.add(e.getWikiMessage());
				this.messages.add(new WikiMessage("upgrade.message.stylesheet.failure",  e.getMessage()));
			} catch (DataAccessException e) {
				logger.warn("Failure while updating JAMWiki stylesheet", e);
				this.messages.add(new WikiMessage("upgrade.message.stylesheet.failure",  e.getMessage()));
			}
		}
		return upgradeRequired;
	}
}
