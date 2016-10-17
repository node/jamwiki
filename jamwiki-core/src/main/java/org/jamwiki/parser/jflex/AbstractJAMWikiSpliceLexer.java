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

import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class that is extended by the JFlex splice lexer.
 */
public abstract class AbstractJAMWikiSpliceLexer extends JFlexLexer {

	protected static final WikiLogger logger = WikiLogger.getLogger(AbstractJAMWikiSpliceLexer.class.getName());
	protected int section = 0;
	protected int sectionDepth = 0;
	protected int targetSection = 0;
	protected String replacementText;
	protected boolean inTargetSection = false;

	/**
	 *
	 */
	protected String processHeading(int level, String headingText, int tagType) {
		this.section++;
		if (inTargetSection && this.sectionDepth >= level) {
			inTargetSection = false;
		} else if (this.targetSection == this.section) {
			this.parse(tagType, headingText, level);
			inTargetSection = true;
			this.sectionDepth = level;
			if (this.mode == JFlexParser.MODE_SPLICE) return this.replacementText;
		}
		return returnText(headingText);
	}

	/**
	 *
	 */
	protected String returnText(String text) {
		return ((inTargetSection && this.mode == JFlexParser.MODE_SPLICE) || (!inTargetSection && this.mode == JFlexParser.MODE_SLICE)) ? "" : text;
	}

	/**
	 *
	 */
	protected void setReplacementText(String replacementText) {
		// replacementText must end with a newline, otherwise sections get spliced together
		if (replacementText == null) return;
		if (!replacementText.endsWith("\n")) {
			replacementText += "\n";
		}
		this.replacementText = replacementText;
	}

	/**
	 *
	 */
	protected void setTargetSection(int targetSection) {
		this.targetSection = targetSection;
	}
}
