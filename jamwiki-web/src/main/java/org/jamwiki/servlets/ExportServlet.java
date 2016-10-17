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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.migrate.MigrationException;
import org.jamwiki.migrate.MigrationUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to export an XML file from a wiki topic.
 */
public class ExportServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(ExportServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_EXPORT = "export.jsp";

	/**
	 *
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		// if a file is ready to export then export it as XML, otherwise display a page
		this.layout = true;
		if (!StringUtils.isBlank(request.getParameter("export")) && this.exportFile(request, response, next, pageInfo)) {
			// do not load defaults or redirect if processing is successful - return as raw XML
			this.layout = false;
			return null;
		}
		this.view(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private boolean exportFile(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) {
		String topicNames = request.getParameter("topics");
		boolean download = (!StringUtils.isBlank(request.getParameter("download")));
		boolean excludeHistory = (!StringUtils.isBlank(request.getParameter("history")));
		boolean success = false;
		try {
			if (StringUtils.isBlank(topicNames)) {
				throw new WikiException(new WikiMessage("export.error.notopic"));
			}
			// write export file to the server
			File directory = WikiUtil.getTempDirectory();
			if (!directory.exists()) {
				throw new WikiException(new WikiMessage("upload.error.directorycreate", directory.getAbsolutePath()));
			}
			// use current timestamp as unique file name
			String filename = System.currentTimeMillis() + ".xml";
			File file = new File(directory, filename);
			List<String> topicNameList = Arrays.asList(StringUtils.split(topicNames, "\n\r"));
			String virtualWiki = pageInfo.getVirtualWikiName();
			MigrationUtil.exportToFile(file, virtualWiki, topicNameList, excludeHistory);
			this.renderXml(response, file, download);
			success = true;
			file.delete();
		} catch (IOException e) {
			logger.error("Failure while exporting to file", e);
			pageInfo.addError(new WikiMessage("export.error.migration", e.getMessage()));
		} catch (MigrationException e) {
			logger.error("Failure while exporting from file", e);
			pageInfo.addError(new WikiMessage("export.error.migration", e.getMessage()));
		} catch (WikiException e) {
			pageInfo.addError(e.getWikiMessage());
		}
		if (!success) {
			next.addObject("topicNames", topicNames);
			next.addObject("download", download);
			next.addObject("excludeHistory", excludeHistory);
		}
		return success;
	}
	
	/**
	 *
	 */
	private void renderXml(HttpServletResponse response, File file, boolean download) throws IOException {
		response.setContentType("application/xml");
		response.setCharacterEncoding("UTF-8");
		if (download) {
			// download instead of displaying in a browser window
			response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
		}
		PrintWriter out = response.getWriter();
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String s;
			while ((s = bufferedReader.readLine()) != null) {
				out.println(s);
			}
		} finally {
			IOUtils.closeQuietly(bufferedReader);
			IOUtils.closeQuietly(fileReader);
		}
		out.close();
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
		if (StringUtils.isBlank(request.getParameter("export"))) {
			// by default export without history
			next.addObject("excludeHistory", true);
		}
		pageInfo.setContentJsp(JSP_EXPORT);
		pageInfo.setPageTitle(new WikiMessage("export.title"));
		pageInfo.setSpecial(true);
	}
}