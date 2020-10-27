package org.intellij.markdown.flavours.space.lexer;

import org.intellij.markdown.MarkdownTokenTypes;
import org.intellij.markdown.flavours.gfm.GFMTokenTypes;
import org.intellij.markdown.IElementType;
import org.intellij.markdown.lexer.GeneratedLexer;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/* Auto generated File */
%%

%class _SFMLexer
%unicode
%public
%caseless

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

  private int calcBalance(int startPos) {
      int balance = 0;
      for (int i = startPos; i >= 0; --i) {
          char c = yycharat(i);
          if (c == ')') {
              balance++;
          }
          else if (c == '(') {
              balance--;
              if (balance <= 0) break;
          }
      }
      return balance;
  }

  private void pushbackAutolink() {
      int length = yylength();
      if (yycharat(length - 1) == '/') {
          while (yycharat(length - 2) == '/') length--;
          yypushback(yylength() - length);
          return;
      }

      int balance = -1;

      // See GFM_AUTOLINK rule
      String badEnding = ".,:;!?\"'*_~]`";

      for (int i = length - 1; i >= 0; --i) {
          char c = yycharat(i);
          if (c == ')') {
              if (balance == -1) {
                  balance = calcBalance(i);
              }

              // If there are not enough opening brackets to match this closing one, drop this bracket
              if (balance > 0) {
                  balance--;
              }
              else {
                  break;
              }
          }
          else if (badEnding.indexOf(c) == -1) {
              break;
          }

          length--;
      }

      yypushback(yylength() - length);
  }
  }

  private final String beforeSfmAutolink = " \n\t(";
  private final String afterSfmAutolink = " \n\t).,;:!?~";

  private boolean breaksWord() {
      return (zzStartRead != 0 && beforeSfmAutolink.indexOf(yycharat(-1)) == -1) ||
       (yycharat(yylength() - 1) != '/'
        && zzStartRead+yylength() < zzEndRead
        && afterSfmAutolink.indexOf(yycharat(yylength())) == -1);
  }

%}

TLD = com|org|net|int|edu|gov|mil|team|space|travel|site|pro|name|new|info|dev|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bl|bm|bn|bo|bq|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cw|cx|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mf|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pm|pn|pr|ps|pt|pw|qa|re|ro|ru|rw|sa|sb|sc|sd|se|sg|si|sj|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|um|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ಭಾರತ|한국|ଭାରତ|ভাৰত|ভারত|বাংলা|қаз|срб|бг|бел|சிங்கப்பூர்|мкд|ею|中国|中國|భారత్|ලංකා|ભારત|भारतम्|भारत|भारोत|укр|香港|台湾|台灣|мон|澳門|გე|ไทย|рф|ລາວ|ευ|ελ|ഭാരതം|ਭਾਰਤ|இலங்கை|இந்தியா|հայ|新加坡|ye|yt|za|zm|zw

DIGIT = [0-9]
ALPHANUM = [\p{Letter}\p{Number}]
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

HOST_PART={ALPHANUM}([a-zA-Z0-9_-]*{ALPHANUM})?
PATH_PART=[\S&&[^\]]]|\][^\[(]
// See pushbackAutolink method
//GFM_AUTOLINK = (("http" "s"? | "ftp" | "file")"://" | "www.") {HOST_PART} ("." {HOST_PART})* (":" [0-9]+)? ("/"{PATH_PART}+)* "/"?
SFM_AUTOLINK = (({SCHEME}"://") {HOST_PART} ("." {HOST_PART})* | ({HOST_PART} ".")+ {TLD} ) (":" [0-9]+)? ("/"{PATH_PART}+)* "/"?

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

  {SFM_AUTOLINK} {
    pushbackAutolink();
    if (breaksWord()) {
      return Token.TEXT;
    } else {
      return GFMTokenTypes.GFM_AUTOLINK;
    }
  }

  {ALPHANUM}+ (({WHITE_SPACE}+ | "_"+) {ALPHANUM}+)* / {WHITE_SPACE}+ {SFM_AUTOLINK} {
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