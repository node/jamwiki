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

import java.io.IOException;
import java.util.List;
import org.jamwiki.DataAccessException;
import org.jamwiki.JAMWikiUnitTest;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.utils.Pagination;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for data handler functionality.
 */
public class AnsiDataHandlerTest extends JAMWikiUnitTest {

	/**
	 *
	 */
	@Test
	public void testReloadRecentChanges() throws DataAccessException {
		// verify that this runs without throwing exceptions
		WikiBase.getDataHandler().reloadRecentChanges();
	}

	/**
	 *
	 */
	@Test
	public void testTopicLookup1() throws DataAccessException {
		Topic topic = WikiBase.getDataHandler().lookupTopic("en", WikiBase.SPECIAL_PAGE_SYSTEM_CSS, false);
		assertEquals("Incorrect topic name", topic.getName(), WikiBase.SPECIAL_PAGE_SYSTEM_CSS);
	}

	/**
	 *
	 */
	@Test
	public void testPurgeTopicVersion() throws DataAccessException, IOException, WikiException {
		// load a test topic with three versions
		String topicName = "Purge Topic Test";
		Topic topic = null;
		for (int i = 0; i < 3; i++) {
			String contents = "Test topic content " + i;
			if (topic == null) {
				// create a new record
				topic = this.setupTopic(null, topicName, contents);
			} else {
				// update the existing record
				this.setupTopic(topic);
			}
		}
		// delete the first two versions
		Pagination pagination = new Pagination(1000, 0);
		List<RecentChange> versions = WikiBase.getDataHandler().getTopicHistory(topic, pagination, true);
		assertEquals("Incorrect number of test versions present", 3, versions.size());
		WikiBase.getDataHandler().purgeTopicVersion(topic, versions.get(0).getTopicVersionId(), null, "127.0.0.1");
		WikiBase.getDataHandler().purgeTopicVersion(topic, versions.get(1).getTopicVersionId(), null, "127.0.0.1");
		// attempts to delete the third version should thrown an error
		boolean exceptionThrown = false;
		try {
			WikiBase.getDataHandler().purgeTopicVersion(topic, versions.get(2).getTopicVersionId(), null, "127.0.0.1");
		} catch (WikiException e) {
			exceptionThrown = true;
		}
		assertTrue("Deleting a topic version should fail when only one topic version exists", exceptionThrown);
		versions = WikiBase.getDataHandler().getTopicHistory(topic, pagination, true);
		assertEquals("Incorrect number of test versions present", 1, versions.size());
		// test retrieval of deleted revisions
		WikiBase.getDataHandler().deleteTopic(topic, null);
		versions = WikiBase.getDataHandler().getTopicHistory(topic, pagination, true);
		assertEquals("Incorrect number of deleted test versions present", 1, versions.size());
	}

	/**
	 *
	 */
	@Test
	public void testWriteAndTopicLookup1() throws DataAccessException, IOException, WikiException {
		String FILE_NAME = "Help_-_Test";
		String TOPIC_NAME = "Help:Test";
		this.setupTopic(null, FILE_NAME);
		Topic topic = WikiBase.getDataHandler().lookupTopic("en", TOPIC_NAME, false);
		assertEquals("Incorrect topic name (case-sensitive)", topic.getName(), TOPIC_NAME);
		topic = WikiBase.getDataHandler().lookupTopic("en", "HELP:Test", false);
		assertEquals("Incorrect topic name (case-insensitive)", topic.getName(), TOPIC_NAME);
	}
}
