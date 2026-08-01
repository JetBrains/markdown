package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class StreamingMarkdownFileTest {
    @Test
    fun emptyFileIsMarkdownRoot() {
        val file = EmptyStreamingMarkdownFile()

        assertEquals(MarkdownElementTypes.MARKDOWN_FILE, file.type)
        assertEquals(0, file.startOffset)
        assertEquals(0, file.endOffset)
        assertNull(file.parent)
        assertTrue(file.stableChildren.isEmpty())
        assertTrue(file.unstableTail.isEmpty())
        assertTrue(file.children.isEmpty())
    }

    @Test
    fun appendEmptyChunkDoesNothing() {
        val file = EmptyStreamingMarkdownFile()

        file.append("")

        assertEquals(0, file.endOffset)
        assertTrue(file.stableChildren.isEmpty())
        assertTrue(file.unstableTail.isEmpty())
        assertTrue(file.children.isEmpty())
    }

    @Test
    fun appendUpdatesEndOffset() {
        val file = EmptyStreamingMarkdownFile()

        file.append("hello")

        assertEquals(5, file.endOffset)
        assertChildrenAreStablePrefixPlusUnstableTail(file)
    }

    @Test
    fun childrenAreStablePrefixPlusUnstableTail() {
        val file = EmptyStreamingMarkdownFile()

        file.append("hello\n\n")
        file.append("world")

        assertChildrenAreStablePrefixPlusUnstableTail(file)
    }

    @Test
    fun stableChildrenAreNotReplacedByLaterAppend() {
        val file = EmptyStreamingMarkdownFile()

        file.append("hello\n\n")
        val stablePrefix = file.stableChildren.toList()

        file.append("world")

        assertTrue(stablePrefix.isNotEmpty())
        assertTrue(file.stableChildren.size >= stablePrefix.size)
        stablePrefix.forEachIndexed { index, stableNode ->
            assertSame(stableNode, file.stableChildren[index])
        }
        assertChildrenAreStablePrefixPlusUnstableTail(file)
    }

    @Test
    fun unstableTailCanBeReinterpretedByLaterChunk() {
        val file = EmptyStreamingMarkdownFile()

        file.append("hello")

        assertTopLevelTypes(file.unstableTail, MarkdownElementTypes.PARAGRAPH)

        file.append("\n---")

        assertTopLevelTypes(file.stableChildren)
        assertTopLevelTypes(file.unstableTail, MarkdownElementTypes.SETEXT_2)
        assertChildrenAreStablePrefixPlusUnstableTail(file)
    }

    @Test
    fun longFormattedParagraphStaysAsSingleUnstableParagraph() {
        val file = EmptyStreamingMarkdownFile()

        file.append("This is a long paragraph with **strong text**, ")
        file.append("_emphasis_, `inline code`, and [a link](https://example.com)")

        assertTopLevelTypes(file.stableChildren)
        assertTopLevelTypes(file.unstableTail, MarkdownElementTypes.PARAGRAPH)
        assertTrue(file.unstableTail.single().containsNodeOfType(MarkdownElementTypes.STRONG))
        assertTrue(file.unstableTail.single().containsNodeOfType(MarkdownElementTypes.EMPH))
        assertTrue(file.unstableTail.single().containsNodeOfType(MarkdownElementTypes.CODE_SPAN))
        assertTrue(file.unstableTail.single().containsNodeOfType(MarkdownElementTypes.INLINE_LINK))
        assertChildrenAreStablePrefixPlusUnstableTail(file)
    }

    @Test
    fun longFormattedParagraphIsPromotedAfterBlankLine() {
        val file = EmptyStreamingMarkdownFile()

        file.append("This is a long paragraph with **strong text**, ")
        file.append("_emphasis_, `inline code`, and [a link](https://example.com)")
        file.append("\n\n")

        val paragraph = file.stableChildren.single { it.type == MarkdownElementTypes.PARAGRAPH }
        assertTrue(paragraph.containsNodeOfType(MarkdownElementTypes.STRONG))
        assertTrue(paragraph.containsNodeOfType(MarkdownElementTypes.EMPH))
        assertTrue(paragraph.containsNodeOfType(MarkdownElementTypes.CODE_SPAN))
        assertTrue(paragraph.containsNodeOfType(MarkdownElementTypes.INLINE_LINK))
        assertTrue(file.unstableTail.isEmpty())
        assertChildrenAreStablePrefixPlusUnstableTail(file)
    }

    @Test
    fun stablePrefixIsPromotedAfterAppend() {
        val file = EmptyStreamingMarkdownFile()

        file.append("hello\n\n")

        assertEquals(MarkdownElementTypes.PARAGRAPH, file.stableChildren.firstOrNull()?.type)
        assertTrue(file.unstableTail.isEmpty())
        assertChildrenAreStablePrefixPlusUnstableTail(file)
    }

    @Test
    fun unstableTailOffsetsAreRelativeToFullText() {
        val file = EmptyStreamingMarkdownFile()

        file.append("hello\n\n")
        file.append("world")

        assertEquals(7, file.unstableTail.single().startOffset)
        assertEquals(12, file.unstableTail.single().endOffset)
    }

    @Test
    fun appendChecksCancellationWhileUnstableTailGrows() {
        var checkCount = 0
        val file = EmptyStreamingMarkdownFile(
            GFMFlavourDescriptor(),
            CancellationToken { checkCount++ },
        )

        // An unterminated fence keeps the whole document unstable, so each append reparses all of it.
        file.append("```\n")
        val checksAfterFirstAppend = checkCount
        file.append("still inside the fence\n")

        assertTrue(file.stableChildren.isEmpty())
        assertTrue(file.unstableTail.isNotEmpty())
        assertTrue(checkCount > checksAfterFirstAppend)
    }

    @Test
    fun appendPropagatesCancellation() {
        val file = EmptyStreamingMarkdownFile(
            GFMFlavourDescriptor(),
            CancellationToken { throw TestCancellationException() },
        )

        assertFailsWith<TestCancellationException> {
            file.append("# heading\n\nparagraph\n\n- item\n")
        }
    }

    private class TestCancellationException : RuntimeException()

    private fun assertTopLevelTypes(nodes: List<ASTNode>, vararg types: IElementType) {
        assertEquals(types.toList(), nodes.map { it.type })
    }

    private fun assertChildrenAreStablePrefixPlusUnstableTail(file: StreamingMarkdownFile) {
        assertEquals(file.stableChildren + file.unstableTail, file.children)
    }

    private fun ASTNode.containsNodeOfType(type: IElementType): Boolean {
        return this.type == type || children.any { it.containsNodeOfType(type) }
    }
}
