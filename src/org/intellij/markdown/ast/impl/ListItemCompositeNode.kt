package org.intellij.markdown.ast.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode

public class ListItemCompositeNode(children: List<ASTNode>)
: CompositeASTNode(MarkdownElementTypes.LIST_ITEM, children) {
    public var parent: ListCompositeNode? = null
        internal set
}