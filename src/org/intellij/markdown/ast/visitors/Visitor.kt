package org.intellij.markdown.ast.visitors

import org.intellij.markdown.ast.ASTNode

interface Visitor {
    fun visitNode(node: ASTNode)
}