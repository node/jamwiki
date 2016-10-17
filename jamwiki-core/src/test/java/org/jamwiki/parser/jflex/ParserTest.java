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
package org.jamwiki.parser.jflex;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.jamwiki.Environment;
import org.jamwiki.JAMWikiUnitTest;
import org.jamwiki.TestFileUtil;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class will first get a list of all parser result files in the /data/results
 * directory and then retrieve the corresponding /data/topics file, parse it, and
 * compare the parser output to the results file.
 */
public class ParserTest extends JAMWikiUnitTest {

	/** Hard-code a list of files that are known to fail parsing. */
	private static final List<String> KNOWN_FAILURES = Arrays.asList(
		"Heading9", // see JAMWIKI-27
		"HtmlMismatchTest3",
		"Inclusion~test", // template params not parsed in parser functions
		"ParserFunctionsBroken",
		"PreformattedInvalid1",
		"Template_-_Paramtest", // issues with params when parsing a template page
		"UnbalancedTag1",
		"UnbalancedTag3"
	);
	private ParserTestUtils parserTestUtils = new ParserTestUtils();

	/**
	 *
	 */
	@Test
	public void testCategory() throws Throwable {
		ParserOutput parserOutput = this.parserTestUtils.parseForParserOutput("WikiCategory");
		assertEquals("Expected two categories", 2, parserOutput.getCategories().size());
		assertTrue("Category:Test expected in categories", parserOutput.getCategories().containsKey("Category:Test"));
		assertNull("Category:Test should not have a sort key", parserOutput.getCategories().get("Category:Test"));
		assertTrue("Category:Sort Key expected", parserOutput.getCategories().containsKey("Category:Sort Key"));
		assertEquals("sort key expected", parserOutput.getCategories().get("Category:Sort Key"), "sort key");
	}

	/**
	 *
	 */
	@Test
	public void testCategoryNested() throws Throwable {
		ParserOutput parserOutput = this.parserTestUtils.parseForParserOutput("TemplateIncludeCategory");
		assertEquals("Expected one category", 1, parserOutput.getCategories().size());
		assertNotNull("Category:Test expected", parserOutput.getCategories().containsKey("Category:Test"));
	}

	/**
	 *
	 */
	@Test
	public void testCategoryTemplate1() throws Throwable {
		ParserOutput parserOutput = this.parserTestUtils.parseForParserOutput("TemplateCategory1");
		assertEquals("Expected one category", 1, parserOutput.getCategories().size());
		assertNotNull("Category:Test Example1 expected", parserOutput.getCategories().containsKey("Category:Test Example1"));
	}

	/**
	 *
	 */
	@Test
	public void testCategoryTemplate2() throws Throwable {
		ParserOutput parserOutput = this.parserTestUtils.parseForParserOutput("TemplateCategory2");
		assertEquals("Expected two categories", 2, parserOutput.getCategories().size());
		assertNotNull("Category:Test Example1 expected", parserOutput.getCategories().containsKey("Category:Test Example1"));
		assertNotNull("Category:Test Example2 expected", parserOutput.getCategories().containsKey("Category:Test Example2"));
	}

	/**
	 *
	 */
	@Test
	public void testInterwiki1() throws Throwable {
		// this topic has two interwiki links, but they both go to the same wikipedia page
		ParserOutput parserOutput = new ParserOutput();
		String parserResult = this.parserTestUtils.parserResult(parserOutput, "InterWiki1");
		assertEquals("Number of interwiki links found does not match expected", 2, parserOutput.getInterwikiLinks().size());
		assertEquals("Interwiki link text does not match expected", "<a class=\"interwiki\" title=\"Wikipedia\" href=\"http://en.wikipedia.org/wiki/Main_Page\">Wikipedia</a>", parserOutput.getInterwikiLinks().get(0));
	}

	/**
	 *
	 */
	@Test
	public void testSubst() throws Throwable {
		// verify that "subst:" content was properly replaced when topic was saved
		String TOPIC1_NAME = "Subst2";
		String TOPIC2_NAME = "Template:Test Template";
		String VIRTUAL_WIKI_NAME = "en";
		Topic topic1 = WikiBase.getDataHandler().lookupTopic(VIRTUAL_WIKI_NAME, TOPIC1_NAME, false);
		String contents1 = ParserUtil.parseMinimal(this.parserTestUtils.parserInput(TOPIC1_NAME), topic1.getTopicContent());
		Topic topic2 = WikiBase.getDataHandler().lookupTopic(VIRTUAL_WIKI_NAME, TOPIC2_NAME, false);
		assertTrue("Topic Subst2 should contain as content Template:Test Template", contents1.indexOf(topic2.getTopicContent()) != -1);
	}

	/**
	 *
	 */
	@Test
	public void testVirtualWiki1() throws Throwable {
		// this topic has one virtual wiki link
		ParserOutput parserOutput = new ParserOutput();
		String parserResult = this.parserTestUtils.parserResult(parserOutput, "WikiLink1");
		assertEquals("Interwiki1", 1, parserOutput.getVirtualWikiLinks().size());
		assertEquals("Interwiki1", "<a href=\"/wiki/test/WikiLink1\" title=\"WikiLink1\">test:WikiLink1</a>", parserOutput.getVirtualWikiLinks().get(0));
	}

