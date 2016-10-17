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
import java.util.Collection;
import java.util.List;

/**
 * Utility class useful for paginating through a result set.
 */
public class Pagination {

	/** Logger */
	public static final WikiLogger logger = WikiLogger.getLogger(Pagination.class.getName());

	private final int numResults;
	private final int offset;

	/**
	 * Create a pagination object with specified initial values.
	 *
	 * @param numResults The maximum number of results that can be retrieved or
	 *  displayed.
	 * @param offset The offset for the pagination.  An offset of 100 indicates
	 *  that the first 100 results should be ignored and numResults should be
	 *  returned starting at 100.
	 */
	public Pagination(int numResults, int offset) {
		this.numResults = numResults;
		this.offset = offset;
	}

	/**
	 * Return the last result of the current pagination, equivalent to offset + numResults.
	 *
	 * @return Return the last result of the current pagination, equivalent to
	 *  offset + numResults.
	 */
	public int getEnd() {
		return this.offset + this.numResults;
	}

	/**
	 * Return the number of results that this pagination allows.
	 *
	 * @return The number of results that this pagination allows.
	 */
	public int getNumResults() {
		return this.numResults;
	}

	/**
	 * Return the offset that this pagination allows.  Offset indicates
	 * the starting point of any result to return, for example an offset of
	 * 10 with numResults of 5 indicates results 10-14.
	 *
	 * @return The offset that this pagination allows.
	 */
	public int getOffset() {
		return this.offset;
	}

	/**
	 * Return the starting point of any pagination, which is equivalent to the
	 * offset.  Offset indicates the starting point of any result to return,
	 * for example an offset of 10 with numResults of 5 indicates results 10-14.
	 *
	 * @return The starting point of any pagination.
	 */
	public int getStart() {
		return this.offset;
	}

	/**
	 * Utility method for retrieving a list that is a subset of a larger list
	 * containing only the items specified by the pagination object.
	 *
	 * @param pagination The pagination object being used to generate the subset.
	 * @param items The full list of items that is being paginated.
	 * @return A subset of the original list corresponding only to those items
	 *  that match the pagination parameters.
	 */
	public static <T> List<T> retrievePaginatedSubset(Pagination pagination, Collection<T> items) {
		List<T> results = new ArrayList<T>();
		if (items != null && !items.isEmpty()) {
			int count = 0;
			for (T item : items) {
				count++;
				if (count < (pagination.getOffset() + 1)) {
					continue;
				}
				if (count > (pagination.getOffset() + pagination.getNumResults())) {
					break;
				}
				results.add(item);
			}
		}
		return results;
	}
}
