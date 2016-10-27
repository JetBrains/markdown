package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

open class CompositeASTNode(type: IElementType, override val children: List<ASTNode>)
    : ASTNodeImpl(type, children.firstOrNull()?.startOffset ?: 0, children.lastOrNull()?.endOffset ?: 0)
