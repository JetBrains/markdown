package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.LocalParseResult
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.*

class InlineLinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<IntRange>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var iterator: TokensCache.Iterator = tokens.ListIterator(indices, 0)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val inlineLink = parseInlineLink(iterator)
                if (inlineLink != null) {
                    iterator = inlineLink.iteratorPosition.advance()
                    result = result.withNodes(inlineLink.resultNodes)
                            .withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(inlineLink.delegateIndices))
                    continue
                }
            }

            delegateIndices.add(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
    }

    companion object {
        fun parseInlineLink(iterator: TokensCache.Iterator): LocalParseResult? {
            val startIndex = iterator.index
            var it = iterator

            val linkText = LinkParserUtil.parseLinkText(it)
                    ?: return null
            it = linkText.iteratorPosition
            if (it.rawLookup(1) != MarkdownTokenTypes.LPAREN) {
                return null
            }

            it = it.advance().advance()
            if (it.type == MarkdownTokenTypes.EOL) {
                it = it.advance()
            }
            val linkDestination = LinkParserUtil.parseLinkDestination(it)
            if (linkDestination != null) {
                it = linkDestination.iteratorPosition.advance()
                if (it.type == MarkdownTokenTypes.EOL) {
                    it = it.advance()
                }
            }
            val linkTitle = LinkParserUtil.parseLinkTitle(it)
            if (linkTitle != null) {
                it = linkTitle.iteratorPosition.advance()
                if (it.type == MarkdownTokenTypes.EOL) {
                    it = it.advance()
                }
            }
            if (it.type != MarkdownTokenTypes.RPAREN) {
                return null
            }

            return LocalParseResult(it,
                    linkText.resultNodes
                            + (linkDestination?.resultNodes ?: emptyList())
                            + (linkTitle?.resultNodes ?: emptyList())
                            + SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.INLINE_LINK),
                    linkText.delegateIndices)
        }
    }
}
