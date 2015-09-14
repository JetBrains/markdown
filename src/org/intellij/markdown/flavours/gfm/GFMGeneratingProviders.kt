package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.InlineHolderGeneratingProvider
import org.intellij.markdown.html.SimpleTagProvider

internal class CheckedListItemGeneratingProvider : SimpleTagProvider("li") {
    override fun openTag(text: String, node: ASTNode): String {
        return ""
    }

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        assert(node is ListItemCompositeNode)

        val checkBoxElement = node.findChildOfType(GFMTokenTypes.CHECK_BOX)
        val inputHtml: CharSequence
        if (checkBoxElement != null) {
            visitor.consumeHtml("<li class=\"task-list-item\">")

            val checkedString = getIsCheckedString(checkBoxElement, text)
            inputHtml = "<input type=\"checkbox\" class=\"task-list-item-checkbox\"${checkedString} disabled />"
        } else {
            visitor.consumeHtml("<li>")
            inputHtml = ""
        }

        val isLoose = (node as ListItemCompositeNode).parent!!.loose

        var flushedInput = false
        for (child in node.children) {
            if (child is LeafASTNode) {
                continue
            }
            if (!flushedInput) {
                if (child.type == MarkdownElementTypes.PARAGRAPH) {
                    SubParagraphGeneratingProvider(isLoose, inputHtml).
                            processNode(visitor, text, child)
                } else {
                    visitor.consumeHtml(inputHtml)
                    child.accept(visitor)
                }
                flushedInput = true
            } else {
                child.accept(visitor)
            }
        }

        visitor.consumeHtml(closeTag(text, node))
    }

    private fun getIsCheckedString(node: ASTNode?, text: String): String {
        var isChecked = node?.getTextInNode(text)?.let {
            it.length() > 1 && it[1] != ' '
        } == true

        val checkedString = if (isChecked) " checked" else ""
        return checkedString
    }

    private class SubParagraphGeneratingProvider(val wrapInParagraph: Boolean, val inputHtml: String)
    : InlineHolderGeneratingProvider() {
        override fun openTag(text: String, node: ASTNode): String {
            return (if (wrapInParagraph) "<p>" else "") + inputHtml
        }

        override fun closeTag(text: String, node: ASTNode): String {
            return if (wrapInParagraph) "</p>" else ""
        }
    }
}