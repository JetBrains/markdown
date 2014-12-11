package org.intellij.markdown;

import com.intellij.lexer.EmptyLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LexerTestCase;
import org.intellij.markdown.lexer.MarkdownLexer;

public class MarkdownLexerTest extends LexerTestCase {
    @Override protected Lexer createLexer() {
        return new EmptyLexer();
    }

    @Override protected String getDirPath() {
        return "test/data/lexer";
    }

    public void testSimple() {
        defaultTest();
    }

    public void testUnorderedLists() {
        defaultTest();
    }

    public void testOrderedLists() {
        defaultTest();
    }

    public void testHeaders() {
        defaultTest();
    }

    public void testBlockquotes() {
        defaultTest();
    }

    public void testHorizontalRules() {
        defaultTest();
    }

    public void testCodeBlocks() {
        defaultTest();
    }

    public void testCodeFence() {
        defaultTest();
    }

    public void testHtmlBlocks() {
        defaultTest();
    }

    public void testLinkDefinitions() {
        defaultTest();
    }

    public void testCodeSpan() {
        defaultTest();
    }

    public void testLinks() {
        defaultTest();
    }

    public void testAutolinks() {
        defaultTest();
    }

    private void defaultTest() {
        doFileTest("md");
    }

    @Override protected String printTokens(String text, int start) {
        return printTokens(new MarkdownLexer(text));
    }

    public static String printTokens(MarkdownLexer lexer) {

        String result = "";
        while (true) {
            IElementType tokenType = lexer.getType();
            if (tokenType == null) {
                break;
            }
            String tokenText = getTokenText(lexer);
            String tokenTypeName = tokenType.toString();
            String line = tokenTypeName + " ('" + tokenText + "')\n";
            result += line;
            lexer.advance();
        }
        return result;
    }

    private static String getTokenText(MarkdownLexer lexer) {
        String text = lexer.getOriginalText().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
        text = StringUtil.replace(text, "\n", "\\n");
        return text;
    }
}
