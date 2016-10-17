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
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicType;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.utils.ResourceUtil;
import org.jamwiki.utils.SortedProperties;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.web.servlet.ModelAndView;

/**
 * Used to provide admins with the ability to create and edit JAMWiki message
 * keys.  Note that the application server must be restarted for any
 * translation changes to be visible on the site.
 */
public class TranslationServlet extends JAMWikiServlet {

	private static final WikiLogger logger = WikiLogger.getLogger(TranslationServlet.class.getName());
	/** The name of the JSP file used to render the servlet output. */
	protected static final String JSP_ADMIN_TRANSLATION = "admin-translation.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	public ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String function = request.getParameter("function");
		if (!StringUtils.isBlank(function)) {
			translate(request, pageInfo);
		}
		view(request, next, pageInfo);
		return next;
	}

	/**
	 *
	 */
	private String filename(String language) {
		String filename = "ApplicationResources.properties";
		if (!StringUtils.isBlank(language) && !language.equalsIgnoreCase("en")) {
			// FIXME - should also check for valid language code
			filename = "ApplicationResources_" + language + ".properties";
		}
		return filename;
	}

	/**
	 * If a language is specified in the form, use it, other default to default language or
	 * request language if no default is available.
	 */
	private String retrieveLanguage(HttpServletRequest request) {
		String language = request.getParameter("language");
		if (StringUtils.isBlank(language)) {
			WikiUser user = ServletUtil.currentWikiUser();
			if (!StringUtils.isBlank(user.getDefaultLocale())) {
				language = user.getDefaultLocale().split("_")[0];
			} else if (request.getLocale() != null) {
				language = request.getLocale().getLanguage();
			} else {
				language = "en";
			}
		}
		return language;
	}

	/**
	 *
	 */
	private TreeSet<String> retrieveTranslationCodes() throws Exception {
		TreeSet<String> codes = new TreeSet<String>();
		File propertyRoot = ResourceUtil.getClassLoaderRoot();
		File[] files = propertyRoot.listFiles();
		File file;
		String filename;
		for (int i = 0; i < files.length; i++) {
			file = files[i];
			if (!file.isFile()) {
				continue;
			}
			filename = file.getName();
			if (StringUtils.isBlank(filename)) {
				continue;
			}
			if (!filename.startsWith("ApplicationResources_") || !filename.endsWith(".properties")) {
				continue;
			}
			String code = filename.substring("ApplicationResources_".length(), filename.length() - ".properties".length());
			if (!StringUtils.isBlank(code)) {
				codes.add(code);
			}
		}
		// there is no ApplicationResources_en.properties file - only ApplicationResources.properties, so add "en"
		codes.add("en");
		return codes;
	}

	/**
	 *
	 */
	private void translate(HttpServletRequest request, WikiPageInfo pageInfo) throws Exception {
		// first load existing translations
		SortedProperties translations = new SortedProperties();
		String language = this.retrieveLanguage(request);
		if (!StringUtils.isBlank(language)) {
			String filename = filename(language);
			translations.putAll(Environment.loadProperties(filename));
		}
		// now update with translations from the request
		Enumeration names = request.getParameterNames();
		String name;
		while (names.hasMoreElements()) {
			name = (String)names.nextElement();
			if (!name.startsWith("translations[") || !name.endsWith("]")) {
				continue;
			}
			String key = name.substring("translations[".length(), name.length() - "]".length());
			String value = request.getParameter(name);
			translations.setProperty(key, value);
		}
		Environment.saveProperties(filename(language), translations, null);
		this.writeTopic(request, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String language = this.retrieveLanguage(request);
		SortedProperties translations = new SortedProperties(Environment.loadProperties("ApplicationResources.properties"));
		if (!StringUtils.isBlank(language)) {
			String filename = filename(language);
			// add all translated keys to the base translation list
			translations.putAll(Environment.loadProperties(filename));
			// if the user wants to see only untranslated values, return the intersection of the base
			// translation list and the translated file list
			if (BooleanUtils.toBoolean(request.getParameter("hideTranslated"))) {
				Map tmp = Utilities.intersect(translations, Environment.loadProperties("ApplicationResources.properties"));
				translations = new SortedProperties();
				translations.putAll(tmp);
				next.addObject("hideTranslated", true);
			}
		}
		pageInfo.setContentJsp(JSP_ADMIN_TRANSLATION);
		pageInfo.setAdmin(true);
		pageInfo.setPageTitle(new WikiMessage("translation.title"));
		next.addObject("translations", new TreeMap(translations));
		next.addObject("codes", this.retrieveTranslationCodes());
		next.addObject("language", language);
		SortedProperties defaultTranslations = new SortedProperties(Environment.loadProperties("ApplicationResources.properties"));
		next.addObject("defaultTranslations", new TreeMap(defaultTranslations));
	}

	/**
	 *
	 */
	private void writeTopic(HttpServletRequest request, WikiPageInfo pageInfo) throws Exception {
		String virtualWiki = pageInfo.getVirtualWikiName();
		String language = request.getParameter("language");
		Namespace namespace = Namespace.namespace(Namespace.JAMWIKI_ID);
		String pageName = Utilities.decodeTopicName(filename(language), true);
		String contents = "<pre><nowiki>\n" + ResourceUtil.readFile(filename(language)) + "\n</nowiki></pre>";
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, namespace, pageName, false);
		if (topic == null) {
			topic = new Topic(virtualWiki, namespace, pageName);
		}
		int charactersChanged = StringUtils.length(contents) - StringUtils.length(topic.getTopicContent());
		topic.setTopicContent(contents);
		topic.setReadOnly(true);
		topic.setTopicType(TopicType.SYSTEM_FILE);
		WikiUser user = ServletUtil.currentWikiUser();
		TopicVersion topicVersion = new TopicVersion(user, ServletUtil.getIpAddress(request), null, contents, charactersChanged);
		WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, null);
	}
}
