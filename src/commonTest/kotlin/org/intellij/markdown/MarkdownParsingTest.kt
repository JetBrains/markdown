package org.intellij.markdown

import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.*

class MarkdownParsingTest : TestCase() {

    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()) {
        val src = readFromFile(getTestDataPath() + "/" + testName + ".md")
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

    @Test
    fun testEmpty() {
        assertEquals("Markdown:MARKDOWN_FILE", getParsedTreeText(""))
    }

    @Test
    fun testSmall1() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:EOL('\\n')",
                getParsedTreeText("\n"))
    }

    @Test
    fun testSmall2() {
        assertEquals(
                "Markdown:MARKDOWN_FILE\n" +
                "  Markdown:PARAGRAPH\n" +
                "    Markdown:TEXT('test')",
                getParsedTreeText("test"))
    }

    @Test
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

    @Test
    fun testOneSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n" +
                "  WHITE_SPACE(' ')",
                getParsedTreeText(" "))
    }

    @Test
    fun testLeadingSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:PARAGRAPH\n    WHITE_SPACE(' ')\n    Markdown:TEXT('Test')",
                getParsedTreeText(" Test"))
    }

    @Test
    fun testTrailingSpace() {
        assertEquals("Markdown:MARKDOWN_FILE\n  Markdown:PARAGRAPH\n    Markdown:TEXT('Test')\n    WHITE_SPACE(' ')",
                getParsedTreeText("Test "))
    }

    @Test
    fun testLeadingAndTrailingWhitespaces() {
        defaultTest()
    }

    @Test
    fun testSimple() {
        defaultTest()
    }

    @Test
    fun testCodeBlocks() {
        defaultTest()
    }

    @Test
    fun testUnorderedLists() {
        defaultTest()
    }

    @Test
    fun testOrderedLists() {
        defaultTest()
    }

    @Test
    fun testBlockquotes() {
        defaultTest()
    }

    @Test
    fun testHeaders() {
        defaultTest()
    }

    @Test
    fun testHtmlBlocks() {
        defaultTest()
    }

    @Test
    fun testEmphStrong() {
        defaultTest()
    }

    @Test
    fun testCodeFence() {
        defaultTest()
    }

    @Test
    fun testCodeSpan() {
        defaultTest()
    }

    @Test
    fun testLinkDefinitions() {
        defaultTest()
    }

    @Test
    fun testInlineLinks() {
        defaultTest()
    }

    @Test
    fun testReferenceLinks() {
        defaultTest()
    }

    @Test
    fun testTightLooseLists() {
        defaultTest()
    }

    @Test
    fun testHruleAndSetext() {
        defaultTest()
    }

    @Test
    fun testTabStops() {
        defaultTest()
    }

    @Test
    fun testHardLineBreaks() {
        defaultTest()
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testPuppetApache() {
        defaultTest()
    }

    @Test
    fun testRuby16750() {
        defaultTest()
    }

    @Test
    fun testExample208() {
        defaultTest()
    }

    @Test
    fun testExample221() {
        defaultTest()
    }

    @Test
    fun testExample226() {
        defaultTest()
    }

    @Test
    fun testImages() {
        defaultTest()
    }

    @Test
    fun testStrikethrough() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testGfmAutolink() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testSfmAutolink() {
        defaultTest(SFMFlavourDescriptor())
    }

    @Test
    fun testCheckLists() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testGfmTable() {
        defaultTest(GFMFlavourDescriptor())
    }
    
    @Test
    fun testGfmAtxWithoutSpace() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testRuby17337() {
        defaultTest(GFMFlavourDescriptor())
    }
    
    @Test
    fun testEa79689() {
        defaultTest()
    }
    
    @Test
    fun testRuby18237() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testRuby18936() {
        defaultTest(CommonMarkFlavourDescriptor())
    }

    fun testBug28() {
        defaultTest(GFMFlavourDescriptor())
    }
    
    @Test
    fun testNewlinesAndAnyChars() {
        defaultTest(GFMFlavourDescriptor())
    }

    private fun getTestDataPath(): String {
        return getIntellijMarkdownHome() + "/${MARKDOWN_TEST_DATA_PATH}/parser"
    }
}
