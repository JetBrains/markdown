package org.intellij.markdown.ast.visitors

import org.intellij.markdown.ast.ASTNode

public trait Visitor {
    fun visitNode(node: ASTNode)
}