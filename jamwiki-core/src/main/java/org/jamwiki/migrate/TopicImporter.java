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
import java.util.Map;
import org.jamwiki.model.Topic;

/**
 * Interface that controls how topics are imported.
 */
public interface TopicImporter {

	/**
	 * Parse the contents of the file, returning a map of topics and the associated topic
	 * versions for all data contained in the file.
	 *
	 * @param file The file containing all topic data to be imported.
	 * @param virtualWiki The virtual wiki into which the topic data will be imported.
	 * @return A map of imported topics and a list of topic version IDs imported for each topic.
	 * @throws MigrationException Thrown if any error occurs during import.
	 */
	public Map<Topic, List<Integer>> importFromFile(File file, String virtualWiki) throws MigrationException;
}
