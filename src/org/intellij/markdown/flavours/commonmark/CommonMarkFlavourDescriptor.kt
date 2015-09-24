package org.intellij.markdown.flavours.commonmark

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.html.*
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.lexer._MarkdownLexer
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.*
import java.io.Reader

public open class CommonMarkFlavourDescriptor : MarkdownFlavourDescriptor {
    override val markerProcessorFactory: MarkerProcessorFactory = CommonMarkMarkerProcessor.Factory

    override fun createInlinesLexer(): MarkdownLexer {
        return MarkdownLexer(_MarkdownLexer(null as Reader?))
    }

    override val sequentialParserManager = object : SequentialParserManager() {
        override fun getParserSequence(): List<SequentialParser> {
            return listOf(AutolinkParser(listOf(MarkdownTokenTypes.AUTOLINK)),
                    BacktickParser(),
                    ImageParser(),
                    InlineLinkParser(),
                    ReferenceLinkParser(),
                    EmphStrongParser())
        }
    }

    override fun createHtmlGeneratingProviders(linkMap: LinkMap): Map<IElementType, GeneratingProvider> = hashMapOf(

            MarkdownElementTypes.MARKDOWN_FILE to SimpleTagProvider("body"),
            MarkdownElementTypes.HTML_BLOCK to HtmlBlockGeneratingProvider(),
            MarkdownTokenTypes.HTML_TAG to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
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

            MarkdownTokenTypes.SETEXT_CONTENT to TrimmingInlineHolderProvider(),
            MarkdownElementTypes.SETEXT_1 to SimpleTagProvider("h1"),
            MarkdownElementTypes.SETEXT_2 to SimpleTagProvider("h2"),

            MarkdownTokenTypes.ATX_CONTENT to TrimmingInlineHolderProvider(),
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
                    return "<a ${HtmlGenerator.getSrcPosAttribute(node)} href=\"${LinkMap.normalizeDestination(linkText)}\">$link</a>"
                }
            },


            MarkdownElementTypes.LINK_LABEL to TransparentInlineHolderProvider(),
            MarkdownElementTypes.LINK_TEXT to TransparentInlineHolderProvider(),
            MarkdownElementTypes.LINK_TITLE to TransparentInlineHolderProvider(),

            MarkdownElementTypes.INLINE_LINK to InlineLinkGeneratingProvider(),

            MarkdownElementTypes.FULL_REFERENCE_LINK to ReferenceLinksGeneratingProvider(linkMap),
            MarkdownElementTypes.SHORT_REFERENCE_LINK to ReferenceLinksGeneratingProvider(linkMap),

            MarkdownElementTypes.IMAGE to ImageGeneratingProvider(linkMap),

            MarkdownElementTypes.LINK_DEFINITION to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                }
            },

            MarkdownElementTypes.CODE_FENCE to CodeFenceGeneratingProvider(),

            MarkdownElementTypes.CODE_BLOCK to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    visitor.consumeHtml("<pre><code ${HtmlGenerator.getSrcPosAttribute(node)}>")
                    visitor.consumeHtml(HtmlGenerator.trimIndents(HtmlGenerator.leafText(text, node, false), 4))
                    visitor.consumeHtml("\n")
                    visitor.consumeHtml("</code></pre>")
                }
            },

            MarkdownTokenTypes.HORIZONTAL_RULE to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    visitor.consumeHtml("<hr />")
                }

            },
            MarkdownTokenTypes.HARD_LINE_BREAK to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    visitor.consumeHtml("<br />")
                }
            },

            MarkdownElementTypes.PARAGRAPH to object : TrimmingInlineHolderProvider() {
                override fun openTag(text: String, node: ASTNode): String {
                    return "<p ${HtmlGenerator.getSrcPosAttribute(node)}>"
                }

                override fun closeTag(text: String, node: ASTNode): String {
                    return "</p>"
                }
            },
            MarkdownElementTypes.EMPH to SimpleInlineTagProvider("em", 1, -1),
            MarkdownElementTypes.STRONG to SimpleInlineTagProvider("strong", 2, -2),
            MarkdownElementTypes.CODE_SPAN to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    val output = node.children.subList(1, node.children.size() - 1).map { node ->
                        HtmlGenerator.leafText(text, node, false)
                    }.joinToString("").trim()
                    visitor.consumeHtml("<code ${HtmlGenerator.getSrcPosAttribute(node)}>${output}</code>")
                }
            }

    )

}