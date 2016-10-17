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
package org.jamwiki.web.utils;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;
import org.jamwiki.DataAccessException;
import org.jamwiki.utils.WikiCache;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.web.model.WikiDiff;

/**
 * Utility class for processing the difference between two topics and returing a list
 * of WikiDiff objects that can be used to display the diff.
 */
public class DiffUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(DiffUtil.class.getName());
	/** The number of lines of unchanged text to display before and after each diff. */
	// FIXME - make this a property value
	private static final int DIFF_UNCHANGED_LINE_DISPLAY = 2;
	/** Cache name for the cache of diff information. */
	private static final WikiCache<String, List<WikiDiff>> CACHE_DIFF_INFORMATION = new WikiCache<String, List<WikiDiff>>("org.jamwiki.utils.DiffUtil.CACHE_DIFF_INFORMATION");

	/**
	 *
	 */
	private DiffUtil() {
	}

	/**
	 *
	 */
	private static void addToCache(String newVersion, String oldVersion, List<WikiDiff> results) {
		String key = generateCacheKey(newVersion, oldVersion);
		CACHE_DIFF_INFORMATION.addToCache(key, results);
	}

	/**
	 * Utility method for determining whether or not to append lines of context around a diff.
	 */
	private static boolean canPostBuffer(Difference nextDiff, int current, String[] replacementArray, boolean adding) {
		if (current < 0 || current >= replacementArray.length) {
			// if out of a valid range, don't buffer
			return false;
		}
		if (nextDiff == null) {
			// if in a valid range and no next diff, buffer away
			return true;
		}
		int nextStart = (adding) ? nextDiff.getAddedStart() : nextDiff.getDeletedStart();
		// if in a valid range and the next diff starts several lines away, buffer away.  otherwise
		// the default is not to diff.
		return (nextStart > current);
	}

	/**
	 * Utility method for determining whether or not to prepend lines of context around a diff.
	 */
	private static boolean canPreBuffer(Difference previousDiff, int current, int currentStart, String[] replacementArray, int bufferAmount, boolean adding) {
		if (current < 0 || current >= replacementArray.length) {
			// current position is out of range for buffering
			return false;
		}
		if (previousDiff == null) {
			// if no previous diff, buffer away
			return true;
		}
		if (bufferAmount == -1) {
			// if everything is being buffered and there was a previous diff do not pre-buffer
			return false;
		}
		int previousEnd = (adding) ? previousDiff.getAddedEnd() : previousDiff.getDeletedEnd();
		if (previousEnd != -1) {
			// if there was a previous diff but it was several lines previous, buffer away.
			// if there was a previous diff, and it overlaps with the current diff, don't buffer.
			return (current > (previousEnd + bufferAmount));
		}
		int previousStart = (adding) ? previousDiff.getAddedStart() : previousDiff.getDeletedStart();
		if (current <= (previousStart + bufferAmount)) {
			// the previous diff did not specify an end, and the current diff would overlap with
			// buffering from its start, don't buffer
			return false;
		}
		// the previous diff did not specify an end, and the current diff will not overlap
		// with buffering from its start, buffer away.  otherwise the default is not to buffer.
		return (currentStart > current);
	}

	/**
	 * Return a list of WikiDiff objects that can be used to create a display of the
	 * diff content.
	 *
	 * @param newVersion The String that is to be compared to, ie the later version of a topic.
	 * @param oldVersion The String that is to be considered as having changed, ie the earlier
	 *  version of a topic.
	 * @return Returns a list of WikiDiff objects that correspond to the changed text.
	 */
	public static List<WikiDiff> diff(String newVersion, String oldVersion) throws DataAccessException {
		List<WikiDiff> result = DiffUtil.retrieveFromCache(newVersion, oldVersion);
		if (result != null) {
			return result;
		}
		String version1 = newVersion;
		String version2 = oldVersion;
		if (version2 == null) {
			version2 = "";
		}
		if (version1 == null) {
			version1 = "";
		}
		// remove line-feeds to avoid unnecessary noise in the diff due to
		// cut & paste or other issues
		version2 = StringUtils.remove(version2, '\r');
		version1 = StringUtils.remove(version1, '\r');
		result = DiffUtil.process(version1, version2);
		DiffUtil.addToCache(newVersion, oldVersion, result);
		return result;
	}

	/**
	 * Generate a mostly-unique key to use for the cache.  This key uses the first ten characters
	 * of the string and a hash of the full string, which is not guaranteed to be unique but should
	 * be unique enough.
	 */
	private static String generateCacheKey(String newVersion, String oldVersion) {
		StringBuilder result = new StringBuilder();
		if (newVersion == null) {
			result.append(-1);
		} else if (newVersion.length() <= 10) {
			result.append(newVersion);
		} else {
			result.append(newVersion.substring(0, 10)).append(newVersion.hashCode());
		}
		result.append('-');
		if (oldVersion == null) {
			result.append(-1);
		} else if (oldVersion.length() <= 10) {
			result.append(oldVersion);
		} else {
			result.append(oldVersion.substring(0, 10)).append(oldVersion.hashCode());
		}
		return result.toString();
	}

	/**
	 * Format the list of Difference objects into a list of WikiDiff objects, which will
	 * include information about what values are different and also include some unchanged
	 * values surrounded the changed values, thus giving some context.
	 */
	private static List<WikiDiff> generateWikiDiffs(List<Difference> diffs, String[] oldArray, String[] newArray) {
		List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>();
		Difference previousDiff = null;
		Difference nextDiff = null;
		List<WikiDiff> changedLineWikiDiffs = null;
		String[] oldLineArray = null;
		String[] newLineArray = null;
		List<Difference> changedLineDiffs = null;
		List<WikiDiff> wikiSubDiffs = null;
		Difference nextLineDiff = null;
		int i = 0;
		for (Difference currentDiff : diffs) {
			i++;
			wikiDiffs.addAll(DiffUtil.preBufferDifference(currentDiff, previousDiff, oldArray, newArray, DIFF_UNCHANGED_LINE_DISPLAY));
			changedLineWikiDiffs = DiffUtil.processDifference(currentDiff, oldArray, newArray);
			// loop through the difference and diff the individual lines so that it is possible to highlight the exact
			// text that was changed
			for (WikiDiff changedLineWikiDiff : changedLineWikiDiffs) {
				oldLineArray = DiffUtil.stringToArray(changedLineWikiDiff.getOldText());
				newLineArray = DiffUtil.stringToArray(changedLineWikiDiff.getNewText());
				changedLineDiffs = new Diff<String>(oldLineArray, newLineArray).diff();
				wikiSubDiffs = new ArrayList<WikiDiff>();
				int j = 0;
				for (Difference changedLineDiff : changedLineDiffs) {
					// build sub-diff list, which is the difference for the individual
					// line item
					j++;
					if (j == 1) {
						// pre-buffering is only necessary for the first element as post-buffering
						// will handle all further buffering when bufferAmount is -1.
						wikiSubDiffs.addAll(DiffUtil.preBufferDifference(changedLineDiff, null, oldLineArray, newLineArray, -1));
					}
					wikiSubDiffs.addAll(DiffUtil.processDifference(changedLineDiff, oldLineArray, newLineArray));
					nextLineDiff = (j < changedLineDiffs.size()) ? changedLineDiffs.get(j) : null;
					wikiSubDiffs.addAll(DiffUtil.postBufferDifference(changedLineDiff, nextLineDiff, oldLineArray, newLineArray, -1));
				}
				changedLineWikiDiff.setSubDiffs(wikiSubDiffs);
			}
			wikiDiffs.addAll(changedLineWikiDiffs);
			nextDiff = (i < diffs.size()) ? diffs.get(i) : null;
			wikiDiffs.addAll(DiffUtil.postBufferDifference(currentDiff, nextDiff, oldArray, newArray, DIFF_UNCHANGED_LINE_DISPLAY));
			previousDiff = currentDiff;
		}
		return wikiDiffs;
	}

	/**
	 *
	 */
	private static boolean hasMoreDiffInfo(int addedCurrent, int deletedCurrent, Difference currentDiff) {
		if (addedCurrent == -1) {
			addedCurrent = 0;
		}
		if (deletedCurrent == -1) {
			deletedCurrent = 0;
		}
		return (addedCurrent <= currentDiff.getAddedEnd() || deletedCurrent <= currentDiff.getDeletedEnd());
	}

	/**
	 * If possible, append a few lines of unchanged text that appears after to the changed line
	 * in order to add context to the current list of WikiDiff objects.
	 *
	 * @param currentDiff The current diff object.
	 * @param nextDiff The diff object that immediately follows this object (if any).
	 * @param oldArray The original array of string objects that was compared from in order to
	 *  generate the diff.
	 * @param newArray The original array of string objects that was compared to in order to
	 *  generate the diff.
	 * @param bufferAmount The number of unchanged elements to display after the diff, or -1 if
	 *  all unchanged lines should be displayed.
	 */
	private static List<WikiDiff> postBufferDifference(Difference currentDiff, Difference nextDiff, String[] oldArray, String[] newArray, int bufferAmount) {
		List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>();
		if (bufferAmount == 0) {
			// do not buffer
			return wikiDiffs;
		}
		int deletedCurrent = (currentDiff.getDeletedEnd() == -1) ? currentDiff.getDeletedStart() : (currentDiff.getDeletedEnd() + 1);
		int addedCurrent = (currentDiff.getAddedEnd() == -1) ? currentDiff.getAddedStart() : (currentDiff.getAddedEnd() + 1);
		int numIterations = bufferAmount;
		if (bufferAmount == -1) {
			// buffer everything
			numIterations = (nextDiff != null) ? Math.max(nextDiff.getAddedStart() - addedCurrent, nextDiff.getDeletedStart() - deletedCurrent) : Math.max(oldArray.length - deletedCurrent, newArray.length - addedCurrent);
		}
		String oldText = null;
		String newText = null;
		for (int i = 0; i < numIterations; i++) {
			int position = (deletedCurrent < 0) ? 0 : deletedCurrent;
			oldText = null;
			newText = null;
			if (canPostBuffer(nextDiff, deletedCurrent, oldArray, false)) {
				oldText = oldArray[deletedCurrent];
				deletedCurrent++;
			}
			if (canPostBuffer(nextDiff, addedCurrent, newArray, true)) {
				newText = newArray[addedCurrent];
				addedCurrent++;
			}
			if (oldText == null && newText == null) {
				logger.debug("Possible DIFF bug: no elements post-buffered.  position: " + position + " / deletedCurrent: " + deletedCurrent + " / addedCurrent " + addedCurrent + " / numIterations: " + numIterations);
				break;
			}
			wikiDiffs.add(new WikiDiff(oldText, newText, position));
		}
		return wikiDiffs;
	}

	/**
	 * If possible, prepend a few lines of unchanged text that before after to the changed line
	 * in order to add context to the current list of WikiDiff objects.
	 *
	 * @param currentDiff The current diff object.
	 * @param previousDiff The diff object that immediately preceded this object (if any).
	 * @param oldArray The original array of string objects that was compared from in order to
	 *  generate the diff.
	 * @param newArray The original array of string objects that was compared to in order to
	 *  generate the diff.
	 * @param bufferAmount The number of unchanged elements to display after the diff, or -1 if
	 *  all unchanged lines should be displayed.
	 */
	private static List<WikiDiff> preBufferDifference(Difference currentDiff, Difference previousDiff, String[] oldArray, String[] newArray, int bufferAmount) {
		List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>();
		if (bufferAmount == 0) {
			return wikiDiffs;
		}
		if (bufferAmount == -1 && previousDiff != null) {
			// when buffering everything, only pre-buffer for the first element as the post-buffer code
			// will handle everything else.
			return wikiDiffs;
		}
		// deletedCurrent is the current position in oldArray to start buffering from
		int deletedCurrent = (bufferAmount == -1 || bufferAmount > currentDiff.getDeletedStart()) ? 0 : (currentDiff.getDeletedStart() - bufferAmount);
		// addedCurrent is the current position in newArray to start buffering from
		int addedCurrent = (bufferAmount == -1 || bufferAmount > currentDiff.getAddedStart()) ? 0 : (currentDiff.getAddedStart() - bufferAmount);
		if (previousDiff != null) {
			// if there was a previous diff make sure that it is not being overlapped
			deletedCurrent = Math.max(previousDiff.getDeletedEnd() + 1, deletedCurrent);
			addedCurrent = Math.max(previousDiff.getAddedEnd() + 1, addedCurrent);
		}
		// number of iterations is number of loops required to fully buffer the added and deleted diff
		int numIterations = Math.max(currentDiff.getDeletedStart() - deletedCurrent, currentDiff.getAddedStart() - addedCurrent);
		String oldText = null;
		String newText = null;
		for (int i = 0; i < numIterations; i++) {
			int position = (deletedCurrent < 0) ? 0 : deletedCurrent;
			oldText = null;
			newText = null;
			// if diffs are close together, do not allow buffers to overlap
			if (canPreBuffer(previousDiff, deletedCurrent, currentDiff.getDeletedStart(), oldArray, bufferAmount, false)) {
				oldText = oldArray[deletedCurrent];
				deletedCurrent++;
			}
			if (canPreBuffer(previousDiff, addedCurrent, currentDiff.getAddedStart(), newArray, bufferAmount, true)) {
				newText = newArray[addedCurrent];
				addedCurrent++;
			}
			if (oldText == null && newText == null) {
				logger.debug("Possible DIFF bug: no elements pre-buffered.  position: " + position + " / deletedCurrent: " + deletedCurrent + " / addedCurrent " + addedCurrent + " / numIterations: " + numIterations);
				break;
			}
			wikiDiffs.add(new WikiDiff(oldText, newText, position));
		}
		return wikiDiffs;
	}

	/**
	 * @param newVersion The String that is being compared to.
	 * @param oldVersion The String that is being compared against.
	 */
	private static List<WikiDiff> process(String newVersion, String oldVersion) {
		if (logger.isTraceEnabled()) {
			logger.trace("Diffing: " + oldVersion + " against: " + newVersion);
		}
		if (newVersion.equals(oldVersion)) {
			return new ArrayList<WikiDiff>();
		}
		String[] oldArray = DiffUtil.split(oldVersion);
		String[] newArray = DiffUtil.split(newVersion);
		Diff<String> diffObject = new Diff<String>(oldArray, newArray);
		List<Difference> diffs = diffObject.diff();
		return DiffUtil.generateWikiDiffs(diffs, oldArray, newArray);
	}

	/**
	 * Process the diff object and add it to the output.  Text will either have been
	 * deleted or added (it cannot have remained the same, since a diff object represents
	 * a change).  This method steps through the diff result and converts it into an
	 * array of objects that can be used to easily represent the diff.
	 */
	private static List<WikiDiff> processDifference(Difference currentDiff, String[] oldArray, String[] newArray) {
		List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>();
		// if text was deleted then deletedCurrent represents the starting position of the deleted text.
		int deletedCurrent = currentDiff.getDeletedStart();
		// if text was added then addedCurrent represents the starting position of the added text.
		int addedCurrent = currentDiff.getAddedStart();
		// count is simply used to ensure that the loop is not infinite, which should never happen
		int count = 0;
		// the text of the element that changed
		String oldText = null;
		// the text of what the element was changed to
		String newText = null;
		while (hasMoreDiffInfo(addedCurrent, deletedCurrent, currentDiff)) {
			// the position within the diff array (line number, character, etc) at which the change
			// started (starting at 0)
			int position = ((deletedCurrent < 0) ? 0 : deletedCurrent);
			oldText = null;
			newText = null;
			if (currentDiff.getDeletedEnd() >= 0 && currentDiff.getDeletedEnd() >= deletedCurrent) {
				oldText = oldArray[deletedCurrent];
				deletedCurrent++;
			}
			if (currentDiff.getAddedEnd() >= 0 && currentDiff.getAddedEnd() >= addedCurrent) {
				newText = newArray[addedCurrent];
				addedCurrent++;
			}
			wikiDiffs.add(new WikiDiff(oldText, newText, position));
			// FIXME - this shouldn't be necessary
			count++;
			if (count > 5000) {
				logger.warn("Infinite loop in DiffUtils.processDifference");
				break;
			}
		}
		return wikiDiffs;
	}

	/**
	 * Determine if diff information is available in the cache.  If so return it,
	 * otherwise return <code>null</code>.
	 */
	private static List<WikiDiff> retrieveFromCache(String newVersion, String oldVersion) throws DataAccessException {
		String key = generateCacheKey(newVersion, oldVersion);
		return CACHE_DIFF_INFORMATION.retrieveFromCache(key);
	}

	/**
	 * Split up a String into an array of values using the specified string pattern.
	 *
	 * @param original The value that is being split.
	 */
	private static String[] split(String original) {
		if (original == null) {
			return new String[0];
		}
		return original.split("\n");
	}

	/**
	 * Convert a string to a string array of characters.
	 *
	 * @param original The value that is being split.
	 */
	private static String[] stringToArray(String original) {
		if (original == null) {
			return new String[0];
		}
		String[] result = new String[original.length()];
		for (int i = 0; i < result.length; i++) {
			result[i] = String.valueOf(original.charAt(i));
		}
		return result;
	}
}
