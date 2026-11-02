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
import org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.*

/**
 * CommonMark spec based flavour, to be used as a base for other flavours
 *
 * @param useSafeLinks `true` if all rendered links should be checked for XSS and `false` otherwise.
 * See [makeXssSafeDestination]
 *
 * @param absolutizeAnchorLinks `true` if anchor links (e.g. `#foo`) should be resolved against `baseURI` and
 * `false` otherwise
 */
open class CommonMarkFlavourDescriptor(protected val useSafeLinks: Boolean = true,
                                       protected val absolutizeAnchorLinks: Boolean = false) : MarkdownFlavourDescriptor {
    override val markerProcessorFactory: MarkerProcessorFactory = CommonMarkMarkerProcessor.Factory

    override fun createInlinesLexer(): MarkdownLexer {
        return MarkdownLexer(_MarkdownLexer())
    }

    override val sequentialParserManager = object : SequentialParserManager() {
        override fun getParserSequence(): List<SequentialParser> {
            return listOf(AutolinkParser(listOf(MarkdownTokenTypes.AUTOLINK)),
                    BacktickParser(),
                    ImageParser(),
                    InlineLinkParser(),
                    ReferenceLinkParser(),
                    EmphasisLikeParser(EmphStrongDelimiterParser())
            )
        }
    }

    override fun createHtmlGeneratingProviders(linkMap: LinkMap,
                                               baseURI: URI?): Map<IElementType, GeneratingProvider> = hashMapOf(

            MarkdownElementTypes.MARKDOWN_FILE to SimpleTagProvider("body"),
            MarkdownElementTypes.HTML_BLOCK to HtmlBlockGeneratingProvider(),
            MarkdownTokenTypes.HTML_TAG to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    visitor.consumeHtml(node.getTextInNode(text))
                }
            },

            MarkdownElementTypes.BLOCK_QUOTE to SimpleTagProvider("blockquote"),

            MarkdownElementTypes.ORDERED_LIST to object : SimpleTagProvider("ol") {
                override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    var attribute: String? = null
                    node.findChildOfType(MarkdownElementTypes.LIST_ITEM)
                            ?.findChildOfType(MarkdownTokenTypes.LIST_NUMBER)
                            ?.getTextInNode(text)?.toString()?.trim()?.let {
                        val number = it.substring(0, it.length - 1).trimStart('0')
                        if (!number.equals("1")) {
                            attribute = "start=\"${ if (number.isEmpty()) "0" else number }\""
                        }
                    }
                    visitor.consumeTagOpen(node, "ol", attribute)
                }
            },
            MarkdownElementTypes.UNORDERED_LIST to SimpleTagProvider("ul"),
            MarkdownElementTypes.LIST_ITEM to ListItemGeneratingProvider(),

            MarkdownTokenTypes.SETEXT_CONTENT to TrimmingInlineHolderProvider(),
            MarkdownElementTypes.SETEXT_1 to SimpleTagWithLinkProvider("h1"),
            MarkdownElementTypes.SETEXT_2 to SimpleTagWithLinkProvider("h2"),

            MarkdownTokenTypes.ATX_CONTENT to TrimmingInlineHolderProvider(),
            MarkdownElementTypes.ATX_1 to SimpleTagWithLinkProvider("h1"),
            MarkdownElementTypes.ATX_2 to SimpleTagWithLinkProvider("h2"),
            MarkdownElementTypes.ATX_3 to SimpleTagWithLinkProvider("h3"),
            MarkdownElementTypes.ATX_4 to SimpleTagWithLinkProvider("h4"),
            MarkdownElementTypes.ATX_5 to SimpleTagWithLinkProvider("h5"),
            MarkdownElementTypes.ATX_6 to SimpleTagWithLinkProvider("h6"),

            MarkdownElementTypes.AUTOLINK to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    val linkText = node.getTextInNode(text)
                    val linkLabel = EntityConverter.replaceEntities(
                        linkText.subSequence(1, linkText.length - 1),
                        processEntities = true,
                        processEscapes = false
                    )
                    val linkDestination = LinkMap.normalizeDestination(linkText, false).let {
                        if (useSafeLinks) makeXssSafeDestination(it) else it
                    }
                    visitor.consumeTagOpen(node, "a", "href=\"$linkDestination\"")
                    visitor.consumeHtml(linkLabel)
                    visitor.consumeTagClose("a")
                }

            },


            MarkdownElementTypes.LINK_LABEL to TransparentInlineHolderProvider(),
            MarkdownElementTypes.LINK_TEXT to TransparentInlineHolderProvider(),
            MarkdownElementTypes.LINK_TITLE to TransparentInlineHolderProvider(),

            MarkdownElementTypes.INLINE_LINK to
                    InlineLinkGeneratingProvider(baseURI, absolutizeAnchorLinks).makeXssSafe(useSafeLinks),

            MarkdownElementTypes.FULL_REFERENCE_LINK to
                    ReferenceLinksGeneratingProvider(linkMap, baseURI, absolutizeAnchorLinks).makeXssSafe(useSafeLinks),
            MarkdownElementTypes.SHORT_REFERENCE_LINK to
                    ReferenceLinksGeneratingProvider(linkMap, baseURI, absolutizeAnchorLinks).makeXssSafe(useSafeLinks),

            MarkdownElementTypes.IMAGE to ImageGeneratingProvider(linkMap, baseURI).makeXssSafe(useSafeLinks),

            MarkdownElementTypes.LINK_DEFINITION to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                }
            },

            MarkdownElementTypes.CODE_FENCE to CodeFenceGeneratingProvider(),

            MarkdownElementTypes.CODE_BLOCK to object : GeneratingProvider {
                override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    visitor.consumeHtml("<pre>")
                    visitor.consumeTagOpen(node, "code")

                    for (child in node.children) {
                        if (child.type == MarkdownTokenTypes.CODE_LINE) {
                            visitor.consumeHtml(HtmlGenerator.trimIndents(HtmlGenerator.leafText(text, child, false), 4))
                        } else if (child.type == MarkdownTokenTypes.EOL) {
                            visitor.consumeHtml("\n")
                        }
                    }

                    visitor.consumeHtml("\n")
                    visitor.consumeTagClose("code")
                    visitor.consumeHtml("</pre>")
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
                override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    visitor.consumeTagOpen(node, "p")
                }

                override fun closeTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                    visitor.consumeTagClose("p")
                }
            },
            MarkdownElementTypes.EMPH to SimpleInlineTagProvider("em", 1, -1),
            MarkdownElementTypes.STRONG to SimpleInlineTagProvider("strong", 2, -2),
            MarkdownElementTypes.CODE_SPAN to CodeSpanGeneratingProvider()
    )
}