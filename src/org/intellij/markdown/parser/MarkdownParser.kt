package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.sequentialparsers.LexerBasedTokensCache
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil

class MarkdownParser(private val flavour: MarkdownFlavourDescriptor) {

    fun buildMarkdownTreeFromString(text: String): ASTNode {
        return parse(MarkdownElementTypes.MARKDOWN_FILE, text, true)
    }

    fun parse(root: IElementType, textString: String, parseInlines: Boolean = true): ASTNode {
        val productionHolder = ProductionHolder()
        val markerProcessor = flavour.markerProcessorFactory.createMarkerProcessor(productionHolder)

        val rootMarker = productionHolder.mark()

        val text = CharArrayCharSequence(textString.toCharArray())
        val textHolder = LookaheadText(text)
        var pos: LookaheadText.Position? = textHolder.startPosition
        while (pos != null) {
            productionHolder.updatePosition(pos.offset)
            pos = markerProcessor.processPosition(pos)
        }

        productionHolder.updatePosition(text.length)
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

    fun parseInline(root: IElementType, text: CharSequence, textStart: Int, textEnd: Int): ASTNode {
        val lexer = flavour.createInlinesLexer()
        lexer.start(text, textStart, textEnd)
        val tokensCache = LexerBasedTokensCache(lexer)

        val wholeRange = 0..tokensCache.filteredTokens.size
        val nodes = flavour.sequentialParserManager.runParsingSequence(tokensCache,
                SequentialParserUtil.filterBlockquotes(tokensCache, wholeRange))

        return MyBuilder(ASTNodeBuilder(text), tokensCache).
                buildTree(nodes + listOf(SequentialParser.Node(wholeRange, root)))
    }

    private inner class InlineExpandingASTNodeBuilder(text: CharSequence) : ASTNodeBuilder(text) {
        override fun createLeafNodes(type: IElementType, startOffset: Int, endOffset: Int): List<ASTNode> {
            return if (type is MarkdownElementType && type.isLazy)
                listOf(parseInline(type, text, startOffset, endOffset))
            else
                super.createLeafNodes(type, startOffset, endOffset)
        }
    }
}
