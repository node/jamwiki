/*
 * This class implements the MediaWiki syntax (http://meta.wikimedia.org/wiki/Help:Editing).
 * It will also escape any HTML tags that have not been specifically allowed to be
 * present.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jamwiki.parser.TableOfContents;

%%

%public
%class JAMWikiLexer
%extends AbstractJAMWikiLexer
%type String
%unicode
%ignorecase

/* character expressions */
newline            = "\n"
whitespace         = [ \n\t\f]
entity             = (&#[Xx]([0-9a-fA-F]{2,5});) | (&#([0-9]{2,4});) | (&[A-Za-z]{2,6};)

/* non-container expressions */
hr                 = "---" "-"+
wikiheading6       = "======" (.+) "======" ({whitespace})*
wikiheading5       = "=====" (.+) "=====" ({whitespace})*
wikiheading4       = "====" (.+) "====" ({whitespace})*
wikiheading3       = "===" (.+) "===" ({whitespace})*
wikiheading2       = "==" (.+) "==" ({whitespace})*
wikiheading1       = "=" (.+) "=" ({whitespace})*
bold               = "'''"
bolditalic         = "'''''"
italic             = "''"

/* lists */
listitem           = [\*#\:;]+ [^\*#\:;]
listend            = ({newline})+ [^\*#\:;\n\t\f ]
listdt             = ":"

/* nowiki */
nowiki             = "<nowiki>" ~"</nowiki>"

/* parser directives */
noparse            = "<__NOPARSE>" ~"</__NOPARSE>"

/* pre */
attributeValueInQuotes = "\"" ~"\""
attributeValueInSingleQuotes = "'" ~"'"
attributeValueNoQuotes = [^>\n]+
htmlattribute      = ([ \t]+) [a-zA-Z:]+ ([ \t]*=[ \t]*({attributeValueInQuotes}|{attributeValueInSingleQuotes}|{attributeValueNoQuotes}))*
htmlprestart       = "<pre" ({htmlattribute})* [ \t]* (\/)? ">"
htmlpreend         = "</pre>"
wikipre            = (" ") ([^\n])
wikipreend         = [^ ] | {newline}

/* allowed html */
inlinetag          = abbr|b|big|cite|code|del|em|font|i|ins|s|small|span|strike|strong|sub|sup|tt|u|var
blockleveltag      = blockquote|caption|center|col|colgroup|dd|div|dl|dt|hr|li|ol|table|tbody|td|tfoot|th|thead|tr|ul
htmlkeyword        = {inlinetag}|{blockleveltag}
htmlbr             = "<" (\/)? [ \t]* br ({htmlattribute})* [ \t]* (\/)? ">"
htmlparagraphopen  = "<p" ({htmlattribute})* [ \t]* (\/)? ">"
htmlparagraphclose = "</p>"
htmltagopen        = "<" ({htmlkeyword}) ({htmlattribute})* [ \t]* (\/)? ">"
htmltagclose       = "</" {htmlkeyword} ">"
htmltagnocontent   = "<" {htmlkeyword} ({htmlattribute})* [ \t]* "/>"
htmlheading        = "<h" [1-6][^>]* ">" ~("</h" [1-6] ">")

/* javascript */
javascript         = {newline}* "<script" [^>]* ">" ~"</script>"

/* processing commands */
notoc              = "__NOTOC__"
toc                = "__TOC__"
forcetoc           = "__FORCETOC__"

/* tables */
tableattribute     = ([ \t]*) [a-zA-Z:]+ ([ \t]*=[^>\n\|]+[ \t]*)*
tablestart         = [ \t]* "{|" (.)* {newline}
tableend           = [ \t]* "|}" ({newline})?
tablecell          = [ \t]* "|" [^\+\-\}] | "|" ({tableattribute})+ "|" [^\|]
tablecells         = "||" | "!!"
tablecellsstyle    = "||" ({tableattribute})+ "|" ([^|])
tableheading       = [ \t]* "!" | [ \t]* "!" ({tableattribute})+ "|" [^\|]
tablerow           = [ \t]* "|-" [ \t]* ({tableattribute})* {newline}
tablecaption       = [ \t]* "|+" | [ \t]* "|+" ({tableattribute})+ "|" [^\|]

/* wiki links */
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllinkwiki       = "[" ({protocol}) ([^\]\n]+) "]"
htmllinkraw        = ({protocol}) ([^ <'\"\n\t]+)
htmllink           = ({htmllinkwiki}) | ({htmllinkraw})
wikilinkcontent    = [^\n\]] | "]" [^\n\]] | {htmllink}
wikilink           = "[[" ({wikilinkcontent})+ "]]" [a-z]*
nestedwikilink     = "[[" ({wikilinkcontent})+ "|" ({wikilinkcontent} | {wikilink})+ "]]"

/* references */
reference          = "<ref" ([ \t]+name[ \t]*=[^>\/\n]+[ \t]*)? ([ \t]*>) ~"</ref>"
referencenocontent = "<ref" ([ \t]+name[ \t]*=[^>\/\n]+[ \t]*) [ \t]* "/>"
references         = "<references" ([ \t]*[\/]?[ \t]*>)

/* paragraphs */
endparagraph       = {newline} (({whitespace})*{newline})*

%state TABLE, LIST, PRE, WIKIPRE

%%

/* ----- paragraphs ----- */

<YYINITIAL, TABLE> {
    {endparagraph} {
        if (logger.isTraceEnabled()) logger.trace("endparagraph: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_PARAGRAPH, yytext());
    }
}

<YYINITIAL, WIKIPRE, PRE, LIST, TABLE> {

    /* ----- nowiki ----- */

    {nowiki} {
        if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
        String content = JFlexParserUtil.tagContent(yytext());
        return "<nowiki>" + StringEscapeUtils.escapeHtml4(content) + "</nowiki>";
    }

    /* ----- parser directives ----- */

    {noparse} {
        if (logger.isTraceEnabled()) logger.trace("noparse: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_NO_PARSE, yytext());
    }
}

/* ----- pre ----- */

<YYINITIAL, LIST, TABLE> {
    {htmlprestart} {
        if (logger.isTraceEnabled()) logger.trace("htmlprestart: " + yytext() + " (" + yystate() + ")");
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml4(yytext());
        }
        beginState(PRE);
        this.pushTag("pre", yytext());
        return "";
    }
}
<PRE> {
    {htmlpreend} {
        if (logger.isTraceEnabled()) logger.trace("htmlpreend: " + yytext() + " (" + yystate() + ")");
        // state only changes to pre if allowHTML() is true, so no need to check here
        endState();
        this.popTag("pre");
        return "";
    }
}
<YYINITIAL, WIKIPRE, LIST, TABLE> {
    ^{wikipre} {
        if (logger.isTraceEnabled()) logger.trace("wikipre: " + yytext() + " (" + yystate() + ")");
        // rollback all but the first (space) character for further processing
        yypushback(yytext().length() - 1);
        if (yystate() != WIKIPRE) {
            beginState(WIKIPRE);
            this.pushTag("pre", null);
        }
        return "";
    }
}
<WIKIPRE> {
    ^{wikipreend} {
        if (logger.isTraceEnabled()) logger.trace("wikipreend: " + yytext() + " (" + yystate() + ")");
        endState();
        // rollback everything to allow processing as non-pre text
        yypushback(yytext().length());
        this.popTag("pre");
        return  "\n";
    }
}

/* ----- table of contents ----- */

<YYINITIAL, LIST, TABLE> {
    {notoc} {
        if (logger.isTraceEnabled()) logger.trace("notoc: " + yytext() + " (" + yystate() + ")");
        this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_NO_TOC);
        return "";
    }
    ^{notoc} [ \t]* {newline} {
        if (logger.isTraceEnabled()) logger.trace("notoc: " + yytext() + " (" + yystate() + ")");
        this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_NO_TOC);
        return "";
    }
    {toc} {
        if (logger.isTraceEnabled()) logger.trace("toc: " + yytext() + " (" + yystate() + ")");
        this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_TOC_INITIALIZED);
        this.parserInput.getTableOfContents().setForceTOC(true);
        return yytext();
    }
    ^{toc} [ \t]* {newline} {
        if (logger.isTraceEnabled()) logger.trace("toc: " + yytext() + " (" + yystate() + ")");
        this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_TOC_INITIALIZED);
        this.parserInput.getTableOfContents().setForceTOC(true);
        return yytext();
    }
    {forcetoc} {
        if (logger.isTraceEnabled()) logger.trace("forcetoc: " + yytext() + " (" + yystate() + ")");
        this.parserInput.getTableOfContents().setForceTOC(true);
        return "";
    }
    ^{forcetoc} [ \t]* {newline} {
        if (logger.isTraceEnabled()) logger.trace("forcetoc: " + yytext() + " (" + yystate() + ")");
        this.parserInput.getTableOfContents().setForceTOC(true);
        return "";
    }
}

