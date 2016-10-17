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
package org.jamwiki.web.model;

import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides an object representing the difference between two objects as created
 * by {@link org.jamwiki.web.utils.DiffUtil}.
 */
public class WikiDiff implements Serializable {

	/** The newly modified text, or <code>null</code> if text was deleted. */
	private String newText;
	/** The old text that was changed, or <code>null</code> if new text was added. */
	private String oldText;
	/** The zero-based position of the text that was changed. */
	private int position = -1;
	/** The diff may (optionally) contain a list of sub-diffs, such as when diffing two topics and then further showing what changed on a line. */
	private List<WikiDiff> subDiffs;

	/**
	 *
	 */
	public WikiDiff(String oldText, String newText, int position) {
		this.oldText = oldText;
		this.newText = newText;
		this.position = position;
	}

	/**
	 *
	 */
	public boolean getChange() {
		return !StringUtils.equals(this.oldText, this.newText);
	}

	/**
	 *
	 */
	public String getNewText() {
		return this.newText;
	}

	/**
	 *
	 */
	public void setNewText(String newText) {
		this.newText = newText;
	}

	/**
	 *
	 */
	public String getOldText() {
		return this.oldText;
	}

	/**
	 *
	 */
	public void setOldText(String oldText) {
		this.oldText = oldText;
	}

	/**
	 *
	 */
	public int getPosition() {
		return this.position;
	}

	/**
	 *
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 *
	 */
	public List<WikiDiff> getSubDiffs() {
		return this.subDiffs;
	}

	/**
	 *
	 */
	public void setSubDiffs(List<WikiDiff> subDiffs) {
		this.subDiffs = subDiffs;
	}
}