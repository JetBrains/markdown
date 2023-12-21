package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getParentOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.lexer._GFMLexer
import org.intellij.markdown.html.*
import org.intellij.markdown.html.entities.EntityConverter
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkerProcessorFactory
import org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.*

/**
 * GitHub Markdown spec based flavour, to be used as a base for other flavours.
 *
 * @param useSafeLinks `true` if all rendered links should be checked for XSS and `false` otherwise.
 * See [makeXssSafeDestination]
 *
 * @param absolutizeAnchorLinks `true` if anchor links (e.g. `#foo`) should be resolved against `baseURI` and
 * `false` otherwise
 *
 * @param makeHttpsAutoLinks enables use of HTTPS schema for auto links.
 */
open class GFMFlavourDescriptor(
        useSafeLinks: Boolean = true,
        absolutizeAnchorLinks: Boolean = false,
        private val makeHttpsAutoLinks: Boolean = false
) : CommonMarkFlavourDescriptor(useSafeLinks, absolutizeAnchorLinks) {
    override val markerProcessorFactory: MarkerProcessorFactory = GFMMarkerProcessor.Factory

    override fun createInlinesLexer(): MarkdownLexer {
        return MarkdownLexer(_GFMLexer())
    }

    override val sequentialParserManager = object : SequentialParserManager() {
        override fun getParserSequence(): List<SequentialParser> {
            return listOf(AutolinkParser(listOf(MarkdownTokenTypes.AUTOLINK, GFMTokenTypes.GFM_AUTOLINK)),
                    MathParser(),
                    BacktickParser(),
                    ImageParser(),
                    InlineLinkParser(),
                    ReferenceLinkParser(),
                    EmphasisLikeParser(EmphStrongDelimiterParser(), StrikeThroughDelimiterParser()))
        }
    }

    override fun createHtmlGeneratingProviders(linkMap: LinkMap,
                                               baseURI: URI?): Map<IElementType, GeneratingProvider> {
        return super.createHtmlGeneratingProviders(linkMap, baseURI) + hashMapOf(
                GFMElementTypes.STRIKETHROUGH to object : SimpleInlineTagProvider("span", 2, -2) {
                    override fun openTag(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                        visitor.consumeTagOpen(node, tagName, "class=\"user-del\"")
                    }
                },

                GFMElementTypes.TABLE to TablesGeneratingProvider(),

                GFMTokenTypes.CELL to TrimmingInlineHolderProvider(),
                MarkdownElementTypes.CODE_SPAN to TableAwareCodeSpanGeneratingProvider(),

                GFMTokenTypes.GFM_AUTOLINK to object : GeneratingProvider {
                    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
                        val linkText = node.getTextInNode(text)

                        // #28: do not render GFM autolinks under link titles
                        // (though it's "OK" according to CommonMark spec)
                        if (node.getParentOfType(MarkdownElementTypes.LINK_LABEL,
                                        MarkdownElementTypes.LINK_TEXT) != null) {
                            visitor.consumeHtml(linkText)
                            return
                        }

                        // according to GFM_AUTOLINK rule in lexer, link either starts with scheme or with 'www.'
                        val absoluteLink = if (hasSchema(linkText)) linkText else {
                            if (makeHttpsAutoLinks) {
                                "https://$linkText"
                            } else {
                                "http://$linkText"
                            }
                        }

                        val link = EntityConverter.replaceEntities(linkText, true, false)
                        val normalizedDestination = LinkMap.normalizeDestination(absoluteLink, false).let {
                            if (useSafeLinks) makeXssSafeDestination(it) else it
                        }
                        visitor.consumeTagOpen(node, "a", "href=\"$normalizedDestination\"")
                        visitor.consumeHtml(link)
                        visitor.consumeTagClose("a")
                    }

                    private fun hasSchema(linkText: CharSequence): Boolean {
                        val index = linkText.indexOf('/')
                        if (index == -1) return false
                        return index != 0
                                && index + 1 < linkText.length
                                && linkText[index - 1] == ':'
                                && linkText[index + 1] == '/'
                    }
                },

                MarkdownElementTypes.LIST_ITEM to CheckedListItemGeneratingProvider(),

                GFMElementTypes.INLINE_MATH to MathGeneratingProvider(inline = true),
                GFMElementTypes.BLOCK_MATH to MathGeneratingProvider()
          )
    }
}
