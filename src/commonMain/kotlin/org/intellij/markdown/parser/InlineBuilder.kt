package org.intellij.markdown.parser

import org.intellij.markdown.ExperimentalApi
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import org.intellij.markdown.lexer.Compat.assert
import org.intellij.markdown.parser.sequentialparsers.TokensCache

@OptIn(ExperimentalApi::class)
class InlineBuilder @ExperimentalApi constructor(
    nodeBuilder: ASTNodeBuilder,
    private val tokensCache: TokensCache,
    cancellationToken: CancellationToken
): TreeBuilder(nodeBuilder, cancellationToken) {
    @OptIn(ExperimentalApi::class)
    constructor(nodeBuilder: ASTNodeBuilder, tokensCache: TokensCache): this(nodeBuilder, tokensCache, CancellationToken.NonCancellable)

    private var currentTokenPosition = -1

    override fun flushEverythingBeforeEvent(event: MyEvent, currentNodeChildren: MutableList<MyASTNodeWrapper>?) {
        if (currentTokenPosition == -1) {
            currentTokenPosition = event.position
        }

        while (currentTokenPosition < event.position) {
            flushOneTokenToTree(tokensCache, currentNodeChildren, currentTokenPosition)
            currentTokenPosition++
        }
    }

    private fun flushOneTokenToTree(tokensCache: TokensCache, currentNodeChildren: MutableList<MyASTNodeWrapper>?, currentTokenPosition: Int) {
        val iterator = tokensCache.Iterator(currentTokenPosition)
        assert(iterator.type != null)
        val nodes = nodeBuilder.createLeafNodes(iterator.type!!, iterator.start, iterator.end)
        for (node in nodes) {
            currentNodeChildren?.add(MyASTNodeWrapper(node, iterator.index, iterator.index + 1))
        }
    }

    override fun createASTNodeOnClosingEvent(event: MyEvent, currentNodeChildren: List<MyASTNodeWrapper>, isTopmostNode: Boolean): MyASTNodeWrapper {
        val newNode: ASTNode

        val type = event.info.type
        val startTokenId = event.info.range.first
        val endTokenId = event.info.range.last

        val childrenWithWhitespaces = ArrayList<ASTNode>(currentNodeChildren.size)

        if (isTopmostNode) {
            // Set exitOffset to an unreachable offset pointing to the left.
            // This way we ensure that all raw tokens before are included into the current node.
            addRawTokens(tokensCache, childrenWithWhitespaces, startTokenId, -1, -1)
        }
        for (index in 1 until currentNodeChildren.size) {
            val prev = currentNodeChildren[index - 1]
            val next = currentNodeChildren[index]

            childrenWithWhitespaces.add(prev.astNode)

            addRawTokens(tokensCache, childrenWithWhitespaces, prev.endTokenIndex - 1, +1, tokensCache.Iterator(next.startTokenIndex).start)
        }
        if (currentNodeChildren.isNotEmpty()) {
            childrenWithWhitespaces.add(currentNodeChildren.last().astNode)
        }
        if (isTopmostNode) {
            addRawTokens(tokensCache, childrenWithWhitespaces, endTokenId - 1, +1, tokensCache.Iterator(endTokenId).start)
        }

        newNode = nodeBuilder.createCompositeNode(type, childrenWithWhitespaces)
        return MyASTNodeWrapper(newNode, startTokenId, endTokenId)
    }

    private fun addRawTokens(tokensCache: TokensCache, childrenWithWhitespaces: MutableList<ASTNode>, from: Int, dx: Int, exitOffset: Int) {
        val iterator = tokensCache.Iterator(from)
        var rawIdx = 0
        while (iterator.rawLookup(rawIdx + dx) != null && iterator.rawStart(rawIdx + dx) != exitOffset) {
            rawIdx += dx
        }
        while (rawIdx != 0) {
            val rawType = iterator.rawLookup(rawIdx)!!
            childrenWithWhitespaces.addAll(nodeBuilder.createLeafNodes(rawType, iterator.rawStart(rawIdx), iterator.rawStart(rawIdx + 1)))
            rawIdx -= dx
        }
    }
}
