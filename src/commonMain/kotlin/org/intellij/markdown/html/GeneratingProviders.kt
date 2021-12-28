package org.intellij.markdown.html

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.MarkdownTokenTypes.Companion.ATX_CONTENT
import org.intellij.markdown.MarkdownTokenTypes.Companion.SETEXT_CONTENT
import org.intellij.markdown.ast.*
import org.intellij.markdown.ast.impl.ListCompositeNode
import org.intellij.markdown.ast.impl.ListItemCompositeNode
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.lexer.Compat.assert
import org.intellij.markdown.parser.LinkMap

abstract class OpenCloseGeneratingProvider : GeneratingProvider {
    abstract fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode)
    abstract fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode)

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        openTag(visitor, text, node)
        node.acceptChildren(visitor)
        closeTag(visitor, text, node)
    }
}

abstract class InlineHolderGeneratingProvider : OpenCloseGeneratingProvider() {
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

open class SimpleTagProvider(val tagName: String) : OpenCloseGeneratingProvider() {
    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeTagOpen(node, tagName)
    }

    override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeTagClose(tagName)
    }

}

open class SimpleTagWithLinkProvider(val tagName: String) : OpenCloseGeneratingProvider() {

    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeTagOpen(
                node,
                tagName,
                getPureIdString(node,text)
        )
    }

    override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        visitor.consumeTagClose(tagName)
    }

    private fun getPureIdString(
            node: ASTNode,
            text: String
    ) : String {
        val stringBuilder = StringBuilder()
        getPureIdStringDfs(
                node,
                text,
                stringBuilder
        )
        val idValue : CharSequence = LinkMap.normalizeDestination(stringBuilder.toString().trim(), true)
        return "id = \"$idValue\""
    }

    private fun getPureIdStringDfs(
            node: ASTNode,
            text: String,
            stringBuilder: StringBuilder
    ) {
        if (node.type in listOf(SETEXT_CONTENT, ATX_CONTENT)) {
            stringBuilder.append(text.substring(node.startOffset, node.endOffset))
        } else {
            for(children in node.children){
                getPureIdStringDfs(
                        children,
                        text,
                        stringBuilder
                )
            }
        }
    }

}

open class SimpleInlineTagProvider(val tagName: String, val renderFrom: Int = 0, val renderTo: Int = 0)
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

open class TransparentInlineHolderProvider(renderFrom: Int = 0, renderTo: Int = 0)
: SimpleInlineTagProvider("", renderFrom, renderTo) {
    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
    }

    override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
    }
}

open class TrimmingInlineHolderProvider : InlineHolderGeneratingProvider() {
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

        val listNode = node.parent
        assert(listNode is ListCompositeNode)
        val isLoose = (listNode as ListCompositeNode).loose

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

        var lastChildWasContent = false

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
                }\"")
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

abstract class LinkGeneratingProvider(val baseURI: URI?, val resolveAnchors: Boolean = false) : GeneratingProvider {
    final override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val info = getRenderInfo(text, node)
                ?: return fallbackProvider.processNode(visitor, text, node)
        renderLink(visitor, text, node, info)
    }

    protected fun makeAbsoluteUrl(destination : CharSequence) : CharSequence {
        if (!resolveAnchors && destination.startsWith('#')) {
            return destination
        }

        return baseURI?.resolveToStringSafe(destination.toString()) ?: destination
    }

    open fun renderLink(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode, info: RenderInfo) {
        visitor.consumeTagOpen(node, "a", "href=\"${makeAbsoluteUrl(info.destination)}\"", info.title?.let { "title=\"$it\"" })
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

open class InlineLinkGeneratingProvider(baseURI: URI?, resolveAnchors: Boolean = false)
    : LinkGeneratingProvider(baseURI, resolveAnchors) {
    override fun getRenderInfo(text: String, node: ASTNode): RenderInfo? {
        val label = node.findChildOfType(MarkdownElementTypes.LINK_TEXT)
                ?: return null
        return RenderInfo(
                label,
                node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(text)?.let {
                    LinkMap.normalizeDestination(it, true)
                } ?: "",
                node.findChildOfType(MarkdownElementTypes.LINK_TITLE)?.getTextInNode(text)?.let {
                    LinkMap.normalizeTitle(it)
                }
        )
    }
}

open class ReferenceLinksGeneratingProvider(private val linkMap: LinkMap, baseURI: URI?, resolveAnchors: Boolean = false)
    : LinkGeneratingProvider(baseURI, resolveAnchors) {
    override fun getRenderInfo(text: String, node: ASTNode): RenderInfo? {
        val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_LABEL }
            ?: return null
        val linkInfo = linkMap.getLinkInfo(label.getTextInNode(text))
                ?: return null
        val linkTextNode = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }

        return RenderInfo(
            linkTextNode ?: label,
            EntityConverter.replaceEntities(linkInfo.destination, processEntities = true, processEscapes = true),
            linkInfo.title?.let { EntityConverter.replaceEntities(it, processEntities = true, processEscapes = true) }
        )
    }
}

open class ImageGeneratingProvider(linkMap: LinkMap, baseURI: URI?) : LinkGeneratingProvider(baseURI) {
    protected val referenceLinkProvider = ReferenceLinksGeneratingProvider(linkMap, baseURI)
    protected val inlineLinkProvider = InlineLinkGeneratingProvider(baseURI)

    override fun getRenderInfo(text: String, node: ASTNode): RenderInfo? {
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

    override fun renderLink(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode, info: RenderInfo) {
        visitor.consumeTagOpen(node, "img",
                "src=\"${makeAbsoluteUrl(info.destination)}\"",
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

