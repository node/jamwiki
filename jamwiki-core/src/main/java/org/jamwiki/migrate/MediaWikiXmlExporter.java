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
package org.jamwiki.migrate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiVersion;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.RecentChange;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.Pagination;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
import org.jamwiki.utils.XMLUtil;

/**
 * Provide functionality for exporting a JAMWiki topic to Mediawiki XML format.
 */
public class MediaWikiXmlExporter implements TopicExporter {

	private static final WikiLogger logger = WikiLogger.getLogger(MediaWikiXmlExporter.class.getName());
	private static final SimpleDateFormat MEDIAWIKI_DATE_FORMATTER = new SimpleDateFormat(MediaWikiConstants.ISO_8601_DATE_FORMAT);

	/**
	 *
	 */
	public void exportToFile(File file, String virtualWiki, List<String> topicNames, boolean excludeHistory) throws MigrationException {
		OutputStreamWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		boolean success = false;
		try {
			fileWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.7/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.7/ http://www.mediawiki.org/xml/export-0.7.xsd\" version=\"0.7\" xml:lang=\"en\">");
			this.writeSiteInfo(bufferedWriter, virtualWiki);
			this.writePages(bufferedWriter, virtualWiki, topicNames, excludeHistory);
			bufferedWriter.append("\n</mediawiki>");
			success = true;
		} catch (DataAccessException e) {
			throw new MigrationException(e);
		} catch (IOException e) {
			throw new MigrationException(e);
		} finally {
			IOUtils.closeQuietly(bufferedWriter);
			IOUtils.closeQuietly(fileWriter);
			if (!success) {
				// make sure partial files are deleted
				file.delete();
			}
		}
	}

	/**
	 * Return the URL of the index page for the wiki.
	 *
	 * @throws DataAccessException Thrown if any error occurs while retrieving data.
	 */
	private String retrieveBaseUrl() throws DataAccessException {
		VirtualWiki virtualWiki = VirtualWiki.defaultVirtualWiki();
		String url = Environment.getValue(Environment.PROP_SERVER_URL);
		WikiLink wikiLink = new WikiLink(WikiUtil.WEBAPP_CONTEXT_PATH, virtualWiki.getName(), virtualWiki.getRootTopicName());
		url += wikiLink.toRelativeUrl();
		return url;
	}

	/**
	 *
	 */
	private void writeSiteInfo(Writer writer, String virtualWikiName) throws DataAccessException, IOException {
		VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
		writer.append("\n<siteinfo>");
		String sitename = virtualWiki.getSiteName();
		writer.append('\n');
		XMLUtil.buildTag(writer, "sitename", sitename, true);
		String base = this.retrieveBaseUrl();
		writer.append('\n');
		XMLUtil.buildTag(writer, "base", base, true);
		String generator = "JAMWiki " + WikiVersion.CURRENT_WIKI_VERSION;
		writer.append('\n');
		XMLUtil.buildTag(writer, "generator", generator, true);
		/*
		Cannot have two titles differing only by case of first letter.  Default behavior through 1.5, $wgCapitalLinks = true
			<enumeration value="first-letter" />
		Complete title is case-sensitive. Behavior when $wgCapitalLinks = false
			<enumeration value="case-sensitive" />
		Cannot have two titles differing only by case. Not yet implemented as of MediaWiki 1.5
			<enumeration value="case-insensitive" />
		*/
		writer.append('\n');
		XMLUtil.buildTag(writer, "case", "case-sensitive", true);
		writer.append("\n<namespaces>");
		Map<String, String> attributes = new LinkedHashMap<String, String>();
		List<Namespace> namespaces = WikiBase.getDataHandler().lookupNamespaces();
		for (Namespace namespace : namespaces) {
			attributes.put("key", Integer.toString(namespace.getId()));
			writer.append('\n');
			XMLUtil.buildTag(writer, "namespace", namespace.getLabel(virtualWikiName), attributes, true);
		}
		writer.append("\n</namespaces>");
		writer.append("\n</siteinfo>");
	}

