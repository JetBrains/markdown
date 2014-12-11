package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.sequentialparsers.SequentialParser

public class MarkdownParser(private val markerProcessor: MarkerProcessor) {

    public fun buildMarkdownTreeFromString(text: String): ASTNode {
        val cache = TokensCache(MarkdownLexer(text))
        return parse(MarkdownElementTypes.MARKDOWN_FILE, cache)
    }

    public fun parse(root: IElementType, tokensCache: TokensCache): ASTNode {
        markerProcessor.tokensCache = tokensCache

        val startOffset = 0

        var iterator = tokensCache.Iterator(startOffset)
        while (iterator.type != null) {
            val tokenType = iterator.type!!
            iterator = markerProcessor.processToken(tokenType, iterator)
            iterator = iterator.advance()
        }
        markerProcessor.flushMarkers(iterator)


        val builder = MyBuilder()

        builder.addNode(SequentialParser.Node(0..iterator.index, root))
        builder.addNodes(markerProcessor.getProduction())

        return builder.buildTree(tokensCache)
    }

}
