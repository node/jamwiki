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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.io.IOUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.ImageData;
import org.jamwiki.model.WikiFile;
import org.jamwiki.parser.image.ImageUtil;
import org.jamwiki.utils.WikiLogger;

/**
 * Get image requests handler.
 */
public class ImageServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageServlet.class.getName());

	/**
	 * This servlet requires slightly different initialization parameters from most
	 * servlets.
	 */
	public ImageServlet() {
		this.layout = false;
	}

	/**
	 * Handle image requests, returning the binary image data.  This method
	 * can be invoked either via the Special:Image path, for database files,
	 * or via a path that appears to end users as any other image request but
	 * that is actually a servlet request that will serve a file from the
	 * filesystem.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws ServletException, IOException {
		File file = this.retrieveFile(request);
		if (file == null) {
			this.streamFileFromDatabase(request, response);
		} else {
			this.streamFileFromFileSystem(file, response);
		}
		return null;
	}

	/**
	 * If a file corresponding to the request is on the filesystem return it,
	 * otherwise return <code>null</code>.
	 */
	private File retrieveFile(HttpServletRequest request) {
		String filename = request.getRequestURI().substring(request.getContextPath().length());
		try {
			filename = URLDecoder.decode(filename, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// this doesn't happen - UTF-8 is always supported
		}
		File file = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), filename);
		return (file.exists()) ? file : null;
	}

	/**
	 * Serve a file from the database.  In some cases users may choose to store
	 * files directly in the database, and this method provides a way of serving
	 * those files.
	 */
	private void streamFileFromDatabase(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String filename = request.getRequestURI().substring(request.getContextPath().length() + 1);
		Object[] args;
		try {
			args = ImageUtil.DB_FILE_URL_FORMAT.parse(filename);
		} catch (ParseException e) {
			logger.debug("Invalid database file request: " + filename);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		int fileId = Integer.parseInt(args[1].toString());
		int fileVersionId = Integer.parseInt(args[2].toString());
		int resized = Integer.parseInt(args[3].toString());
		ImageData imageData;
		try {
			if (fileVersionId != 0) {
				imageData = WikiBase.getDataHandler().getImageVersionData(fileVersionId, resized);
			} else {
				imageData = WikiBase.getDataHandler().getImageData(fileId, resized);
			}
		} catch (DataAccessException dae) {
			throw new ServletException(dae);
		}
		if (imageData == null) {
			logger.debug("Database file does not exist: fileId=" + fileId + " / fileVersionId=" + fileVersionId + " / resized=" + resized + " / request=" + request.getRequestURI());
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		response.setContentType(imageData.mimeType);
		response.setContentLength(imageData.data.length);
		OutputStream os = null;
		try {
			os = response.getOutputStream();
			os.write(imageData.data);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}

	/**
	 * Serve a file from the filesystem.  This is less efficient than serving the file
	 * directly via Tomcat or Apache, but allows files to be stored outside of the
	 * webapp and thus keeps wiki data (files) separate from application code.
	 */
	private void streamFileFromFileSystem(File file, HttpServletResponse response) throws ServletException, IOException {
		ServletOutputStream out = null;
		InputStream in = null;
		if (file.isDirectory() || !file.canRead()) {
			logger.debug("File does not exist: " + file.getAbsolutePath());
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String mimeType = getServletContext().getMimeType(file.getAbsolutePath());
		if (mimeType == null) {
			mimeType = WikiFile.UNKNOWN_MIME_TYPE;
		}
		try {
			response.setContentType(mimeType);
			response.setContentLength((int)file.length());
			out = response.getOutputStream();
			in = new FileInputStream(file);
			IOUtils.copy(in, out);
			out.flush();
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
}
