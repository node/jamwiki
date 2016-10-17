/*
 * The pre-processor performs processes metadata and prepares the
 * document for the full parsing by the processor.
 */
package org.jamwiki.parser.jflex;

%%

%public
%class JAMWikiPreLexer
%extends JFlexLexer
%type String
%unicode
%ignorecase

/* character expressions */
newline            = "\n"
whitespace         = {newline} | [ \t\f]

/* nowiki */
nowiki             = "<nowiki>" ~"</nowiki>"

/* parser directives */
noparse            = "<__NOPARSE>" ~"</__NOPARSE>"

/* pre */
htmlpreattributes  = class|dir|id|lang|style|title
htmlpreattribute   = ([ \t]+) {htmlpreattributes} ([ \t]*=[^>\n]+[ \t]*)*
htmlprestart       = "<pre" ({htmlpreattribute})* [ \t]* (\/)? ">"
htmlpreend         = "</pre>"
htmlpre            = ({htmlprestart}) ~({htmlpreend})
wikipre            = (" ") ([^\n])
wikipreend         = [^ ] | {newline}

/* processing commands */
noeditsection      = "__NOEDITSECTION__"

/* wiki links */
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllinkwiki       = "[" ({protocol}) ([^\]\n]+) "]"
htmllinkraw        = ({protocol}) ([^ <'\"\n\t]+)
htmllink           = ({htmllinkwiki}) | ({htmllinkraw})
wikilinkcontent    = [^\n\]] | "]" [^\n\]] | {htmllink}
wikilink           = "[[" ({wikilinkcontent})+ "]]" [a-z]*
nestedwikilink     = "[[" ({wikilinkcontent})+ "|" ({wikilinkcontent} | {wikilink})+ "]]"

/* redirect */
redirect           = "#REDIRECT" [ \t]* {wikilink}

%state WIKIPRE

%%

<YYINITIAL, WIKIPRE> {

    /* ----- nowiki ----- */

    {nowiki} {
        if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }

    /* ----- parser directives ----- */

    {noparse} {
        if (logger.isTraceEnabled()) logger.trace("noparse: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_NO_PARSE, yytext());
    }
}

/* ----- wikipre ----- */

<YYINITIAL, WIKIPRE> {
    ^{wikipre} {
        if (logger.isTraceEnabled()) logger.trace("wikipre: " + yytext() + " (" + yystate() + ")");
        // rollback all but the first (space) character for further processing
        yypushback(yytext().length() - 1);
        if (yystate() != WIKIPRE) {
            beginState(WIKIPRE);
        }
        return yytext();
    }
}
<WIKIPRE> {
    ^{wikipreend} {
        if (logger.isTraceEnabled()) logger.trace("wikipreend: " + yytext() + " (" + yystate() + ")");
        endState();
        // rollback everything to allow processing as non-pre text
        yypushback(yytext().length());
        return "";
    }
    {whitespace} | . {
        // no need to log this
        return yytext();
    }
}

<YYINITIAL> {

    /* ----- redirect ----- */

    ^{redirect} {
        if (logger.isTraceEnabled()) logger.trace("redirect: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_REDIRECT, yytext());
    }

    /* ----- pre ----- */

    {htmlpre} {
        if (logger.isTraceEnabled()) logger.trace("htmlpre: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }

    /* ----- processing commands ----- */

    ^{noeditsection} [ \t]* {newline} {
        if (logger.isTraceEnabled()) logger.trace("noeditsection: " + yytext() + " (" + yystate() + ")");
        this.parserInput.setAllowSectionEdit(false);
        return (this.mode < JFlexParser.MODE_PREPROCESS) ? yytext() : "";
    }
    {noeditsection} {
        if (logger.isTraceEnabled()) logger.trace("noeditsection: " + yytext() + " (" + yystate() + ")");
        this.parserInput.setAllowSectionEdit(false);
        return (this.mode < JFlexParser.MODE_PREPROCESS) ? yytext() : "";
    }

    /* ----- wiki links ----- */

    {wikilink} {
        if (logger.isTraceEnabled()) logger.trace("wikilink: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_LINK, yytext());
    }
    {nestedwikilink} {
        if (logger.isTraceEnabled()) logger.trace("nestedwikilink: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_LINK, yytext(), "nested");
    }

    /* ----- other ----- */

    {whitespace} | . {
        // no need to log this
        return yytext();
    }
}
