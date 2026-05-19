package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import kotlin.test.Test
import kotlin.test.assertEquals
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

    private fun assertTopLevelTypes(nodes: List<ASTNode>, vararg types: IElementType) {
        assertEquals(types.toList(), nodes.map { it.type })
    }

    private fun assertChildrenAreStablePrefixPlusUnstableTail(file: StreamingMarkdownFile) {
        assertEquals(file.stableChildren + file.unstableTail, file.children)
    }
}
