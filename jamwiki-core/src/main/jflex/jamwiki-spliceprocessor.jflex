/*
 * This class provides the capability to slice and splice an article to
 * insert or remove a section of text.  In this case a "section" is
 * defined as a body of text between two heading tags of the same level,
 * such as two &lt;h2&gt; tags.
 */
package org.jamwiki.parser.jflex;

%%

%public
%class JAMWikiSpliceLexer
%extends AbstractJAMWikiSpliceLexer
%type String
%unicode
%ignorecase

/* character expressions */
newline            = "\n"
whitespace         = {newline} | [ \t\f]

/* html attributes */
attributeValueInQuotes = "\"" ~"\""
attributeValueInSingleQuotes = "'" ~"'"
attributeValueNoQuotes = [^>\n]+
htmlattribute      = ([ \t]+) [a-zA-Z:]+ ([ \t]*=[ \t]*({attributeValueInQuotes}|{attributeValueInSingleQuotes}|{attributeValueNoQuotes}))*

/* non-container expressions */
wikiheading1       = "=" [^=\n]+ ~"="
wikiheading2       = "==" [^=\n]+ ~"=="
wikiheading3       = "===" [^=\n]+ ~"==="
wikiheading4       = "====" [^=\n]+ ~"===="
wikiheading5       = "=====" [^=\n]+ ~"====="
wikiheading6       = "======" [^=\n]+ ~"======"
h1                 = "<h1" ({htmlattribute})* [ \t]* ">" ~"</h1>"
h2                 = "<h2" ({htmlattribute})* [ \t]* ">" ~"</h2>"
h3                 = "<h3" ({htmlattribute})* [ \t]* ">" ~"</h3>"
h4                 = "<h4" ({htmlattribute})* [ \t]* ">" ~"</h4>"
h5                 = "<h5" ({htmlattribute})* [ \t]* ">" ~"</h5>"
h6                 = "<h6" ({htmlattribute})* [ \t]* ">" ~"</h6>"

/* nowiki */
nowiki             = "<nowiki>" ~"</nowiki>"

/* pre */
htmlprestart       = "<pre" ({htmlattribute})* [ \t]* (\/)? ">"
htmlpreend         = "</pre>"

/* comments */
htmlcomment        = "<!--" ~"-->"

%state PRE

%%

<YYINITIAL, PRE> {

    /* ----- nowiki ----- */

    {nowiki} {
        if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
        return returnText(yytext());
    }
}

/* ----- preformatted text ----- */

<YYINITIAL> {
    {htmlprestart} {
        if (allowHTML()) {
            beginState(PRE);
        }
        return returnText(yytext());
    }
}

<PRE> {
    {htmlpreend} {
        // state only changes to pre if allowHTML() is true, so no need to check here
        endState();
        return returnText(yytext());
    }
}

<YYINITIAL> {

    /* ----- comments ----- */

    {htmlcomment} {
        return returnText(yytext());
    }

    /* ----- headings ----- */

    ^{wikiheading1} {
        return processHeading(1, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading2} {
        return processHeading(2, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading3} {
        return processHeading(3, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading4} {
        return processHeading(4, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading5} {
        return processHeading(5, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading6} {
        return processHeading(6, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    {h1} {
        return processHeading(1, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h2} {
        return processHeading(2, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h3} {
        return processHeading(3, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h4} {
        return processHeading(4, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h5} {
        return processHeading(5, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h6} {
        return processHeading(6, yytext(), TAG_TYPE_HTML_HEADING);
    }
}

<YYINITIAL, PRE> {

    /* ----- default ----- */

    {whitespace} | . {
        return returnText(yytext());
    }
}
