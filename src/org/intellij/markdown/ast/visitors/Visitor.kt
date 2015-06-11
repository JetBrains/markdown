package org.intellij.markdown.ast.visitors

import org.intellij.markdown.ast.ASTNode

public interface Visitor {
    fun visitNode(node: ASTNode)
}