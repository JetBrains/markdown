package org.intellij.markdown.ast

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.impl.ListCompositeNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode

public object ASTNodeBuilder {
    public fun createLeafNode(type: IElementType, startOffset: Int, endOffset: Int): LeafASTNode {
        return LeafASTNode(type, startOffset, endOffset)
    }

    public fun createCompositeNode(type: IElementType, children: List<ASTNode>): CompositeASTNode {
        when (type) {
            MarkdownElementTypes.UNORDERED_LIST,
            MarkdownElementTypes.ORDERED_LIST -> {
                return ListCompositeNode(type, children)
            }
            MarkdownElementTypes.LIST_ITEM -> {
                return ListItemCompositeNode(children)
            }
            else -> {
                return CompositeASTNode(type, children)
            }
        }
    }
}