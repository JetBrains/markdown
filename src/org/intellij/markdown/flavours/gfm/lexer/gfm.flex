package org.intellij.markdown.flavours.gfm.lexer;

import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;
import org.intellij.markdown.IElementType;
import org.intellij.markdown.lexer.GeneratedLexer;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/* Auto generated File */
%%

%class _GFMLexer
%unicode
%public

%function advance
%type IElementType

%implements GeneratedLexer

%{
  private static class Token extends MarkdownTokenTypes {}

  private Stack<Integer> stateStack = new Stack<Integer>();

  private boolean isHeader = false;

  private ParseDelimited parseDelimited = new ParseDelimited();

  private static class ParseDelimited {
    char exitChar = 0;
    IElementType returnType = null;
    boolean inlinesAllowed = true;
  }

  private static class LinkDef {
    boolean wasUrl;
    boolean wasParen;
  }

  private static class HtmlHelper {
    private static final String BLOCK_TAGS_STRING =
            "article, header, aside, hgroup, blockquote, hr, iframe, body, li, map, button, " +
            "object, canvas, ol, caption, output, col, p, colgroup, pre, dd, progress, div, " +
            "section, dl, table, td, dt, tbody, embed, textarea, fieldset, tfoot, figcaption, " +
            "th, figure, thead, footer, footer, tr, form, ul, h1, h2, h3, h4, h5, h6, video, " +
            "script, style";

    static final Set<String> BLOCK_TAGS = getBlockTagsSet();

    private static Set<String> getBlockTagsSet() {
      Set<String> result = new HashSet<String>();
      String[] tags = BLOCK_TAGS_STRING.split(", ");
      for (String tag : tags) {
        result.add(tag);
      }
      return result;
    }
  }

  private static IElementType getDelimiterTokenType(char c) {
    switch (c) {
      case '"': return Token.DOUBLE_QUOTE;
      case '\'': return Token.SINGLE_QUOTE;
      case '(': return Token.LPAREN;
      case ')': return Token.RPAREN;
      case '[': return Token.LBRACKET;
      case ']': return Token.RBRACKET;
      case '<': return Token.LT;
      case '>': return Token.GT;
      default: return Token.BAD_CHARACTER;
    }
  }

  private IElementType parseDelimited(IElementType contentsType, boolean allowInlines) {
    char first = yycharat(0);
    char last = yycharat(yylength() - 1);

    stateStack.push(yystate());

    parseDelimited.exitChar = last;
    parseDelimited.returnType = contentsType;
//    parseDelimited.inlinesAllowed = allowInlines;
    parseDelimited.inlinesAllowed = true;

    yybegin(PARSE_DELIMITED);

    yypushback(yylength() - 1);
    return getDelimiterTokenType(first);
  }

  private void processEol() {
    int newlinePos = 1;
    while (newlinePos < yylength() && yycharat(newlinePos) != '\n') {
      newlinePos++;
    }

    // there is always one at 0 so that means there are two at least
    if (newlinePos != yylength()) {
      yypushback(yylength() - newlinePos);
      return;
    }

    yybegin(YYINITIAL);
    yypushback(yylength() - 1);

    isHeader = false;
  }

  private void popState() {
    if (stateStack.isEmpty()) {
      yybegin(AFTER_LINE_START);
    }
    else {
      yybegin(stateStack.pop());
    }
  }

  private void resetState() {
    yypushback(yylength());

    popState();
  }

  private String getTagName() {
    if (yylength() > 1 && yycharat(1) == '/') {
      return yytext().toString().substring(2, yylength() - 1).trim();
    }
    return yytext().toString().substring(1);
  }

  private boolean isBlockTag(String tagName) {
    return HtmlHelper.BLOCK_TAGS.contains(tagName.toLowerCase());
  }

  private boolean canInline() {
    return yystate() == AFTER_LINE_START || yystate() == PARSE_DELIMITED && parseDelimited.inlinesAllowed;
  }

  private IElementType getReturnGeneralized(IElementType defaultType) {
    if (canInline()) {
      return defaultType;
    }
    return parseDelimited.returnType;
  }

  private int countChars(CharSequence s, char c) {
    int result = 0;
    for (int i = 0; i < s.length(); ++i) {
      if (s.charAt(i) == c)
        result++;
    }
    return result;
  }

%}

DIGIT = [0-9]
ALPHANUM = [a-zA-Z0-9]
WHITE_SPACE = [ \t\f]
EOL = \R
ANY_CHAR = [^]

DOUBLE_QUOTED_TEXT = \" (\\\" | [^\n\"])* \"
SINGLE_QUOTED_TEXT = "'" (\\"'" | [^\n'])* "'"
QUOTED_TEXT = {SINGLE_QUOTED_TEXT} | {DOUBLE_QUOTED_TEXT}

HTML_COMMENT = "<!" "-"{2,4} ">" | "<!--" ([^-] | "-"[^-])* "-->"
PROCESSING_INSTRUCTION = "<?" ([^?] | "?"[^>])* "?>"
DECLARATION = "<!" [A-Z]+ {WHITE_SPACE}+ [^>] ">"
CDATA = "<![CDATA[" ([^\]] | "]"[^\]] | "]]"[^>])* "]]>"

