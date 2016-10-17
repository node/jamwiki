/*
 * The template processor performs initial parsing steps to replace
 * syntax that should not be saved to the database, processes templates
 * and prepares the document for further processing.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang3.StringUtils;

%%

%public
%class JAMWikiTemplateLexer
%extends AbstractJAMWikiTemplateLexer
%type String
%unicode
%ignorecase

/* character expressions */
newline            = "\n"
whitespace         = {newline} | [ \t\f]

/* nowiki */
nowiki             = "<nowiki>" ~"</nowiki>"

/* parser directives */
noparsedirective   = "<__NOPARSE>" | "</__NOPARSE>"

/* pre */
htmlpreattributes  = class|dir|id|lang|style|title
htmlpreattribute   = ([ \t]+) {htmlpreattributes} ([ \t]*=[^>\n]+[ \t]*)*
htmlprestart       = "<pre" ({htmlpreattribute})* [ \t]* (\/)? ">"
htmlpreend         = "</pre>"
htmlpre            = ({htmlprestart}) ~({htmlpreend})

/* comments */
htmlcomment        = "<!--" ~"-->"

/* templates */
templatestart      = "{{" | "{{{{"
paramstart         = "{{{"
templateendchar    = "}"
templateparam      = "{{{" [^\{\}\n]+ "}}}"
includeonly        = "<includeonly>" ~"</includeonly>"
noinclude          = "<noinclude>" ~"</noinclude>"
onlyinclude        = "<onlyinclude>" ~"</onlyinclude>"

/* signatures */
wikisignature      = ([~]{3,5})

%state TEMPLATE

%%

<YYINITIAL> {

    /* ----- nowiki ----- */

    {nowiki} {
        if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }

    /* ----- pre ----- */

    {htmlpre} {
        if (logger.isTraceEnabled()) logger.trace("htmlpre: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }
}

/* ----- templates ----- */

<YYINITIAL, TEMPLATE> {
    // template start possibilities:
    //   * "{{xy" = template start
    //   * "{{{x" = possibly a param start
    //   * "{{{{" = template start + either another template or a param
    {templatestart} {
        if (logger.isTraceEnabled()) logger.trace("templatestart: " + yytext() + " (" + yystate() + ")");
        if (yytext().length() == 4) {
            // push back the two extra characters
            yypushback(2);
        }
        if (!allowTemplates()) {
            return yytext();
        }
        this.templateString.append(yytext().substring(0, 2));
        if (yystate() != TEMPLATE) {
            beginState(TEMPLATE);
        }
        return "";
    }
    {paramstart} {
        if (logger.isTraceEnabled()) logger.trace("paramstart: " + yytext() + " (" + yystate() + ")");
        yypushback(2);
        if (yystate() == YYINITIAL) {
            return yytext().substring(0, 1);
        } else {
            this.templateString.append(yytext().substring(0, 1));
            return "";
        }
    }
    {includeonly} {
        if (logger.isTraceEnabled()) logger.trace("includeonly: " + yytext() + " (" + yystate() + ")");
        String parsed = this.parse(TAG_TYPE_INCLUDE_ONLY, yytext());
        if (yystate() == TEMPLATE) {
            this.templateString.append(parsed);
        }
        return (yystate() == YYINITIAL) ? parsed : "";
    }
    {noinclude} {
        if (logger.isTraceEnabled()) logger.trace("noinclude: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_NO_INCLUDE, yytext());
    }
    {onlyinclude} {
        if (logger.isTraceEnabled()) logger.trace("onlyinclude: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_ONLY_INCLUDE, yytext());
    }
}
<YYINITIAL> {
    {templateparam} {
        if (logger.isTraceEnabled()) logger.trace("templateparam: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }
}
<TEMPLATE> {
    {templateendchar} {
        if (logger.isTraceEnabled()) logger.trace("templateendchar: " + yytext() + " (" + yystate() + ")");
        this.templateString.append(yytext());
        if (JFlexParserUtil.findMatchingEndTag(this.templateString, 0, "{", "}") != -1) {
            endState();
            String result = this.parse(TAG_TYPE_TEMPLATE, this.templateString.toString());
            this.templateString = new StringBuilder();
            return result;
        }
        return "";
    }
    {whitespace} | . {
        // no need to log this
        this.templateString.append(yytext());
        return "";
    }
}

<YYINITIAL> {

    /* ----- signatures ----- */

    {wikisignature} {
        if (logger.isTraceEnabled()) logger.trace("wikisignature: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_SIGNATURE, yytext());
    }

    /* ----- comments ----- */

    ^{htmlcomment} [ \t]* {newline} {
        if (logger.isTraceEnabled()) logger.trace("htmlcomment + newline: " + yytext() + " (" + yystate() + ")");
        // strip out the comment and newline
        return (this.mode < JFlexParser.MODE_TEMPLATE) ? yytext() : "";
    }

    {htmlcomment} {
        if (logger.isTraceEnabled()) logger.trace("htmlcomment: " + yytext() + " (" + yystate() + ")");
        // strip out the comment
        return (this.mode < JFlexParser.MODE_TEMPLATE) ? yytext() : "";
    }

    /* ----- other ----- */

    {whitespace} | . {
        // no need to log this
        return yytext();
    }
}

/* ----- parser directives ----- */

<YYINITIAL, TEMPLATE> {
    {noparsedirective} {
        if (logger.isTraceEnabled()) logger.trace("noparsedirective: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_NO_PARSE, yytext());
    }
}

<<EOF>> {
    if (logger.isTraceEnabled()) logger.trace("EOF (" + yystate() + ")");
    if (StringUtils.isBlank(this.templateString)) {
        return null;
    }
    // FIXME - this leaves unparsed text
    String output = this.templateString.toString();
    this.templateString = new StringBuilder();
    return output;
}
