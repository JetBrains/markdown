package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes.MARKDOWN_FILE
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor

/**
 * Mutable AST node that accept appending chunks.
 *
 * THIS INSTANCE IS NOT CONCURRENT SAFE. NEVER ACCESS IT CONCURRENTLY.
 *
 * When [append] is called, only the [unstableTail] part will be reparsed.
 * The only way to update its state is to invoke [append].
 * Then [endOffset], [stableChildren], [unstableTail] and [children] will be updated.
 *
 * To avoid memory leak, it doesn't hold the whole original Markdown string.
 * The caller may accumulate the original string outside the [StreamingMarkdownFile].
 *
 * After each append, [StreamingMarkdownFile] represents a complete Markdown document ending at the current offset.
 */
interface StreamingMarkdownFile: ASTNode {
    override val type : IElementType
        get() = MARKDOWN_FILE
    override val startOffset : Int
        get() = 0
    override val endOffset : Int
    override val parent: Nothing?
        get() = null
    override val children : List<ASTNode>
        get() = stableChildren + unstableTail

    /**
     * The accumulated stable part of the Markdown file.
     *
     * It's append-only, and the appended [ASTNode] will not be reparsed or replaced.
     */
    val stableChildren : List<ASTNode>

    /**
     * The unstable part of the Markdown file.
     * It may be updated by [append]. Every [append] will reparse this part.
     * If some part of [unstableTail] gets stable, it will be removed and appended to [stableChildren].
     */
    val unstableTail: List<ASTNode>

    /**
     * Appends a chunk of text to the current Markdown document.
     *
     * This method updates the internal state of the document by parsing the chunk
     * and merging it into the unstable portion of the Markdown file. Once the append
     * operation is completed, the document represents a fully parsed and updated
     * Markdown structure ending at the current offset.
     *
     * Note: This method is not thread-safe and should not be called concurrently.
     */
    fun append(chunk: CharSequence)
}

@Suppress("FunctionName")
fun EmptyStreamingMarkdownFile(
    flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor(),
): StreamingMarkdownFile = StreamingMarkdownFileImpl(MarkdownParser(flavour))

private class StreamingMarkdownFileImpl(
    private val parser: MarkdownParser,
) : StreamingMarkdownFile {
    // FIXME: use backing fields feature with Kotlin 2.3.0
    private val stableChildrenBacking = mutableListOf<ASTNode>()
    private var stableTextLength = 0
    private val unstableText = StringBuilder()
    // FIXME: use backing fields feature with Kotlin 2.3.0
    private var unstableTailBacking: List<ASTNode> = emptyList()

    override val endOffset: Int
        get() = stableTextLength + unstableText.length

    override val stableChildren: List<ASTNode>
        get() = stableChildrenBacking

    override val unstableTail: List<ASTNode>
        get() = unstableTailBacking

    override fun append(chunk: CharSequence) {
        if (chunk.isEmpty()) {
            return
        }

        unstableText.append(chunk)

        val (unstableTree, unstableStartOffset) = parser.parseStreaming(
            MARKDOWN_FILE,
            unstableText,
            parseInlines = true,
            baseOffset = stableTextLength
        )
        val stableBoundary = unstableStartOffset - stableTextLength
        if (stableBoundary > 0) {
            val newStableChildren = unstableTree.children
                .filter { it.endOffset <= stableTextLength + stableBoundary }
            stableChildrenBacking.addAll(newStableChildren)
            stableTextLength += stableBoundary
            unstableText.deleteRange(0, stableBoundary)
            unstableTailBacking = unstableTree.children
                .filter { it.startOffset >= stableTextLength }
        } else {
            unstableTailBacking = unstableTree.children
        }
    }
}
