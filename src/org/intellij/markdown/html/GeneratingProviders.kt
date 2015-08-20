package org.intellij.markdown.html

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.parser.LinkMap

internal class ListItemGeneratingProvider : HtmlGenerator.SimpleTagProvider("li") {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        assert(node is ListItemCompositeNode)

        visitor.consumeHtml(openTag(text, node))
        val isLoose = (node as ListItemCompositeNode).parent!!.loose

        for (child in node.children) {
            if (child.type == MarkdownElementTypes.PARAGRAPH && !isLoose) {
                SilentParagraphGeneratingProvider.processNode(visitor, text, child)
            } else {
                child.accept(visitor)
            }
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

internal class HtmlBlockGeneratingProvider : HtmlGenerator.GeneratingProvider {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        for (child in node.children) {
            if (child.type in listOf(MarkdownTokenTypes.EOL, MarkdownTokenTypes.HTML_BLOCK_CONTENT)) {
                visitor.consumeHtml(child.getTextInNode(text))
            }
        }
        visitor.consumeHtml("\n")
    }
}

internal class CodeFenceGeneratingProvider : HtmlGenerator.GeneratingProvider {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val indentBefore = node.getTextInNode(text).commonPrefixWith(" ".repeat(10)).length()

        visitor.consumeHtml("<pre><code")
        var state = 0

        var childrenToConsider = node.children
        if (childrenToConsider.last().type == MarkdownTokenTypes.CODE_FENCE_END) {
            childrenToConsider = childrenToConsider.subList(0, childrenToConsider.size() - 1)
        }

        var lastChildWasContent = false;

        for (child in childrenToConsider) {
            if (state == 1 && child.type in listOf(MarkdownTokenTypes.CODE_FENCE_CONTENT,
                    MarkdownTokenTypes.EOL)) {
                visitor.consumeHtml(HtmlGenerator.trimIndents(HtmlGenerator.leafText(text, child, false), indentBefore))
                lastChildWasContent = child.type == MarkdownTokenTypes.CODE_FENCE_CONTENT
            }
            if (state == 0 && child.type == MarkdownTokenTypes.FENCE_LANG) {
                visitor.consumeHtml(" class=\"language-${
                HtmlGenerator.leafText(text, child).toString().trim().split(' ')[0]
                }\"");
            }
            if (state == 0 && child.type == MarkdownTokenTypes.EOL) {
                state = 1
                visitor.consumeHtml(">")
            }
        }
        if (state == 0) {
            visitor.consumeHtml(">")
        }
        if (lastChildWasContent) {
            visitor.consumeHtml("\n")
        }
        visitor.consumeHtml("</code></pre>")
    }
}

internal class ReferenceLinksGeneratingProvider(private val linkMap: LinkMap)
: HtmlGenerator.TransparentInlineHolderProvider() {

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val linkLabelNode = node.children.firstOrNull({ it.type == MarkdownElementTypes.LINK_LABEL })
                ?: return fallbackProvider.processNode(visitor, text, node)
        val linkInfo = linkMap.getLinkInfo(linkLabelNode.getTextInNode(text))
                ?: return fallbackProvider.processNode(visitor, text, node)
        val linkTextNode = node.children.firstOrNull({ it.type == MarkdownElementTypes.LINK_TEXT })

        val titleText = linkInfo.title?.let { " title=\"${EntityConverter.replaceEntities(it, true, true)}\"" } ?: ""
        visitor.consumeHtml("<a href=\"${EntityConverter.replaceEntities(linkInfo.destination, true, true)}\"${titleText}>")

        processLabel(visitor, text, linkTextNode ?: linkLabelNode)

        visitor.consumeHtml("</a>")
    }

    companion object {
        val fallbackProvider = HtmlGenerator.TransparentInlineHolderProvider()

        val labelProvider = HtmlGenerator.TransparentInlineHolderProvider(1, -1)

        public fun processLabel(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
            labelProvider.processNode(visitor, text, node)
        }
    }

}