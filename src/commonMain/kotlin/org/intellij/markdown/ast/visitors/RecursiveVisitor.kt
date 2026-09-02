package org.intellij.markdown.ast.visitors

import org.intellij.markdown.ExperimentalApi
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.LazyASTNode

@OptIn(ExperimentalApi::class)
open class RecursiveVisitor : Visitor {
    override fun visitNode(node: ASTNode) {
        if (node is CompositeASTNode || node is LazyASTNode) {
            for (child in node.children) {
                visitNode(child)
            }
        }
    }
}
