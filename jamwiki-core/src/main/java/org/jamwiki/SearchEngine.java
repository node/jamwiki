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

import java.io.IOException;
import java.util.List;
import org.jamwiki.model.SearchResultEntry;
import org.jamwiki.model.Topic;

/**
 * This interface provides all methods needed for interacting with a search
 * engine.
 *
 * @see org.jamwiki.WikiBase#getSearchEngine
 */
public interface SearchEngine {

	/** Lucene search engine class */
	public static final String SEARCH_ENGINE_LUCENE = "org.jamwiki.search.LuceneSearchEngine";
	/** RankingAlgorithm search engine class */
	public static final String SEARCH_ENGINE_RANKINGALGORITHM = "org.jamwiki.search.RankingAlgorithmSearchEngine";

	/**
	 * Add a topic to the search index.
	 *
	 * @param topic The Topic object that is to be added to the index.
	 */
	void addToIndex(Topic topic);

	/**
	 * Force a flush of any pending commits to the search index.
	 *
	 * @param virtualWiki The virtual wiki for which pending updates are being
	 *  committed.
	 */
	void commit(String virtualWiki);

	/**
	 * Remove a topic from the search index.
	 *
	 * @param topic The topic object that is to be removed from the index.
	 */
	void deleteFromIndex(Topic topic);

	/**
	 * Find all documents that contain a specific search term, ordered by relevance.
	 *
	 * @param virtualWiki The virtual wiki for the topic.
	 * @param text The search term being searched for.
	 * @param namespaces A list of all namespaces that should be searched when
	 *  retrieving results.  If this list is <code>null</code> or empty then all
	 *  namespaces will be searched.
	 * @return A list of SearchResultEntry objects for all documents that
	 *  contain the search term.
	 */
	List<SearchResultEntry> findResults(String virtualWiki, String text, List<Integer> namespaces);

	/**
	 * Refresh the current search index by re-visiting all topic pages.
	 *
	 * @throws Exception Thrown if any error occurs while re-indexing the Wiki.
	 */
	void refreshIndex() throws Exception;

	/**
	 * Set a flag indicating whether or not every update of the search index
	 * should be immediately committed to the index.  This is useful mainly
	 * during batch updates when for performance reasons it is advantageous
	 * to commit only after the update is done.
	 *
	 * @param autoCommit A boolean indicating whether or not every update of
	 *  the search index should be immediately committed to the index.
	 */
	void setAutoCommit(boolean autoCommit);

	/**
	 * Temporarily disable the search engine from all write operations such as
	 * adding and updating topics in its index, but not from read operations
	 * such as performing searches.  This is useful in cases such as bulk
	 * loading of topic versions where the search engine only needs to be
	 * updated after the last topic version is inserted since all intermediate
	 * updates would have been overridden.
	 *
	 * @param disabled <code>true</code> to disable updates to the search engine
	 *  index.
	 */
	void setDisabled(boolean disabled);

	/**
	 * Trigger a shutdown of a search engine instance, allowing resources to
	 * be freed and any pending changes committed.
	 *
	 * @throws IOException Thrown if a failure occurs during shutdown.
	 */
	void shutdown() throws IOException;

	/**
	 * Update a topic in the search index.
	 *
	 * @param topic The Topic object that is to be updated in the index.
	 */
	void updateInIndex(Topic topic);
}
