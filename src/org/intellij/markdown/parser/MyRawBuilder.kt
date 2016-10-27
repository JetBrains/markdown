package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.ASTNodeBuilder
import java.util.*

class MyRawBuilder(nodeBuilder: ASTNodeBuilder) : TreeBuilder(nodeBuilder) {

    override fun flushEverythingBeforeEvent(event: TreeBuilder.MyEvent, currentNodeChildren: MutableList<TreeBuilder.MyASTNodeWrapper>?) {
    }

    override fun createASTNodeOnClosingEvent(event: TreeBuilder.MyEvent, currentNodeChildren: List<TreeBuilder.MyASTNodeWrapper>, isTopmostNode: Boolean): TreeBuilder.MyASTNodeWrapper {
        val newNode: ASTNode

        val type = event.info.`type`
        val startOffset = event.info.range.start
        val endOffset = event.info.range.endInclusive

        if (type is MarkdownElementType && type.isToken) {
            val nodes = nodeBuilder.createLeafNodes(type, startOffset, endOffset)
            return TreeBuilder.MyASTNodeWrapper(nodes.first(), startOffset, endOffset)
        }

        val childrenWithWhitespaces = ArrayList<ASTNode>(currentNodeChildren.size)

//        if (currentNodeChildren.isNotEmpty()) {
            addRawTokens(childrenWithWhitespaces,
                    startOffset,
                    currentNodeChildren.firstOrNull()?.startTokenIndex ?: endOffset)

            for (i in 1..currentNodeChildren.size - 1) {
                val prev = currentNodeChildren.get(i - 1)
                val next = currentNodeChildren.get(i)

                childrenWithWhitespaces.add(prev.astNode)

                addRawTokens(childrenWithWhitespaces, prev.endTokenIndex, next.startTokenIndex)
            }
            if (!currentNodeChildren.isEmpty()) {
                childrenWithWhitespaces.add(currentNodeChildren.last().astNode)
                addRawTokens(childrenWithWhitespaces, currentNodeChildren.last().endTokenIndex, endOffset)
            }
//        }

        newNode = nodeBuilder.createCompositeNode(type, childrenWithWhitespaces)
        return TreeBuilder.MyASTNodeWrapper(newNode, startOffset, endOffset)
    }

    private fun addRawTokens(childrenWithWhitespaces: MutableList<ASTNode>, from: Int, to: Int) {
        // Let's for now assume that it's just whitespace
        if (from != to) {
            childrenWithWhitespaces.addAll(nodeBuilder.createLeafNodes(MarkdownTokenTypes.WHITE_SPACE, from, to))
        }
    }

}
