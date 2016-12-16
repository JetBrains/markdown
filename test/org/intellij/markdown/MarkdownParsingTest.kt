package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import java.io.File

class MarkdownParsingTest : TestCase() {

    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()) {
        val src = File(getTestDataPath() + "/" + testName + ".md").readText()
        val result = getParsedTreeText(src, flavour)

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".txt", result)
    }

    private fun getParsedTreeText(inputText: String,
                                  flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()): String {
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(inputText)
        return treeToStr(inputText, tree)
    }

    private fun treeToStr(src: String, tree: ASTNode): String {
        return treeToStr(src, tree, StringBuilder(), 0).toString()
    }

    private fun treeToStr(src: String, tree: ASTNode, sb: StringBuilder, depth: Int): StringBuilder {
        if (sb.length > 0) {
            sb.append('\n')
        }
        repeat(depth * 2) { sb.append(' '); }

        sb.append(tree.type.toString())
        if (tree is LeafASTNode) {
            val str = src.substring(tree.startOffset, tree.endOffset)
            sb.append("('").append(str.replace("\\n".toRegex(), "\\\\n")).append("')")
        }
        for (child in tree.children) {
            treeToStr(src, child, sb, depth + 1)
        }

        return sb
    }

    fun testEmpty() {
        assertEquals("Markdown:MARKDOWN_FILE", getParsedTreeText(""))
    }

    fun testSmall1() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:EOL('\\n')",
                getParsedTreeText("\n"))
    }

    fun testSmall2() {
        assertEquals(
                "Markdown:MARKDOWN_FILE\n" +
                "  Markdown:PARAGRAPH\n" +
                "    Markdown:TEXT('test')",
                getParsedTreeText("test"))
    }

    fun testSmall3() {
        assertEquals(
                "Markdown:MARKDOWN_FILE\n" +
                "  Markdown:PARAGRAPH\n" +
                "    Markdown:EMPH\n" +
                "      Markdown:EMPH('*')\n" +
                "      Markdown:TEXT('test')\n" +
                "      Markdown:EMPH('*')",
                getParsedTreeText("*test*"))
    }

    fun testOneSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n" +
                "  WHITE_SPACE(' ')",
                getParsedTreeText(" "))
    }

    fun testLeadingSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:PARAGRAPH\n    WHITE_SPACE(' ')\n    Markdown:TEXT('Test')",
                getParsedTreeText(" Test"))
    }

    fun testTrailingSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:PARAGRAPH\n    Markdown:TEXT('Test')\n    WHITE_SPACE(' ')",
                getParsedTreeText("Test "))
    }

    fun testLeadingAndTrailingWhitespaces() {
        defaultTest()
    }

    fun testSimple() {
        defaultTest()
    }

    fun testCodeBlocks() {
        defaultTest()
    }

    fun testUnorderedLists() {
        defaultTest()
    }

    fun testOrderedLists() {
        defaultTest()
    }

    fun testBlockquotes() {
        defaultTest()
    }

    fun testHeaders() {
        defaultTest()
    }

    fun testHtmlBlocks() {
        defaultTest()
    }

    fun testEmphStrong() {
        defaultTest()
    }

    fun testCodeFence() {
        defaultTest()
    }

    fun testCodeSpan() {
        defaultTest()
    }

    fun testLinkDefinitions() {
        defaultTest()
    }

    fun testInlineLinks() {
        defaultTest()
    }

    fun testReferenceLinks() {
        defaultTest()
    }

    fun testTightLooseLists() {
        defaultTest()
    }

    fun testHruleAndSetext() {
        defaultTest()
    }

    fun testTabStops() {
        defaultTest()
    }

    fun testHardLineBreaks() {
        defaultTest()
        defaultTest(GFMFlavourDescriptor())
    }

    fun testPuppetApache() {
        defaultTest()
    }

    fun testRuby16750() {
        defaultTest()
    }

    fun testExample208() {
        defaultTest()
    }

    fun testExample221() {
        defaultTest()
    }

    fun testExample226() {
        defaultTest()
    }

    fun testImages() {
        defaultTest()
    }

    fun testStrikethrough() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testGfmAutolink() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testCheckLists() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testGfmTable() {
        defaultTest(GFMFlavourDescriptor())
    }
    
    fun testGfmAtxWithoutSpace() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testRuby17337() {
        defaultTest(GFMFlavourDescriptor())
    }
    
    fun testEa79689() {
        defaultTest()
    }
    
    fun testRuby18237() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testRuby18936() {
        defaultTest(CommonMarkFlavourDescriptor())
    }
    
    fun testNewlinesAndAnyChars() {
        defaultTest(GFMFlavourDescriptor())
    }

    private fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/parser").absolutePath
    }
}
