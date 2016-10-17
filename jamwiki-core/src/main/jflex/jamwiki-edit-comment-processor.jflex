/*
 * This class parses edit comments, allowing a limited subset of Mediawiki
 * syntax for use in edit comment display.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang3.StringEscapeUtils;

%%

%public
%class JAMWikiEditCommentLexer
%extends AbstractJAMWikiLexer
%type String
%unicode
%ignorecase

/* character expressions */
whitespace         = [ \n\t\f]
entity             = (&#([0-9]{2,4});) | (&[A-Za-z]{2,6};)

/* edit section */
editsection        = ("/*" [^*]* "*/") | ("/*" ([^/]"*")+ "*/")

/* wiki links */
wikilinkcontent    = [^\n\]] | "]" [^\n\]]
wikilink           = "[[" ({wikilinkcontent})+ "]]" [a-z]*

%%

<YYINITIAL> {
    {wikilink} {
        if (logger.isTraceEnabled()) logger.trace("wikilink: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_LINK, yytext());
    }
    {editsection} {
        if (logger.isTraceEnabled()) logger.trace("editsection: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_EDIT_SECTION, yytext());
    }
    {entity} {
        if (logger.isTraceEnabled()) logger.trace("entity: " + yytext() + " (" + yystate() + ")");
        String raw = yytext();
        return (JFlexParserUtil.isHtmlEntity(raw)) ? raw : StringEscapeUtils.escapeHtml4(raw);
    }
    {whitespace} | . {
        // no need to log this
        return StringEscapeUtils.escapeHtml4(yytext());
    }
}
