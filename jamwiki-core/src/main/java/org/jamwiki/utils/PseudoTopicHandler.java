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

import java.util.ArrayList;
import java.util.List;

/**
 * Class for controlling "pseudotopics". A pseudotopic is a topic name that maps to
 * an internal Wikk page, such as Special:RecentChanges and Special:Edit.
 */
public class PseudoTopicHandler {

	private static List<String> PSEUDO_TOPICS;

	/** Logger */
	private static final WikiLogger logger = WikiLogger.getLogger(PseudoTopicHandler.class.getName());

	/**
	 *
	 */
	private PseudoTopicHandler() {
	}

	/**
	 *
	 */
	private static void init() {
		// TODO - it should be possible to read these values from the SimpleUrlHandlerMapping,
		// although at present there is no way to do so outside of the context of a webapp.
		// A future enhancement might be to use a shared property file for configuration
		// of both.
		PSEUDO_TOPICS = new ArrayList<String>();
		PSEUDO_TOPICS.add("jamwiki.css");
		PSEUDO_TOPICS.add("Special:Account");
		PSEUDO_TOPICS.add("Special:Admin");
		PSEUDO_TOPICS.add("Special:AllPages");
		PSEUDO_TOPICS.add("Special:Allpages");
		PSEUDO_TOPICS.add("Special:Block");
		PSEUDO_TOPICS.add("Special:BlockList");
		PSEUDO_TOPICS.add("Special:Blocklist");
		PSEUDO_TOPICS.add("Special:Categories");
		PSEUDO_TOPICS.add("Special:Contributions");
		PSEUDO_TOPICS.add("Special:Diff");
		PSEUDO_TOPICS.add("Special:Edit");
		PSEUDO_TOPICS.add("Special:Export");
		PSEUDO_TOPICS.add("Special:FileList");
		PSEUDO_TOPICS.add("Special:Filelist");
		PSEUDO_TOPICS.add("Special:History");
		PSEUDO_TOPICS.add("Special:ImageList");
		PSEUDO_TOPICS.add("Special:Imagelist");
		PSEUDO_TOPICS.add("Special:Import");
		PSEUDO_TOPICS.add("Special:LinkTo");
		PSEUDO_TOPICS.add("Special:Linkto");
		PSEUDO_TOPICS.add("Special:ListUsers");
		PSEUDO_TOPICS.add("Special:Listusers");
		PSEUDO_TOPICS.add("Special:Log");
		PSEUDO_TOPICS.add("Special:Logs");
		PSEUDO_TOPICS.add("Special:Login");
		PSEUDO_TOPICS.add("Special:Logout");
		PSEUDO_TOPICS.add("Special:Maintenance");
		PSEUDO_TOPICS.add("Special:Manage");
		PSEUDO_TOPICS.add("Special:Move");
		PSEUDO_TOPICS.add("Special:OrphanedPages");
		PSEUDO_TOPICS.add("Special:Orphanedpages");
		PSEUDO_TOPICS.add("Special:Print");
		PSEUDO_TOPICS.add("Special:RecentChanges");
		PSEUDO_TOPICS.add("Special:Recentchanges");
		PSEUDO_TOPICS.add("Special:RecentChangesFeed");
		PSEUDO_TOPICS.add("Special:Roles");
		PSEUDO_TOPICS.add("Special:Search");
		PSEUDO_TOPICS.add("Special:Setup");
		PSEUDO_TOPICS.add("Special:Source");
		PSEUDO_TOPICS.add("Special:SpecialPages");
		PSEUDO_TOPICS.add("Special:Specialpages");
		PSEUDO_TOPICS.add("Special:TopicsAdmin");
		PSEUDO_TOPICS.add("Special:Topicsadmin");
		PSEUDO_TOPICS.add("Special:Translation");
		PSEUDO_TOPICS.add("Special:UnBlock");
		PSEUDO_TOPICS.add("Special:Unblock");
		PSEUDO_TOPICS.add("Special:Upgrade");
		PSEUDO_TOPICS.add("Special:Upload");
		PSEUDO_TOPICS.add("Special:VirtualWiki");
		PSEUDO_TOPICS.add("Special:Virtualwiki");
		PSEUDO_TOPICS.add("Special:WatchList");
		PSEUDO_TOPICS.add("Special:Watchlist");
	}

	/**
	 * Return true if there is a mapping for the given topic
	 *
	 * @param pseudotopicName The name of the pseudo-topic that is being tested
	 *  for existence.
	 * @return <code>true</code> if a pseudo-topic with the specified name
	 *  exists, <code>false</code> otherwise.
	 */
	public static boolean isPseudoTopic(String pseudotopicName) {
		if (PSEUDO_TOPICS == null) {
			init();
		}
		return PSEUDO_TOPICS.contains(pseudotopicName);
	}
}
