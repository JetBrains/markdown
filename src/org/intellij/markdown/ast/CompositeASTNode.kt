package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

public class CompositeASTNode(type: IElementType, override val children: List<ASTNode>)
    : ASTNodeImpl(type, children.first?.startOffset ?: 0, children.last?.endOffset ?: 0)
