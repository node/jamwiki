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
package org.jamwiki.parser.jflex;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiVersion;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * Process magic words.  See http://www.mediawiki.org/wiki/Help:Magic_words
 */
public class MagicWordUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(MagicWordUtil.class.getName());
	// current date values
	private static final String MAGIC_CURRENT_DAY = "CURRENTDAY";
	private static final String MAGIC_CURRENT_DAY2 = "CURRENTDAY2";
	private static final String MAGIC_CURRENT_DAY_NAME = "CURRENTDAYNAME";
	private static final String MAGIC_CURRENT_DAY_OF_WEEK = "CURRENTDOW";
	private static final String MAGIC_CURRENT_MONTH = "CURRENTMONTH";
	private static final String MAGIC_CURRENT_MONTH_ABBR = "CURRENTMONTHABBREV";
	private static final String MAGIC_CURRENT_MONTH_NAME = "CURRENTMONTHNAME";
	private static final String MAGIC_CURRENT_TIME = "CURRENTTIME";
	private static final String MAGIC_CURRENT_HOUR = "CURRENTHOUR";
	private static final String MAGIC_CURRENT_WEEK = "CURRENTWEEK";
	private static final String MAGIC_CURRENT_YEAR = "CURRENTYEAR";
	private static final String MAGIC_CURRENT_TIMESTAMP = "CURRENTTIMESTAMP";
	// local date values
	private static final String MAGIC_LOCAL_DAY = "LOCALDAY";
	private static final String MAGIC_LOCAL_DAY2 = "LOCALDAY2";
	private static final String MAGIC_LOCAL_DAY_NAME = "LOCALDAYNAME";
	private static final String MAGIC_LOCAL_DAY_OF_WEEK = "LOCALDOW";
	private static final String MAGIC_LOCAL_MONTH = "LOCALMONTH";
	private static final String MAGIC_LOCAL_MONTH_ABBR = "LOCALMONTHABBREV";
	private static final String MAGIC_LOCAL_MONTH_NAME = "LOCALMONTHNAME";
	private static final String MAGIC_LOCAL_TIME = "LOCALTIME";
	private static final String MAGIC_LOCAL_HOUR = "LOCALHOUR";
	private static final String MAGIC_LOCAL_WEEK = "LOCALWEEK";
	private static final String MAGIC_LOCAL_YEAR = "LOCALYEAR";
	private static final String MAGIC_LOCAL_TIMESTAMP = "LOCALTIMESTAMP";
	// statistics
	private static final String MAGIC_CURRENT_VERSION = "CURRENTVERSION";
	private static final String MAGIC_NUMBER_ARTICLES = "NUMBEROFARTICLES";
	private static final String MAGIC_NUMBER_ARTICLES_R = "NUMBEROFARTICLES:R";
	private static final String MAGIC_NUMBER_PAGES = "NUMBEROFPAGES";
	private static final String MAGIC_NUMBER_PAGES_R = "NUMBEROFPAGES:R";
	private static final String MAGIC_NUMBER_FILES = "NUMBEROFFILES";
	private static final String MAGIC_NUMBER_FILES_R = "NUMBEROFFILES:R";
	private static final String MAGIC_NUMBER_USERS = "NUMBEROFUSERS";
	private static final String MAGIC_NUMBER_USERS_R = "NUMBEROFUSERS:R";
	// page values
	private static final String MAGIC_PAGE_NAME = "PAGENAME";
	private static final String MAGIC_PAGE_NAME_E = "PAGENAMEE";
	private static final String MAGIC_SUB_PAGE_NAME = "SUBPAGENAME";
	private static final String MAGIC_SUB_PAGE_NAME_E = "SUBPAGENAMEE";
	private static final String MAGIC_BASE_PAGE_NAME = "BASEPAGENAME";
	private static final String MAGIC_BASE_PAGE_NAME_E = "BASEPAGENAMEE";
	private static final String MAGIC_NAMESPACE = "NAMESPACE";
	private static final String MAGIC_NAMESPACE_E = "NAMESPACEE";
	private static final String MAGIC_FULL_PAGE_NAME = "FULLPAGENAME";
	private static final String MAGIC_FULL_PAGE_NAME_E = "FULLPAGENAMEE";
	private static final String MAGIC_TALK_SPACE = "TALKSPACE";
	private static final String MAGIC_TALK_SPACE_E = "TALKSPACEE";
	private static final String MAGIC_SUBJECT_SPACE = "SUBJECTSPACE";
	private static final String MAGIC_SUBJECT_SPACE_E = "SUBJECTSPACEE";
	private static final String MAGIC_ARTICLE_SPACE = "ARTICLESPACE";
	private static final String MAGIC_ARTICLE_SPACE_E = "ARTICLESPACEE";
	private static final String MAGIC_TALK_PAGE_NAME = "TALKPAGENAME";
	private static final String MAGIC_TALK_PAGE_NAME_E = "TALKPAGENAMEE";
	private static final String MAGIC_SUBJECT_PAGE_NAME = "SUBJECTPAGENAME";
	private static final String MAGIC_SUBJECT_PAGE_NAME_E = "SUBJECTPAGENAMEE";
	private static final String MAGIC_ARTICLE_PAGE_NAME = "ARTICLEPAGENAME";
	private static final String MAGIC_ARTICLE_PAGE_NAME_E = "ARTICLEPAGENAMEE";
	private static final String MAGIC_REVISION_ID = "REVISIONID";
	private static final String MAGIC_REVISION_DAY = "REVISIONDAY";
	private static final String MAGIC_REVISION_DAY2 = "REVISIONDAY2";
	private static final String MAGIC_REVISION_MONTH = "REVISIONMONTH";
	private static final String MAGIC_REVISION_MONTH1 = "REVISIONMONTH1";
	private static final String MAGIC_REVISION_YEAR = "REVISIONYEAR";
	private static final String MAGIC_REVISION_TIMESTAMP = "REVISIONTIMESTAMP";
	private static final String MAGIC_REVISION_USER = "REVISIONUSER";
	private static final String MAGIC_SITE_NAME = "SITENAME";
	private static final String MAGIC_SERVER = "SERVER";
	private static final String MAGIC_SERVER_NAME = "SERVERNAME";
	private static List<String> MAGIC_WORDS_DATETIME = new ArrayList<String>();
	private static List<String> MAGIC_WORDS_METADATA = new ArrayList<String>();
	private static List<String> MAGIC_WORDS_NAMESPACES = new ArrayList<String>();
	private static List<String> MAGIC_WORDS_PAGE_NAMES = new ArrayList<String>();
	private static List<String> MAGIC_WORDS_STATISTICS = new ArrayList<String>();

	static {
		// current date values
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_DAY);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_DAY2);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_DAY_NAME);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_DAY_OF_WEEK);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_MONTH);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_MONTH_ABBR);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_MONTH_NAME);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_TIME);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_HOUR);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_WEEK);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_YEAR);
		MAGIC_WORDS_DATETIME.add(MAGIC_CURRENT_TIMESTAMP);
		// local date values
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_DAY);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_DAY2);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_DAY_NAME);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_DAY_OF_WEEK);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_MONTH);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_MONTH_ABBR);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_MONTH_NAME);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_TIME);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_HOUR);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_WEEK);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_YEAR);
		MAGIC_WORDS_DATETIME.add(MAGIC_LOCAL_TIMESTAMP);
		// statistics
		MAGIC_WORDS_STATISTICS.add(MAGIC_CURRENT_VERSION);
		MAGIC_WORDS_STATISTICS.add(MAGIC_NUMBER_ARTICLES);
		MAGIC_WORDS_STATISTICS.add(MAGIC_NUMBER_ARTICLES_R);
		MAGIC_WORDS_STATISTICS.add(MAGIC_NUMBER_PAGES);
		MAGIC_WORDS_STATISTICS.add(MAGIC_NUMBER_PAGES_R);
		MAGIC_WORDS_STATISTICS.add(MAGIC_NUMBER_FILES);
		MAGIC_WORDS_STATISTICS.add(MAGIC_NUMBER_FILES_R);
		MAGIC_WORDS_STATISTICS.add(MAGIC_NUMBER_USERS);
		MAGIC_WORDS_STATISTICS.add(MAGIC_NUMBER_USERS_R);
		// page values
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_PAGE_NAME);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_PAGE_NAME_E);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_SUB_PAGE_NAME);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_SUB_PAGE_NAME_E);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_BASE_PAGE_NAME);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_BASE_PAGE_NAME_E);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_FULL_PAGE_NAME);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_FULL_PAGE_NAME_E);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_TALK_PAGE_NAME);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_TALK_PAGE_NAME_E);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_SUBJECT_PAGE_NAME);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_SUBJECT_PAGE_NAME_E);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_ARTICLE_PAGE_NAME);
		MAGIC_WORDS_PAGE_NAMES.add(MAGIC_ARTICLE_PAGE_NAME_E);
		// namespaces
		MAGIC_WORDS_NAMESPACES.add(MAGIC_NAMESPACE);
		MAGIC_WORDS_NAMESPACES.add(MAGIC_NAMESPACE_E);
		MAGIC_WORDS_NAMESPACES.add(MAGIC_TALK_SPACE);
		MAGIC_WORDS_NAMESPACES.add(MAGIC_TALK_SPACE_E);
		MAGIC_WORDS_NAMESPACES.add(MAGIC_SUBJECT_SPACE);
		MAGIC_WORDS_NAMESPACES.add(MAGIC_SUBJECT_SPACE_E);
		MAGIC_WORDS_NAMESPACES.add(MAGIC_ARTICLE_SPACE);
		MAGIC_WORDS_NAMESPACES.add(MAGIC_ARTICLE_SPACE_E);
		// revisions
		MAGIC_WORDS_METADATA.add(MAGIC_REVISION_ID);
		MAGIC_WORDS_METADATA.add(MAGIC_REVISION_DAY);
		MAGIC_WORDS_METADATA.add(MAGIC_REVISION_DAY2);
		MAGIC_WORDS_METADATA.add(MAGIC_REVISION_MONTH);
		MAGIC_WORDS_METADATA.add(MAGIC_REVISION_MONTH1);
		MAGIC_WORDS_METADATA.add(MAGIC_REVISION_YEAR);
		MAGIC_WORDS_METADATA.add(MAGIC_REVISION_TIMESTAMP);
		MAGIC_WORDS_METADATA.add(MAGIC_REVISION_USER);
		MAGIC_WORDS_METADATA.add(MAGIC_SITE_NAME);
		MAGIC_WORDS_METADATA.add(MAGIC_SERVER);
		MAGIC_WORDS_METADATA.add(MAGIC_SERVER_NAME);
	}

	/**
	 * Determine if a template name corresponds to a magic word requiring
	 * special handling.  See http://meta.wikimedia.org/wiki/Help:Magic_words
	 * for a list of Mediawiki magic words.  If the template name is a magic
	 * word then return the magic word name and any arguments.
	 */
	protected static String[] parseMagicWordInfo(String name) {
		int pos = name.indexOf(':');
		String magicWord = (pos != -1) ? name.substring(0, pos).trim() : name.trim();
		if (!MagicWordUtil.isMagicWord(magicWord)) {
			return null;
		}
		String magicWordArguments = null;
		if (pos != -1 && (pos + 2) <= name.length()) {
			magicWordArguments = name.substring(pos + 1).trim();
		}
		return new String[]{magicWord, magicWordArguments};
	}

	/**
	 * Determine if a template name corresponds to a magic word requiring
	 * special handling.  See http://meta.wikimedia.org/wiki/Help:Magic_words
	 * for a list of Mediawiki magic words.
	 */
	protected static boolean isMagicWord(String name) {
		if (MAGIC_WORDS_DATETIME.contains(name)) {
			return true;
		} else if (MAGIC_WORDS_STATISTICS.contains(name)) {
			return true;
		} else if (MAGIC_WORDS_PAGE_NAMES.contains(name)) {
			return true;
		} else if (MAGIC_WORDS_NAMESPACES.contains(name)) {
			return true;
		} else if (MAGIC_WORDS_METADATA.contains(name)) {
			return true;
		}
		return false;
	}

	/**
	 * Process a magic word, returning the value corresponding to the magic
	 * word value.  See http://meta.wikimedia.org/wiki/Help:Magic_words for a
	 * list of Mediawiki magic words.
	 */
	protected static String processMagicWord(ParserInput parserInput, ParserOutput parserOutput, int mode, String magicWord, String magicWordArguments) throws DataAccessException, ParserException {
		String[] magicWordArgumentArray = JFlexParserUtil.retrieveTokenizedArgumentArray(parserInput, parserOutput, mode, magicWordArguments);
		if (MAGIC_WORDS_DATETIME.contains(magicWord)) {
			return processMagicWordDateTime(parserInput, magicWord);
		} else if (MAGIC_WORDS_STATISTICS.contains(magicWord)) {
			return processMagicWordStatistics(parserInput, magicWord);
		} else if (MAGIC_WORDS_PAGE_NAMES.contains(magicWord)) {
			return processMagicWordPageNames(parserInput, magicWord, magicWordArgumentArray);
		} else if (MAGIC_WORDS_NAMESPACES.contains(magicWord)) {
			return processMagicWordNamespaces(parserInput, magicWord, magicWordArgumentArray);
		} else if (MAGIC_WORDS_METADATA.contains(magicWord)) {
			return processMagicWordMetadata(parserInput, magicWord);
		}
		return magicWord;
	}

	/**
	 * Process date & time magic words.
	 */
	private static String processMagicWordDateTime(ParserInput parserInput, String name) throws DataAccessException {
		SimpleDateFormat formatter = new SimpleDateFormat();
		Date current = new Date(System.currentTimeMillis());
		// local date values
		if (name.equals(MAGIC_LOCAL_DAY)) {
			formatter.applyPattern("d");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_DAY2)) {
			formatter.applyPattern("dd");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_DAY_NAME)) {
			formatter.applyPattern("EEEE");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_DAY_OF_WEEK)) {
			formatter.applyPattern("F");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_MONTH)) {
			formatter.applyPattern("MM");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_MONTH_ABBR)) {
			formatter.applyPattern("MMM");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_MONTH_NAME)) {
			formatter.applyPattern("MMMM");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_TIME)) {
			formatter.applyPattern("HH:mm");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_HOUR)) {
			formatter.applyPattern("HH");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_WEEK)) {
			formatter.applyPattern("w");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_YEAR)) {
			formatter.applyPattern("yyyy");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_LOCAL_TIMESTAMP)) {
			formatter.applyPattern("yyyyMMddHHmmss");
			return formatter.format(current);
		}
		// current date values
		TimeZone utc = TimeZone.getTimeZone("GMT+00");
		formatter.setTimeZone(utc);
		if (name.equals(MAGIC_CURRENT_DAY)) {
			formatter.applyPattern("d");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_DAY2)) {
			formatter.applyPattern("dd");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_DAY_NAME)) {
			formatter.applyPattern("EEEE");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_DAY_OF_WEEK)) {
			formatter.applyPattern("F");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_MONTH)) {
			formatter.applyPattern("MM");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_MONTH_ABBR)) {
			formatter.applyPattern("MMM");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_MONTH_NAME)) {
			formatter.applyPattern("MMMM");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_TIME)) {
			formatter.applyPattern("HH:mm");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_HOUR)) {
			formatter.applyPattern("HH");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_WEEK)) {
			formatter.applyPattern("w");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_YEAR)) {
			formatter.applyPattern("yyyy");
			return formatter.format(current);
		}
		if (name.equals(MAGIC_CURRENT_TIMESTAMP)) {
			formatter.applyPattern("yyyyMMddHHmmss");
			return formatter.format(current);
		}
		return name;
	}

	/**
	 * Process statistic magic words.
	 */
	private static String processMagicWordStatistics(ParserInput parserInput, String name) throws DataAccessException {
		NumberFormat numFormatter = NumberFormat.getInstance();
		if (name.equals(MAGIC_CURRENT_VERSION)) {
			return WikiVersion.CURRENT_WIKI_VERSION;
		}
		if (name.equals(MAGIC_NUMBER_ARTICLES)) {
			int results = WikiBase.getDataHandler().lookupTopicCount(parserInput.getVirtualWiki(), Namespace.MAIN_ID);
			return numFormatter.format(results);
		}
		if (name.equals(MAGIC_NUMBER_ARTICLES_R)) {
			int results = WikiBase.getDataHandler().lookupTopicCount(parserInput.getVirtualWiki(), Namespace.MAIN_ID);
			return Integer.toString(results);
		}
		if (name.equals(MAGIC_NUMBER_PAGES)) {
			int results = WikiBase.getDataHandler().lookupTopicCount(parserInput.getVirtualWiki(), null);
			return numFormatter.format(results);
		}
		if (name.equals(MAGIC_NUMBER_PAGES_R)) {
			int results = WikiBase.getDataHandler().lookupTopicCount(parserInput.getVirtualWiki(), null);
			return Integer.toString(results);
		}
		if (name.equals(MAGIC_NUMBER_FILES)) {
			int results = WikiBase.getDataHandler().lookupWikiFileCount(parserInput.getVirtualWiki());
			return numFormatter.format(results);
		}
		if (name.equals(MAGIC_NUMBER_FILES_R)) {
			int results = WikiBase.getDataHandler().lookupWikiFileCount(parserInput.getVirtualWiki());
			return Integer.toString(results);
		}
		if (name.equals(MAGIC_NUMBER_USERS)) {
			int results = WikiBase.getDataHandler().lookupWikiUserCount();
			return numFormatter.format(results);
		}
		if (name.equals(MAGIC_NUMBER_USERS_R)) {
			int results = WikiBase.getDataHandler().lookupWikiUserCount();
			return Integer.toString(results);
		}
		return name;
	}

	/**
	 * Process page value magic words.
	 */
	private static String processMagicWordPageNames(ParserInput parserInput, String name, String[] magicWordArgumentArray) throws DataAccessException {
		// if there is an argument then use it as the topic name, otherwise
		// default to the current topic.
		String topic = parserInput.getTopicName();
		if (magicWordArgumentArray.length > 0 && !StringUtils.isBlank(magicWordArgumentArray[0])) {
			topic = magicWordArgumentArray[0];
		}
		WikiLink wikiLink = new WikiLink(parserInput.getContext(), parserInput.getVirtualWiki(), topic);
		if (name.equals(MAGIC_FULL_PAGE_NAME)) {
			return topic;
		}
		if (name.equals(MAGIC_FULL_PAGE_NAME_E)) {
			return Utilities.encodeAndEscapeTopicName(topic);
		}
		if (name.equals(MAGIC_PAGE_NAME)) {
			return wikiLink.getArticle();
		}
		if (name.equals(MAGIC_PAGE_NAME_E)) {
			return Utilities.encodeAndEscapeTopicName(wikiLink.getArticle());
		}
		if (name.equals(MAGIC_SUB_PAGE_NAME) || name.equals(MAGIC_SUB_PAGE_NAME_E)) {
			String pageName = wikiLink.getArticle();
			int pos = pageName.lastIndexOf('/');
			if (pos != -1 && pos < pageName.length()) {
				pageName = pageName.substring(pos + 1);
			}
			return name.equals(MAGIC_SUB_PAGE_NAME) ? pageName : Utilities.encodeAndEscapeTopicName(pageName);
		}
		if (name.equals(MAGIC_BASE_PAGE_NAME) || name.equals(MAGIC_BASE_PAGE_NAME_E)) {
			String pageName = wikiLink.getArticle();
			int pos = pageName.lastIndexOf('/');
			if (pos != -1 && pos < pageName.length()) {
				pageName = pageName.substring(0, pos);
			}
			return name.equals(MAGIC_BASE_PAGE_NAME) ? pageName : Utilities.encodeAndEscapeTopicName(pageName);
		}
		if (name.equals(MAGIC_TALK_PAGE_NAME)) {
			return LinkUtil.extractCommentsLink(parserInput.getVirtualWiki(), topic);
		}
		if (name.equals(MAGIC_TALK_PAGE_NAME_E)) {
			return Utilities.encodeAndEscapeTopicName(LinkUtil.extractCommentsLink(parserInput.getVirtualWiki(), topic));
		}
		if (name.equals(MAGIC_SUBJECT_PAGE_NAME) || name.equals(MAGIC_ARTICLE_PAGE_NAME)) {
			return LinkUtil.extractTopicLink(parserInput.getVirtualWiki(), topic);
		}
		if (name.equals(MAGIC_SUBJECT_PAGE_NAME_E) || name.equals(MAGIC_ARTICLE_PAGE_NAME_E)) {
			return Utilities.encodeAndEscapeTopicName(LinkUtil.extractTopicLink(parserInput.getVirtualWiki(), topic));
		}
		return name;
	}

	/**
	 * Process namespace magic words.
	 */
	private static String processMagicWordNamespaces(ParserInput parserInput, String name, String[] magicWordArgumentArray) throws DataAccessException {
		// if there is an argument then use it as the topic name, otherwise
		// default to the current topic.
		String topic = parserInput.getTopicName();
		if (magicWordArgumentArray.length > 0 && !StringUtils.isBlank(magicWordArgumentArray[0])) {
			topic = magicWordArgumentArray[0];
		}
		WikiLink wikiLink = new WikiLink(parserInput.getContext(), parserInput.getVirtualWiki(), topic);
		if (name.equals(MAGIC_NAMESPACE)) {
			return wikiLink.getNamespace().getLabel(parserInput.getVirtualWiki());
		}
		if (name.equals(MAGIC_NAMESPACE_E)) {
			return Utilities.encodeAndEscapeTopicName(wikiLink.getNamespace().getLabel(parserInput.getVirtualWiki()));
		}
		if (name.equals(MAGIC_TALK_SPACE)) {
			Namespace result = Namespace.findCommentsNamespace(wikiLink.getNamespace());
			return (result != null) ? result.getLabel(parserInput.getVirtualWiki()) : name;
		}
		if (name.equals(MAGIC_TALK_SPACE_E)) {
			Namespace result = Namespace.findCommentsNamespace(wikiLink.getNamespace());
			return (result != null) ? Utilities.encodeAndEscapeTopicName(result.getLabel(parserInput.getVirtualWiki())) : Utilities.encodeAndEscapeTopicName(name);
		}
		if (name.equals(MAGIC_SUBJECT_SPACE) || name.equals(MAGIC_ARTICLE_SPACE)) {
			Namespace result = Namespace.findMainNamespace(wikiLink.getNamespace());
			return (result != null) ? result.getLabel(parserInput.getVirtualWiki()) : name;
		}
		if (name.equals(MAGIC_SUBJECT_SPACE_E) || name.equals(MAGIC_ARTICLE_SPACE_E)) {
			Namespace result = Namespace.findMainNamespace(wikiLink.getNamespace());
			return (result != null) ? Utilities.encodeAndEscapeTopicName(result.getLabel(parserInput.getVirtualWiki())) : Utilities.encodeAndEscapeTopicName(name);
		}
		return name;
	}

	/**
	 * Process metadata magic words.
	 */
	private static String processMagicWordMetadata(ParserInput parserInput, String name) throws DataAccessException {
		SimpleDateFormat formatter = new SimpleDateFormat();
		TimeZone utc = TimeZone.getTimeZone("GMT+00");
		Topic topic = WikiBase.getDataHandler().lookupTopic(parserInput.getVirtualWiki(), parserInput.getTopicName(), false);
		TopicVersion topicVersion = null;
		Date revision = null;
		// null check needed for the test data handler, which does not implement topic versions
		if (topic != null && topic.getCurrentVersionId() != null) {
			topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topic.getCurrentVersionId());
			revision = topicVersion.getEditDate();
		}
		formatter.setTimeZone(utc);
		if (name.equals(MAGIC_REVISION_DAY)) {
			if (revision == null) {
				return "";
			}
			formatter.applyPattern("d");
			return formatter.format(revision);
		}
		if (name.equals(MAGIC_REVISION_DAY2)) {
			if (revision == null) {
				return "";
			}
			formatter.applyPattern("dd");
			return formatter.format(revision);
		}
		if (name.equals(MAGIC_REVISION_MONTH)) {
			if (revision == null) {
				return "";
			}
			formatter.applyPattern("M");
			return formatter.format(revision);
		}
		if (name.equals(MAGIC_REVISION_MONTH1)) {
			if (revision == null) {
				return "";
			}
			formatter.applyPattern("MM");
			return formatter.format(revision);
		}
		if (name.equals(MAGIC_REVISION_YEAR)) {
			if (revision == null) {
				return "";
			}
			formatter.applyPattern("yyyy");
			return formatter.format(revision);
		}
		if (name.equals(MAGIC_REVISION_TIMESTAMP)) {
			if (revision == null) {
				return "";
			}
			formatter.applyPattern("yyyyMMddHHmmss");
			return formatter.format(revision);
		}
		if (name.equals(MAGIC_REVISION_USER)) {
			if (topicVersion == null) {
				return "";
			}
			WikiUser wikiUser = (topicVersion.getAuthorId() != null) ? WikiBase.getDataHandler().lookupWikiUser(topicVersion.getAuthorId()) : null;
			return (wikiUser != null) ? wikiUser.getUsername() : topicVersion.getAuthorDisplay();
		}
		if (name.equals(MAGIC_REVISION_ID)) {
			return (topicVersion == null) ? "" : Integer.toString(topicVersion.getTopicVersionId());
		}
		if (name.equals(MAGIC_SITE_NAME)) {
			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(parserInput.getVirtualWiki());
			return virtualWiki.getSiteName();
		}
		if (name.equals(MAGIC_SERVER)) {
			return Environment.getValue(Environment.PROP_SERVER_URL);
		}
		if (name.equals(MAGIC_SERVER_NAME)) {
			// strip the opening "http://" if there is one
			String result = Environment.getValue(Environment.PROP_SERVER_URL);
			int pos = result.indexOf("://");
			return (pos == -1) ? result : result.substring(pos + "://".length());
		}
		return name;
	}
}
