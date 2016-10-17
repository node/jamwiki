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
package org.jamwiki.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.WikiVersion;

/**
 * JSP tag that displays the current Wiki version as specified by
 * {@link org.jamwiki.WikiVersion#CURRENT_WIKI_VERSION}.
 */
public class WikiVersionTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiVersionTag.class.getName());

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			this.pageContext.getOut().print(WikiVersion.CURRENT_WIKI_VERSION);
		} catch (IOException e) {
			logger.error("Failure while retrieving Wiki version", e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}
}
