package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

public class CompositeASTNode(type: IElementType, override val children: List<ASTNode>)
    : ASTNodeImpl(type, children[0].startOffset, children[children.size() - 1].endOffset)
