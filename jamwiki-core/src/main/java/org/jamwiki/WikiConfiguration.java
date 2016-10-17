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
package org.jamwiki;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.model.WikiConfigurationObject;
import org.jamwiki.utils.ResourceUtil;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The <code>WikiConfiguration</code> class provides the infrastructure for
 * retrieving configuration values.  Note that with JAMWiki configuration
 * values differ from site properties by being generally less site-specific
 * and falling into specific categories, such as parser values.
 */
public class WikiConfiguration {

	/** Standard logger. */
	private static final WikiLogger logger = WikiLogger.getLogger(WikiConfiguration.class.getName());

	private static WikiConfiguration instance = null;

	private List<WikiConfigurationObject> queryHandlers = null;
	private List<String> dateFormats = null;
	private Map<String, String> editors = null;
	private List<WikiConfigurationObject> parsers = null;
	private List<WikiConfigurationObject> jflexParserCustomTags = null;
	private List<WikiConfigurationObject> searchEngines = null;
	private List<String> timeFormats = null;
	private Map<String, String> translations = null;
	private List<String> smtpContentTypes = null;

	/** Name of the configuration file. */
	private static final String JAMWIKI_CONFIGURATION_FILE = "jamwiki-configuration-1.3.xml";
	/** XSD for the configuration file. */
	private static final String JAMWIKI_CONFIGURATION_XSD = "jamwiki-configuration-1.3.xsd";
	private static final String XML_CONFIGURATION_ROOT = "configuration";
	private static final String XML_QUERY_HANDLER = "query-handler";
	private static final String XML_QUERY_HANDLER_ROOT = "query-handlers";
	private static final String XML_DATE_FORMAT = "date-format";
	private static final String XML_DATE_FORMAT_ROOT = "date-formats";
	private static final String XML_EDITOR = "editor";
	private static final String XML_EDITOR_ROOT = "editors";
	private static final String XML_INIT_PARAM = "init-param";
	private static final String XML_INIT_PARAM_NAME = "param-name";
	private static final String XML_INIT_PARAM_VALUE = "param-value";
	private static final String XML_PARAM_CLASS = "class";
	private static final String XML_PARAM_KEY = "key";
	private static final String XML_PARAM_KEY2 = "key2";
	private static final String XML_PARAM_NAME = "name";
	private static final String XML_PARAM_STATE = "state";
	private static final String XML_PARSER = "parser";
	private static final String XML_PARSER_ROOT = "parsers";
	private static final String XML_PARSER_CUSTOM_TAG = "custom-tag";
	private static final String XML_PARSER_CUSTOM_TAG_ROOT = "jflex-parser-custom-tags";
	private static final String XML_SEARCH_ENGINE = "search-engine";
	private static final String XML_SEARCH_ENGINE_ROOT = "search-engines";
	private static final String XML_TIME_FORMAT = "time-format";
	private static final String XML_TIME_FORMAT_ROOT = "time-formats";
	private static final String XML_TRANSLATION = "translation";
	private static final String XML_TRANSLATION_ROOT = "translations";

	/**
	 *
	 */
	private WikiConfiguration() {
		this.initialize();
	}

	/**
	 *
	 */
	public static WikiConfiguration getInstance() {
		if (WikiConfiguration.instance == null) {
			WikiConfiguration.instance = new WikiConfiguration();
		}
		return WikiConfiguration.instance;
	}

	/**
	 * Return a list of strings corresponding to the date formats (in
	 * java.text.SimpleDateFormat format) that a user can select from her
	 * preferences for displaying dates.
	 */
	public List<String> getDateFormats() {
		return this.dateFormats;
	}

	/**
	 *
	 */
	public Map<String, String> getEditors() {
		return this.editors;
	}

	/**
	 *
	 */
	public List<WikiConfigurationObject> getParsers() {
		return this.parsers;
	}

	/**
	 *
	 */
	public List<WikiConfigurationObject> getQueryHandlers() {
		return this.queryHandlers;
	}

	/**
	 *
	 */
	public List<WikiConfigurationObject> getJflexParserCustomTags() {
		return this.jflexParserCustomTags;
	}

	/**
	 *
	 */
	public List<WikiConfigurationObject> getSearchEngines() {
		return this.searchEngines;
	}

