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
import java.util.List;

/**
 * Interface that controls how topics are exported.
 */
public interface TopicExporter {

	/**
	 * Given a map of topics and the associated topic versions, generate a file suitable for
	 * importing into another wiki.
	 *
	 * @param file The file containing all exported topic data.
	 * @param virtualWiki The virtual wiki for which topics will be exported.
	 * @param topicNames A list of topic names to export.
	 * @param excludeHistory Set to <code>true</code> if only the most recent topic
	 *  version, not the full topic history, should be exported.
	 * @throws MigrationException Thrown if any error occurs during export.
	 */
	public void exportToFile(File file, String virtualWiki, List<String> topicNames, boolean excludeHistory) throws MigrationException;
}
