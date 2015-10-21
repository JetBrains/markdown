package org.intellij.markdown;

import junit.framework.TestCase
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import kotlin.test.assertEquals

public class MarkdownParsingTest : TestCase() {

    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()) {
        val src = File(getTestDataPath() + "/" + testName + ".md").readText();
        val result = getParsedTreeText(src, flavour);

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".txt", result);
    }

    private fun getParsedTreeText(inputText: String,
                                  flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()): String {
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(inputText);
        return treeToStr(inputText, tree);
    }

    private fun treeToStr(src: String, tree: ASTNode): String {
        return treeToStr(src, tree, StringBuilder(), 0).toString();
    }

    private fun treeToStr(src: String, tree: ASTNode, sb: StringBuilder, depth: Int): StringBuilder {
        if (sb.length > 0) {
            sb.append('\n');
        }
        repeat(depth * 2) { sb.append(' '); }

        sb.append(tree.type.toString());
        if (tree is LeafASTNode) {
            val str = src.substring(tree.startOffset, tree.endOffset);
            sb.append("('").append(str.replace("\\n".toRegex(), "\\\\n")).append("')");
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

    public fun testOneSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n" +
                "  WHITE_SPACE(' ')",
                getParsedTreeText(" "));
    }

    public fun testLeadingSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:PARAGRAPH\n    WHITE_SPACE(' ')\n    Markdown:TEXT('Test')",
                getParsedTreeText(" Test"));
    }

    public fun testTrailingSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:PARAGRAPH\n    Markdown:TEXT('Test')\n    WHITE_SPACE(' ')",
                getParsedTreeText("Test "));
    }

    public fun testLeadingAndTrailingWhitespaces() {
        defaultTest()
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

    public fun testTightLooseLists() {
        defaultTest()
    }

    public fun testHruleAndSetext() {
        defaultTest()
    }

    public fun testPuppetApache() {
        defaultTest()
    }

    public fun testRuby16750() {
        defaultTest()
    }

    public fun testExample208() {
        defaultTest()
    }

    public fun testExample221() {
        defaultTest()
    }

    public fun testExample226() {
        defaultTest()
    }

    public fun testImages() {
        defaultTest()
    }

    public fun testStrikethrough() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testGfmAutolink() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testCheckLists() {
        defaultTest(GFMFlavourDescriptor())
    }

    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/parser").absolutePath;
    }
}
