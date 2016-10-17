/*
 * This processor runs after the template processor and before the pre-processor
 * and converts XML-like custom tags to standard wiki syntax.  For example, if
 * a custom "bold" tag were to be created with syntax of the form
 * "<bold>text</bold>", then this processor would initialize an instance of the
 * "bold" tag processor, pass it the tags attributes and content, and most likely
 * return an output of the form "'''bold'''".
 */
package org.jamwiki.parser.jflex;

%%

%public
%class JAMWikiCustomTagLexer
%extends AbstractJAMWikiCustomTagLexer
%type String
%unicode
%ignorecase

/* character expressions */
whitespace         = [ \t\f\n]

/* nowiki */
nowiki             = "<nowiki>" ~"</nowiki>"

/* pre */
htmlpreattributes  = class|dir|id|lang|style|title
htmlpreattribute   = ([ \t]+) {htmlpreattributes} ([ \t]*=[^>\n]+[ \t]*)*
htmlprestart       = "<pre" ({htmlpreattribute})* [ \t]* (\/)? ">"
htmlpreend         = "</pre>"
htmlpre            = ({htmlprestart}) ~({htmlpreend})

/* tags */
tagopen            = "<" [^>\/] [^>]* ">"
tagnocontent       = "<" [^>]+ "/>"
tagclose           = "</" [^>]+ ">"

%%

/* ----- nowikiki/pre tags do not allow custom tags ----- */

{nowiki} | {htmlpre} {
    if (logger.isTraceEnabled()) logger.trace("nowiki / htmlpre: " + yytext() + " (" + yystate() + ")");
    return this.processText(yytext());
}

/* ----- custom tags ----- */

{tagnocontent} {
    if (logger.isTraceEnabled()) logger.trace("tagnocontent: " + yytext() + " (" + yystate() + ")");
    return this.parsePossibleCustomTagOpen(yytext());
}

{tagopen} {
    if (logger.isTraceEnabled()) logger.trace("tagopen: " + yytext() + " (" + yystate() + ")");
    return this.parsePossibleCustomTagOpen(yytext());
}

{tagclose} {
    if (logger.isTraceEnabled()) logger.trace("tagclose: " + yytext() + " (" + yystate() + ")");
    return this.parsePossibleCustomTagClose(yytext());
}

/* ----- other ----- */

{whitespace} | . {
    // no need to log this
    return this.processText(yytext());
}

<<EOF>> {
    if (logger.isTraceEnabled()) logger.trace("EOF (" + yystate() + ")");
    return this.flushCustomTagStack();
}
