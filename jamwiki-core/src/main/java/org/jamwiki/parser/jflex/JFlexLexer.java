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

import java.util.Stack;
import org.jamwiki.Environment;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.utils.WikiLogger;

/**
 * Abstract class that is extended by the JFlex lexers.  This class primarily
 * contains utility methods useful during parsing.
 */
public abstract class JFlexLexer {

	protected static final WikiLogger logger = WikiLogger.getLogger(JFlexLexer.class.getName());

	/** Member variable used to keep track of the state history for the lexer. */
	protected Stack<Integer> states = new Stack<Integer>();
	/** Parser configuration information. */
	protected ParserInput parserInput;
	/** Parser parsing results. */
	protected ParserOutput parserOutput;
	/** Parser mode, which provides input to the parser about what steps to take. */
	protected int mode = JFlexParser.MODE_POSTPROCESS;

	protected static final int TAG_TYPE_EDIT_SECTION = 3;
	protected static final int TAG_TYPE_HTML_HEADING = 5;
	protected static final int TAG_TYPE_HTML_LINK = 10;
	protected static final int TAG_TYPE_IMAGE_LINK = 15;
	protected static final int TAG_TYPE_INCLUDE_ONLY = 20;
	protected static final int TAG_TYPE_JAVASCRIPT = 25;
	protected static final int TAG_TYPE_NO_INCLUDE = 30;
	protected static final int TAG_TYPE_NO_PARSE = 31;
	protected static final int TAG_TYPE_ONLY_INCLUDE = 32;
	protected static final int TAG_TYPE_PARAGRAPH = 33;
	protected static final int TAG_TYPE_REDIRECT = 34;
	protected static final int TAG_TYPE_TEMPLATE = 35;
	protected static final int TAG_TYPE_WIKI_BOLD_ITALIC = 40;
	protected static final int TAG_TYPE_WIKI_HEADING = 45;
	protected static final int TAG_TYPE_WIKI_LINK = 50;
	protected static final int TAG_TYPE_WIKI_REFERENCE = 55;
	protected static final int TAG_TYPE_WIKI_REFERENCES = 60;
	protected static final int TAG_TYPE_WIKI_SIGNATURE = 65;
	private static final EditSectionTag TAG_EDIT_SECTION = new EditSectionTag();
	private static final HtmlHeadingTag TAG_HTML_HEADING = new HtmlHeadingTag();
	private static final HtmlLinkTag TAG_HTML_LINK = new HtmlLinkTag();
	private static final ImageLinkTag TAG_IMAGE_LINK = new ImageLinkTag();
	private static final IncludeOnlyTag TAG_INCLUDE_ONLY = new IncludeOnlyTag();
	private static final JavascriptTag TAG_JAVASCRIPT = new JavascriptTag();
	private static final NoIncludeTag TAG_NO_INCLUDE = new NoIncludeTag();
	private static final NoParseDirectiveTag TAG_NO_PARSE = new NoParseDirectiveTag();
	private static final OnlyIncludeTag TAG_ONLY_INCLUDE = new OnlyIncludeTag();
	private static final ParagraphTag TAG_PARAGRAPH = new ParagraphTag();
	private static final RedirectTag TAG_REDIRECT = new RedirectTag();
	private static final TemplateTag TAG_TEMPLATE = new TemplateTag();
	private static final WikiBoldItalicTag TAG_WIKI_BOLD_ITALIC = new WikiBoldItalicTag();
	private static final WikiHeadingTag TAG_WIKI_HEADING = new WikiHeadingTag();
	private static final WikiLinkTag TAG_WIKI_LINK = new WikiLinkTag();
	private static final WikiReferenceTag TAG_WIKI_REFERENCE = new WikiReferenceTag();
	private static final WikiReferencesTag TAG_WIKI_REFERENCES = new WikiReferencesTag();
	private static final WikiSignatureTag TAG_WIKI_SIGNATURE = new WikiSignatureTag();

