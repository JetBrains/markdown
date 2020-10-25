package org.intellij.markdown.html

import org.intellij.markdown.ast.ASTNode

interface GeneratingProvider {
    fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode)
}