package org.intellij.markdown.html

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.parser.LinkMap
import kotlin.text.MatchResult
import kotlin.text.Regex


public class HtmlGenerator(private val markdownText: String, private val root: ASTNode, private val linkMap: LinkMap) {
    private val htmlString: StringBuilder = StringBuilder()
    private val providers: Map<IElementType, GeneratingProvider> = initProviders()

    public fun generateHtml(): String {
        HtmlGeneratingVisitor().visitNode(root)
        return htmlString.toString()
    }

    inner class HtmlGeneratingVisitor : RecursiveVisitor() {
        override fun visitNode(node: ASTNode) {
            @suppress("USELESS_ELVIS")
            (providers.get(node.type)?.processNode(this, markdownText, node)
                    ?: node.acceptChildren(this))
        }

        public fun visitLeaf(node: ASTNode) {
            @suppress("USELESS_ELVIS")
            (providers.get(node.type)?.processNode(this, markdownText, node)
                    ?: consumeHtml(leafText(markdownText, node)))
        }

        public final fun consumeHtml(html: CharSequence) {
            htmlString.append(html)
        }
    }

    interface GeneratingProvider {
        fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode)
    }

    abstract class OpenCloseGeneratingProvider : GeneratingProvider {
        abstract fun openTag(text: String, node: ASTNode): String;
        abstract fun closeTag(text: String, node: ASTNode): String;

        override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
            visitor.consumeHtml(openTag(text, node))
            node.acceptChildren(visitor)
            visitor.consumeHtml(closeTag(text, node))
        }
    }

    abstract class NonRecursiveGeneratingProvider : GeneratingProvider {
        abstract fun generateTag(text: String, node: ASTNode): CharSequence;

        final override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
            visitor.consumeHtml(generateTag(text, node))
        }
    }

    abstract class InlineHolderGeneratingProvider : OpenCloseGeneratingProvider() {
        open fun childrenToRender(node: ASTNode): List<ASTNode> {
            return node.children
        }

        override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
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

    open class SimpleTagProvider(val tagName: String) : OpenCloseGeneratingProvider() {
        override fun openTag(text: String, node: ASTNode): String {
            return "<$tagName>"
        }

        override fun closeTag(text: String, node: ASTNode): String {
            return "</$tagName>"
        }
    }

    open class SimpleInlineTagProvider(val tagName: String, val renderFrom: Int = 0, val renderTo: Int = 0)
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

    open class TransparentInlineHolderProvider(renderFrom: Int = 0, renderTo: Int = 0)
    : SimpleInlineTagProvider("", renderFrom, renderTo) {
        override fun openTag(text: String, node: ASTNode): String {
            return ""
        }

        override fun closeTag(text: String, node: ASTNode): String {
            return ""
        }
    }

    open class TrimmingTransparentInlineHolderProvider() : TransparentInlineHolderProvider() {
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

    fun initProviders(): Map<IElementType, GeneratingProvider> {
        return hashMapOf(

                MarkdownElementTypes.MARKDOWN_FILE to SimpleTagProvider("body"),
                MarkdownElementTypes.HTML_BLOCK to HtmlBlockGeneratingProvider(),
                MarkdownTokenTypes.HTML_TAG to object : GeneratingProvider {
                    override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                        visitor.consumeHtml(node.getTextInNode(text));
                    }
                },

                MarkdownElementTypes.BLOCK_QUOTE to SimpleTagProvider("blockquote"),

                MarkdownElementTypes.ORDERED_LIST to object : SimpleTagProvider("ol") {
                    override fun openTag(text: String, node: ASTNode): String {
                        node.findChildOfType(MarkdownElementTypes.LIST_ITEM)
                                ?.findChildOfType(MarkdownTokenTypes.LIST_NUMBER)
                                ?.getTextInNode(text)?.toString()?.trim()?.let {
                            val number = it.substring(0, it.length() - 1).trimStart('0')
                            if (number.equals("1")) {
                                return "<ol>"
                            }

                            return "<ol start=\"${ if (number.isEmpty()) "0" else number }\">"
                        }
                        return "<ol>"
                    }
                },
                MarkdownElementTypes.UNORDERED_LIST to SimpleTagProvider("ul"),
                MarkdownElementTypes.LIST_ITEM to ListItemGeneratingProvider(),

                MarkdownTokenTypes.SETEXT_CONTENT to TrimmingTransparentInlineHolderProvider(),
                MarkdownElementTypes.SETEXT_1 to SimpleTagProvider("h1"),
                MarkdownElementTypes.SETEXT_2 to SimpleTagProvider("h2"),

                MarkdownTokenTypes.ATX_CONTENT to TrimmingTransparentInlineHolderProvider(),
                MarkdownElementTypes.ATX_1 to SimpleTagProvider("h1"),
                MarkdownElementTypes.ATX_2 to SimpleTagProvider("h2"),
                MarkdownElementTypes.ATX_3 to SimpleTagProvider("h3"),
                MarkdownElementTypes.ATX_4 to SimpleTagProvider("h4"),
                MarkdownElementTypes.ATX_5 to SimpleTagProvider("h5"),
                MarkdownElementTypes.ATX_6 to SimpleTagProvider("h6"),

                MarkdownElementTypes.AUTOLINK to object : NonRecursiveGeneratingProvider() {
                    override fun generateTag(text: String, node: ASTNode): String {
                        val linkText = node.getTextInNode(text)
                        val link = EntityConverter.replaceEntities(linkText.subSequence(1, linkText.length() - 1), true, false)
                        return "<a href=\"${LinkMap.normalizeDestination(linkText)}\">$link</a>"
                    }
                },


                MarkdownElementTypes.LINK_LABEL to TransparentInlineHolderProvider(),
                MarkdownElementTypes.LINK_TEXT to TransparentInlineHolderProvider(),
                MarkdownElementTypes.LINK_TITLE to TransparentInlineHolderProvider(),
                //                MarkdownElementTypes.LINK_DESTINATION to object : TransparentInlineHolderProvider(1, -1) {
                //                    override fun childrenToRender(node: ASTNode): List<ASTNode> {
                //                        if (node.children.first().type == MarkdownTokenTypes.LT) {
                //                            return super.childrenToRender(node)
                //                        } else {
                //                            return node.children
                //                        }
                //                    }
                //                },
                //                //                    MarkdownElementTypes.LINK_TEXT to TransparentInlineHolderProvider(1, -1),

                MarkdownElementTypes.INLINE_LINK to object : GeneratingProvider {
                    override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                        val destinationNode = node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
                        val titleNode = node.findChildOfType(MarkdownElementTypes.LINK_TITLE)

                        val destinationText = destinationNode?.getTextInNode(text)?.let {
                            LinkMap.normalizeDestination(it)
                        } ?: ""
                        val titleText = if (titleNode != null)
                            " title=\"${titleNode.getTextInNode(text).let { LinkMap.normalizeTitle(it) }}\""
                        else
                            ""

                        visitor.consumeHtml("<a href=\"${destinationText}\"${titleText}>")

                        node.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.let { label ->
                            ReferenceLinksGeneratingProvider.processLabel(visitor, text, label)
                        }
                        visitor.consumeHtml("</a>")
                    }
                },

                MarkdownElementTypes.FULL_REFERENCE_LINK to ReferenceLinksGeneratingProvider(linkMap),
                MarkdownElementTypes.SHORT_REFERENCE_LINK to ReferenceLinksGeneratingProvider(linkMap),

                MarkdownElementTypes.LINK_DEFINITION to object : GeneratingProvider {
                    override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    }
                },

                MarkdownElementTypes.CODE_FENCE to CodeFenceGeneratingProvider(),

                MarkdownElementTypes.CODE_BLOCK to object : GeneratingProvider {
                    override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                        visitor.consumeHtml("<pre><code>")
                        visitor.consumeHtml(trimIndents(leafText(text, node, false), 4))
                        visitor.consumeHtml("\n")
                        visitor.consumeHtml("</code></pre>")
                    }
                },

                MarkdownTokenTypes.HORIZONTAL_RULE to object : GeneratingProvider {
                    override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                        visitor.consumeHtml("<hr />")
                    }

                },
                MarkdownTokenTypes.HARD_LINE_BREAK to object : GeneratingProvider {
                    override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                        visitor.consumeHtml("<br />")
                    }
                },

                MarkdownElementTypes.PARAGRAPH to SimpleInlineTagProvider("p"),
                MarkdownElementTypes.EMPH to SimpleInlineTagProvider("em", 1, -1),
                MarkdownElementTypes.STRONG to SimpleInlineTagProvider("strong", 2, -2),
                MarkdownElementTypes.CODE_SPAN to object : GeneratingProvider {
                    override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                        val output = node.children.subList(1, node.children.size() - 1).map { node ->
                            leafText(text, node, false)
                        }.joinToString("").trim()
                        visitor.consumeHtml("<code>${output}</code>")
                    }
                }

        )
    }

    companion object {
        fun leafText(text: String, node: ASTNode, replaceEscapesAndEntities: Boolean = true): CharSequence {
            if (node.type == MarkdownTokenTypes.BLOCK_QUOTE) {
                return ""
            }
            return EntityConverter.replaceEntities(node.getTextInNode(text), replaceEscapesAndEntities, replaceEscapesAndEntities)
        }

        fun trimIndents(text: CharSequence, indent: Int): CharSequence {
            if (indent == 0) {
                return text
            }
            val regex = Regex("(\n|^)" + " {0,${indent}}")
            return regex.replace(text, fun(m: MatchResult) = m.groups[1]?.value!!)
        }

    }
}