	/**
	 * Utility method used to indicate whether HTML tags are allowed in wiki syntax
	 * or not.
	 */
	protected boolean allowHTML() {
		return Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML);
	}

	/**
	 * Utility method used to indicate whether Javascript is allowed in wiki syntax
	 * or not.  Note that enabling Javascript opens a site up to cross-site-scripting
	 * attacks.
	 */
	protected boolean allowJavascript() {
		return Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT);
	}

	/**
	 * Begin a new parser state and store the old state onto the stack.
	 *
	 * @param state The new parsing state that is being entered.
	 */
	protected void beginState(int state) {
		// store current state
		states.push(yystate());
		// switch to new state
		yybegin(state);
	}

	/**
	 * End processing of a parser state and switch to the previous parser state.
	 */
	protected void endState() {
		// revert to previous state
		if (states.empty()) {
			logger.warn("Attempt to call endState for an empty stack with text: " + yytext());
			return;
		}
		int next = states.pop();
		yybegin(next);
	}

	/**
	 * Return the current lexer mode (defined in the lexer specification file).
	 */
	protected int getMode() {
		return this.mode;
	}

	/**
	 * This method is used to retrieve information used about parser configuration settings.
	 *
	 * @return Parser configuration information.
	 */
	public ParserInput getParserInput() {
		return this.parserInput;
	}

	/**
	 * This method is used to set the ParserOutput field, which is used to retrieve
	 * parsed information from the parser.
	 *
	 * @return Parsed information generated by the parser
	 */
	public ParserOutput getParserOutput() {
		return this.parserOutput;
	}

	/**
	 * Initialize the parser settings.  This functionality should be done
	 * from the constructor, but since JFlex generates code it is not possible
	 * to modify the constructor parameters.
	 *
	 * @param parserInput The ParserInput object containing parser parameters
	 *  required for successful parsing.
	 * @param parserOutput The current parsed document.  When parsing is done
	 *  in multiple stages that output values are also built in stages.
	 * @param mode The parser mode to use when parsing.  Mode affects what
	 *  type of parsing actions are taken when processing raw text.
	 */
	protected void init(ParserInput parserInput, ParserOutput parserOutput, int mode) {
		this.parserInput = parserInput;
		this.parserOutput = parserOutput;
		this.mode = mode;
	}

	/**
	 * Execute the lexer, returning the parsed content.
	 */
	protected String lex() throws Exception {
		String line;
		StringBuilder result = new StringBuilder();
		while ((line = this.yylex()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	/**
	 *
	 */
	protected String parse(int type, String raw, Object... args) {
		if (this.getParserInput().getInfiniteLoopCount() >= Environment.getIntValue(Environment.PROP_PARSER_MAXIMUM_INFINITE_LOOP_LIMIT)) {
			// do not attempt any further parsing
			return raw;
		}
		JFlexParserTag jflexParserTag = null;
		switch (type) {
			case TAG_TYPE_EDIT_SECTION:
				jflexParserTag = TAG_EDIT_SECTION;
				break;
			case TAG_TYPE_HTML_HEADING:
				jflexParserTag = TAG_HTML_HEADING;
				break;
			case TAG_TYPE_HTML_LINK:
				jflexParserTag = TAG_HTML_LINK;
				break;
			case TAG_TYPE_IMAGE_LINK:
				jflexParserTag = TAG_IMAGE_LINK;
				break;
			case TAG_TYPE_INCLUDE_ONLY:
				jflexParserTag = TAG_INCLUDE_ONLY;
				break;
			case TAG_TYPE_JAVASCRIPT:
				jflexParserTag = TAG_JAVASCRIPT;
				break;
			case TAG_TYPE_NO_INCLUDE:
				jflexParserTag = TAG_NO_INCLUDE;
				break;
			case TAG_TYPE_NO_PARSE:
				jflexParserTag = TAG_NO_PARSE;
				break;
			case TAG_TYPE_ONLY_INCLUDE:
				jflexParserTag = TAG_ONLY_INCLUDE;
				break;
			case TAG_TYPE_PARAGRAPH:
				jflexParserTag = TAG_PARAGRAPH;
				break;
			case TAG_TYPE_REDIRECT:
				jflexParserTag = TAG_REDIRECT;
				break;
			case TAG_TYPE_TEMPLATE:
				jflexParserTag = TAG_TEMPLATE;
				break;
			case TAG_TYPE_WIKI_BOLD_ITALIC:
				jflexParserTag = TAG_WIKI_BOLD_ITALIC;
				break;
			case TAG_TYPE_WIKI_HEADING:
				jflexParserTag = TAG_WIKI_HEADING;
				break;
			case TAG_TYPE_WIKI_LINK:
				jflexParserTag = TAG_WIKI_LINK;
				break;
			case TAG_TYPE_WIKI_REFERENCE:
				jflexParserTag = TAG_WIKI_REFERENCE;
				break;
			case TAG_TYPE_WIKI_REFERENCES:
				jflexParserTag = TAG_WIKI_REFERENCES;
				break;
			case TAG_TYPE_WIKI_SIGNATURE:
				jflexParserTag = TAG_WIKI_SIGNATURE;
				break;
			default:
				throw new IllegalArgumentException("Invalid tag type: " + type);
		}
		try {
			return jflexParserTag.parse(this, raw, args);
		} catch (Throwable t) {
			logger.info("Unable to parse " + raw, t);
			return raw;
		}
	}

	/**
	 * JFlex internal method used to change the lexer state values.
	 */
	public abstract void yybegin(int newState);

	/**
	 * JFlex internal method used to parse the next token.
	 */
	public abstract String yylex() throws Exception;

	/**
	 * JFlex internal method used to push text back onto the parser stack.
	 */
	public abstract void yypushback(int number);

	/**
	 * JFlex internal method used to retrieve the current lexer state value.
	 */
	public abstract int yystate();

	/**
	 * JFlex internal method used to retrieve the current text matched by the
	 * yylex() method.
	 */
	public abstract String yytext();
}