TAG_NAME = [a-zA-Z]{ALPHANUM}*
ATTRIBUTE_NAME = [a-zA-Z:-] ({ALPHANUM} | [_.:-])*
ATTRIBUTE_VALUE = {QUOTED_TEXT} | [^ \t\f\n\r\"\'=<>`]+
ATTRIBUTE_VALUE_SPEC = {WHITE_SPACE}* "=" {WHITE_SPACE}* {ATTRIBUTE_VALUE}
ATTRUBUTE = {WHITE_SPACE}+ {ATTRIBUTE_NAME} {ATTRIBUTE_VALUE_SPEC}?
OPEN_TAG = "<" {TAG_NAME} {ATTRUBUTE}* {WHITE_SPACE}* "/"? ">"
CLOSING_TAG = "</" {TAG_NAME} {WHITE_SPACE}* ">"
HTML_TAG = {OPEN_TAG} | {CLOSING_TAG} | {HTML_COMMENT} | {PROCESSING_INSTRUCTION} | {DECLARATION} | {CDATA}

TAG_START = "<" {TAG_NAME}
TAG_END = "</" {TAG_NAME} {WHITE_SPACE}* ">"

SCHEME = [a-zA-Z]+
AUTOLINK = "<" {SCHEME} ":" [^ \t\f\n<>]+ ">"
EMAIL_AUTOLINK = "<" [a-zA-Z0-9.!#$%&'*+/=?\^_`{|}~-]+ "@"[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])? (\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)* ">"
GFM_AUTOLINK = (("http" "s"? | "ftp")"://" | "www.") ({ALPHANUM}([a-zA-Z0-9-]*{ALPHANUM})? ".")+ [a-zA-Z]{2,6} ("/"[a-zA-Z0-9.,+%_&!?#=-]+)* "/"?

%state TAG_START, AFTER_LINE_START, PARSE_DELIMITED, CODE

%%



<YYINITIAL> {

  // blockquote
  {WHITE_SPACE}{0,3} ">" {
    return Token.BLOCK_QUOTE;
  }

  {ANY_CHAR} {
    resetState();
  }

  {WHITE_SPACE}* {EOL} {
    resetState();
  }
}


<AFTER_LINE_START, PARSE_DELIMITED> {
  // The special case of a backtick
  \\"`"+ {
    return getReturnGeneralized(Token.ESCAPED_BACKTICKS);
  }

  // Escaping
  \\[\\\"'`*_{}\[\]()#+.,!:@#$%&~<>/-] {
    return getReturnGeneralized(Token.TEXT);
  }

  // Backticks (code span)
  "`"+ {
    if (canInline()) {
      return Token.BACKTICK;
    }
    return parseDelimited.returnType;
  }

  // Emphasis
  {WHITE_SPACE}+ ("*" | "_") {WHITE_SPACE}+ {
    return getReturnGeneralized(Token.TEXT);
  }

  "*" | "_" {
    return getReturnGeneralized(Token.EMPH);
  }

  "~" {
    return getReturnGeneralized(GFMTokenTypes.TILDE);
  }

  {AUTOLINK} { return parseDelimited(Token.AUTOLINK, false); }
  {EMAIL_AUTOLINK} { return parseDelimited(Token.EMAIL_AUTOLINK, false); }

  {HTML_TAG} { return Token.HTML_TAG; }

}

<AFTER_LINE_START> {

  {WHITE_SPACE}+ {
    return Token.WHITE_SPACE;
  }

  \" | "'"| "(" | ")" | "[" | "]" | "<" | ">" {
    return getDelimiterTokenType(yycharat(0));
  }
  ":" { return Token.COLON; }
  "!" { return Token.EXCLAMATION_MARK; }

  \\ / {EOL} {
    return Token.HARD_LINE_BREAK;
  }

  {WHITE_SPACE}* ({EOL} {WHITE_SPACE}*)+ {
    int lastSpaces = yytext().toString().indexOf("\n");
    if (lastSpaces >= 2) {
      yypushback(yylength() - lastSpaces);
      return Token.HARD_LINE_BREAK;
    }
    else if (lastSpaces > 0) {
      yypushback(yylength() - lastSpaces);
      return Token.WHITE_SPACE;
    }

    processEol();
    return Token.EOL;
  }

  {GFM_AUTOLINK} { return GFMTokenTypes.GFM_AUTOLINK; }

  {ALPHANUM}+ / {WHITE_SPACE}+ {GFM_AUTOLINK} {
    return Token.TEXT;
  }

  // optimize and eat underscores inside words
  {ALPHANUM}+ (({WHITE_SPACE}+ | "_"+) {ALPHANUM}+)* {
    return Token.TEXT;
  }

  {ANY_CHAR} { return Token.TEXT; }

}

<PARSE_DELIMITED> {
  {EOL} { resetState(); }

  {EOL} | {ANY_CHAR} {
    if (yycharat(0) == parseDelimited.exitChar) {
      yybegin(stateStack.pop());
      return getDelimiterTokenType(yycharat(0));
    }
    return parseDelimited.returnType;
  }

}

{ANY_CHAR} { return Token.TEXT; }