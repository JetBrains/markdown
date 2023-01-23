package org.intellij.markdown.parser

import org.intellij.markdown.*
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.sequentialparsers.LexerBasedTokensCache
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil

class MarkdownParser(
    private val flavour: MarkdownFlavourDescriptor,
    private val assertionsEnabled: Boolean = true
) {
    constructor(flavour: MarkdownFlavourDescriptor): this(flavour, true)

    fun buildMarkdownTreeFromString(text: String): ASTNode {
        return parse(MarkdownElementTypes.MARKDOWN_FILE, text, true)
    }

    fun parse(root: IElementType, text: String, parseInlines: Boolean = true): ASTNode {
        return try {
            doParse(root, text, parseInlines)
        }
        catch (e: MarkdownParsingException) {
            if (assertionsEnabled)
                throw e
            else
                topLevelFallback(root, text)
        }
    }

    fun parseInline(root: IElementType, text: CharSequence, textStart: Int, textEnd: Int): ASTNode {
        return try {
            doParseInline(root, text, textStart, textEnd)
        }
        catch (e: MarkdownParsingException) {
            if (assertionsEnabled)
                throw e
            else
                inlineFallback(root, textStart, textEnd)
        }
    }

    private fun doParse(root: IElementType, text: String, parseInlines: Boolean = true): ASTNode {
        val productionHolder = ProductionHolder()
        val markerProcessor = flavour.markerProcessorFactory.createMarkerProcessor(productionHolder)

        val rootMarker = productionHolder.mark()

        val textHolder = LookaheadText(text)
        var pos: LookaheadText.Position? = textHolder.startPosition
        while (pos != null) {
            productionHolder.updatePosition(pos.offset)
            pos = markerProcessor.processPosition(pos)
        }

        productionHolder.updatePosition(text.length)
        markerProcessor.flushMarkers()

        rootMarker.done(root)

        val nodeBuilder = if (parseInlines) {
            InlineExpandingASTNodeBuilder(text)
        } else {
            ASTNodeBuilder(text)
        }

        val builder = TopLevelBuilder(nodeBuilder)

        return builder.buildTree(productionHolder.production)
    }

    private fun doParseInline(root: IElementType, text: CharSequence, textStart: Int, textEnd: Int): ASTNode {
        val lexer = flavour.createInlinesLexer()
        lexer.start(text, textStart, textEnd)
        val tokensCache = LexerBasedTokensCache(lexer)

        val wholeRange = 0..tokensCache.filteredTokens.size
        val nodes = flavour.sequentialParserManager.runParsingSequence(tokensCache,
                SequentialParserUtil.filterBlockquotes(tokensCache, wholeRange))

        return InlineBuilder(ASTNodeBuilder(text), tokensCache).
                buildTree(nodes + listOf(SequentialParser.Node(wholeRange, root)))
    }

    private fun topLevelFallback(root: IElementType, text: String): ASTNode {
        return CompositeASTNode(
            root, listOf(inlineFallback(MarkdownElementTypes.PARAGRAPH, 0, text.length))
        )
    }

    private fun inlineFallback(root: IElementType, textStart: Int, textEnd: Int): ASTNode {
        return CompositeASTNode(
            root,
            listOf(LeafASTNode(MarkdownTokenTypes.TEXT, textStart, textEnd))
        )
    }

    private inner class InlineExpandingASTNodeBuilder(text: CharSequence) : ASTNodeBuilder(text) {
        override fun createLeafNodes(type: IElementType, startOffset: Int, endOffset: Int): List<ASTNode> {
            return when (type) {
                MarkdownElementTypes.PARAGRAPH,
                MarkdownTokenTypes.ATX_CONTENT,
                MarkdownTokenTypes.SETEXT_CONTENT,
                GFMTokenTypes.CELL ->
                    listOf(parseInline(type, text, startOffset, endOffset))
                else ->
                    super.createLeafNodes(type, startOffset, endOffset)
            }
        }
    }

}
