package org.intellij.markdown;

import com.intellij.lexer.EmptyLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LexerTestCase;
import org.intellij.markdown.lexer.MarkdownLexer;

public class MarkdownLexerTest : LexerTestCase() {
    override fun createLexer(): Lexer? {
        return EmptyLexer();
    }

    override fun getDirPath(): String? {
        return "test/data/lexer";
    }

    public fun testSimple() {
        defaultTest();
    }

    public fun testUnorderedLists() {
        defaultTest();
    }

    public fun testOrderedLists() {
        defaultTest();
    }

    public fun testHeaders() {
        defaultTest();
    }

    public fun testBlockquotes() {
        defaultTest();
    }

    public fun testHorizontalRules() {
        defaultTest();
    }

    public fun testCodeBlocks() {
        defaultTest();
    }

    public fun testCodeFence() {
        defaultTest();
    }

    public fun testHtmlBlocks() {
        defaultTest();
    }

    public fun testLinkDefinitions() {
        defaultTest();
    }

    public fun testCodeSpan() {
        defaultTest();
    }

    public fun testLinks() {
        defaultTest();
    }

    public fun testAutolinks() {
        defaultTest();
    }

    private fun defaultTest() {
        doFileTest("md");
    }

    override fun printTokens(text: String, start: Int): String? {
        return printTokens(MarkdownLexer(text));
    }

    class object {
        public fun printTokens(lexer: MarkdownLexer): String {

            var result = "";
            while (true) {
                val tokenType = lexer.type;
                if (tokenType == null) {
                    break;
                }
                val tokenText = getTokenText(lexer);
                val tokenTypeName = tokenType.toString();
                val line = tokenTypeName +" ('" + tokenText + "')\n";
                result += line;
                lexer.advance();
            }
            return result;
        }

        private fun getTokenText(lexer: MarkdownLexer): String {
            var text = lexer.originalText.subSequence(lexer.tokenStart, lexer.tokenEnd).toString();
            text = StringUtil.replace(text, "\n", "\\n");
            return text;
        }
    }
}