	/**
	 * Return a list of strings corresponding to the time formats (in
	 * java.text.SimpleDateFormat format) that a user can select from her
	 * preferences for displaying times.
	 */
	public List<String> getTimeFormats() {
		return this.timeFormats;
	}

	/**
	 * Return a list of all translations available to JAMWiki.
	 */
	public Map<String, String> getTranslations() {
		return this.translations;
	}
	
	/**
	 * 
	 */
	public List<String> getSmtpContentTypes() {
		return this.smtpContentTypes;
	}

	/**
	 *
	 */
	private void initialize() {
		this.queryHandlers = new ArrayList<WikiConfigurationObject>();
		this.dateFormats = new ArrayList<String>();
		this.editors = new LinkedHashMap<String, String>();
		this.jflexParserCustomTags = new ArrayList<WikiConfigurationObject>();
		this.parsers = new ArrayList<WikiConfigurationObject>();
		this.searchEngines = new ArrayList<WikiConfigurationObject>();
		this.translations = new LinkedHashMap<String, String>();
		this.smtpContentTypes = new ArrayList<String>();
		// content types hard coded. This is not likely to change.
		this.smtpContentTypes.add("text/plain");
		this.smtpContentTypes.add("text/html");
		this.timeFormats = new ArrayList<String>();
		File file = null;
		Document document = null;
		try {
			// "get resource file" for the XSD to ensure it is copied to the setup directory
			ResourceUtil.getJAMWikiResourceFile(JAMWIKI_CONFIGURATION_XSD);
			file = this.retrieveConfigFile();
			document = XMLUtil.parseXML(file, false);
		} catch (ParseException e) {
			// this should never happen unless someone mangles the config file
			throw new IllegalStateException("Unable to parse configuration file " + JAMWIKI_CONFIGURATION_FILE, e);
		} catch (IOException e) {
			// this should never happen unless someone deletes the file
			throw new IllegalStateException("Unable to find configuration file " + JAMWIKI_CONFIGURATION_FILE, e);
		}
		Node node = document.getElementsByTagName(XML_CONFIGURATION_ROOT).item(0);
		NodeList children = node.getChildNodes();
		Node child = null;
		for (int i = 0; i < children.getLength(); i++) {
			child = children.item(i);
			if (child.getNodeName().equals(XML_PARSER_ROOT)) {
				this.parsers = this.parseConfigurationObjects(child, XML_PARSER);
			} else if (child.getNodeName().equals(XML_PARSER_CUSTOM_TAG_ROOT)) {
				this.jflexParserCustomTags = this.parseConfigurationObjects(child, XML_PARSER_CUSTOM_TAG);
			} else if (child.getNodeName().equals(XML_QUERY_HANDLER_ROOT)) {
				this.queryHandlers = this.parseConfigurationObjects(child, XML_QUERY_HANDLER);
			} else if (child.getNodeName().equals(XML_EDITOR_ROOT)) {
				this.parseMapNodes(child, this.editors, XML_EDITOR);
			} else if (child.getNodeName().equals(XML_SEARCH_ENGINE_ROOT)) {
				this.searchEngines = this.parseConfigurationObjects(child, XML_SEARCH_ENGINE);
			} else if (child.getNodeName().equals(XML_TRANSLATION_ROOT)) {
				this.parseMapNodes(child, this.translations, XML_TRANSLATION);
			} else if (child.getNodeName().equals(XML_DATE_FORMAT_ROOT)) {
				this.parseListNodes(child, this.dateFormats, XML_DATE_FORMAT);
			} else if (child.getNodeName().equals(XML_TIME_FORMAT_ROOT)) {
				this.parseListNodes(child, this.timeFormats, XML_TIME_FORMAT);
			} else {
				logUnknownChild(node, child);
			}
		}
		logger.info("Configuration values loaded from " + file.getPath());
	}

