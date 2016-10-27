package org.intellij.markdown.ast.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode

class ListItemCompositeNode(children: List<ASTNode>)
: CompositeASTNode(MarkdownElementTypes.LIST_ITEM, children) {
    var parent: ListCompositeNode? = null
        internal set
}