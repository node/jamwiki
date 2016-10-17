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
import org.jamwiki.DataAccessException;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.parser.image.ImageMetadata;
import org.jamwiki.parser.image.ImageUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * JSP tag used to build an HTML image link for a specified topic that
 * corresponds to an image that has been uploaded to the Wiki.
 */
public class ImageLinkTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageLinkTag.class.getName());
	private String allowEnlarge = null;
	private WikiFileVersion fileVersion = null;
	private String maxHeight = null;
	private String maxWidth = null;
	private String style = null;
	private String value = null;
	private String virtualWiki = null;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		int linkHeight = (this.maxHeight != null) ? Integer.valueOf(this.maxHeight) : -1;
		int linkWidth = (this.maxWidth != null) ? Integer.valueOf(this.maxWidth) : -1;
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String tagVirtualWiki = (StringUtils.isBlank(this.virtualWiki)) ? WikiUtil.getVirtualWikiFromRequest(request) : this.virtualWiki;
		String html = null;
		ImageMetadata imageMetadata = new ImageMetadata();
		imageMetadata.setMaxHeight(linkHeight);
		imageMetadata.setMaxWidth(linkWidth);
		// set the link field empty to prevent the image from being clickable
		imageMetadata.setLink("");
		if (this.allowEnlarge != null) {
			imageMetadata.setAllowEnlarge(Boolean.valueOf(this.allowEnlarge));
		}
		try {
			try {
				html = ImageUtil.buildImageLinkHtml(request.getContextPath(), tagVirtualWiki, this.value, imageMetadata, this.style, true, this.fileVersion);
			} catch (IOException e) {
				// FIXME - display a broken image icon or something better
				logger.warn("I/O Failure while parsing image link: " + e.getMessage(), e);
				html = this.value;
			} catch (DataAccessException e) {
				logger.error("Failure while building url " + html + " with value " + this.value, e);
				throw new JspException(e);
			}
			if (html != null) {
				this.pageContext.getOut().print(html);
			}
		} catch (IOException e) {
			logger.error("Failure while building url " + html + " with value " + this.value, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
	}

	/**
	 *
	 */
	public String getAllowEnlarge() {
		return this.allowEnlarge;
	}

	/**
	 *
	 */
	public void setAllowEnlarge(String allowEnlarge) {
		this.allowEnlarge = allowEnlarge;
	}

	/**
	 * If rendering an image for anything but the current version then
	 * the file version object must be supplied.
	 */
	public WikiFileVersion getFileVersion() {
		return this.fileVersion;
	}

	/**
	 * If rendering an image for anything but the current version then
	 * the file version object must be supplied.
	 */
	public void setFileVersion(WikiFileVersion fileVersion) {
		this.fileVersion = fileVersion;
	}

	/**
	 *
	 */
	public String getMaxHeight() {
		return this.maxHeight;
	}

	/**
	 *
	 */
	public void setMaxHeight(String maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 *
	 */
	public String getMaxWidth() {
		return this.maxWidth;
	}

	/**
	 *
	 */
	public void setMaxWidth(String maxWidth) {
		this.maxWidth = maxWidth;
	}

	/**
	 *
	 */
	public String getStyle() {
		return this.style;
	}

	/**
	 *
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 *
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 *
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 *
	 */
	public String getVirtualWiki() {
		return this.virtualWiki;
	}

	/**
	 *
	 */
	public void setVirtualWiki(String virtualWiki) {
		this.virtualWiki = virtualWiki;
	}
}
