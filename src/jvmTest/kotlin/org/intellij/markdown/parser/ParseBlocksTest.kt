package org.intellij.markdown.parser

import org.intellij.markdown.MARKDOWN_TEST_DATA_PATH
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.getIntellijMarkdownHome
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParseBlocksTest {
    @Test
    fun blockParsingResultMatchesFullFileParsing() {
        val files = listOf(MARKDOWN_TEST_DATA_PATH, "src/jvmTest/resources/data")
            .flatMap { path -> File(getIntellijMarkdownHome(), path).walk().filter { it.extension == "md" } }
        assertTrue(files.isNotEmpty())
        for (flavour in listOf(CommonMarkFlavourDescriptor(), GFMFlavourDescriptor())) {
            val parser = MarkdownParser(flavour)
            for (file in files) {
                val text = file.readText()
                val expected = parser.parse(MarkdownElementTypes.MARKDOWN_FILE, text, parseInlines = false).children.map { it.dump() }
                assertEquals(expected, parseInBlocks(parser, text).map { it.dump() }, file.name)
            }
        }
    }

    @Test
    fun stopsAtNextSafePoint() {
        val text = "- item\n\n  continued item\n\nParagraph.\n"
        val tree = MarkdownParser(CommonMarkFlavourDescriptor()).parseBlocks(text, 0)
        assertEquals(text.indexOf("Paragraph"), tree.endOffset)
    }

    private fun parseInBlocks(parser: MarkdownParser, text: String): List<ASTNode> {
        val nodes = ArrayList<ASTNode>()
        var offset = 0
        while (offset < text.length) {
            val tree = parser.parseBlocks(text, offset)
            assertEquals(offset, tree.startOffset)
            assertTrue(tree.endOffset > offset)
            nodes.addAll(tree.children)
            offset = tree.endOffset
        }
        return nodes
    }

    private fun ASTNode.dump(): String = "$type[$startOffset, $endOffset](${children.joinToString { it.dump() }})"
}