	/**
	 *
	 */
	private void writePages(Writer writer, String virtualWiki, List<String> topicNames, boolean excludeHistory) throws DataAccessException, IOException, MigrationException {
		// note that effort is being made to re-use temporary objects as this
		// code can generate an OOM "GC overhead limit exceeded" with HUGE (500MB) topics
		// since the garbage collector ends up being invoked excessively.
		TopicVersion topicVersion;
		Topic topic;
		WikiUser user;
		// choose 100,000 as an arbitrary default
		int maxRevisions = (Environment.getIntValue(Environment.PROP_MAX_TOPIC_VERSION_EXPORT) > 0) ? Environment.getIntValue(Environment.PROP_MAX_TOPIC_VERSION_EXPORT) : 100000;
		int revisionsRetrieved = 0;
		List<Integer> topicVersionIds;
		Map<String, String> textAttributes = new LinkedHashMap<String, String>();
		textAttributes.put("xml:space", "preserve");
		for (String topicName : topicNames) {
			topicVersionIds = new ArrayList<Integer>();
			topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false);
			if (topic == null) {
				throw new MigrationException("Failure while exporting: topic " + virtualWiki + ':' + topicName + " does not exist");
			}
			writer.append("\n<page>");
			writer.append('\n');
			XMLUtil.buildTag(writer, "title", topic.getName(), true);
			writer.append('\n');
			XMLUtil.buildTag(writer, "ns", topic.getNamespace().getId());
			writer.append('\n');
			XMLUtil.buildTag(writer, "id", topic.getTopicId());
			if (excludeHistory || (maxRevisions - revisionsRetrieved) <= 1) {
				// only include the most recent version
				topicVersionIds.add(topic.getCurrentVersionId());
			} else {
				// FIXME - changes sorted newest-to-oldest, should be reverse
				Pagination pagination = new Pagination(maxRevisions - revisionsRetrieved, 0);
				List<RecentChange> changes = WikiBase.getDataHandler().getTopicHistory(topic, pagination, true);
				revisionsRetrieved += changes.size();
				for (int i = (changes.size() - 1); i >= 0; i--) {
					topicVersionIds.add(changes.get(i).getTopicVersionId());
				}
			}
			for (int topicVersionId : topicVersionIds) {
				topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId);
				writer.append("\n<revision>");
				writer.append('\n');
				XMLUtil.buildTag(writer, "id", topicVersion.getTopicVersionId());
				writer.append('\n');
				XMLUtil.buildTag(writer, "timestamp", this.parseJAMWikiTimestamp(topicVersion.getEditDate()), true);
				writer.append("\n<contributor>");
				user = (topicVersion.getAuthorId() != null) ? WikiBase.getDataHandler().lookupWikiUser(topicVersion.getAuthorId()) : null;
				if (user != null) {
					writer.append('\n');
					XMLUtil.buildTag(writer, "username", user.getUsername(), true);
					writer.append('\n');
					XMLUtil.buildTag(writer, "id", user.getUserId());
				} else if (Utilities.isIpAddress(topicVersion.getAuthorDisplay())) {
					writer.append('\n');
					XMLUtil.buildTag(writer, "ip", topicVersion.getAuthorDisplay(), true);
				} else {
					writer.append('\n');
					XMLUtil.buildTag(writer, "username", topicVersion.getAuthorDisplay(), true);
				}
				writer.append("\n</contributor>");
				writer.append('\n');
				if (topicVersion.getEditType() == TopicVersion.EDIT_MINOR) {
					XMLUtil.buildTag(writer, "minor", "", true);
					writer.append('\n');
				}
				XMLUtil.buildTag(writer, "comment", topicVersion.getEditComment(), true);
				writer.append('\n');
				textAttributes.put("bytes", Long.toString(topicVersion.getVersionContent().getBytes().length));
				XMLUtil.buildTag(writer, "text", topicVersion.getVersionContent(), textAttributes, true);
				writer.append("\n</revision>");
				// explicitly null out temp variables to improve garbage collection and
				// avoid OOM "GC overhead limit exceeded" errors on HUGE (500MB) topics
				topicVersion = null;
				user = null;
			}
			writer.append("\n</page>");
		}
	}

	/**
	 *
	 */
	private String parseJAMWikiTimestamp(Timestamp timestamp) {
		return MEDIAWIKI_DATE_FORMATTER.format(timestamp);
	}
}
