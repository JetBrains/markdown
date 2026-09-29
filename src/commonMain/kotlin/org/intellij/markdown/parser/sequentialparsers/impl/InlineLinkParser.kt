package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.LocalParsingResult
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class InlineLinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        val scanIndex by lazy { LinkParserUtil.ScanIndex(tokens, rangesToGlue) }

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val inlineLink = parseInlineLink(iterator, scanIndex = scanIndex)
                if (inlineLink != null) {
                    iterator = inlineLink.iteratorPosition.advance()
                    result = result.withOtherParsingResult(inlineLink)
                    continue
                }
            }

            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    companion object {
        fun parseInlineLink(
            iterator: TokensCache.Iterator,
            linkStarts: Set<Int>? = null,
            scanIndex: LinkParserUtil.ScanIndex? = null
        ): LocalParsingResult? {
            if (linkStarts != null && iterator.index !in linkStarts) {
                return null
            }

            val startIndex = iterator.index
            // The link text is collected only once the link is known to be complete,
            // so that with a scan index a failed candidate costs no scan of it.
            var it = if (scanIndex != null) {
                scanIndex.matchingBracket(iterator)
            } else {
                LinkParserUtil.parseLinkText(iterator)?.iteratorPosition
            } ?: return null
            if (it.rawLookup(1) != MarkdownTokenTypes.LPAREN) {
                return null
            }

            it = it.advance().advance()
            if (it.type == MarkdownTokenTypes.EOL) {
                it = it.advance()
            }
            val linkDestination = LinkParserUtil.parseLinkDestination(it, scanIndex)
            if (linkDestination != null) {
                it = linkDestination.iteratorPosition.advance()
                if (it.type == MarkdownTokenTypes.EOL) {
                    it = it.advance()
                }
            }
            val linkTitle = LinkParserUtil.parseLinkTitle(it, scanIndex)
            if (linkTitle != null) {
                it = linkTitle.iteratorPosition.advance()
                if (it.type == MarkdownTokenTypes.EOL) {
                    it = it.advance()
                }
            }
            if (it.type != MarkdownTokenTypes.RPAREN) {
                return null
            }
            val linkText = LinkParserUtil.parseLinkText(iterator)
                    ?: return null

            return LocalParsingResult(it,
                    linkText.parsedNodes
                            + (linkDestination?.parsedNodes ?: emptyList())
                            + (linkTitle?.parsedNodes ?: emptyList())
                            + SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.INLINE_LINK),
                    linkText.rangesToProcessFurther)
        }


    }
}