/* ----- tables ----- */

<YYINITIAL, TABLE> {
    ^{tablestart} {
        if (logger.isTraceEnabled()) logger.trace("tablestart: " + yytext() + " (" + yystate() + ")");
        if (this.paragraphIsOpen()) {
            // make sure a previous paragraph doesn't need to be closed
            this.popTag("p");
        }
        beginState(TABLE);
        String tagAttributes = yytext().trim().substring(2).trim();
        this.pushTag("table", "<table " + tagAttributes + ">");
        return "";
    }
}
<TABLE> {
    ^{tablecaption} {
        if (logger.isTraceEnabled()) logger.trace("tablecaption: " + yytext() + " (" + yystate() + ")");
        processTableStack();
        if (yytext().length() > 2) {
            // for captions with CSS specified an extra character is matched
            yypushback(1);
        }
        parseTableCell(yytext(), "caption", "|+");
        return "";
    }
    ^{tableheading} {
        if (logger.isTraceEnabled()) logger.trace("tableheading: " + yytext() + " (" + yystate() + ")");
        // if a column was already open, close it
        processTableStack();
        // FIXME - hack!  make sure that a table row is open
        if (!this.peekTag().getTagType().equals("tr")) {
            this.pushTag("tr", null);
        }
        if (yytext().trim().length() > 2) {
            // for headings with CSS specified an extra character is matched
            yypushback(1);
        }
        parseTableCell(yytext(), "th", "!");
        return "";
    }
    ^{tablecell} {
        if (logger.isTraceEnabled()) logger.trace("tablecell: " + yytext() + " (" + yystate() + ")");
        // if a column was already open, close it
        processTableStack();
        // FIXME - hack!  make sure that a table row is open
        if (!this.peekTag().getTagType().equals("tr")) {
            this.pushTag("tr", null);
        }
        // extra character matched by both regular expressions so push it back
        yypushback(1);
        parseTableCell(yytext(), "td", "|");
        return "";
    }
    {tablecells} {
        if (logger.isTraceEnabled()) logger.trace("tablecells: " + yytext() + " (" + yystate() + ")");
        if (this.peekTag().getTagType().equals("td") && yytext().equals("||")) {
            this.popTag("td");
            this.pushTag("td", null);
            return "";
        }
        if (this.peekTag().getTagType().equals("th")) {
            this.popTag("th");
            this.pushTag("th", null);
            return "";
        }
        return yytext();
    }
    {tablecellsstyle} {
        if (logger.isTraceEnabled()) logger.trace("tablecellsstyle: " + yytext() + " (" + yystate() + ")");
        if (!this.peekTag().getTagType().equals("td")) {
            return yytext();
        }
        // one extra character matched by the pattern, so roll it back
        yypushback(1);
        this.popTag("td");
        parseTableCell(yytext(), "td", "|");
        return "";
    }
    ^{tablerow} {
        if (logger.isTraceEnabled()) logger.trace("tablerow: " + yytext() + " (" + yystate() + ")");
        // if a column was already open, close it
        processTableStack();
        if (!this.peekTag().getTagType().equals("table") && !this.peekTag().getTagType().equals("caption")) {
            this.popTag("tr");
        }
        String openTagRaw = null;
        if (yytext().trim().length() > 2) {
            openTagRaw = "<tr " + yytext().substring(2).trim() + ">";
        }
        this.pushTag("tr", openTagRaw);
        return "";
    }
    ^{tableend} {
        if (logger.isTraceEnabled()) logger.trace("tableend: " + yytext() + " (" + yystate() + ")");
        // if a column was already open, close it
        processTableStack();
        // end TABLE state
        endState();
        this.popTag("tr");
        this.popTag("table");
        return "";
    }
}

