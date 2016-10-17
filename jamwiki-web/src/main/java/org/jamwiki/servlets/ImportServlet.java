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
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.migrate.MigrationException;
import org.jamwiki.migrate.MigrationUtil;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to import an XML file creating a topic as a result.
 */
public class ImportServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(ImportServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_IMPORT = "import.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String contentType = ((request.getContentType() == null) ? "" : request.getContentType().toLowerCase());
		if (contentType.indexOf("multipart") == -1) {
			view(request, next, pageInfo);
		} else {
			importFile(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void importFile(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		try {
			List<FileItem> fileItems = ServletUtil.processMultipartRequest(request);
			for (FileItem fileItem : fileItems) {
				if (fileItem.isFormField()) {
					continue;
				}
				File file = saveFileItem(fileItem);
				WikiUser user = ServletUtil.currentWikiUser();
				String virtualWiki = pageInfo.getVirtualWikiName();
				String ipAddress = ServletUtil.getIpAddress(request);
				Locale locale = request.getLocale();
				List<String> successfulImports = MigrationUtil.importFromFile(file, virtualWiki, user, ipAddress, locale);
				file.delete();
				next.addObject("successfulImports", successfulImports);
				break;
			}
		} catch (MigrationException e) {
			logger.error("Failure while importing from file", e);
			pageInfo.addError(new WikiMessage("import.error.migration", e.getMessage()));
		} catch (WikiException e) {
			logger.error("Failure while importing from file", e);
			pageInfo.addError(e.getWikiMessage());
		}
		view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		pageInfo.setContentJsp(JSP_IMPORT);
		pageInfo.setPageTitle(new WikiMessage("import.title"));
		pageInfo.setSpecial(true);
	}

	/**
	 *
	 */
	private File saveFileItem(FileItem item) throws WikiException {
		// upload user file to the server
		File directory = WikiUtil.getTempDirectory();
		if (!directory.exists()) {
			throw new WikiException(new WikiMessage("upload.error.directorycreate", directory.getAbsolutePath()));
		}
		// use current timestamp as unique file name
		String filename = System.currentTimeMillis() + ".xml";
		File xmlFile = new File(directory, filename);
		// transfer remote file
		try {
			item.write(xmlFile);
		} catch (Exception e) {
			logger.error("Failure while saving uploaded file item", e);
			throw new WikiException(new WikiMessage("error.unknown", e.getMessage()));
		}
		return xmlFile;
	}
}
