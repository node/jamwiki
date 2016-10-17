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
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.parser.image.ImageUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * JSP tag used to build a URL to an image file.  This tag does NOT
 * generate image HTML, instead it simply returns the URL to the image
 * file.
 */
public class ImageUrlTag extends TagSupport {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageUrlTag.class.getName());
	private WikiFileVersion fileVersion;
	private String topicName;
	private String virtualWiki;

	/**
	 *
	 */
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest)this.pageContext.getRequest();
		String tagVirtualWiki = (StringUtils.isBlank(this.virtualWiki)) ? WikiUtil.getVirtualWikiFromRequest(request) : this.virtualWiki;
		try {
			String relativeFileUrl = null;
			if (fileVersion != null) {
				relativeFileUrl = fileVersion.getUrl();
				if (!ImageUtil.isImagesOnFS()) {
					relativeFileUrl = ImageUtil.buildDatabaseRelativeUrl(fileVersion.getFileId(), fileVersion.getFileVersionId(), null, fileVersion.getUrl());
				}
			} else {
				WikiFile wikiFile = WikiBase.getDataHandler().lookupWikiFile(tagVirtualWiki, this.topicName);
				if (wikiFile == null) {
				}
				relativeFileUrl = wikiFile.getUrl();
				if (!ImageUtil.isImagesOnFS()) {
					relativeFileUrl = ImageUtil.buildDatabaseRelativeUrl(wikiFile.getFileId(), null, null, wikiFile.getUrl());
				}
			}
			String url = ImageUtil.buildImageUrl(request.getContextPath(), relativeFileUrl, false);
			this.pageContext.getOut().print(url);
		} catch (IOException e) {
			logger.error("Failure while building image url for topic " + this.topicName, e);
			throw new JspException(e);
		} catch (DataAccessException e) {
			logger.error("Failure while building image url for topic " + this.topicName, e);
			throw new JspException(e);
		}
		return EVAL_PAGE;
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
	public String getTopicName() {
		return this.topicName;
	}

	/**
	 *
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
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
