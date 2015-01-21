package org.intellij.markdown.ast.visitors

import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode

public open class RecursiveVisitor : Visitor {
    override fun visitNode(node: ASTNode) {
        if (node is CompositeASTNode) {
            for (child in node.children) {
                visitNode(child)
            }
        }
    }
}

