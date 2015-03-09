package org.intellij.markdown;

import org.intellij.markdown.lexer.MarkdownLexer;
import junit.framework.TestCase
import java.io.File

public class MarkdownLexerTest : TestCase() {
    private fun getDirPath() = File("test/data/lexer").getAbsolutePath()

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

    public fun testHtmlEntities() {
        defaultTest()
    }

    private fun defaultTest() {
        doFileTest();
    }

    fun printTokens(text: String): String {
        val lexer = MarkdownLexer(text)

        var result = "";
        while (true) {
            val tokenType = lexer.type;
            if (tokenType == null) {
                break;
            }
            val tokenText = getTokenText(lexer);
            val tokenTypeName = tokenType.toString();
            val line = tokenTypeName + " ('" + tokenText + "')";

            if (result.isNotEmpty()) {
                result += "\n";
            }
            result += line;
            lexer.advance();
        }
        return result;
    }

    private fun getTokenText(lexer: MarkdownLexer) = lexer.originalText
            .subSequence(lexer.tokenStart, lexer.tokenEnd)
            .toString()
            .replace("\n", "\\n")

    fun doFileTest() {
        val filePath = getDirPath() + "/" + testName + ".md"
        val result = printTokens(File(filePath).readText())

        assertSameLinesWithFile(getDirPath() + "/" + testName + ".txt", result);
    }
}
