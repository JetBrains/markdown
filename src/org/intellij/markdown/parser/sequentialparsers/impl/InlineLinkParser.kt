package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.ArrayList

public class InlineLinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var iterator: TokensCache.Iterator = tokens.ListIterator(indices, 0)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val localDelegates = ArrayList<Int>()
                val resultNodes = ArrayList<SequentialParser.Node>()
                val afterLink = parseInlineLink(resultNodes, localDelegates, iterator)
                if (afterLink != null) {
                    iterator = afterLink.advance()
                    result = result.withNodes(resultNodes).withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(localDelegates))
                    continue
                }
            }

            delegateIndices.add(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
    }

    companion object {
        fun parseInlineLink(result: MutableCollection<SequentialParser.Node>, delegateIndices: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
            val startIndex = iterator.index
            var it = iterator

            val afterText = LinkParserUtil.parseLinkText(result, delegateIndices, it)
                    ?: return null
            it = afterText
            if (it.rawLookup(1) != MarkdownTokenTypes.LPAREN) {
                return null
            }

            it = it.advance().advance()
            if (it.type == MarkdownTokenTypes.EOL) {
                it = it.advance()
            }
            val afterDestination = LinkParserUtil.parseLinkDestination(result, it)
            if (afterDestination != null) {
                it = afterDestination.advance()
                if (it.type == MarkdownTokenTypes.EOL) {
                    it = it.advance()
                }
            }
            val afterTitle = LinkParserUtil.parseLinkTitle(result, it)
            if (afterTitle != null) {
                it = afterTitle.advance()
                if (it.type == MarkdownTokenTypes.EOL) {
                    it = it.advance()
                }
            }
            if (it.type != MarkdownTokenTypes.RPAREN) {
                return null
            }

            result.add(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.INLINE_LINK))
            return it
        }
    }
}
