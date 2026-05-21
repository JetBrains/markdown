package org.intellij.markdown.parser

import org.intellij.markdown.*
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.sequentialparsers.LexerBasedTokensCache
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil

class MarkdownParser @ExperimentalApi constructor(
    private val flavour: MarkdownFlavourDescriptor,
    private val assertionsEnabled: Boolean = true,
    private val cancellationToken: CancellationToken = CancellationToken.NonCancellable
) {
    constructor(flavour: MarkdownFlavourDescriptor): this(flavour, true)

    @OptIn(ExperimentalApi::class)
    constructor(
        flavour: MarkdownFlavourDescriptor,
        assertionsEnabled: Boolean
    ): this(flavour, assertionsEnabled, CancellationToken.NonCancellable)

    fun buildMarkdownTreeFromString(text: String, baseOffset: Int): ASTNode {
        return parse(MarkdownElementTypes.MARKDOWN_FILE, text, true, baseOffset)
    }

    // To keep the ABI compatibility.
    fun buildMarkdownTreeFromString(text: String): ASTNode {
        return buildMarkdownTreeFromString(text, 0)
    }

    fun parse(root: IElementType, text: String, parseInlines: Boolean, baseOffset: Int): ASTNode {
        return try {
            doParse(root, text, parseInlines, baseOffset).tree
        }
        catch (e: MarkdownParsingException) {
            if (assertionsEnabled)
                throw e
            else
                topLevelFallback(root, text, baseOffset)
        }
    }

    // To keep the ABI compatibility.
    fun parse(root: IElementType, text: String, parseInlines: Boolean = true): ASTNode {
        return parse(root, text, parseInlines, 0)
    }

    fun parseInline(root: IElementType, text: CharSequence, textStart: Int, textEnd: Int, baseOffset: Int): ASTNode {
        return try {
            doParseInline(root, text, textStart, textEnd, baseOffset)
        }
        catch (e: MarkdownParsingException) {
            if (assertionsEnabled)
                throw e
            else
                inlineFallback(root, textStart, textEnd, baseOffset)
        }
    }

    // To keep the ABI compatibility.
    fun parseInline(root: IElementType, text: CharSequence, textStart: Int, textEnd: Int): ASTNode {
        return parseInline(root, text, textStart, textEnd, 0)
    }

    internal fun parseStreaming(root: IElementType, text: String, parseInlines: Boolean, baseOffset: Int): StreamingMarkdownParseResult {
        return try {
            val (tree, openMarkers) = doParse(root, text, parseInlines, baseOffset)
            StreamingMarkdownParseResult(
                tree = tree,
                unstableStartOffset = findUnstableStartOffset(openMarkers, text, baseOffset)
            )
        }
        catch (e: MarkdownParsingException) {
            if (assertionsEnabled) {
                throw e
            } else {
                StreamingMarkdownParseResult(
                    tree = topLevelFallback(root, text, baseOffset),
                    unstableStartOffset = baseOffset
                )
            }
        }
    }

    @OptIn(ExperimentalApi::class)
    private fun doParse(
        root: IElementType,
        text: String,
        parseInlines: Boolean = true,
        baseOffset: Int = 0
    ): MarkdownParseResult {
        val productionHolder = ProductionHolder()
        val markerProcessor = flavour.markerProcessorFactory.createMarkerProcessor(productionHolder)

        val rootMarker = productionHolder.mark()

        val textHolder = LookaheadText(text)
        var pos: LookaheadText.Position? = textHolder.startPosition
        while (pos != null) {
            cancellationToken.checkCancelled()
            productionHolder.updatePosition(pos.offset)
            pos = markerProcessor.processPosition(pos)
        }

        // Take a snapshot before flush
        val openMarkers = markerProcessor.markersStackSnapshot

        productionHolder.updatePosition(text.length)
        markerProcessor.flushMarkers()

        rootMarker.done(root)

        val nodeBuilder = if (parseInlines) {
            InlineExpandingASTNodeBuilder(text, baseOffset)
        } else {
            ASTNodeBuilder(text, cancellationToken, baseOffset)
        }

        val builder = TopLevelBuilder(nodeBuilder)

        return MarkdownParseResult(
            tree = builder.buildTree(productionHolder.production),
            openMarkers = openMarkers
        )
    }

    /**
     * This handles 2 cases:
     *
     * For code blocks/HTML blocks, the internal [MarkerBlock]s are snapshoted before [MarkerProcessor.flushMarkers] to find the unenclosed block and decide the unstable boundary.
     * For other cases, the last blank line is used to decide the unstable boundary.
     */
    private fun findUnstableStartOffset(
        openMarkers: List<MarkerBlock>,
        text: String,
        baseOffset: Int
    ): Int {
        var unstableStartOffset = text.length
        @OptIn(ExperimentalApi::class)
        val firstOpenMarkerOffset = openMarkers.minOfOrNull { it.startOffset }
        firstOpenMarkerOffset?.let { unstableStartOffset = minOf(it, unstableStartOffset) }
        val lastBlankLineEnd = text.lastBlankLineEndOrNull() ?: 0
        unstableStartOffset = minOf(lastBlankLineEnd, unstableStartOffset)
        return unstableStartOffset + baseOffset
    }

    /**
     * Return the exclusive end of the last blank line in the string.
     * This guarantees O(1) memory usage and O(n) worst-case time complexity.
     */
    private tailrec fun String.lastBlankLineEndOrNull(lineEnd: Int = lastIndexOf('\n')): Int? {
        val lastLineEnd = lastIndexOf('\n', lineEnd - 1)
        if (lastLineEnd == -1) return null
        if ((lastLineEnd..lineEnd).all { this[it].isWhitespace() }) return lineEnd + 1
        return lastBlankLineEndOrNull(lastLineEnd)
    }

    @OptIn(ExperimentalApi::class)
    private fun doParseInline(root: IElementType, text: CharSequence, textStart: Int, textEnd: Int, baseOffset: Int = 0): ASTNode {
        val lexer = flavour.createInlinesLexer()
        lexer.start(text, textStart, textEnd)
        val tokensCache = LexerBasedTokensCache(lexer)

        val wholeRange = 0..tokensCache.filteredTokens.size
        val nodes = flavour.sequentialParserManager.runParsingSequence(
            tokensCache = tokensCache,
            rangesToParse = SequentialParserUtil.filterBlockquotes(tokensCache, wholeRange),
            cancellationToken = cancellationToken
        )

        val builder = InlineBuilder(ASTNodeBuilder(text, cancellationToken, baseOffset), tokensCache, cancellationToken)
        return builder.buildTree(nodes + listOf(SequentialParser.Node(wholeRange, root)))
    }

    private fun topLevelFallback(root: IElementType, text: String, baseOffset: Int): ASTNode {
        return CompositeASTNode(
            root, listOf(inlineFallback(MarkdownElementTypes.PARAGRAPH, 0, text.length, baseOffset))
        )
    }

    private fun inlineFallback(root: IElementType, textStart: Int, textEnd: Int, baseOffset: Int): ASTNode {
        return CompositeASTNode(
            root,
            listOf(LeafASTNode(MarkdownTokenTypes.TEXT, textStart + baseOffset, textEnd + baseOffset))
        )
    }

    @OptIn(ExperimentalApi::class)
    private inner class InlineExpandingASTNodeBuilder(
        text: CharSequence,
        private val baseOffset: Int
    ) : ASTNodeBuilder(text, cancellationToken, baseOffset) {
        override fun createLeafNodes(type: IElementType, startOffset: Int, endOffset: Int): List<ASTNode> {
            return when (type) {
                MarkdownElementTypes.PARAGRAPH,
                MarkdownTokenTypes.ATX_CONTENT,
                MarkdownTokenTypes.SETEXT_CONTENT,
                GFMTokenTypes.CELL ->
                    listOf(parseInline(type, text, startOffset, endOffset, baseOffset))
                else ->
                    super.createLeafNodes(type, startOffset, endOffset)
            }
        }
    }
}

internal data class MarkdownParseResult(
    val tree: ASTNode,
    val openMarkers: List<MarkerBlock>
)

internal data class StreamingMarkdownParseResult(
    val tree: ASTNode,
    val unstableStartOffset: Int
)
