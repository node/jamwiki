/*
 * This class provides capability for parsing HTML or HTML-like tags of the form
 * <tag attribute="value">.  While it generates the HTML markup for the tag, it is
 * most useful as a tool to build an HtmlTagItem object that can then be further
 * processed.
 */
package org.jamwiki.parser.jflex;

%%

%public
%class JAMWikiHtmlTagLexer
%extends AbstractJAMWikiHtmlTagLexer
%char
%type String
%unicode
%ignorecase

whitespace         = [ \t\f]

/* Full XHTML 1.0 Transitional DTD */
coreattrs          = id|class|style|title
i18n               = lang|xml:lang|dir
events             = onclick|ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyup
TextAlign          = align
scriptAttr         = id|charset|type|language|src|defer|xml:space
brAttr             = {coreattrs}|clear
fontAttr           = {coreattrs}|{i18n}|size|color|face
cellhalign         = align|char|charoff
cellvalign         = valign
/*
    *** TODO ***
PCDATA             = [^&<>]
focus              = accesskey|tabindex|onfocus|onblur
special_extra      = object|applet|img|map|iframe
special_basic      = br|span
special            = {special_basic}|{special_extra}
fontstyle_extra    = big|small|font|basefont
fontstyle_basic    = tt|i|b|u|s|strike
fontstyle          = {fontstyle_basic}|{fontstyle_extra}
phrase_extra       = sub|sup
phrase_basic       = em|strong|dfn|code|q|samp|kbd|var|cite|abbr|acronym
phrase             = {phrase_basic}|{phrase_extra}
inline_forms       = input|select|textarea|label|button
misc_inline        = ins|del|script
misc               = noscript|{misc_inline}
inline             = a|{special}|{fontstyle}|{phrase}|{inline_forms}
Inline             = ({PCDATA}|{inline}|{misc_inline})*
lists              = ul|ol|dl
blocktext          = pre|hr|blockquote|address|center|noframes
block              = p|{heading}|div|{lists}|{blocktext}|isindex|fieldset|table
Flow               = ({PCDATA}|{block}|form|{inline}|{misc})*
pre_content        = ({PCDATA}|a|{special_basic}|{fontstyle_basic}|{phrase_basic}|{inline_forms}|{misc_inline})*
form_content       = ({PCDATA}|{block}|{inline}|{misc})*
button_content     = ({PCDATA}|p|{heading}|div|{lists}|{blocktext}|table|br|span|object|applet|img|map|{fontstyle}|{phrase}|{misc})*
noscriptAttr       = {attrs}
iframeAttr         = {coreattrs}|longdesc|name|src|frameborder|marginwidth|marginheight|scrolling|align|height|width
hrAttr             = {attrs}|align|noshade|size|width
qAttr              = {attrs}|cite
basefontAttr       = id|size|color|face
table              = caption|col|colgroup|thead|tfoot|tbody|tr|th|td
*/

