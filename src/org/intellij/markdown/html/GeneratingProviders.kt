package org.intellij.markdown.html

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.*
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.parser.LinkMap
import java.io.File
import java.util.*
import kotlin.text.Regex

public abstract class OpenCloseGeneratingProvider : GeneratingProvider {
    abstract fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode);
    abstract fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode);

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        openTag(visitor, text, node)
        node.acceptChildren(visitor)
        closeTag(visitor, text, node)
    }
}

public abstract class InlineHolderGeneratingProvider : OpenCloseGeneratingProvider() {
    open fun childrenToRender(node: ASTNode): List<ASTNode> {
        return node.children
    }

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        openTag(visitor, text, node)

        for (child in childrenToRender(node)) {
            if (child is LeafASTNode) {
                visitor.visitLeaf(child)
            } else {
                child.accept(visitor)
            }
        }

        closeTag(visitor, text, node)
    }
}

public open class SimpleTagProvider(val tagName: String) : OpenCloseGeneratingProvider() {
    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeTagOpen(node, tagName)
    }

    override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeTagClose(tagName)
    }

}

public open class SimpleInlineTagProvider(val tagName: String, val renderFrom: Int = 0, val renderTo: Int = 0)
: InlineHolderGeneratingProvider() {
    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeTagOpen(node, tagName)
    }

    override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeTagClose(tagName)
    }

    override fun childrenToRender(node: ASTNode): List<ASTNode> {
        return node.children.subList(renderFrom, node.children.size + renderTo)
    }


}

public open class TransparentInlineHolderProvider(renderFrom: Int = 0, renderTo: Int = 0)
: SimpleInlineTagProvider("", renderFrom, renderTo) {
    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
    }

    override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
    }
}

public open class TrimmingInlineHolderProvider() : InlineHolderGeneratingProvider() {
    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
    }

    override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
    }

    override fun childrenToRender(node: ASTNode): List<ASTNode> {
        val children = node.children
        var from = 0
        while (from < children.size && children[from].type == MarkdownTokenTypes.WHITE_SPACE) {
            from++
        }
        var to = children.size
        while (to > from && children[to - 1].type == MarkdownTokenTypes.WHITE_SPACE) {
            to--
        }

        return children.subList(from, to)
    }
}

internal class ListItemGeneratingProvider : SimpleTagProvider("li") {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        assert(node is ListItemCompositeNode)

        openTag(visitor, text, node)
        val isLoose = (node as ListItemCompositeNode).parent!!.loose

        for (child in node.children) {
            if (child.type == MarkdownElementTypes.PARAGRAPH && !isLoose) {
                SilentParagraphGeneratingProvider.processNode(visitor, text, child)
            } else {
                child.accept(visitor)
            }
        }

        closeTag(visitor, text, node)
    }

    object SilentParagraphGeneratingProvider : InlineHolderGeneratingProvider() {
        override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        }

        override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
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
        val indentBefore = node.getTextInNode(text).commonPrefixWith(" ".repeat(10)).length

        visitor.consumeHtml("<pre>")

        var state = 0

        var childrenToConsider = node.children
        if (childrenToConsider.last().type == MarkdownTokenTypes.CODE_FENCE_END) {
            childrenToConsider = childrenToConsider.subList(0, childrenToConsider.size - 1)
        }

        var lastChildWasContent = false;

        val attributes = ArrayList<String>()
        for (child in childrenToConsider) {
            if (state == 1 && child.type in listOf(MarkdownTokenTypes.CODE_FENCE_CONTENT,
                    MarkdownTokenTypes.EOL)) {
                visitor.consumeHtml(HtmlGenerator.trimIndents(HtmlGenerator.leafText(text, child, false), indentBefore))
                lastChildWasContent = child.type == MarkdownTokenTypes.CODE_FENCE_CONTENT
            }
            if (state == 0 && child.type == MarkdownTokenTypes.FENCE_LANG) {
                attributes.add("class=\"language-${
                HtmlGenerator.leafText(text, child).toString().trim().split(' ')[0]
                }\"");
            }
            if (state == 0 && child.type == MarkdownTokenTypes.EOL) {
                visitor.consumeTagOpen(node, "code", *attributes.toTypedArray())
                state = 1
            }
        }
        if (state == 0) {
            visitor.consumeTagOpen(node, "code", *attributes.toTypedArray())
        }
        if (lastChildWasContent) {
            visitor.consumeHtml("\n")
        }
        visitor.consumeHtml("</code></pre>")
    }
}

internal abstract class LinkGeneratingProvider(val documentBase: File?=null) : GeneratingProvider {
    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val info = getRenderInfo(text, node)
                ?: return fallbackProvider.processNode(visitor, text, node)
        renderLink(visitor, text, node, info)
    }

    open fun renderLink(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode, info: RenderInfo) {
        visitor.consumeTagOpen(node, "a", "href=\"${info.destination}\"", info.title?.let { "title=\"$it\"" })
        labelProvider.processNode(visitor, text, info.label)
        visitor.consumeTagClose("a")
    }

    abstract fun getRenderInfo(text: String, node: ASTNode): RenderInfo?

    data class RenderInfo(val label: ASTNode, val destination: CharSequence, val title: CharSequence?)

    companion object {
        val fallbackProvider = TransparentInlineHolderProvider()

        val labelProvider = TransparentInlineHolderProvider(1, -1)
    }
}

internal class InlineLinkGeneratingProvider(documentBase: File?=null) : LinkGeneratingProvider(documentBase) {
    override fun getRenderInfo(text: String, node: ASTNode): LinkGeneratingProvider.RenderInfo? {
        val label = node.findChildOfType(MarkdownElementTypes.LINK_TEXT) ?: return null

        val destination = node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(text)?.let {
            val normLinkDest = LinkMap.normalizeDestination(it)

            // normalize relative image links to become absolute file URLs
            if (File(normLinkDest.toString()).isAbsolute || normLinkDest.startsWith("http")) {
                normLinkDest
            } else {
                val relativePath = File(normLinkDest.toString()).toPath()
                if(documentBase!=null)
                    documentBase.toPath().resolve(relativePath).toUri().toURL().toString()
                else
                    normLinkDest
            }
        } ?: ""

        val title = node.findChildOfType(MarkdownElementTypes.LINK_TITLE)?.getTextInNode(text)?.let {
            LinkMap.normalizeTitle(it)
        }

        return LinkGeneratingProvider.RenderInfo(label, destination, title)
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

internal class ImageGeneratingProvider(linkMap: LinkMap, documentBase: File?) : LinkGeneratingProvider(documentBase) {
    val referenceLinkProvider = ReferenceLinksGeneratingProvider(linkMap)
    val inlineLinkProvider = InlineLinkGeneratingProvider(documentBase)

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

    override fun renderLink(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode, info: LinkGeneratingProvider.RenderInfo) {
        visitor.consumeTagOpen(node, "img",
                "src=\"${info.destination}\"",
                "alt=\"${getPlainTextFrom(info.label, text)}\"",
                info.title?.let { "title=\"$it\"" },
                autoClose = true)
    }

    private fun getPlainTextFrom(node: ASTNode, text: String): CharSequence {
        return REGEX.replace(node.getTextInNode(text), "")
    }

    companion object {
        val REGEX = Regex("[^a-zA-Z0-9 ]")
    }
}

