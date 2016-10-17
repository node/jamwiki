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
package org.jamwiki.model;

/**
 * Provides an object that can be used to store search result information.
 */
public class SearchResultEntry {

	/** The topic of this entry */
	private final String topic;
	/** the hit ranking */
	private final float ranking;
	/** Result summary */
	private final String summary;

	/**
	 *
	 */
	public SearchResultEntry(String topic, float ranking, String summary) {
		this.topic = topic;
		this.ranking = ranking;
		this.summary = summary;
	}

	/**
	 *
	 */
	public float getRanking() {
		return this.ranking;
	}

	/**
	 *
	 */
	public String getSummary() {
		return this.summary;
	}

	/**
	 *
	 */
	public String getTopic() {
		return this.topic;
	}
}
