package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.*

class MyBuilder(nodeBuilder: ASTNodeBuilder, private val tokensCache: TokensCache) : TreeBuilder(nodeBuilder) {

    private var currentTokenPosition = -1

    override fun flushEverythingBeforeEvent(event: TreeBuilder.MyEvent, currentNodeChildren: MutableList<TreeBuilder.MyASTNodeWrapper>?) {
        if (currentTokenPosition == -1) {
            currentTokenPosition = event.position
        }

        while (currentTokenPosition < event.position) {
            if (currentNodeChildren != null) {
                flushOneTokenToTree(tokensCache, currentNodeChildren, currentTokenPosition)
            }
            currentTokenPosition++
        }
    }

    private fun flushOneTokenToTree(tokensCache: TokensCache, currentNodeChildren: MutableList<TreeBuilder.MyASTNodeWrapper>, currentTokenPosition: Int) {
        val iterator = tokensCache.Iterator(currentTokenPosition)
        assert(iterator.type != null)
        val nodes = nodeBuilder.createLeafNodes(iterator.type!!, iterator.start, iterator.end)
        for (node in nodes) {
            currentNodeChildren.add(TreeBuilder.MyASTNodeWrapper(node, iterator.index, iterator.index + 1))
        }
    }

    override fun createASTNodeOnClosingEvent(event: TreeBuilder.MyEvent, currentNodeChildren: List<TreeBuilder.MyASTNodeWrapper>, isTopmostNode: Boolean): TreeBuilder.MyASTNodeWrapper {
        val newNode: ASTNode

        val type = event.info.`type`
        val startTokenId = event.info.range.start
        val endTokenId = event.info.range.endInclusive

        val startOffset = tokensCache.Iterator(startTokenId).start
        val endOffset = if (endTokenId != startTokenId) {
            tokensCache.Iterator(endTokenId - 1).end
        } else {
            startOffset
        }

        if (type is MarkdownElementType && type.isToken) {
            val nodes = nodeBuilder.createLeafNodes(type, startOffset, endOffset)
            return TreeBuilder.MyASTNodeWrapper(nodes.first(), startTokenId, endTokenId)
        }

        val childrenWithWhitespaces = ArrayList<ASTNode>(currentNodeChildren.size)

        if (isTopmostNode) {
            addRawTokens(tokensCache, childrenWithWhitespaces, startTokenId, -1, +1)
        }
        for (i in 1..currentNodeChildren.size - 1) {
            val prev = currentNodeChildren.get(i - 1)
            val next = currentNodeChildren.get(i)

            childrenWithWhitespaces.add(prev.astNode)

            addRawTokens(tokensCache, childrenWithWhitespaces, prev.endTokenIndex - 1, +1, tokensCache.Iterator(next.startTokenIndex).start)
        }
        if (!currentNodeChildren.isEmpty()) {
            childrenWithWhitespaces.add(currentNodeChildren.last().astNode)
        }
        if (isTopmostNode) {
            addRawTokens(tokensCache, childrenWithWhitespaces, endTokenId - 1, +1, tokensCache.Iterator(endTokenId).start)
        }

        newNode = nodeBuilder.createCompositeNode(type, startOffset, endOffset, childrenWithWhitespaces)
        return TreeBuilder.MyASTNodeWrapper(newNode, startTokenId, endTokenId)
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
