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
import java.util.List;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.TestFileUtil;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;
import static org.junit.Assert.*;

/**
 * Provide utility methods useful for testing the JFlex parser.
 */
public class ParserTestUtils {

	/**
	 * Generate a generic ParserInput object that can be used for testing.
	 */
	public ParserInput parserInput(String topicName) {
		// set dummy values for parser input
		ParserInput parserInput = new ParserInput("en", topicName);
		parserInput.setContext("/wiki");
		parserInput.setLocale(LocaleUtils.toLocale("en_US"));
		parserInput.setWikiUser(null);
		parserInput.setUserDisplay("0.0.0.0");
		parserInput.setAllowSectionEdit(true);
		return parserInput;
	}

	/**
	 *
	 */
	private void executeParserTest(String fileName, String resultDirName) throws IOException, ParserException {
		ParserOutput parserOutput = new ParserOutput();
		String parserResult = this.parserResult(parserOutput, fileName);
		String expectedResult = this.expectedResult(fileName, resultDirName);
		assertEquals("Testing file " + fileName, expectedResult, parserResult);
	}

	/**
	 *
	 */
	private String expectedResult(String fileName, String resultDirName) throws IOException, ParserException {
		String result = TestFileUtil.retrieveFileContent(resultDirName, fileName);
		return this.sanitize(result);
	}

	/**
	 * Read files located in a /results directory, and then parse the file
	 * in the /topics directory with the same name to ensure that the parsed
	 * topic generates the expected result.
	 *
	 * @param resultDirName The directory that contains the parser results.
	 *  There must be a corresponding /topics directory that contains the
	 *  topic file that will generate the result.
	 * @param knownFailures If there are result files that are known to parse
	 *  incorrectly then they can be specified in advance, and they will not
	 *  be executed as part of the test.
	 */
	public void parseAllResults(String resultDirName, List<String> knownFailures) throws IOException {
		File resultDir = TestFileUtil.getClassLoaderFile(resultDirName);
		File[] resultFiles = resultDir.listFiles();
		String fileName = null;
		for (int i = 0; i < resultFiles.length; i++) {
			fileName = resultFiles[i].getName();
			if (knownFailures != null && knownFailures.contains(fileName)) {
				continue;
			}
			executeParserTest(fileName, resultDirName);
		}
	}

	/**
	 *
	 */
	public ParserOutput parseForParserOutput(String topicName) throws Throwable {
		ParserOutput parserOutput = new ParserOutput();
		this.parserResult(parserOutput, topicName);
		return parserOutput;
	}

	/**
	 * Given a topic file name (located within the TEST_TOPICS_DIR), parse the
	 * topic and return the parsed output.
	 */
	public String parserResult(ParserOutput parserOutput, String fileName) throws IOException, ParserException {
		String raw = TestFileUtil.retrieveFileContent(TestFileUtil.TEST_TOPICS_DIR, fileName);
		String topicName = TestFileUtil.decodeTopicName(fileName);
		ParserInput parserInput = this.parserInput(topicName);
		return ParserUtil.parse(parserInput, parserOutput, raw);
	}

	/**
	 *
	 */
	private String sanitize(String value) {
		return StringUtils.remove(value, '\r').trim();
	}
}
