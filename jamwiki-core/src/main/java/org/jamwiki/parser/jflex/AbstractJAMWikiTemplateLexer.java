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

import org.jamwiki.Environment;
import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class that is extended by the template JFlex lexer.
 */
public abstract class AbstractJAMWikiTemplateLexer extends JFlexLexer {

	protected static final WikiLogger logger = WikiLogger.getLogger(AbstractJAMWikiTemplateLexer.class.getName());
	/** String used to build up current template tag content. */
	protected StringBuilder templateString = new StringBuilder();

	/**
	 * Utility method used to indicate whether templates are allowed in wiki syntax
	 * or not.
	 */
	protected boolean allowTemplates() {
		return Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_TEMPLATES);
	}
}