/* ----- headings ----- */

<YYINITIAL> {
    ^{hr} {
        if (logger.isTraceEnabled()) logger.trace("hr: " + yytext() + " (" + yystate() + ")");
        this.pushTag(null, "<hr />");
        this.popTag("hr");
    }
}
<YYINITIAL, TABLE> {
    ^{wikiheading6}$ {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 6);
    }
    ^{wikiheading5}$ {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 5);
    }
    ^{wikiheading4}$ {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 4);
    }
    ^{wikiheading3}$ {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 3);
    }
    ^{wikiheading2}$ {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 2);
    }
    ^{wikiheading1}$ {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 1);
    }
    {htmlheading} {
        return this.parse(TAG_TYPE_HTML_HEADING, yytext());
    }
}

/* ----- lists ----- */

<YYINITIAL, LIST, TABLE> {
    ^{listitem} {
        if (logger.isTraceEnabled()) logger.trace("listitem: " + yytext() + " (" + yystate() + ")");
        if (yystate() != LIST) beginState(LIST);
        // one non-list character matched, roll it back
        yypushback(1);
        this.processListStack(yytext());
        return "";
    }
}
<LIST> {
    {listend} {
        if (logger.isTraceEnabled()) logger.trace("listend: " + yytext() + " (" + yystate() + ")");
        String raw = yytext();
        // roll back any matches to allow re-parsing
        yypushback(raw.length());
        endState();
        this.popAllListTags();
        return "";
    }
    {listdt} {
        if (logger.isTraceEnabled()) logger.trace("listdt: " + yytext() + " (" + yystate() + ")");
        if (this.peekTag().getTagType().equals("dt")) {
            // special case list of the form "; term : definition"
            this.popTag("dt");
            this.pushTag("dd", null);
            return "";
        }
        return yytext();
    }
}

