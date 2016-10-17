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
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.jamwiki.WikiException;
import org.jamwiki.model.Watchlist;
import org.jamwiki.servlets.ServletUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * JSP tag used to highlight the tag content if the specified topic is in the
 * current users's watchlist.
 */
public class WatchlistTag extends BodyTagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(WatchlistTag.class.getName());
	private String topic = null;

	/**
	 *
	 */
	public int doStartTag() throws JspException {
		if (this.isWatchedTopic()) {
			try {
				this.pageContext.getOut().print("<strong>");
			} catch (IOException e) {
				logger.error("Failure processing watchlist item " + this.topic, e);
				throw new JspException(e);
			}
		}
		return EVAL_BODY_INCLUDE;
	}

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		if (this.isWatchedTopic()) {
			try {
				this.pageContext.getOut().print("</strong>");
			} catch (IOException e) {
				logger.error("Failure processing watchlist item " + this.topic, e);
				throw new JspException(e);
			}
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	private boolean isWatchedTopic() throws JspException {
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String virtualWiki = WikiUtil.getVirtualWikiFromRequest(request);
		try {
			Watchlist watchlist = ServletUtil.currentWatchlist(request, virtualWiki);
			return (watchlist.containsTopic(this.topic));
		} catch (WikiException e) {
			logger.error("Failure processing watchlist item " + this.topic, e);
			throw new JspException(e);
		}
	}

	/**
	 *
	 */
	public String getTopic() {
		return this.topic;
	}

	/**
	 *
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}
}
