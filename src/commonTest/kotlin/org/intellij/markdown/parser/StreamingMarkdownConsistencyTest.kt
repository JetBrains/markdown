package org.intellij.markdown.parser

import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals

class StreamingMarkdownConsistencyTest {
    private val parser = MarkdownParser(GFMFlavourDescriptor())

    @Test
    fun fixedChunks() {
        assertStreamingMatchesFullParse(
            text = LARGE_MARKDOWN_TEXT,
            chunkSizes = listOf(1, 2, 3, 5, 8, 13, 21, 34)
        )
    }

    @Test
    fun deterministicPseudoRandomChunks() {
        assertStreamingMatchesFullParse(
            text = LARGE_MARKDOWN_TEXT,
            chunkSizes = pseudoRandomChunkSizes(LARGE_MARKDOWN_TEXT.length)
        )
    }

    @Test
    fun setextHeadingArrivesLineByLine() {
        val text = """
            A heading
            ---------

            A paragraph after the heading.
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun tableArrivesLineByLine() {
        val text = """
            | Name | Value |
            | ---- | ----- |
            | one  | **1** |
            | two  | `2`   |
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun nestedListArrivesLineByLine() {
        val text = """
            - first item
            - second item
              - nested item
            - third item
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun orderedListArrivesLineByLine() {
        val text = """
            1. first item
            2. second item
            3. third item
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun listItemContinuationArrivesLineByLine() {
        val text = """
            - first item
              continuation of first item
            - second item
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun blockQuoteArrivesLineByLine() {
        val text = """
            > first quote line
            > second quote line with *emphasis*

            paragraph after quote
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun lazyBlockQuoteContinuationArrivesLineByLine() {
        val text = """
            > first quote line
            lazy continuation line

            paragraph after quote
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun codeFenceArrivesLineByLine() {
        val text = """
            ```kotlin
            fun answer(): Int {
                return 42
            }
            ```

            after fence
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun indentedCodeBlockArrivesLineByLine() {
        val text = """
                first code line
                second code line

            paragraph after indented code
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun htmlBlockArrivesLineByLine() {
        val text = """
            <div>
            text in html block
            </div>

            paragraph after html
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun referenceDefinitionArrivesAfterReferenceLink() {
        val text = """
            [JetBrains][jb]

            [jb]: https://www.jetbrains.com
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = text.lineChunkSizes()
        )
    }

    @Test
    fun formattedParagraphArrivesInSmallChunks() {
        val text = """
            This paragraph has **strong text**, _emphasis_, `inline code`, [a link](https://example.com),
            and enough words to be split across several chunks without ending the paragraph too early.
        """.trimIndent()
        assertStreamingMatchesFullParse(
            text = text,
            chunkSizes = listOf(4, 7, 2, 11, 5, 13)
        )
    }

    private fun assertStreamingMatchesFullParse(text: String, chunkSizes: List<Int>) {
        val file = EmptyStreamingMarkdownFile()
        val consumedText = StringBuilder()
        var offset = 0
        var chunkIndex = 0
        while (offset < text.length) {
            val chunkSize = minOf(chunkSizes[chunkIndex % chunkSizes.size], text.length - offset)
            val chunk = text.substring(offset, offset + chunkSize)
            file.append(chunk)
            consumedText.append(chunk)

            val expectedTree = parser.buildMarkdownTreeFromString(consumedText.toString())
            assertEquals(
                expectedTree.children.toComparableTree(),
                file.children.toComparableTree(),
                "Streaming parse should match full parse after consuming ${offset + chunkSize} chars with chunk '$chunk'"
            )

            offset += chunkSize
            chunkIndex++
        }
    }

    private fun pseudoRandomChunkSizes(length: Int): List<Int> {
        var state = 17L
        var remaining = length
        val result = mutableListOf<Int>()
        while (remaining > 0) {
            state = state * 1103515245 + 12345
            val size = (kotlin.math.abs(state) % 37 + 1).toInt()
            result.add(minOf(size, remaining))
            remaining -= size
        }
        return result
    }

    private fun String.lineChunkSizes(): List<Int> {
        val result = mutableListOf<Int>()
        var chunkStart = 0
        for (index in indices) {
            if (this[index] == '\n') {
                result.add(index - chunkStart + 1)
                chunkStart = index + 1
            }
        }
        if (chunkStart < length) {
            result.add(length - chunkStart)
        }
        return result
    }

    private fun List<ASTNode>.toComparableTree(): List<ComparableNode> {
        return map { it.toComparableTree() }
    }

    private fun ASTNode.toComparableTree(): ComparableNode {
        return ComparableNode(
            type = type.toString(),
            startOffset = startOffset,
            endOffset = endOffset,
            children = children.toComparableTree()
        )
    }

    private data class ComparableNode(
        val type: String,
        val startOffset: Int,
        val endOffset: Int,
        val children: List<ComparableNode>
    )

    private companion object {
        private val LARGE_MARKDOWN_TEXT = """
            # Streaming parser consistency

            This is a long paragraph with **strong text**, _emphasis_, `inline code`,
            [an inline link](https://example.com), and enough words to cross many chunk
            boundaries while still being a single paragraph in Markdown.

            ## Lists and quotes

            - first item with **bold**
            - second item with `code`
              - nested item with [link](https://kotlinlang.org)

            > A block quote can contain *emphasis* and multiple lines.
            > It should stay structurally equivalent when parsed through streaming chunks.

            ```kotlin
            fun answer(): Int {
                return 42
            }
            ```

            | Name | Value |
            | ---- | ----- |
            | one  | **1** |
            | two  | `2`   |

            A final paragraph without a trailing blank line, so the last append still has an unstable tail
        """.trimIndent()
    }
}
