package org.intellij.markdown;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.UsefulTestCase;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.ast.LeafASTNode;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor;

import java.io.File;
import kotlin.test.assertEquals

public class MarkdownParsingTest : UsefulTestCase() {

    private fun defaultTest() {
        val src = FileUtil.loadFile(File(getTestDataPath() + "/" + getTestName(true) + ".md")).trim();
        val result = getParsedTreeText(src);

        UsefulTestCase.assertSameLinesWithFile(getTestDataPath() + "/" + getTestName(false) + ".txt", result);
    }

    private fun getParsedTreeText(inputText: String): String {
        val tree = MarkdownParser(CommonMarkMarkerProcessor.Factory()).buildMarkdownTreeFromString(inputText);
        return treeToStr(inputText, tree);
    }

    private fun treeToStr(src: String, tree: ASTNode): String {
        return treeToStr(src, tree, StringBuilder(), 0).toString();
    }

    private fun treeToStr(src: String, tree: ASTNode, sb: StringBuilder, depth: Int): StringBuilder {
        if (sb.length() > 0) {
            sb.append('\n');
        }
        (depth * 2).times { sb.append(' '); }

        sb.append(tree.type.toString());
        if (tree is LeafASTNode) {
            val str = src.substring(tree.startOffset, tree.endOffset);
            sb.append("('").append(str.replaceAll("\\n", "\\\\n")).append("')");
        }
        for (child in tree.children) {
            treeToStr(src, child, sb, depth + 1);
        }

        return sb;
    }

    public fun testEmpty() {
        assertEquals("Markdown:MARKDOWN_FILE", getParsedTreeText(""));
    }

    public fun testSmall1() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:EOL('\\n')",
                getParsedTreeText("\n"));
    }

    public fun testSmall2() {
        assertEquals(
                "Markdown:MARKDOWN_FILE\n" +
                "  Markdown:PARAGRAPH\n" +
                "    Markdown:TEXT('test')",
                getParsedTreeText("test"));
    }

    public fun testSmall3() {
        assertEquals(
                "Markdown:MARKDOWN_FILE\n" +
                "  Markdown:PARAGRAPH\n" +
                "    Markdown:EMPH\n" +
                "      Markdown:EMPH('*')\n" +
                "      Markdown:TEXT('test')\n" +
                "      Markdown:EMPH('*')",
                getParsedTreeText("*test*"));
    }

    public fun testSimple() {
        defaultTest();
    }

    public fun testCodeBlocks() {
        defaultTest();
    }

    public fun testUnorderedLists() {
        defaultTest();
    }

    public fun testOrderedLists() {
        defaultTest();
    }

    public fun testBlockquotes() {
        defaultTest();
    }

    public fun testHeaders() {
        defaultTest();
    }

    public fun testHtmlBlocks() {
        defaultTest();
    }

    public fun testEmphStrong() {
        defaultTest();
    }

    public fun testCodeFence() {
        defaultTest();
    }

    public fun testCodeSpan() {
        defaultTest();
    }

    public fun testLinkDefinitions() {
        defaultTest();
    }

    public fun testInlineLinks() {
        defaultTest();
    }

    public fun testReferenceLinks() {
        defaultTest();
    }

    protected fun getTestDataPath(): String {
        return File("test/data/parser").getAbsolutePath();
    }
}
