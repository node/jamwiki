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
import org.jamwiki.DataAccessException;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiUtil;

/**
 * JSP tag used to generate a pagination object.
 */
public class PaginationTag extends BodyTagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(PaginationTag.class.getName());

	private String rootUrl = null;
	private String total = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		try {
			this.pageContext.getOut().print(pagination(this.rootUrl, Integer.valueOf(this.total)));
		} catch (IOException e) {
			logger.error("Failure while building pagination object", e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	private StringBuilder buildOption(int num, Pagination pagination, String baseUrl) {
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		StringBuilder output = new StringBuilder();
		if (num == pagination.getNumResults()) {
			output.append(num);
			return output;
		}
		output.append("<a href=\"");
		String virtualWiki = WikiUtil.getVirtualWikiFromRequest(request);
		WikiLink wikiLink = LinkUtil.parseWikiLink(request.getContextPath(), virtualWiki, baseUrl);
		String query = LinkUtil.appendQueryParam(wikiLink.getQuery(), "num", Integer.toString(num));
		query += "&amp;offset=0";
		wikiLink.setQuery(query);
		try {
			output.append(LinkUtil.buildTopicUrl(wikiLink));
		} catch (DataAccessException e) {
			logger.warn("Failure while building pagination element", e);
			return new StringBuilder();
		}
		output.append("\">");
		output.append(num);
		output.append("</a>");
		return output;
	}

	/**
	 *
	 */
	public String getRootUrl() {
		return this.rootUrl;
	}

	/**
	 *
	 */
	public String getTotal() {
		return this.total;
	}

	/**
	 *
	 */
	private StringBuilder nextPage(Pagination pagination, String baseUrl, int count, boolean previous) {
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		StringBuilder output = new StringBuilder();
		Object[] objects = new Object[1];
		objects[0] = pagination.getNumResults();
		if (pagination.getOffset() == 0 && previous) {
			output.append(Utilities.formatMessage("common.pagination.previous", request.getLocale(), objects));
			return output;
		}
		if (pagination.getNumResults() != count && !previous) {
			output.append(Utilities.formatMessage("common.pagination.next", request.getLocale(), objects));
			return output;
		}
		output.append("<a href=\"");
		String virtualWiki = WikiUtil.getVirtualWikiFromRequest(request);
		WikiLink wikiLink = LinkUtil.parseWikiLink(request.getContextPath(), virtualWiki, baseUrl);
		int offset = pagination.getOffset() + pagination.getNumResults();
		if (previous) {
			offset = pagination.getOffset() - pagination.getNumResults();
			if (offset < 0) {
				offset = 0;
			}
		}
		String query = LinkUtil.appendQueryParam(wikiLink.getQuery(), "num", Integer.toString(pagination.getNumResults()));
		query += "&amp;offset=" + offset;
		wikiLink.setQuery(query);
		try {
			output.append(LinkUtil.buildTopicUrl(wikiLink));
		} catch (DataAccessException e) {
			logger.warn("Failure while building pagination element", e);
			return new StringBuilder();
		}
		output.append("\">");
		if (previous) {
			output.append(Utilities.formatMessage("common.pagination.previous", request.getLocale(), objects));
		} else {
			output.append(Utilities.formatMessage("common.pagination.next", request.getLocale(), objects));
		}
		output.append("</a>");
		return output;
	}

	/**
	 *
	 */
	private StringBuilder numResults(Pagination pagination, String baseUrl) {
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		StringBuilder output = new StringBuilder();
		output.append(Utilities.formatMessage("common.pagination.results", request.getLocale())).append(":&#160;");
		output.append(buildOption(10, pagination, baseUrl));
		output.append("&#160;|&#160;");
		output.append(buildOption(25, pagination, baseUrl));
		output.append("&#160;|&#160;");
		output.append(buildOption(50, pagination, baseUrl));
		output.append("&#160;|&#160;");
		output.append(buildOption(100, pagination, baseUrl));
		output.append("&#160;|&#160;");
		output.append(buildOption(250, pagination, baseUrl));
		output.append("&#160;|&#160;");
		output.append(buildOption(500, pagination, baseUrl));
		return output;
	}

	/**
	 *
	 */
	private String pagination(String baseUrl, int count) {
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		Pagination pagination = WikiUtil.buildPagination(request);
		StringBuilder output = new StringBuilder();
		output.append(this.nextPage(pagination, baseUrl, count, true));
		output.append("&#160;|&#160;");
		output.append(this.nextPage(pagination, baseUrl, count, false));
		output.append("&#160;&#160;(");
		output.append(this.numResults(pagination, baseUrl));
		output.append(')');
		return output.toString();
	}

	/**
	 *
	 */
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	/**
	 *
	 */
	public void setTotal(String total) {
		this.total = total;
	}
}