/*
    *** Implemented ***
abbrAttr           = {attrs}
attrs              = {coreattrs}|{i18n}|{events}
bAttr              = {attrs}
bigAttr            = {attrs}
blockquoteAttr     = {attrs}|cite
captionAttr        = {attrs}|align
centerAttr         = {attrs}
citeAttr           = {attrs}
codeAttr           = {attrs}
colAttr            = {attrs}|span|width|{cellhalign}|{cellvalign}
colgroupAttr       = {attrs}|span|width|{cellhalign}|{cellvalign}
ddAttr             = {attrs}
delAttr            = {attrs}|cite|datetime
divAttr            = {attrs}|{TextAlign}
dlAttr             = {attrs}|compact
dtAttr             = {attrs}
emAttr             = {attrs}
headingAttr        = {attrs}|{TextAlign}
iAttr              = {attrs}
insAttr            = {attrs}|cite|datetime
liAttr             = {attrs}|type|value
olAttr             = {attrs}|type|compact|start
pAttr              = {attrs}|{TextAlign}
preAttr            = {attrs}|width|xml:space
sAttr              = {attrs}
smallAttr          = {attrs}
spanAttr           = {attrs}
strikeAttr         = {attrs}
strongAttr         = {attrs}
subAttr            = {attrs}
supAttr            = {attrs}
tableAttr          = {attrs}|summary|width|border|frame|rules|cellspacing|cellpadding|align|bgcolor
tbodyAttr          = {attrs}|{cellhalign}|{cellvalign}
tdAttr             = {attrs}|abbr|axis|headers|scope|rowspan|colspan|{cellhalign}|{cellvalign}|nowrap|bgcolor|width|height
tfootAttr          = {attrs}|{cellhalign}|{cellvalign}
thAttr             = {attrs}|abbr|axis|headers|scope|rowspan|colspan|{cellhalign}|{cellvalign}|nowrap|bgcolor|width|height
theadAttr          = {attrs}|{cellhalign}|{cellvalign}
trAttr             = {attrs}|{cellhalign}|{cellvalign}|bgcolor
ttAttr             = {attrs}
uAttr              = {attrs}
ulAttr             = {attrs}|type|compact
varAttr            = {attrs}
    *** Not implemented by Mediawiki ***
acronymAttr        = {attrs}
addressAttr        = {attrs}
dfnAttr            = {attrs}
kbdAttr            = {attrs}
sampAttr           = {attrs}
    *** Form elements - not supported ***
formAttr           = {attrs}|action|method|name|enctype|onsubmit|onreset|accept|accept-charset|target
labelAttr          = {attrs}|for|accesskey|onfocus|onblur
InputType          = text|password|checkbox|radio|submit|reset|file|hidden|image|button
inputAttr          = {attrs}|{focus}|type|name|value|checked|disabled|readonly|size|maxlength|src|alt|usemap|onselect|onchange|accept|align
selectAttr         = {attrs}|name|size|multiple|disabled|tabindex|onfocus|onblur|onchange
optgroupAttr       = {attrs}|disabled|label
optionAttr         = {attrs}|selected|disabled|label|value
textareaAttr       = {attrs}|{focus}|name|rows|cols|disabled|readonly|onselect|onchange
fieldsetAttr       = {attrs}
legendAttr         = {attrs}|accesskey|align
buttonAttr         = {attrs}|{focus}|name|value|type|disabled
    *** <a> links - not supported ***
a_content          = ({PCDATA}|{special}|{fontstyle}|{phrase}|{inline_forms}|{misc_inline})*
aAttr              = {attrs}|{focus}|charset|type|name|href|hreflang|rel|rev|shape|coords|target
    *** images, image maps, objects - not supported ***
imgAttr            = {attrs}|src|alt|name|longdesc|height|width|usemap|ismap|align|border|hspace|vspace
objectAttr         = {attrs}|declare|classid|codebase|data|type|codetype|archive|standby|height|width|usemap|name|tabindex|align|border|hspace|vspace
paramAttr          = id|name|value|valuetype|type
appletAttr         = {coreattrs}|codebase|archive|code|object|alt|name|width|height|align|hspace|vspace
mapAttr            = {i18n}|{events}|id|class|style|title|name
areaAttr           = {attrs}|{focus}|shape|coords|href|nohref|alt|target
*/

heading            = h1|h2|h3|h4|h5|h6
inlineTag          = abbr|b|big|br|cite|code|del|em|font|i|ins|pre|s|small|span|strike|strong|sub|sup|tt|u|var
blockLevelTag      = blockquote|caption|center|col|colgroup|dd|div|dl|dt|{heading}|hr|li|ol|p|table|tbody|td|tfoot|th|thead|tr|ul
htmlTag            = {inlineTag}|{blockLevelTag}

genericTag         = [a-zA-Z0-9_:\-]+
genericTagAttr     = [a-zA-Z0-9_:\-]+

