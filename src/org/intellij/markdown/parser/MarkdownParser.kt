package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

public class MarkdownParser(private val markerProcessorFactory: MarkerProcessorFactory) {

    public fun buildMarkdownTreeFromString(text: String): ASTNode {
        val cache = TokensCache(MarkdownLexer(text))
        return parse(MarkdownElementTypes.MARKDOWN_FILE, cache)
    }

    public fun parse(root: IElementType, tokensCache: TokensCache): ASTNode {
        val productionHolder = ProductionHolder()
        val markerProcessor = markerProcessorFactory.createMarkerProcessor(productionHolder, tokensCache)

        val rootMarker = productionHolder.mark()

        var iterator = tokensCache.Iterator(0)
        productionHolder.updatePosition(iterator.index)
        while (iterator.type != null) {
            val tokenType = iterator.type!!
            iterator = markerProcessor.processToken(tokenType, iterator)
            iterator = iterator.advance()
            productionHolder.updatePosition(iterator.index)
        }
        markerProcessor.flushMarkers()

        rootMarker.done(root)

        val builder = MyBuilder()

        return builder.buildTree(productionHolder.production, tokensCache)
    }

}
