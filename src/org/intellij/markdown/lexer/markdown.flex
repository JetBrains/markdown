package org.intellij.markdown.lexer;

import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.IElementType;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/* Auto generated File */
%%

%class _MarkdownLexer
%unicode
%public
%column

%function advance
%type IElementType

%{
  private static class Token implements MarkdownTokenTypes {}

  private int yycolumn = 0;

  private Stack<Integer> stateStack = new Stack<Integer>();

  private boolean isHeader = false;

  private int currentIndent = 0;

  private Paragraph paragraph = new Paragraph();
  private BlockQuotes blockQuotes = new BlockQuotes();
  private CodeFence codeFence = new CodeFence();
  private ParseDelimited parseDelimited = new ParseDelimited();

  private static class ParseDelimited {
    char exitChar = 0;
    IElementType returnType = null;
    boolean inlinesAllowed = true;
  }

  private static class Paragraph {
    boolean currentLineIsNotBlank = false;
    int lineCount = 0;
  }

  private static class BlockQuotes {
    int level = 0;
    int currentLineLevel = 0;

    void processMarker() {
      currentLineLevel++;
      adjustLevel();
    }

    void adjustLevel() {
      level = Math.max(level, currentLineLevel);
    }

    void resetLevel() {
      level = 0;
    }
  }

  private static class LinkDef {
    boolean wasUrl;
    boolean wasParen;
  }

  private static class CodeFence {
    char fenceChar;
    int fenceLength;
    boolean typeWasRead;
    // for code span
    int spanLength;
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

  private void increaseIndent(int delta) {
    currentIndent = Math.max(currentIndent, (yycolumn & 0xffffc) + delta);
  }

  private void recalcIndent() {
    int newIndent = yylength() - 1;
    if (newIndent < currentIndent) {
      currentIndent = newIndent & 0xfffc;
    }
  }

  private boolean isFourIndent() {
    return yycolumn >= currentIndent + 2 * blockQuotes.level + 4;
  }

  private void updateParagraphInfoOnNewline() {
    if (paragraph.currentLineIsNotBlank) {
      paragraph.lineCount++;
      paragraph.currentLineIsNotBlank = false;
    }
    else {
      endParagraph();
      blockQuotes.resetLevel();
    }
  }

  private void endParagraph() {
    paragraph.lineCount = 0;
  }

  private void processEol() {
    updateParagraphInfoOnNewline();

    int newlinePos = 1;
    while (newlinePos < yylength() && yycharat(newlinePos) != '\n') {
      newlinePos++;
    }

    // there is always one at 0 so that means there are two at least
    if (newlinePos != yylength()) {
      yypushback(yylength() - newlinePos);
      return;
    }

    recalcIndent();
    yybegin(YYINITIAL);
    yypushback(yylength() - 1);

    isHeader = false;
    blockQuotes.currentLineLevel = 0;
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
EOL = "\n"|"\r"|"\r\n"

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

SECTION_ID = "$"{ALPHANUM}+ | "${" [^\n\r}]+ "}"

%state HTML_BLOCK, TAG_START, AFTER_LINE_START, PARSE_DELIMITED, CODE, CODE_FENCE

%%

<YYINITIAL> {
  {TAG_START} | {TAG_END} {
    if (isFourIndent()) {
      resetState();
    }
    else {
      String tagName = getTagName();
      if (isBlockTag(tagName)) {
        endParagraph();
        yybegin(HTML_BLOCK);
        yypushback(yylength());
      } else {
        resetState();
      }
    }
  }
  {HTML_COMMENT} | {PROCESSING_INSTRUCTION} | {DECLARATION} | {CDATA} {
    if (isFourIndent()) {
      resetState();
    }
    else {
      endParagraph();
      yybegin(HTML_BLOCK);
      yypushback(yylength());
    }
  }

  {WHITE_SPACE}+ {
    if (paragraph.lineCount == 0 && yycolumn + yylength() >= currentIndent + 2 * blockQuotes.currentLineLevel + 4) {
      blockQuotes.resetLevel();
      yybegin(CODE);
    }
    return Token.WHITE_SPACE;
  }

  // Setext headers
  ("="+ | "-"+) / {WHITE_SPACE}* $ {
    if (isFourIndent()) {
      resetState();
    }
    else if (paragraph.lineCount == 1 && blockQuotes.level == blockQuotes.currentLineLevel) {
      return yycharat(0) == '=' ? Token.SETEXT_1 : Token.SETEXT_2;
    }
    else if (yycharat(0) == '-' && yylength() >= 3) {
      endParagraph();
      return Token.HORIZONTAL_RULE;
    }
    else {
      resetState();
    }
  }

  // atx headers
  "#"{1,6} / {WHITE_SPACE} {
    if (isFourIndent()) {
      resetState();
    }
    else {
      isHeader = true;
      endParagraph();
      yybegin(AFTER_LINE_START);

      return Token.ATX_HEADER;
    }
  }

  // blockquote
  ">" {
    blockQuotes.processMarker();
    return Token.BLOCK_QUOTE;
  }

  // Horizontal rule
  // TODO solve the problem of $ and EOL (they do not match)
  ("*" ({WHITE_SPACE}* "*") ({WHITE_SPACE}* "*")+  | "-" ({WHITE_SPACE}* "-") ({WHITE_SPACE}* "-")+  | "___" "_"* ) {WHITE_SPACE}* $ {
    if (isFourIndent()) {
      resetState();
    }
    else {
      endParagraph();
      return Token.HORIZONTAL_RULE;
    }
  }

  // Unordered lists
  [*+-] / {WHITE_SPACE} {
    increaseIndent(4);
    return Token.LIST_BULLET;
  }

  // Ordered lists
  {DIGIT}+ ("." | ")") {
    increaseIndent(4);
    return Token.LIST_NUMBER;
  }

  // Code fence
  "~~~" "~"* | "```" "`"* / [^\n\r`]* {EOL} {
    if (isFourIndent()) {
      resetState();
    }
    else {
      endParagraph();

      codeFence.fenceChar = yycharat(0);
      codeFence.fenceLength = yylength();
      codeFence.typeWasRead = false;

      yybegin(CODE_FENCE);
      return Token.CODE_FENCE_START;
    }
  }


  . {
    paragraph.currentLineIsNotBlank = true;
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
  \\[\\`*_{}\[\]()#+-.,!:@#$%&~<>/] {
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

  {SECTION_ID} {
    return Token.SECTION_ID;
  }

  {AUTOLINK} { return parseDelimited(Token.AUTOLINK, false); }
  {EMAIL_AUTOLINK} { return parseDelimited(Token.EMAIL_AUTOLINK, false); }

  {HTML_TAG} { return Token.HTML_TAG; }

}

<AFTER_LINE_START> {

  // atx header end
  "#"+ {WHITE_SPACE}* $ {
    if (isHeader) {
      return Token.ATX_HEADER;
    }
    return Token.TEXT;
  }

  {WHITE_SPACE}+ {
    return Token.WHITE_SPACE;
  }

  \" | "'"| "\\" | "(" | ")" | "[" | "]" | "<" | ">" {
    return getDelimiterTokenType(yycharat(0));
  }
  ":" { return Token.COLON; }
  "!" { return Token.EXCLAMATION_MARK; }



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

  // optimize
  {ALPHANUM}+ ({WHITE_SPACE}+ {ALPHANUM}+)* {
    return Token.TEXT;
  }

  . { return Token.TEXT; }

}

<PARSE_DELIMITED> {
  {EOL} { resetState(); }

  {EOL} | . {
    if (yycharat(0) == parseDelimited.exitChar) {
      yybegin(stateStack.pop());
      return getDelimiterTokenType(yycharat(0));
    }
    return parseDelimited.returnType;
  }

}

<HTML_BLOCK> {
  {EOL} {WHITE_SPACE}* {EOL} {
    yybegin(YYINITIAL);
    yypushback(yylength());
  }

  {EOL} | .+ { return Token.HTML_BLOCK; }
}

<CODE> {
  ({EOL} {WHITE_SPACE}*)+ {
    processEol();
    return Token.EOL;
  }

  {EOL} | . { return Token.CODE; }
}

<CODE_FENCE> {
  ("~~~" "~"* | "```" "`"*) / {WHITE_SPACE}* $ {
    if (yycharat(0) == codeFence.fenceChar && yylength() >= codeFence.fenceLength) {
      yybegin(YYINITIAL);
      return Token.CODE_FENCE_END;
    }
    return Token.CODE;
  }

  [^ \f\t\n\r]+ {
    if (!codeFence.typeWasRead) {
      codeFence.typeWasRead = true;
      return Token.FENCE_LANG;
    }
    return Token.CODE;
  }

  {EOL} ({WHITE_SPACE}* ">")* {
    int newLevel = countChars(yytext(), '>');
    if (newLevel < blockQuotes.level) {
      yypushback(yylength() - 1);
      processEol();
      return Token.EOL;
    }
    else {
      codeFence.typeWasRead = true;
      return Token.CODE;
    }
  }

  . {
    return Token.CODE;
  }
}

. { return Token.BAD_CHARACTER; }