	/**
	 *
	 */
	@Test
	public void testVirtualWiki2() throws Throwable {
		ParserOutput parserOutput = new ParserOutput();
		String parserResult = this.parserTestUtils.parserResult(parserOutput, "VirtualWiki1");
		assertEquals("Expected no categories", 0, parserOutput.getCategories().size());
		assertEquals("Expected one virtual wiki", 1, parserOutput.getVirtualWikiLinks().size());
		assertEquals("Interwiki1", "<a href=\"/wiki/test/Category:Category1\" title=\"Category:Category1\">test:Category:Category1</a>", parserOutput.getVirtualWikiLinks().get(0));
	}

	/**
	 *
	 */
	@Test
	public void testMagicWordDisplayTitleValid() throws Throwable {
		String topicName = "Magic Words Display Title";
		String displayTitle = "Magic_Words Display_Title";
		String topicContent = "{{DISPLAYTITLE:" + displayTitle + "}}";
		ParserInput parserInput = this.parserTestUtils.parserInput(topicName);
		ParserOutput parserOutput = new ParserOutput();
		ParserUtil.parse(parserInput, parserOutput, topicContent);
		assertEquals("DISPLAYTITLE", displayTitle, parserOutput.getPageTitle());
	}

	/**
	 *
	 */
	@Test
	public void testMagicWordDisplayTitleInvalid() throws Throwable {
		String topicName = "Magic Words Display Title";
		String displayTitle = "Invalid Title";
		String topicContent = "{{DISPLAYTITLE:" + displayTitle + "}}";
		ParserInput parserInput = this.parserTestUtils.parserInput(topicName);
		ParserOutput parserOutput = new ParserOutput();
		ParserUtil.parse(parserInput, parserOutput, topicContent);
		assertNull("DISPLAYTITLE", parserOutput.getPageTitle());
	}

	/**
	 * Test parsing of edit summary text.  This method loads a test file,
	 * parses each line, and verifies that line matches the expected
	 * result in the results file.
	 */
	@Test
	public void testParseEditComment() throws Throwable {
		// read in inputs
		File inputFile = TestFileUtil.retrieveFile(TestFileUtil.TEST_FILES_DIR, "edit-comment-inputs.txt");
		List<String> inputs = FileUtils.readLines(inputFile);
		// read in outputs
		File outputFile = TestFileUtil.retrieveFile(TestFileUtil.TEST_FILES_DIR, "edit-comment-outputs.txt");
		List<String> outputs = FileUtils.readLines(outputFile);
		// verify parsed inputs equal outputs
		ParserInput parserInput = this.parserTestUtils.parserInput("Example1");
		String parsedOutput;
		int i = 0;
		for (String input : inputs) {
			if (i != 0) {
				// the first line is a comment
				parsedOutput = ParserUtil.parseEditComment(parserInput, input);
				assertEquals("Invalid edit comment result " + i, outputs.get(i), parsedOutput);
			}
			i++;
		}
	}

	/**
	 *
	 */
	@Test
	public void testParserNoJavascript() throws IOException {
		// test with JS disabled
		Environment.setBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT, false);
		this.parserTestUtils.parseAllResults(TestFileUtil.TEST_RESULTS_DIR, KNOWN_FAILURES);
	}

	/**
	 *
	 */
	@Test
	public void testParserWithJavascript() throws IOException {
		// test with JS enabled
		Environment.setBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT, true);
		this.parserTestUtils.parseAllResults(TestFileUtil.TEST_JS_RESULTS_DIR, KNOWN_FAILURES);
	}

	/**
	 *
	 */
	@Test
	public void testRedirectCategory1() throws Throwable {
		ParserOutput parserOutput = this.parserTestUtils.parseForParserOutput("Redirect4");
		assertEquals("Expected redirect target to be Example1", "Example1", parserOutput.getRedirect());
		assertEquals("Expected one category", 1, parserOutput.getCategories().size());
		assertEquals("Expected two links", 2, parserOutput.getLinks().size());
	}

	/**
	 *
	 */
	@Test
	public void testRedirectCategory2() throws Throwable {
		ParserOutput parserOutput = this.parserTestUtils.parseForParserOutput("Category:Redirected");
		assertEquals("Expected redirect target to be Category:Test", "Category:Test", parserOutput.getRedirect());
		assertEquals("Expected one category", 1, parserOutput.getCategories().size());
		assertEquals("Expected two links", 2, parserOutput.getLinks().size());
	}

	/**
	 *
	 */
	@Test
	public void testRedirectWithSection() throws Throwable {
		ParserOutput parserOutput = this.parserTestUtils.parseForParserOutput("Redirect5");
		assertEquals("Expected redirect target to be Example1#Section 2", "Example1#Section 2", parserOutput.getRedirect());
	}
}
