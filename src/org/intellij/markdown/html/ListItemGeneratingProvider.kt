package org.intellij.markdown.html

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode

internal class ListItemGeneratingProvider : HtmlGenerator.SimpleTagProvider("li") {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        assert(node is ListItemCompositeNode)

        visitor.consumeHtml(openTag(text, node))
        if (node.children.size() == 3 // Bullet + whitespace + content
                && node.children.last().type == MarkdownElementTypes.PARAGRAPH
                && !(node as ListItemCompositeNode).parent!!.loose) {
            SilentParagraphGeneratingProvider.processNode(visitor, text, node.children.last())
        } else {
            node.acceptChildren(visitor)
        }
        visitor.consumeHtml(closeTag(text, node))
    }

    object SilentParagraphGeneratingProvider : HtmlGenerator.InlineHolderGeneratingProvider() {
        override fun openTag(text: String, node: ASTNode): String {
            return ""
        }

        override fun closeTag(text: String, node: ASTNode): String {
            return ""
        }
    }
}