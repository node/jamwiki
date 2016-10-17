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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.jamwiki.JAMWikiUnitTest;
import org.jamwiki.TestFileUtil;
import org.jamwiki.WikiBase;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.Pagination;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public class MigrationUtilTest extends JAMWikiUnitTest {

	private static final String FILE_TEST_TWO_TOPICS_WITH_HISTORY = "mediawiki-export-two-topics-with-history.xml";
	private static final String FILE_ONE_TOPIC_WITH_UNSORTED_HISTORY = "mediawiki-export-one-topic-with-unsorted-history.xml";
	private static final String FILE_TOPIC_NAME_WITH_QUESTION_MARK = "mediawiki-export-topic-name-with-question-mark.xml";
	private static final String FILE_NAMESPACE_TEST = "mediawiki-export-namespace-test.xml";
	private static final String TEST_FILES_DIR = "data/files/";
	private static final String TOPIC_NAME1 = "Test Page 1";
	private static final String TOPIC_NAME2 = "Template comments:Test Template";
	private static final String TOPIC_NAME3 = "Test Page 2";
	private static final String TOPIC_NAME4 = "Who am i";
	private static final String TOPIC_NAME5 = "Namespace Test";
	private static final String VIRTUAL_WIKI_EN = "en";
	@Rule
	public TemporaryFolder TEMP_FOLDER = new TemporaryFolder();

	private static boolean INITIALIZED = false;

	/**
	 *
	 */
	@Before
	public void setup() throws Exception {
		super.setup();
		if (!INITIALIZED) {
			this.setupTopic(null, "CharacterUtf8");
			this.setupTopic(null, "Example1");
			this.setupTopic(null, "Example2");
			INITIALIZED = true;
		}
	}

	/**
	 *
	 */
	@Test
	public void testExportNonExistentTopic() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> topicNames = new ArrayList<String>();
		topicNames.add("Bogus Topic Name");
		boolean excludeHistory = false;
		File file = TEMP_FOLDER.newFile("export.xml");
		try {
			MigrationUtil.exportToFile(file, virtualWiki, topicNames, excludeHistory);
		} catch (MigrationException e) {
			if (file.exists()) {
				// should have been deleted
				fail("Partial export file not deleted");
			}
			return;
		}
		fail("Expected MigrationException to be thrown");
	}

	/**
	 *
	 */
	@Test
	public void testExportTwoTopics() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> topicNames = new ArrayList<String>();
		topicNames.add("Example1");
		topicNames.add("Example2");
		boolean excludeHistory = false;
		File file = TEMP_FOLDER.newFile("export.xml");
		try {
			MigrationUtil.exportToFile(file, virtualWiki, topicNames, excludeHistory);
		} catch (MigrationException e) {
			fail("Failure during export" + e);
		}
	}

	/**
	 *
	 */
	@Test
	public void testExportUtf8() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> topicNames = new ArrayList<String>();
		topicNames.add("CharacterUtf8");
		boolean excludeHistory = false;
		File file = TEMP_FOLDER.newFile("export.xml");
		try {
			MigrationUtil.exportToFile(file, virtualWiki, topicNames, excludeHistory);
		} catch (MigrationException e) {
			fail("Failure during export" + e);
		}
		Topic topic = WikiBase.getDataHandler().lookupTopic(VIRTUAL_WIKI_EN, "CharacterUtf8", true);
		String fileContent = FileUtils.readFileToString(file, "UTF-8");
		assertTrue("UTF-8 exported incorrectly", fileContent.contains(topic.getTopicContent()));
	}

	/**
	 *
	 */
	@Test
	public void testImportFromFileWithTwoTopics() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> results = this.importTestFile(FILE_TEST_TWO_TOPICS_WITH_HISTORY);
		// validate that the first topic parsed
		assertTrue("Parsed topic '" + TOPIC_NAME1 + "'", results.contains(TOPIC_NAME1));
		Topic topic1 = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME1, false);
		// validate that the parsed topic correctly set topic values
		assertEquals("Topic name '" + TOPIC_NAME1 + "' set correctly", TOPIC_NAME1, topic1.getName());
		assertTrue("Topic content set correctly", topic1.getTopicContent().indexOf("Link to user page: [[User:Test User]]") != -1);
		// validate that namespaces were converted from Mediawiki to JAMWiki correctly
		assertTrue("Topic content namespaces updated correctly", topic1.getTopicContent().indexOf("Link to user talk page: [[User comments: Test User]]") != -1);
		// validate that the second topic parsed
		assertTrue("Parsed topic '" + TOPIC_NAME2 + "'", results.contains(TOPIC_NAME2));
		Topic topic2 = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME2, false);
		// validate that the parsed topic correctly set topic values
		assertEquals("Topic name '" + TOPIC_NAME2 + "' set correctly", TOPIC_NAME2, topic2.getName());
	}

	/**
	 *
	 */
	@Test
	public void testImportFromFileWithUnsortedHistory() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> results = this.importTestFile(FILE_ONE_TOPIC_WITH_UNSORTED_HISTORY);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME3, false);
		// validate that the current topic content is correct
		assertEquals("Incorrect topic ordering: " + topic.getTopicId() + " / " + topic.getCurrentVersionId(), "Newest Revision", topic.getTopicContent());
		Pagination pagination = new Pagination(1000, 0);
		List<RecentChange> revisions = WikiBase.getDataHandler().getTopicHistory(topic, pagination, false);
		// there are three revisions in the import file, plus one import revision
		assertEquals("Incorrect number of revisions imported", 4, revisions.size());
	}

	/**
	 *
	 */
	@Test
	public void testImportFromFileTopicNameWithQuestionMark() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> results = this.importTestFile(FILE_TOPIC_NAME_WITH_QUESTION_MARK);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME4, false);
		assertNotNull("Topic with question mark in name imported correctly", topic);
	}

	/**
	 *
	 */
	@Test
	public void testImportFromFileNamespaceTest() throws Throwable {
		String virtualWiki = VIRTUAL_WIKI_EN;
		List<String> results = this.importTestFile(FILE_NAMESPACE_TEST);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, TOPIC_NAME5, false);
		assertNotNull("Namespace test topic imported correctly", topic);
		// verify that Mediawiki namespaces were correctly converted to JAMWiki namespaces
		assertTrue("Talk:Test", (topic.getTopicContent().indexOf("Talk:Test - [[Comments:Test]]") != -1));
		assertTrue("User:Test", (topic.getTopicContent().indexOf("User:Test - [[User:Test]]") != -1));
		assertTrue("User talk:Test", (topic.getTopicContent().indexOf("User talk:Test - [[User comments:Test]]") != -1));
		assertTrue("Wikipedia:Test", (topic.getTopicContent().indexOf("Wikipedia:Test - [[Project:Test]]") != -1));
		assertTrue("Wikipedia talk:Test", (topic.getTopicContent().indexOf("Wikipedia talk:Test - [[Project comments:Test]]") != -1));
		assertTrue("File:Test", (topic.getTopicContent().indexOf("File:Test - [[File:Test]]") != -1));
		assertTrue("File talk:Test", (topic.getTopicContent().indexOf("File talk:Test - [[File comments:Test]]") != -1));
		assertTrue("Template:Test", (topic.getTopicContent().indexOf("Template:Test - [[Template:Test]]") != -1));
		assertTrue("Template talk:Test", (topic.getTopicContent().indexOf("Template talk:Test - [[Template comments:Test]]") != -1));
		assertTrue("Category:Test", (topic.getTopicContent().indexOf("Category:Test - [[Category:Test]]") != -1));
		assertTrue("Category talk:Test", (topic.getTopicContent().indexOf("Category talk:Test - [[Category comments:Test]]") != -1));
		assertTrue("Custom:Test", (topic.getTopicContent().indexOf("Custom:Test - [[Custom:Test]]") != -1));
		assertTrue("Custom talk:Test", (topic.getTopicContent().indexOf("Custom talk:Test - [[Custom talk:Test]]") != -1));
		assertTrue("Pattern test", (topic.getTopicContent().indexOf("Pattern test - [[  User comments:Test ]]") != -1));
		assertTrue("Case-sensitive test", (topic.getTopicContent().indexOf("Case-sensitive test - [[ User comments:Test]]") != -1));
		assertTrue("Inclusion test", (topic.getTopicContent().indexOf("Inclusion test - [[:User comments:Test]]") != -1));
		assertTrue("Template inclusion test 1", (topic.getTopicContent().indexOf("Template inclusion test 1 - {{User comments:Test}}") != -1));
		assertTrue("Template inclusion test 2", (topic.getTopicContent().indexOf("Template inclusion test 2 - {{:User comments:Test}}") != -1));
	}

	/**
	 * Utility method for importing test files.
	 */
	private List<String> importTestFile(String filename) throws Throwable {
		File file = TestFileUtil.retrieveFile(TEST_FILES_DIR, filename);
		Locale locale = new Locale("en", "US");
		String virtualWiki = VIRTUAL_WIKI_EN;
		String authorDisplay = "127.0.0.1";
		WikiUser user = null;
		return MigrationUtil.importFromFile(file, virtualWiki, user, authorDisplay, locale);
	}
}