/* ----- wiki links ----- */

<YYINITIAL, LIST, TABLE> {
    {wikilink} {
        if (logger.isTraceEnabled()) logger.trace("wikilink: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_LINK, yytext());
    }
    {nestedwikilink} {
        if (logger.isTraceEnabled()) logger.trace("nestedwikilink: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_LINK, yytext(), "nested");
    }
    {htmllinkraw} {
        return this.parse(TAG_TYPE_HTML_LINK, yytext(), false);
    }
    {htmllinkwiki} {
        String raw = yytext();
        // strip the opening and closing brackets
        return this.parse(TAG_TYPE_HTML_LINK, raw.substring(1, raw.length() - 1), true);
    }
}

/* ----- bold / italic ----- */

<YYINITIAL, WIKIPRE, LIST, TABLE> {
    {bold} {
        return this.parse(TAG_TYPE_WIKI_BOLD_ITALIC, yytext(), "b");
    }
    {bolditalic} {
        return this.parse(TAG_TYPE_WIKI_BOLD_ITALIC, yytext(), (String)null);
    }
    {italic} {
        return this.parse(TAG_TYPE_WIKI_BOLD_ITALIC, yytext(), "i");
    }
}

/* ----- references ----- */

<YYINITIAL, LIST, TABLE> {
    {reference} {
        return this.parse(TAG_TYPE_WIKI_REFERENCE, yytext());
    }
    {referencenocontent} {
        return this.parse(TAG_TYPE_WIKI_REFERENCE, yytext());
    }
    {references} {
        if (logger.isTraceEnabled()) logger.trace("references: " + yytext() + " (" + yystate() + ")");
        if (this.paragraphIsOpen()) {
            // if a paragraph is already opened, close it
            this.popTag("p");
        }
        return this.parse(TAG_TYPE_WIKI_REFERENCES, yytext());
    }
}

/* ----- html ----- */

<YYINITIAL, LIST, TABLE> {
    {htmlbr} {
        if (logger.isTraceEnabled()) logger.trace("htmlbr: " + yytext() + " (" + yystate() + ")");
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml4(yytext());
        }
        // <br> may have attributes, so check for them
        HtmlTagItem htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(yytext());
        // Mediawiki standard is to include a newline after br tags
        return (htmlTagItem == null) ? "" : htmlTagItem.toHtml() + '\n';
    }
    {htmlparagraphopen} {
        if (logger.isTraceEnabled()) logger.trace("htmlparagraphopen: " + yytext() + " (" + yystate() + ")");
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml4(yytext());
        }
        if (this.paragraphIsOpen()) {
            // make sure a previous paragraph doesn't need to be closed
            this.popTag("p");
        }
        this.pushTag(null, yytext());
        return "";
    }
    {htmlparagraphclose} {
        if (logger.isTraceEnabled()) logger.trace("htmlparagraphclose: " + yytext() + " (" + yystate() + ")");
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml4(yytext());
        }
        if (this.paragraphIsOpen()) {
            // only perform processing if a paragraph is open.  otherwise just suppress this tag.
            this.parse(TAG_TYPE_PARAGRAPH, null);
        }
        return "";
    }
}
<YYINITIAL, WIKIPRE, LIST, TABLE> {
    {htmltagnocontent} {
        if (logger.isTraceEnabled()) logger.trace("htmltagnocontent: " + yytext() + " (" + yystate() + ")");
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml4(yytext());
        }
        HtmlTagItem htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(yytext());
        if (htmlTagItem != null) {
            JFlexTagItem jflexTagItem = new JFlexTagItem(htmlTagItem);
            this.pushTag(jflexTagItem);
            this.popTag(jflexTagItem.getTagType());
        }
        return "";
    }
    {htmltagopen} {
        if (logger.isTraceEnabled()) logger.trace("htmltagopen: " + yytext() + " (" + yystate() + ")");
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml4(yytext());
        }
        this.pushTag(null, yytext());
        return "";
    }
    {htmltagclose} {
        if (logger.isTraceEnabled()) logger.trace("htmltagclose: " + yytext() + " (" + yystate() + ")");
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml4(yytext());
        }
        HtmlTagItem htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(yytext());
        this.popTag(htmlTagItem.getTagType());
        return "";
    }
}

/* ----- javascript ----- */

<YYINITIAL, LIST, TABLE> {
    {javascript} {
        return this.parse(TAG_TYPE_JAVASCRIPT, yytext());
    }
}

/* ----- other ----- */

<YYINITIAL, WIKIPRE, PRE, LIST, TABLE> {
    {entity} {
        if (logger.isTraceEnabled()) logger.trace("entity: " + yytext() + " (" + yystate() + ")");
        String raw = yytext().toLowerCase();
        return (JFlexParserUtil.isHtmlEntity(raw)) ? raw : StringEscapeUtils.escapeHtml4(raw);
    }
    {whitespace} | . {
        // no need to log this
        return StringEscapeUtils.escapeHtml4(yytext());
    }
}
