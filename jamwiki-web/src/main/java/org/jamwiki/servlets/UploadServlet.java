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
package org.jamwiki.servlets;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.ImageData;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.image.ImageUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to handle file uploads.
 */
public class UploadServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(UploadServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_UPLOAD = "upload.jsp";

	/**
	 * Override defaults to enable user blocking.
	 */
	public UploadServlet() {
		this.blockable = true;
	}

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String contentType = ((request.getContentType() != null) ? request.getContentType().toLowerCase() : "" );
		if (contentType.indexOf("multipart") == -1) {
			view(request, next, pageInfo);
		} else {
			upload(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private String processDestinationFilename(String virtualWiki, String destinationFilename, String filename) {
		if (StringUtils.isBlank(destinationFilename)) {
			return destinationFilename;
		}
		if (!StringUtils.isBlank(FilenameUtils.getExtension(filename)) && StringUtils.isBlank(FilenameUtils.getExtension(destinationFilename))) {
			// if original has an extension, the renamed version must as well
			destinationFilename += (!destinationFilename.endsWith(".") ? "." : "") + FilenameUtils.getExtension(filename);
		}
		// if the user entered a file name of the form "File:Foo.jpg" strip the namespace
		return StringUtils.removeStart(destinationFilename, Namespace.namespace(Namespace.FILE_ID).getLabel(virtualWiki) + Namespace.SEPARATOR);
	}

	/**
	 *
	 */
	private void upload(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		List<FileItem> fileItems = ServletUtil.processMultipartRequest(request);
		String filename = null;
		String destinationFilename = null;
		String contentType = null;
		long fileSize = 0;
		String contents = null;
		boolean isImage = true;
		File uploadedFile = null;
		String url = null;
		byte buff[] = null;
		for (FileItem fileItem : fileItems) {
			String fieldName = fileItem.getFieldName();
			if (fileItem.isFormField()) {
				if (fieldName.equals("description")) {
					// FIXME - these should be parsed
					contents = fileItem.getString("UTF-8");
				} else if (fieldName.equals("destination")) {
					destinationFilename = fileItem.getString("UTF-8");
				}
				continue;
			}
			// file name can have encoding issues, so manually convert
			filename = fileItem.getName();
			if (filename == null) {
				throw new WikiException(new WikiMessage("upload.error.filename"));
			}
			filename = ImageUtil.sanitizeFilename(filename);
			url = ImageUtil.generateFileUrl(virtualWiki, filename, null);
			if (!ImageUtil.isFileTypeAllowed(filename)) {
				String extension = FilenameUtils.getExtension(filename);
				throw new WikiException(new WikiMessage("upload.error.filetype", extension));
			}
			fileSize = fileItem.getSize();
			contentType = fileItem.getContentType();
			if (ImageUtil.isImagesOnFS()) {
				uploadedFile = ImageUtil.buildAbsoluteFile(url);
				fileItem.write(uploadedFile);
				isImage = ImageUtil.isImage(uploadedFile);
			} else {
				buff = fileItem.get();
			}
		}
		if (ImageUtil.isImagesOnFS() && uploadedFile == null) {
			throw new WikiException(new WikiMessage("upload.error.filenotfound"));
		}
		destinationFilename = processDestinationFilename(virtualWiki, destinationFilename, filename);
		String pageName = ImageUtil.generateFilePageName((!StringUtils.isEmpty(destinationFilename) ? destinationFilename : filename));
		if (this.handleSpam(request, pageInfo, pageName, contents, null)) {
			if (ImageUtil.isImagesOnFS()) {
				// delete the spam file
				uploadedFile.delete();
			}
			this.view(request, next, pageInfo);
			next.addObject("contents", contents);
			return;
		}
		if (!StringUtils.isEmpty(destinationFilename)) {
			// rename the uploaded file if a destination file name was specified
			filename = ImageUtil.sanitizeFilename(destinationFilename);
			url = ImageUtil.generateFileUrl(virtualWiki, filename, null);
			if (ImageUtil.isImagesOnFS()) {
				File renamedFile = ImageUtil.buildAbsoluteFile(url);
				if (!uploadedFile.renameTo(renamedFile)) {
					throw new WikiException(new WikiMessage("upload.error.filerename", destinationFilename));
				}
			}
		}
		ImageData imageData = null;
		if (!ImageUtil.isImagesOnFS()) {
			imageData = processImageData(contentType, buff);
			isImage = (imageData.width >= 0);
		}
		String ipAddress = ServletUtil.getIpAddress(request);
		WikiUser user = ServletUtil.currentWikiUser();
		Topic topic = ImageUtil.writeImageTopic(virtualWiki, pageName, contents, user, isImage, ipAddress);
		WikiFileVersion wikiFileVersion = new WikiFileVersion();
		wikiFileVersion.setUploadComment(topic.getTopicContent());
		ImageUtil.writeWikiFile(topic, wikiFileVersion, user, ipAddress, filename, url, contentType, fileSize, imageData);
		ServletUtil.redirect(next, virtualWiki, topic.getName());
	}

	/**
	 * @return ImageData object from uploaded binary data.
	 */
	private ImageData processImageData(String contentType, byte buff[]) {
		int width = -1;
		int height = -1; 
		try {
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(buff));
			if (image != null) {
				width = image.getWidth ();
				height = image.getHeight();
			}
		} catch (IOException e) {
			logger.info("Failure while processing image", e);
		}
		return new ImageData(contentType, width, height, buff);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		pageInfo.setPageTitle(new WikiMessage("upload.title"));
		pageInfo.setContentJsp(JSP_UPLOAD);
		next.addObject("uploadDestination", WikiUtil.getTopicFromRequest(request));
		pageInfo.setSpecial(true);
	}
}
