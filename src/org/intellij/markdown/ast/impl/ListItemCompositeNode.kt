package org.intellij.markdown.ast.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode

class ListItemCompositeNode(startOffset: Int, endOffset: Int, children: List<ASTNode>)
    : CompositeASTNode(MarkdownElementTypes.LIST_ITEM, startOffset, endOffset, children)