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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * This class provides capability to format an edit comment, parsing out any
 * section name that is present and converting it into a link to the section.
 */
public class EditCommentTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(EditCommentTag.class.getName());
	private String comment = null;
	private String topic = null;

	/**
	 * Generate the tag HTML output.
	 */
	public int doEndTag() throws JspException {
		try {
			this.pageContext.getOut().print(this.parseComment());
		} catch (IOException e) {
			logger.error("Failure while building edit comment for comment " + this.comment, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 * Return the full (un-parsed) edit comment.
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * Set the full (un-parsed) edit comment.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Return the topic name
	 */
	public String getTopic() {
		return this.topic;
	}

	/**
	 * Set the topic name.
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * Process the edit comment and return a parsed output string.
	 */
	private String parseComment() throws ParserException {
		if (StringUtils.isBlank(this.getComment())) {
			return "";
		}
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String virtualWiki = WikiUtil.getVirtualWikiFromRequest(request);
		ParserInput parserInput = new ParserInput(virtualWiki, this.getTopic());
		parserInput.setContext(request.getContextPath());
		parserInput.setLocale(request.getLocale());
		return ParserUtil.parseEditComment(parserInput, this.getComment());
	}
}