tagContent         = "<" ({whitespace})* ({genericTag}) ({whitespace})* [^\n]* ">"
tagClose           = "<" ({whitespace})* "/" ({whitespace})* ({genericTag}) ({whitespace})* ">"
tagCloseContent    = ({whitespace})* ">"
tagCloseNoContent  = "/" ({whitespace})* ">"
tagAttributeValueInQuotes = ("\"" ~"\"")|("'" ~"'")
tagAttributeValueNoQuotes = [^ \t\f\"'>/]+
/* <script> tags */
tagScript          = "<" ({whitespace})* "script" [^\n]* ">"
tagScriptClose     = "<" ({whitespace})* "/" ({whitespace})* "script" ({whitespace})* ">"

%state ATTRS_ATTRIBUTE_KEY, ATTRS_TEXTALIGN_ATTRIBUTE_KEY, BLOCKQUOTE_ATTRIBUTE_KEY, BR_ATTRIBUTE_KEY, DL_ATTRIBUTE_KEY, FONT_ATTRIBUTE_KEY, HR_ATTRIBUTE_KEY, HTML_ATTRIBUTE_VALUE, HTML_CLOSE, HTML_OPEN, INS_DEL_ATTRIBUTE_KEY, LI_ATTRIBUTE_KEY, NON_HTML_ATTRIBUTE_KEY, OL_ATTRIBUTE_KEY, PRE_ATTRIBUTE_KEY, SCRIPT_ATTRIBUTE_KEY, TABLE_ATTRIBUTE_KEY, TABLE_CAPTION_ATTRIBUTE_KEY, TABLE_CELL_ATTRIBUTE_KEY, TABLE_COL_ATTRIBUTE_KEY, TABLE_ROW_ATTRIBUTE_KEY, TABLE_SECTION_ATTRIBUTE_KEY, UL_ATTRIBUTE_KEY

%%

<YYINITIAL> {
    {tagScript} {
        this.initialize(HtmlTagItem.Pattern.OPEN);
        int pos = this.yytext().toLowerCase().indexOf("script") + "script".length();
        yypushback(this.html.length() - pos);
        beginState(SCRIPT_ATTRIBUTE_KEY);
        this.tagType = "script";
        return "";
    }
    {tagScriptClose} {
        this.initialize(HtmlTagItem.Pattern.CLOSE);
        this.tagType = "script";
        return this.closeTag();
    }
    {tagClose} {
        this.initialize(HtmlTagItem.Pattern.CLOSE);
        int pos = this.html.indexOf("/");
        yypushback(this.html.length() - (pos + 1));
        beginState(HTML_CLOSE);
        return "";
    }
    {tagContent} {
        this.initialize(HtmlTagItem.Pattern.OPEN);
        yypushback(this.html.length() - 1);
        beginState(HTML_OPEN);
        return "";
    }
    /* error fallthrough */
    . {
        throw new IllegalArgumentException("YYINITIAL: Invalid HTML tag: " + yytext());
    }
}
<HTML_CLOSE> {
    {whitespace} {
        // ignore whitespace
        return "";
    }
    {htmlTag} | {genericTag} {
        this.tagType = yytext().toLowerCase();
        if (this.tagType.equals("br") || this.tagType.equals("hr")) {
            // handle invalid tags of the form </br> or </hr>
            this.tagPattern = HtmlTagItem.Pattern.EMPTY_BODY;
        }
        return "";
    }
    ">" {
        endState();
        return this.closeTag();
    }
    . {
        throw new IllegalArgumentException("HTML_CLOSE: Invalid HTML tag: " + this.html);
    }
}
<HTML_OPEN> {
    {whitespace} {
        // ignore whitespace
        return "";
    }
    {htmlTag} {
        endState();
        this.tagType = yytext().toLowerCase();
        if (this.tagType.equals("div")) {
            beginState(ATTRS_TEXTALIGN_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("p")) {
            beginState(ATTRS_TEXTALIGN_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("h1") || this.tagType.equals("h2") || this.tagType.equals("h3") || this.tagType.equals("h4") || this.tagType.equals("h5") || this.tagType.equals("h6")) {
            beginState(ATTRS_TEXTALIGN_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("center")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("span")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("em")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("strong")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("code")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("var")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("cite")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("abbr")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("sub")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("sup")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("tt")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("i")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("b")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("big")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("small")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("u")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("s")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("strike")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("font")) {
            beginState(FONT_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("pre")) {
            beginState(PRE_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("br")) {
            beginState(BR_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("hr")) {
            beginState(HR_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("blockquote")) {
            beginState(BLOCKQUOTE_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("ul")) {
            beginState(UL_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("ol")) {
            beginState(OL_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("dl")) {
            beginState(DL_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("li")) {
            beginState(LI_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("dd")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("dt")) {
            beginState(ATTRS_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("table")) {
            beginState(TABLE_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("tr")) {
            beginState(TABLE_ROW_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("td")) {
            beginState(TABLE_CELL_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("th")) {
            beginState(TABLE_CELL_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("tbody")) {
            beginState(TABLE_SECTION_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("thead")) {
            beginState(TABLE_SECTION_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("tfoot")) {
            beginState(TABLE_SECTION_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("caption")) {
            beginState(TABLE_CAPTION_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("col")) {
            beginState(TABLE_COL_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("colgroup")) {
            beginState(TABLE_COL_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("del")) {
            beginState(INS_DEL_ATTRIBUTE_KEY);
        } else if (this.tagType.equals("ins")) {
            beginState(INS_DEL_ATTRIBUTE_KEY);
        } else {
            logger.info("Unknown tag type: " + this.html);
        }
        if (this.tagType.equals("br") || this.tagType.equals("col") || this.tagType.equals("hr")) {
            // these tags may not have content, so explicitly set to empty body
            this.tagPattern = HtmlTagItem.Pattern.EMPTY_BODY;
        }
        return "";
    }
    {genericTag} {
        // non-HTML tag, such as <gallery>
        endState();
        this.tagType = yytext().toLowerCase();
        beginState(NON_HTML_ATTRIBUTE_KEY);
        return "";
    }
    . {
        throw new IllegalArgumentException("HTML_OPEN: Invalid HTML tag: " + this.html);
    }
}
<ATTRS_ATTRIBUTE_KEY, ATTRS_TEXTALIGN_ATTRIBUTE_KEY, BLOCKQUOTE_ATTRIBUTE_KEY, BR_ATTRIBUTE_KEY, DL_ATTRIBUTE_KEY, FONT_ATTRIBUTE_KEY, HR_ATTRIBUTE_KEY, HTML_ATTRIBUTE_VALUE, INS_DEL_ATTRIBUTE_KEY, LI_ATTRIBUTE_KEY, NON_HTML_ATTRIBUTE_KEY, OL_ATTRIBUTE_KEY, PRE_ATTRIBUTE_KEY, SCRIPT_ATTRIBUTE_KEY, TABLE_ATTRIBUTE_KEY, TABLE_CAPTION_ATTRIBUTE_KEY, TABLE_CELL_ATTRIBUTE_KEY, TABLE_COL_ATTRIBUTE_KEY, TABLE_ROW_ATTRIBUTE_KEY, TABLE_SECTION_ATTRIBUTE_KEY, UL_ATTRIBUTE_KEY> {
    {tagCloseNoContent} {
        boolean isFinished = ((yychar + this.yytext().length()) == this.html.length());
        if (!isFinished) {
            return "";
        }
        // tag close, done
        endState();
        this.tagPattern = HtmlTagItem.Pattern.EMPTY_BODY;
        return this.closeTag();
    }
    {tagCloseContent} {
        boolean isFinished = ((yychar + this.yytext().length()) == this.html.length());
        if (!isFinished) {
            return "";
        }
        // tag close, done
        endState();
        return this.closeTag();
    }
}
<ATTRS_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n} {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<ATTRS_TEXTALIGN_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|{TextAlign} {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<BLOCKQUOTE_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|cite {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<BR_ATTRIBUTE_KEY> {
    {brAttr} {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<DL_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|compact {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<FONT_ATTRIBUTE_KEY> {
    {fontAttr} {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<HR_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|align|noshade|size|width {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<INS_DEL_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|cite|datetime {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<LI_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|type|value {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<NON_HTML_ATTRIBUTE_KEY> {
    {genericTagAttr} {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<OL_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|type|compact|start {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<PRE_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|width|xml:space {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<TABLE_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|summary|width|border|frame|rules|cellspacing|cellpadding|align|bgcolor {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<TABLE_CAPTION_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|align {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<TABLE_CELL_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|abbr|axis|headers|scope|rowspan|colspan|{cellhalign}|{cellvalign}|nowrap|bgcolor|width|height {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<TABLE_COL_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|span|width|{cellhalign}|{cellvalign} {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<TABLE_ROW_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|{cellhalign}|{cellvalign}|bgcolor {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<TABLE_SECTION_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|{cellhalign}|{cellvalign} {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<UL_ATTRIBUTE_KEY> {
    {coreattrs}|{i18n}|type|compact {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<SCRIPT_ATTRIBUTE_KEY> {
    {scriptAttr} {
        this.initializeCurrentAttribute(yytext());
        return "";
    }
}
<ATTRS_ATTRIBUTE_KEY, ATTRS_TEXTALIGN_ATTRIBUTE_KEY, BLOCKQUOTE_ATTRIBUTE_KEY, DL_ATTRIBUTE_KEY, HR_ATTRIBUTE_KEY, INS_DEL_ATTRIBUTE_KEY, LI_ATTRIBUTE_KEY, OL_ATTRIBUTE_KEY, PRE_ATTRIBUTE_KEY, TABLE_ATTRIBUTE_KEY, TABLE_CAPTION_ATTRIBUTE_KEY, TABLE_CELL_ATTRIBUTE_KEY, TABLE_COL_ATTRIBUTE_KEY, TABLE_ROW_ATTRIBUTE_KEY, TABLE_SECTION_ATTRIBUTE_KEY, UL_ATTRIBUTE_KEY> {
    // handle tags that allow javascript event handlers
    {events} {
        if (allowJavascript()) {
            this.initializeCurrentAttribute(yytext());
        }
        return "";
    }
}
<ATTRS_ATTRIBUTE_KEY, ATTRS_TEXTALIGN_ATTRIBUTE_KEY, BLOCKQUOTE_ATTRIBUTE_KEY, BR_ATTRIBUTE_KEY, DL_ATTRIBUTE_KEY, FONT_ATTRIBUTE_KEY, HR_ATTRIBUTE_KEY, INS_DEL_ATTRIBUTE_KEY, LI_ATTRIBUTE_KEY, NON_HTML_ATTRIBUTE_KEY, OL_ATTRIBUTE_KEY, PRE_ATTRIBUTE_KEY, SCRIPT_ATTRIBUTE_KEY, TABLE_ATTRIBUTE_KEY, TABLE_CAPTION_ATTRIBUTE_KEY, TABLE_CELL_ATTRIBUTE_KEY, TABLE_COL_ATTRIBUTE_KEY, TABLE_ROW_ATTRIBUTE_KEY, TABLE_SECTION_ATTRIBUTE_KEY, UL_ATTRIBUTE_KEY> {
    "=" ({whitespace})* {
        if (this.currentAttributeKey != null) {
            beginState(HTML_ATTRIBUTE_VALUE);
        }
        return "";
    }
    [a-zA-Z]+ {
        // invalid attribute
        return "";
    }
    . {
        // ignore whitespace and any other characters
        return "";
    }
}
<HTML_ATTRIBUTE_VALUE> {
    {tagAttributeValueInQuotes} {
        endState();
        if (!allowJavascript() && yytext().indexOf("javascript") != -1) {
            // potential XSS attack, drop this attribute
            this.removeAttribute(this.currentAttributeKey);
        } else {
            // strip the quotation marks (they will be re-added later)
            this.addAttribute(this.currentAttributeKey, yytext().substring(1, yytext().length() - 1));
        }
        this.currentAttributeKey = null;
        return "";
    }
    {tagAttributeValueNoQuotes} {
        endState();
        if (!allowJavascript() && yytext().indexOf("javascript") != -1) {
            // potential XSS attack, drop this attribute
            this.removeAttribute(this.currentAttributeKey);
        } else {
            // add quotes
            this.addAttribute(this.currentAttributeKey, yytext());
        }
        this.currentAttributeKey = null;
        return "";
    }
    . {
        // ignore anything that doesn't match
        return "";
    }
}
