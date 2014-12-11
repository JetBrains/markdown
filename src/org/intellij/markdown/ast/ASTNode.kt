package org.intellij.markdown.ast

import org.intellij.markdown.IElementType

public trait ASTNode {
    val type : IElementType
    val startOffset : Int
    val endOffset : Int
    val children : List<ASTNode>
}
