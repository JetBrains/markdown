package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.sequentialparsers.LexerBasedTokensCache
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import java.util.Collections

public class MarkdownParser(private val markerProcessorFactory: MarkerProcessorFactory) {

    public fun buildMarkdownTreeFromString(text: String): ASTNode {
        return parse(MarkdownElementTypes.MARKDOWN_FILE, text, true)
    }

    public fun parse(root: IElementType, text: String, parseInlines: Boolean): ASTNode {
        val productionHolder = ProductionHolder()
        val markerProcessor = markerProcessorFactory.createMarkerProcessor(productionHolder)

        val rootMarker = productionHolder.mark()

        val textHolder = LookaheadText(text)
        var pos: LookaheadText.Position? = textHolder.startPosition
        while (pos != null) {
            productionHolder.updatePosition(pos.offset)
            pos = markerProcessor.processPosition(pos)
        }

        productionHolder.updatePosition(text.length())
        markerProcessor.flushMarkers()

        rootMarker.done(root)

        val nodeBuilder: ASTNodeBuilder
        nodeBuilder = if (parseInlines) {
            InlineExpandingASTNodeBuilder(text)
        } else {
            ASTNodeBuilder(text)
        }

        val builder = MyRawBuilder(nodeBuilder)

        return builder.buildTree(productionHolder.production)
    }

    companion object {
        public fun parseInline(root: IElementType, text: CharSequence, textStart: Int, textEnd: Int): ASTNode {
            val lexer = MarkdownLexer(text, textStart, textEnd)
            val tokensCache = LexerBasedTokensCache(lexer)

            val wholeRange = 0..tokensCache.filteredTokens.size()
            val nodes = SequentialParserManager().runParsingSequence(tokensCache,
                    Collections.singletonList(wholeRange))

            return MyBuilder(ASTNodeBuilder(text)).buildTree(nodes + listOf(SequentialParser.Node(wholeRange, root)), tokensCache)
        }
    }

    class InlineExpandingASTNodeBuilder(text: CharSequence) : ASTNodeBuilder(text) {
        override fun createLeafNodes(type: IElementType, startOffset: Int, endOffset: Int): List<ASTNode> {
            return when (type) {
                MarkdownElementTypes.PARAGRAPH,
                MarkdownElementTypes.ATX_1,
                MarkdownElementTypes.ATX_2,
                MarkdownElementTypes.ATX_3,
                MarkdownElementTypes.ATX_4,
                MarkdownElementTypes.ATX_5,
                MarkdownElementTypes.ATX_6,
                MarkdownElementTypes.SETEXT_1,
                MarkdownElementTypes.SETEXT_2 ->
                    listOf(parseInline(type, text, startOffset, endOffset))
                else ->
                    super.createLeafNodes(type, startOffset, endOffset)
            }
        }
    }

}