	/**
	 *
	 */
	private WikiConfigurationObject parseConfigurationObject(Node node) {
		WikiConfigurationObject configurationObject = new WikiConfigurationObject();
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PARAM_CLASS)) {
				configurationObject.setClazz(XMLUtil.getTextContent(child));
			} else if (child.getNodeName().equals(XML_PARAM_KEY)) {
				configurationObject.setKey(XMLUtil.getTextContent(child));
			} else if (child.getNodeName().equals(XML_PARAM_KEY2)) {
				configurationObject.setKey2(XMLUtil.getTextContent(child));
			} else if (child.getNodeName().equals(XML_PARAM_NAME)) {
				configurationObject.setName(XMLUtil.getTextContent(child));
			} else if (child.getNodeName().equals(XML_PARAM_STATE)) {
				configurationObject.setState(XMLUtil.getTextContent(child));
			} else if (child.getNodeName().equals(XML_INIT_PARAM)) {
				NodeList initParamNodes = child.getChildNodes();
				String key = null;
				String value = null;
				for (int k = 0; k < initParamNodes.getLength(); k++) {
					Node initParamNode = initParamNodes.item(k);
					if (initParamNode.getNodeName().equals(XML_INIT_PARAM_NAME)) {
						key = XMLUtil.getTextContent(initParamNode);
					} else if (initParamNode.getNodeName().equals(XML_INIT_PARAM_VALUE)) {
						value = XMLUtil.getTextContent(initParamNode);
					}
				}
				if (!StringUtils.isBlank(key) && !StringUtils.isBlank(value)) {
					configurationObject.addInitParam(key, value);
				}
			} else {
				logUnknownChild(node, child);
			}
		}
		return configurationObject;
	}

	/**
	 *
	 */
	private List<WikiConfigurationObject> parseConfigurationObjects(Node node, String name) {
		List<WikiConfigurationObject> results = new ArrayList<WikiConfigurationObject>();
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(name)) {
				results.add(this.parseConfigurationObject(child));
			} else {
				logUnknownChild(node, child);
			}
		}
		return results;
	}

	/**
	 * Utility method for parsing nodes that are simply lists of values.
	 */
	private void parseListNodes(Node node, List<String> resultsList, String childNodeName) {
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(childNodeName)) {
				resultsList.add(XMLUtil.getTextContent(child));
			} else {
				logUnknownChild(node, child);
			}
		}
	}

	/**
	 * Utility method for parsing a key-value node.
	 */
	private void parseMapNode(Node node, Map<String, String> resultMap) {
		NodeList children = node.getChildNodes();
		String name = "";
		String key = "";
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(XML_PARAM_NAME)) {
				name = XMLUtil.getTextContent(child);
			} else if (child.getNodeName().equals(XML_PARAM_KEY)) {
				key = XMLUtil.getTextContent(child);
			} else {
				logUnknownChild(node, child);
			}
		}
		resultMap.put(key, name);
	}

	/**
	 * Utility method for parsing nodes that are collections of key-value pairs.
	 */
	private void parseMapNodes(Node node, Map<String, String> resultsMap, String childNodeName) {
		NodeList children = node.getChildNodes();
		for (int j = 0; j < children.getLength(); j++) {
			Node child = children.item(j);
			if (child.getNodeName().equals(childNodeName)) {
				this.parseMapNode(child, resultsMap);
			} else {
				logUnknownChild(node, child);
			}
		}
	}

	/**
	 * Utility class to log two XML nodes.
	 * @param node
	 * @param child
	 */
	private void logUnknownChild(Node node, Node child) {
		if (logger.isTraceEnabled()) {
			logger.trace("Unknown child of " + node.getNodeName() + " tag: " + child.getNodeName() + " / " + child.getNodeValue());
		}
	}

	/**
	 * Retrieve the search configuration that matches the current SearchEngine object.
	 */
	public static WikiConfigurationObject getCurrentSearchConfiguration() {
		for (WikiConfigurationObject wikiConfigurationObject : WikiConfiguration.getInstance().getSearchEngines()) {
			if (StringUtils.equals(wikiConfigurationObject.getClazz(), Environment.getValue(Environment.PROP_BASE_SEARCH_ENGINE))) {
				return wikiConfigurationObject;
			}
		}
		throw new IllegalStateException("No search configuraiton available");
	}

	/**
	 * Refresh the current configuration instance from the configuration file.
	 */
	public static void reset() {
		WikiConfiguration.instance = null;
	}

	/**
	 * Returns the XML config file from the system setup folder containing wiki
	 * configuration data.
	 */
	public File retrieveConfigFile() throws IOException {
		return ResourceUtil.getJAMWikiResourceFile(JAMWIKI_CONFIGURATION_FILE);
	}
}
