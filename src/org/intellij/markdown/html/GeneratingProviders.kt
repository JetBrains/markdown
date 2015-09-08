package org.intellij.markdown.html

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.parser.LinkMap
import kotlin.text.Regex

public abstract class OpenCloseGeneratingProvider : GeneratingProvider {
    abstract fun openTag(text: String, node: ASTNode): String;
    abstract fun closeTag(text: String, node: ASTNode): String;

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeHtml(openTag(text, node))
        node.acceptChildren(visitor)
        visitor.consumeHtml(closeTag(text, node))
    }
}

public abstract class NonRecursiveGeneratingProvider : GeneratingProvider {
    abstract fun generateTag(text: String, node: ASTNode): CharSequence;

    final override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeHtml(generateTag(text, node))
    }
}

public abstract class InlineHolderGeneratingProvider : OpenCloseGeneratingProvider() {
    open fun childrenToRender(node: ASTNode): List<ASTNode> {
        return node.children
    }

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeHtml(openTag(text, node))

        for (child in childrenToRender(node)) {
            if (child is LeafASTNode) {
                visitor.visitLeaf(child)
            } else {
                child.accept(visitor)
            }
        }

        visitor.consumeHtml(closeTag(text, node))
    }
}

public open class SimpleTagProvider(val tagName: String) : OpenCloseGeneratingProvider() {
    override fun openTag(text: String, node: ASTNode): String {
        return "<$tagName>"
    }

    override fun closeTag(text: String, node: ASTNode): String {
        return "</$tagName>"
    }
}

public open class SimpleInlineTagProvider(val tagName: String, val renderFrom: Int = 0, val renderTo: Int = 0)
: InlineHolderGeneratingProvider() {
    override fun childrenToRender(node: ASTNode): List<ASTNode> {
        return node.children.subList(renderFrom, node.children.size() + renderTo)
    }

    override fun openTag(text: String, node: ASTNode): String {
        return "<$tagName>"
    }

    override fun closeTag(text: String, node: ASTNode): String {
        return "</$tagName>"
    }
}

public open class TransparentInlineHolderProvider(renderFrom: Int = 0, renderTo: Int = 0)
: SimpleInlineTagProvider("", renderFrom, renderTo) {
    override fun openTag(text: String, node: ASTNode): String {
        return ""
    }

    override fun closeTag(text: String, node: ASTNode): String {
        return ""
    }
}

public open class TrimmingInlineHolderProvider() : InlineHolderGeneratingProvider() {
    override fun openTag(text: String, node: ASTNode): String {
        return ""
    }

    override fun closeTag(text: String, node: ASTNode): String {
        return ""
    }

    override fun childrenToRender(node: ASTNode): List<ASTNode> {
        val children = node.children
        var from = 0
        while (from < children.size() && children[from].type == MarkdownTokenTypes.WHITE_SPACE) {
            from++
        }
        var to = children.size()
        while (to > from && children[to - 1].type == MarkdownTokenTypes.WHITE_SPACE) {
            to--
        }

        return children.subList(from, to)
    }
}

internal class ListItemGeneratingProvider : SimpleTagProvider("li") {
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

    object SilentParagraphGeneratingProvider : InlineHolderGeneratingProvider() {
        override fun openTag(text: String, node: ASTNode): String {
            return ""
        }

        override fun closeTag(text: String, node: ASTNode): String {
            return ""
        }
    }
}

internal class HtmlBlockGeneratingProvider : GeneratingProvider {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        for (child in node.children) {
            if (child.type in listOf(MarkdownTokenTypes.EOL, MarkdownTokenTypes.HTML_BLOCK_CONTENT)) {
                visitor.consumeHtml(child.getTextInNode(text))
            }
        }
        visitor.consumeHtml("\n")
    }
}

internal class CodeFenceGeneratingProvider : GeneratingProvider {
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

internal abstract class LinkGeneratingProvider : GeneratingProvider {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val info = getRenderInfo(text, node)
                ?: return fallbackProvider.processNode(visitor, text, node)
        renderLink(visitor, text, info)
    }

    open fun renderLink(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, info: RenderInfo) {
        visitor.consumeHtml("<a href=\"${info.destination}\"")
        info.title?.let { visitor.consumeHtml(" title=\"$it\"") }
        visitor.consumeHtml(">")
        labelProvider.processNode(visitor, text, info.label)
        visitor.consumeHtml("</a>")
    }

    abstract fun getRenderInfo(text: String, node: ASTNode): RenderInfo?

    data class RenderInfo(val label: ASTNode, val destination: CharSequence, val title: CharSequence?)

    companion object {
        val fallbackProvider = TransparentInlineHolderProvider()

        val labelProvider = TransparentInlineHolderProvider(1, -1)
    }
}

internal class InlineLinkGeneratingProvider : LinkGeneratingProvider() {
    override fun getRenderInfo(text: String, node: ASTNode): LinkGeneratingProvider.RenderInfo? {
        val label = node.findChildOfType(MarkdownElementTypes.LINK_TEXT)
                ?: return null
        return LinkGeneratingProvider.RenderInfo(
                label,
                node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(text)?.let {
                    LinkMap.normalizeDestination(it)
                } ?: "",
                node.findChildOfType(MarkdownElementTypes.LINK_TITLE)?.getTextInNode(text)?.let {
                    LinkMap.normalizeTitle(it)
                }
        )
    }
}

internal class ReferenceLinksGeneratingProvider(private val linkMap: LinkMap)
: LinkGeneratingProvider() {
    override fun getRenderInfo(text: String, node: ASTNode): LinkGeneratingProvider.RenderInfo? {
        val label = node.children.firstOrNull({ it.type == MarkdownElementTypes.LINK_LABEL })
                ?: return null
        val linkInfo = linkMap.getLinkInfo(label.getTextInNode(text))
                ?: return null
        val linkTextNode = node.children.firstOrNull({ it.type == MarkdownElementTypes.LINK_TEXT })

        return LinkGeneratingProvider.RenderInfo(
                linkTextNode ?: label,
                EntityConverter.replaceEntities(linkInfo.destination, true, true),
                linkInfo.title?.let { EntityConverter.replaceEntities(it, true, true) }
        )
    }
}

internal class ImageGeneratingProvider(linkMap: LinkMap) : LinkGeneratingProvider() {
    val referenceLinkProvider = ReferenceLinksGeneratingProvider(linkMap)
    val inlineLinkProvider = InlineLinkGeneratingProvider()

    override fun getRenderInfo(text: String, node: ASTNode): LinkGeneratingProvider.RenderInfo? {
        node.findChildOfType(MarkdownElementTypes.INLINE_LINK)?.let { linkNode ->
            return inlineLinkProvider.getRenderInfo(text, linkNode)
        }
        (node.findChildOfType(MarkdownElementTypes.FULL_REFERENCE_LINK)
                ?: node.findChildOfType(MarkdownElementTypes.SHORT_REFERENCE_LINK))
                ?.let { linkNode ->
            return referenceLinkProvider.getRenderInfo(text, linkNode)
        }
        return null
    }

    override fun renderLink(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, info: LinkGeneratingProvider.RenderInfo) {
        visitor.consumeHtml("<img src=\"${info.destination}\" alt=\"${getPlainTextFrom(info.label, text)}\"")
        info.title?.let { visitor.consumeHtml(" title=\"$it\"") }
        visitor.consumeHtml(" />")
    }

    private fun getPlainTextFrom(node: ASTNode, text: String): CharSequence {
        return REGEX.replace(node.getTextInNode(text), "")
    }

    companion object {
        val REGEX = Regex("[^a-zA-Z0-9 ]")
    }
}

