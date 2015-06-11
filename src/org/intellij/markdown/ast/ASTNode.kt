package org.intellij.markdown.ast

import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.visitors.Visitor

public interface ASTNode {
    val type : IElementType
    val startOffset : Int
    val endOffset : Int
    val children : List<ASTNode>

    public final fun accept(visitor: Visitor) {
        visitor.visitNode(this)
    }

    public final fun acceptChildren(visitor: Visitor) {
        for (child in children) {
            child.accept(visitor)
        }
    }
}
