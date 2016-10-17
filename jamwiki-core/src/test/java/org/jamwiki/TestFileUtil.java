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
package org.jamwiki;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.utils.ResourceUtil;

/**
 *
 */
public class TestFileUtil {

	public static final String TEST_RESULTS_DIR = "data/results/";
	public static final String TEST_JS_RESULTS_DIR = "data/javascript/";
	public static final String TEST_TOPICS_DIR = "data/topics/";
	public static final String TEST_FILES_DIR = "data/files/";

	/**
	 *
	 */
	public static String decodeTopicName(String fileName) {
		// files containing colons aren't allowed, so they are replaced with "_-_"
		String result = StringUtils.replace(fileName, "_-_", ":");
		result = StringUtils.replace(result, "_", " ");
		// files containing slashes aren't allowed, so they are replaced with "~"
		result = StringUtils.replace(result, "~", "/");
		return result;
	}

	/**
	 *
	 */
	public static String encodeTopicName(String topicName) {
		// files containing colons aren't allowed, so they are replaced with "_-_"
		String result = StringUtils.replace(topicName, ":", "_-_");
		return StringUtils.replace(result, " ", "_");
	}

	/**
	 *
	 */
	public static File getClassLoaderFile(String fileName) throws IOException {
		try {
			return ResourceUtil.getClassLoaderFile(fileName);
		} catch (IOException e) {
			// ignore
		}
		return new File(ResourceUtil.getClassLoaderRoot(), fileName);
	}

	/**
	 *
	 */
	public static File retrieveFile(String directory, String fileName) {
		fileName = encodeTopicName(fileName);
		String fullName = directory + fileName;
		try {
			return ResourceUtil.getClassLoaderFile(fullName);
		} catch (IOException e) { }
		try {
			return new File(ResourceUtil.getClassLoaderRoot(), fullName);
		} catch (IOException e) { }
		return null;
	}

	/**
	 *
	 */
	public static String retrieveFileContent(String directory, String fileName) throws IOException {
		File file = TestFileUtil.retrieveFile(directory, fileName);
		if (file == null || !file.exists()) {
			return null;
		}
		return FileUtils.readFileToString(file, "UTF-8");
	}
}
