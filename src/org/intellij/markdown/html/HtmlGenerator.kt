package org.intellij.markdown.html

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.RecursiveVisitor


public class HtmlGenerator(private val markdownText: String, private val root: ASTNode) {
    private val htmlString: StringBuilder = StringBuilder()
    private val providers: Map<IElementType, GeneratingProvider> = initProviders()

    public fun generateHtml(): String {
        HtmlGeneratingVisitor().visitNode(root)
        return htmlString.toString()
    }

    inner class HtmlGeneratingVisitor : RecursiveVisitor() {
        override fun visitNode(node: ASTNode) {
            providers.get(node.type)?.processNode(this, markdownText, node)
                    ?: node.acceptChildren(this)
        }

        public final fun consumeHtml(html: CharSequence) {
            htmlString.append(html)
        }
    }

    trait GeneratingProvider {
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
                    visitor.consumeHtml(leafText(text, child))
                } else {
                    child.accept(visitor)
                }
            }

            visitor.consumeHtml(closeTag(text, node))
        }
    }

    class SimpleTagProvider(val tagName: String) : OpenCloseGeneratingProvider() {
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

    companion object {
        private val entityConverter = EntityConverter()

        fun leafText(text: String, node: ASTNode): CharSequence {
            if (node.type == MarkdownTokenTypes.BLOCK_QUOTE) {
                return ""
            }
            return entityConverter.replaceEntities(node.getTextInNode(text))
        }


        fun initProviders(): Map<IElementType, GeneratingProvider> {
            MarkdownTokenTypes.javaClass.getDeclaredFields()

            return hashMapOf(

                    MarkdownElementTypes.MARKDOWN_FILE to SimpleTagProvider("body"),
                    MarkdownTokenTypes.HTML_BLOCK to object : GeneratingProvider {
                        override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                            visitor.consumeHtml(node.getTextInNode(text));
                        }
                    },
                    MarkdownTokenTypes.HTML_TAG to object : GeneratingProvider {
                        override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                            visitor.consumeHtml(node.getTextInNode(text));
                        }
                    },

                    MarkdownElementTypes.BLOCK_QUOTE to SimpleTagProvider("blockquote"),

                    MarkdownElementTypes.ORDERED_LIST to SimpleTagProvider("ol"),
                    MarkdownElementTypes.UNORDERED_LIST to SimpleTagProvider("ul"),
                    MarkdownElementTypes.LIST_ITEM to SimpleTagProvider("li"),

                    MarkdownElementTypes.SETEXT_1 to SimpleInlineTagProvider("h1", 0, -2),
                    MarkdownElementTypes.SETEXT_2 to SimpleInlineTagProvider("h2", 0, -2),

                    MarkdownElementTypes.ATX_1 to SimpleInlineTagProvider("h1", 1),
                    MarkdownElementTypes.ATX_2 to SimpleInlineTagProvider("h2", 1),
                    MarkdownElementTypes.ATX_3 to SimpleInlineTagProvider("h3", 1),
                    MarkdownElementTypes.ATX_4 to SimpleInlineTagProvider("h4", 1),
                    MarkdownElementTypes.ATX_5 to SimpleInlineTagProvider("h5", 1),
                    MarkdownElementTypes.ATX_6 to SimpleInlineTagProvider("h6", 1),

                    MarkdownElementTypes.AUTOLINK to object : NonRecursiveGeneratingProvider() {
                        override fun generateTag(text: String, node: ASTNode): String {
                            val linkText = node.getTextInNode(text)
                            val link = entityConverter.replaceEntities(linkText.subSequence(1, linkText.length() - 1))
                            return "<a href=\"$link\">$link</a>"
                        }
                    },


                    //                    public val LINK_DEFINITION: IElementType = MarkdownElementType("LINK_DEFINITION")
                    MarkdownElementTypes.LINK_LABEL to TransparentInlineHolderProvider(),
                    MarkdownElementTypes.LINK_DESTINATION to object: TransparentInlineHolderProvider(1, -1) {
                        override fun childrenToRender(node: ASTNode): List<ASTNode> {
                            if (node.children.first().type == MarkdownTokenTypes.LT) {
                                return super.childrenToRender(node)
                            }
                            else {
                                return node.children
                            }
                        }
                    },
                    MarkdownElementTypes.LINK_TITLE to TransparentInlineHolderProvider(1, -1),
                    MarkdownElementTypes.LINK_TEXT to TransparentInlineHolderProvider(1, -1),

                    MarkdownElementTypes.INLINE_LINK to object : GeneratingProvider {
                        override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                            val destinationNode = node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
                            val titleNode = node.findChildOfType(MarkdownElementTypes.LINK_TITLE)

                            val destinationText = destinationNode?.getTextInNode(text) ?: ""
                            val titleText = if (titleNode != null)
                                " title=\"${leafText(text, titleNode)}\""
                            else
                                ""

                            visitor.consumeHtml("<a href=\"${destinationText}\"${titleText}>")
                            node.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.accept(visitor)
                            visitor.consumeHtml("</a>")
                        }
                    },

                    //                    MarkdownElementTypes.FULL_REFERENCE_LINK to object: GeneratingProvider {
                    //                        }
                    //                    },
                    //                    public val SHORT_REFERENCE_LINK: IElementType = MarkdownElementType("SHORT_REFERENCE_LINK")

                    MarkdownElementTypes.CODE_FENCE to object : GeneratingProvider {
                        override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                            visitor.consumeHtml("<pre><code>")
                            var state = 0

                            var childrenToConsider = node.children
                            if (childrenToConsider.last().type == MarkdownTokenTypes.CODE_FENCE_END) {
                                childrenToConsider = childrenToConsider.subList(0, childrenToConsider.size() - 2)
                            }

                            for (child in childrenToConsider) {
                                if (state == 1) {
                                    visitor.consumeHtml(leafText(text, child))
                                }
                                if (state == 0 && child.type == MarkdownTokenTypes.EOL) {
                                    state = 1
                                }
                            }
                            visitor.consumeHtml("</code></pre>")
                        }
                    },

                    MarkdownElementTypes.CODE_BLOCK to object : GeneratingProvider {
                        override fun processNode(visitor: HtmlGeneratingVisitor, text: String, node: ASTNode) {
                            visitor.consumeHtml("<pre><code>")
                            for (child in node.children) {
                                var html = leafText(text, child)
                                if (child.type == MarkdownTokenTypes.EOL) {
                                    html = html.toString().replaceAll("\n(    |\t)", "\n")
                                }
                                visitor.consumeHtml(html)
                            }
                            visitor.consumeHtml("</code></pre>")
                        }
                    },

                    MarkdownElementTypes.PARAGRAPH to SimpleInlineTagProvider("p"),
                    MarkdownElementTypes.EMPH to SimpleInlineTagProvider("em", 1, -1),
                    MarkdownElementTypes.STRONG to SimpleInlineTagProvider("strong", 2, -2),
                    MarkdownElementTypes.CODE_SPAN to SimpleInlineTagProvider("code", 1, -1)

            )
        }
    }
}