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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Namespace;
import org.jamwiki.parser.LinkUtil;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.WikiLink;
import org.jamwiki.parser.image.ImageUtil;
import org.jamwiki.utils.MathUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

/**
 * Process parser functions.  See http://www.mediawiki.org/wiki/Help:Magic_words#Parser_functions.
 */
public abstract class ParserFunctionUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(ParserFunctionUtil.class.getName());
	private static final String PARSER_FUNCTION_ANCHOR_ENCODE = "anchorencode:";
	private static final String PARSER_FUNCTION_FILE_PATH = "filepath:";
	private static final String PARSER_FUNCTION_FULL_URL = "fullurl:";
	private static final String PARSER_FUNCTION_EXPR = "#expr:";
	private static final String PARSER_FUNCTION_IF = "#if:";
	private static final String PARSER_FUNCTION_IF_EQUAL = "#ifeq:";
	private static final String PARSER_FUNCTION_IF_EXIST = "#ifexist:";
	private static final String PARSER_FUNCTION_IF_EXPR = "#ifexpr:";
	private static final String PARSER_FUNCTION_LANGUAGE = "#language:";
	private static final String PARSER_FUNCTION_LOCAL_URL = "localurl:";
	private static final String PARSER_FUNCTION_LOWER_CASE = "lc:";
	private static final String PARSER_FUNCTION_LOWER_CASE_FIRST = "lcfirst:";
	private static final String PARSER_FUNCTION_NAMESPACE = "ns:";
	private static final String PARSER_FUNCTION_NAMESPACE_ESCAPED = "nse:";
	private static final String PARSER_FUNCTION_PAD_LEFT = "padleft:";
	private static final String PARSER_FUNCTION_PAD_RIGHT = "padright:";
	private static final String PARSER_FUNCTION_SWITCH = "#switch:";
	private static final String PARSER_FUNCTION_UPPER_CASE = "uc:";
	private static final String PARSER_FUNCTION_UPPER_CASE_FIRST = "ucfirst:";
	private static final String PARSER_FUNCTION_URL_ENCODE = "urlencode:";
	private static final String MAGIC_DISPLAY_TITLE = "DISPLAYTITLE:";
	private static List<String> PARSER_FUNCTIONS = new ArrayList<String>();

	static {
		// parser functions
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_ANCHOR_ENCODE);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_FILE_PATH);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_FULL_URL);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_EXPR);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_IF);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_IF_EQUAL);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_IF_EXIST);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_IF_EXPR);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_LANGUAGE);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_LOCAL_URL);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_LOWER_CASE);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_LOWER_CASE_FIRST);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_NAMESPACE);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_NAMESPACE_ESCAPED);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_PAD_LEFT);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_PAD_RIGHT);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_SWITCH);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_UPPER_CASE);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_UPPER_CASE_FIRST);
		PARSER_FUNCTIONS.add(PARSER_FUNCTION_URL_ENCODE);
		PARSER_FUNCTIONS.add(MAGIC_DISPLAY_TITLE);
	}

	/**
	 *
	 */
	private static String evaluateExpression(String expr) throws IllegalArgumentException {
		double result = MathUtil.evaluateExpression(expr);
		BigDecimal bigDecimal = new BigDecimal(result);
		if (bigDecimal.scale() > 8) {
			// trim to eight decimal places maximum
			bigDecimal = bigDecimal.setScale(8, BigDecimal.ROUND_HALF_UP);
		}
		bigDecimal = bigDecimal.stripTrailingZeros();
		return bigDecimal.toString();
	}

	/**
	 * Determine if a template name corresponds to a parser function requiring
	 * special handling.  See http://meta.wikimedia.org/wiki/Help:Magic_words
	 * for a list of Mediawiki parser functions.  If the template name is a parser
	 * function then return the parser function name and argument.
	 */
	protected static String[] parseParserFunctionInfo(String name) {
		int pos = name.indexOf(':');
		if (pos == -1) {
			return null;
		}
		boolean hasArguments = ((pos + 2) <= name.length());
		String parserFunction = (hasArguments) ? name.substring(0, pos + 1).trim() : name.trim();
		if (!PARSER_FUNCTIONS.contains(parserFunction)) {
			return null;
		}
		if (!hasArguments && !StringUtils.equals(parserFunction, PARSER_FUNCTION_NAMESPACE) && !StringUtils.equals(parserFunction, PARSER_FUNCTION_NAMESPACE_ESCAPED)) {
			// no argument.  only valid with the namespace parser function.
			return null;
		}
		String parserFunctionArguments = (hasArguments) ? name.substring(pos + 1).trim() : null;
		return new String[]{parserFunction, parserFunctionArguments};
	}

	/**
	 * Process a parser function, returning the value corresponding to the parser
	 * function result.  See http://meta.wikimedia.org/wiki/Help:Magic_words for a
	 * list of Mediawiki parser functions.
	 */
	protected static String processParserFunction(ParserInput parserInput, ParserOutput parserOutput, int mode, String parserFunction, String parserFunctionArguments) throws DataAccessException, ParserException {
		String[] parserFunctionArgumentArray = JFlexParserUtil.retrieveTokenizedArgumentArray(parserInput, parserOutput, mode, parserFunctionArguments);
		if (parserFunction.equals(PARSER_FUNCTION_ANCHOR_ENCODE)) {
			return Utilities.encodeAndEscapeTopicName(parserFunctionArgumentArray[0]);
		}
		if (parserFunction.equals(PARSER_FUNCTION_FILE_PATH)) {
			return ParserFunctionUtil.parseFilePath(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_FULL_URL)) {
			return ParserFunctionUtil.parseFileUrl(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_EXPR)) {
			return ParserFunctionUtil.parseExpr(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_IF)) {
			return ParserFunctionUtil.parseIf(parserInput, parserOutput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_IF_EQUAL)) {
			return ParserFunctionUtil.parseIfEqual(parserInput, parserOutput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_IF_EXIST)) {
			return ParserFunctionUtil.parseIfExist(parserInput, parserOutput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_IF_EXPR)) {
			return ParserFunctionUtil.parseIfExpr(parserInput, parserOutput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_LANGUAGE)) {
			return ParserFunctionUtil.parseLanguage(parserInput, parserOutput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_LOCAL_URL)) {
			return ParserFunctionUtil.parseLocalUrl(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_LOWER_CASE)) {
			return ParserFunctionUtil.parseLowerCase(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_LOWER_CASE_FIRST)) {
			return ParserFunctionUtil.parseLowerCaseFirst(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_NAMESPACE)) {
			return ParserFunctionUtil.parseNamespace(parserInput, parserFunctionArgumentArray, false);
		}
		if (parserFunction.equals(PARSER_FUNCTION_NAMESPACE_ESCAPED)) {
			return ParserFunctionUtil.parseNamespace(parserInput, parserFunctionArgumentArray, true);
		}
		if (parserFunction.equals(PARSER_FUNCTION_PAD_LEFT)) {
			return ParserFunctionUtil.parsePad(parserInput, parserFunctionArgumentArray, true);
		}
		if (parserFunction.equals(PARSER_FUNCTION_PAD_RIGHT)) {
			return ParserFunctionUtil.parsePad(parserInput, parserFunctionArgumentArray, false);
		}
		if (parserFunction.equals(PARSER_FUNCTION_SWITCH)) {
			return ParserFunctionUtil.parseSwitch(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_UPPER_CASE)) {
			return ParserFunctionUtil.parseUpperCase(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_UPPER_CASE_FIRST)) {
			return ParserFunctionUtil.parseUpperCaseFirst(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(PARSER_FUNCTION_URL_ENCODE)) {
			return ParserFunctionUtil.parseUrlEncode(parserInput, parserFunctionArgumentArray);
		}
		if (parserFunction.equals(MAGIC_DISPLAY_TITLE)) {
			return ParserFunctionUtil.parseDisplayTitle(parserInput, parserOutput, parserFunctionArgumentArray);
		}
		return null;
	}

	/**
	 * Parse the {{filepath}} parser function.
	 */
	private static String parseFilePath(ParserInput parserInput, String[] parserFunctionArgumentArray) throws DataAccessException {
		// pre-pend the image namespace to the file name
		String filename = Namespace.namespace(Namespace.FILE_ID).getLabel(parserInput.getVirtualWiki()) + Namespace.SEPARATOR + parserFunctionArgumentArray[0];
		String result = ImageUtil.buildImageFileUrl(parserInput.getContext(), parserInput.getVirtualWiki(), filename, true);
		if (result == null) {
			return "";
		}
		if (parserFunctionArgumentArray.length > 1 && parserFunctionArgumentArray[1].equalsIgnoreCase("nowiki")) {
			// add nowiki tags so that the next round of parsing does not convert to an HTML link
			result = "<nowiki>" + result + "</nowiki>";
		}
		return result;
	}

	/**
	 * Parse the {{fileurl:}} parser function.
	 */
	private static String parseFileUrl(ParserInput parserInput, String[] parserFunctionArgumentArray) throws DataAccessException {
		WikiLink wikiLink = LinkUtil.parseWikiLink(parserInput.getContext(), parserInput.getVirtualWiki(), parserFunctionArgumentArray[0]);
		String result = wikiLink.toRelativeUrl();
		result = LinkUtil.normalize(Environment.getValue(Environment.PROP_SERVER_URL) + result);
		if (parserFunctionArgumentArray.length > 1 && !StringUtils.isBlank(parserFunctionArgumentArray[1])) {
			result += "?" + parserFunctionArgumentArray[1];
		}
		return result;
	}

	/**
	 * Parse the {{#expr:}} parser function.  Usage: {{#expr: expression }}.
	 */
	private static String parseExpr(ParserInput parserInput, String[] parserFunctionArgumentArray) throws DataAccessException,  ParserException {
		String expr = parserFunctionArgumentArray[0];
		if (StringUtils.isBlank(expr)) {
			return "";
		}
		try {
			return ParserFunctionUtil.evaluateExpression(expr);
		} catch (IllegalArgumentException e) {
			Object[] params = new Object[1];
			params[0] = e.getMessage();
			return "<strong class=\"error\">" + Utilities.formatMessage("common.exception.expression", parserInput.getLocale(), params) + "</strong>";
		}
	}

	/**
	 * Parse the {{#if:}} parser function.  Usage: {{#if: test | true | false}}.
	 */
	private static String parseIf(ParserInput parserInput, ParserOutput parserOutput, String[] parserFunctionArgumentArray) throws DataAccessException,  ParserException {
		boolean condition = ((parserFunctionArgumentArray.length >= 1) ? !StringUtils.isBlank(parserFunctionArgumentArray[0]) : false);
		// parse to handle any embedded templates
		if (condition) {
			return (parserFunctionArgumentArray.length >= 2) ? JFlexParserUtil.parseFragment(parserInput, parserOutput, parserFunctionArgumentArray[1], JFlexParser.MODE_TEMPLATE) : "";
		} else {
			return (parserFunctionArgumentArray.length >= 3) ? JFlexParserUtil.parseFragment(parserInput, parserOutput, parserFunctionArgumentArray[2], JFlexParser.MODE_TEMPLATE) : "";
		}
	}

	/**
	 * Parse the {{#ifeq:}} parser function.  Usage: {{#ifeq: value1 | value2 | true | false}}.
	 */
	private static String parseIfEqual(ParserInput parserInput, ParserOutput parserOutput, String[] parserFunctionArgumentArray) throws DataAccessException,  ParserException {
		String arg1 = ((parserFunctionArgumentArray.length >= 1) ? parserFunctionArgumentArray[0] : "");
		String arg2 = ((parserFunctionArgumentArray.length >= 2) ? parserFunctionArgumentArray[1] : "");
		String result1 = ((parserFunctionArgumentArray.length >= 3) ? parserFunctionArgumentArray[2] : "");
		String result2 = ((parserFunctionArgumentArray.length >= 4) ? parserFunctionArgumentArray[3] : "");
		boolean equals = StringUtils.equals(arg1, arg2);
		if (!equals && NumberUtils.isNumber(arg1) && NumberUtils.isNumber(arg2)) {
			// compare numerically
			BigDecimal bigDecimal1 = new BigDecimal(arg1);
			BigDecimal bigDecimal2 = new BigDecimal(arg2);
			equals = (bigDecimal1.compareTo(bigDecimal2) == 0);
		}
		// parse to handle any embedded templates
		if (equals) {
			return JFlexParserUtil.parseFragment(parserInput, parserOutput, result1, JFlexParser.MODE_TEMPLATE);
		} else {
			return JFlexParserUtil.parseFragment(parserInput, parserOutput, result2, JFlexParser.MODE_TEMPLATE);
		}
	}

	/**
	 * Parse the {{#ifexist:}} parser function.  Usage: {{#ifexist: topic | exists | does not exist}}.
	 */
	private static String parseIfExist(ParserInput parserInput, ParserOutput parserOutput, String[] parserFunctionArgumentArray) throws DataAccessException,  ParserException {
		if (parserFunctionArgumentArray.length < 1) {
			return "";
		}
		String topicName = Utilities.decodeAndEscapeTopicName(parserFunctionArgumentArray[0], true);
		// parse to handle any embedded templates
		if (WikiBase.getDataHandler().lookupTopic(parserInput.getVirtualWiki(), topicName, false) != null) {
			return (parserFunctionArgumentArray.length >= 2) ? JFlexParserUtil.parseFragment(parserInput, parserOutput, parserFunctionArgumentArray[1], JFlexParser.MODE_TEMPLATE) : "";
		} else {
			return (parserFunctionArgumentArray.length >= 3) ? JFlexParserUtil.parseFragment(parserInput, parserOutput, parserFunctionArgumentArray[2], JFlexParser.MODE_TEMPLATE) : "";
		}
	}

	/**
	 * Parse the {{#ifexpr:}} parser function.  Usage: {{#if: expr | true | false}}.
	 */
	private static String parseIfExpr(ParserInput parserInput, ParserOutput parserOutput, String[] parserFunctionArgumentArray) throws DataAccessException,  ParserException {
		String expr = parserFunctionArgumentArray[0];
		boolean condition = false;
		if (!StringUtils.isBlank(expr)) {
			try {
				condition = (NumberUtils.toDouble(ParserFunctionUtil.evaluateExpression(expr), 0) != 0);
			} catch (IllegalArgumentException e) {
				Object[] params = new Object[1];
				params[0] = e.getMessage();
				return "<strong class=\"error\">" + Utilities.formatMessage("common.exception.expression", parserInput.getLocale(), params) + "</strong>";
			}
		}
		// parse to handle any embedded templates
		if (condition) {
			return (parserFunctionArgumentArray.length >= 2) ? JFlexParserUtil.parseFragment(parserInput, parserOutput, parserFunctionArgumentArray[1], JFlexParser.MODE_TEMPLATE) : "";
		} else {
			return (parserFunctionArgumentArray.length >= 3) ? JFlexParserUtil.parseFragment(parserInput, parserOutput, parserFunctionArgumentArray[2], JFlexParser.MODE_TEMPLATE) : "";
		}
	}

	/**
	 * Parse the {{#language:}} parser function.  Usage: {{#language:code | optional return code}}.
	 * "code" is the ISO code for the language name to return, and the optional return code
	 * is used if the language should be returned in a language other than the default.  For
	 * example, if the code is "fr" the default is "Francais", but if an optional return code
	 * of "en" is specified then the return value will be "French".
	 */
	private static String parseLanguage(ParserInput parserInput, ParserOutput parserOutput, String[] parserFunctionArgumentArray) {
		if (parserFunctionArgumentArray.length < 1) {
			return "";
		}
		if (!ArrayUtils.contains(Locale.getISOLanguages(), parserFunctionArgumentArray[0])) {
			// invalid locale
			return parserFunctionArgumentArray[0];
		}
		Locale locale = new Locale(parserFunctionArgumentArray[0]);
		String language = locale.getDisplayLanguage(locale);
		if (parserFunctionArgumentArray.length >= 2 && ArrayUtils.contains(Locale.getISOLanguages(), parserFunctionArgumentArray[1])) {
			Locale inLocale = new Locale(parserFunctionArgumentArray[1]);
			language = locale.getDisplayLanguage(inLocale);
		}
		return StringUtils.capitalize(language);
	}

	/**
	 * Parse the {{localurl:}} parser function.
	 */
	private static String parseLocalUrl(ParserInput parserInput, String[] parserFunctionArgumentArray) throws DataAccessException {
		WikiLink wikiLink = LinkUtil.parseWikiLink(parserInput.getContext(), parserInput.getVirtualWiki(), parserFunctionArgumentArray[0]);
		String result = wikiLink.toRelativeUrl();
		if (parserFunctionArgumentArray.length > 1 && !StringUtils.isBlank(parserFunctionArgumentArray[1])) {
			result += "?" + parserFunctionArgumentArray[1];
		}
		return result;
	}

	/**
	 * Parse the {{lc:}} parser function.
	 */
	private static String parseLowerCase(ParserInput parserInput, String[] parserFunctionArgumentArray) {
		return StringUtils.lowerCase(parserFunctionArgumentArray[0]);
	}

	/**
	 * Parse the {{lcfirst:}} parser function.
	 */
	private static String parseLowerCaseFirst(ParserInput parserInput, String[] parserFunctionArgumentArray) {
		return StringUtils.uncapitalize(parserFunctionArgumentArray[0]);
	}

	/**
	 * Parse the {{ns:}} and {{nse:}} parser functions.
	 */
	private static String parseNamespace(ParserInput parserInput, String[] parserFunctionArgumentArray, boolean escape) throws DataAccessException {
		int namespaceId = Namespace.MAIN_ID;
		if (parserFunctionArgumentArray.length > 0 && !StringUtils.isBlank(parserFunctionArgumentArray[0])) {
			namespaceId = NumberUtils.toInt(parserFunctionArgumentArray[0], -10);
		}
		Namespace namespace = null;
		if (namespaceId != -10) {
			namespace = WikiBase.getDataHandler().lookupNamespaceById(namespaceId);
		} else {
			namespace = WikiBase.getDataHandler().lookupNamespace(parserInput.getVirtualWiki(), Utilities.decodeAndEscapeTopicName(parserFunctionArgumentArray[0], true));
		}
		String result = ((namespace == null) ? "" : namespace.getLabel(parserInput.getVirtualWiki()));
		if (StringUtils.isBlank(result)) {
			return "";
		}
		return (escape) ? Utilities.encodeAndEscapeTopicName(result) : result;
	}

	/**
	 * Parse the {{padleft:}} and {{padright:}} parser functions.
	 */
	private static String parsePad(ParserInput parserInput, String[] parserFunctionArgumentArray, boolean isLeft) {
		if (parserFunctionArgumentArray.length < 1) {
			return "";
		}
		String value = parserFunctionArgumentArray[0];
		if (parserFunctionArgumentArray.length < 2) {
			// no length parameter
			return value;
		}
		int length = NumberUtils.toInt(parserFunctionArgumentArray[1], 0);
		if (value.length() >= length) {
			// no padding needed
			return value;
		}
		String padString = (parserFunctionArgumentArray.length > 2) ? parserFunctionArgumentArray[2] : "0";
		return (isLeft) ? StringUtils.leftPad(value, length, padString) : StringUtils.rightPad(value, length, padString);
	}

	/**
	 * Parse the {{#switch:}} parser function.
	 */
	private static String parseSwitch(ParserInput parserInput, String[] parserFunctionArgumentArray) {
		String condition = ((parserFunctionArgumentArray.length >= 1) ? parserFunctionArgumentArray[0].trim() : "#default");
		String defaultCondition = null;
		int pos = 0;
		String caseResult;
		List<String> caseConditions = new ArrayList<String>();
		for (int i = 1; i < parserFunctionArgumentArray.length; i++) {
			pos = parserFunctionArgumentArray[i].indexOf('=');
			if (pos == 0) {
				// invalid argument
				continue;
			}
			if (pos == -1 && i == (parserFunctionArgumentArray.length - 1)) {
				// last argument is the default when no case is specified
				defaultCondition = parserFunctionArgumentArray[i].trim();
				continue;
			}
			if (pos == -1) {
				// no equals sign means default to the next element, ie "first | second = first & second"
				caseConditions.add(parserFunctionArgumentArray[i].trim());
				continue;
			}
			caseConditions.add(parserFunctionArgumentArray[i].substring(0, pos).trim());
			caseResult = (pos < (parserFunctionArgumentArray[i].length() - 1)) ? parserFunctionArgumentArray[i].substring(pos + 1).trim() : "";
			for (String caseCondition : caseConditions) {
				if (StringUtils.equals(condition, caseCondition)) {
					return caseResult;
				}
				if (StringUtils.equals(caseCondition, "#default")) {
					defaultCondition = caseResult;
				}
			}
			caseConditions.clear();
		}
		return (defaultCondition != null) ? defaultCondition : "";
	}

	/**
	 * Parse the {{uc:}} parser function.
	 */
	private static String parseUpperCase(ParserInput parserInput, String[] parserFunctionArgumentArray) {
		return StringUtils.upperCase(parserFunctionArgumentArray[0]);
	}

	/**
	 * Parse the {{ucfirst:}} parser function.
	 */
	private static String parseUpperCaseFirst(ParserInput parserInput, String[] parserFunctionArgumentArray) {
		return StringUtils.capitalize(parserFunctionArgumentArray[0]);
	}

	/**
	 * Parse the {{urlencode:}} parser function.
	 */
	private static String parseUrlEncode(ParserInput parserInput, String[] parserFunctionArgumentArray) {
		try {
			return URLEncoder.encode(parserFunctionArgumentArray[0], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// this should never happen
			throw new IllegalStateException("Unsupporting encoding UTF-8");
		}
	}

	/**
	 *
	 */
	private static String parseDisplayTitle(ParserInput parserInput, ParserOutput parserOutput, String[] parserFunctionArgumentArray) {
		String pageTitle = parserFunctionArgumentArray[0];
		if (pageTitle != null) {
			if (StringUtils.equals(Utilities.decodeAndEscapeTopicName(pageTitle, true), parserInput.getTopicName())) {
				parserOutput.setPageTitle(parserFunctionArgumentArray[0]);
			}
		}
		return "";
	}
}
