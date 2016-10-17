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
package org.jamwiki.parser.jflex.addon;

import java.io.IOException;
import org.jamwiki.JAMWikiUnitTest;
import org.jamwiki.TestFileUtil;
import org.jamwiki.parser.jflex.ParserTestUtils;
import org.junit.Test;

/**
 * This class will first get a list of all parser result files in the /data/results
 * directory and then retrieve the corresponding /data/topics file, parse it, and
 * compare the parser output to the results file.
 */
public class AddonParserTest extends JAMWikiUnitTest {

	private ParserTestUtils parserTestUtils = new ParserTestUtils();

	/**
	 *
	 */
	@Test
	public void testCustomTags() throws IOException {
		this.parserTestUtils.parseAllResults(TestFileUtil.TEST_RESULTS_DIR, null);
	}
}
