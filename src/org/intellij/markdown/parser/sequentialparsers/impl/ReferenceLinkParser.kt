package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.*

class ReferenceLinkParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<IntRange>): SequentialParser.ParsingResult {
        var result = SequentialParser.ParsingResult()
        val delegateIndices = ArrayList<Int>()
        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)

        var iterator: TokensCache.Iterator = tokens.ListIterator(indices, 0)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LBRACKET) {
                val localDelegates = ArrayList<Int>()
                val resultNodes = ArrayList<SequentialParser.Node>()
                val afterLink = parseReferenceLink(resultNodes, localDelegates, iterator)
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
        fun parseReferenceLink(resultNodes: MutableCollection<SequentialParser.Node>, localDelegates: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
            var result: TokensCache.Iterator?

            result = parseFullReferenceLink(resultNodes, localDelegates, iterator)
            if (result != null) {
                return result
            }
            resultNodes.clear()
            localDelegates.clear()
            result = parseShortReferenceLink(resultNodes, localDelegates, iterator)
            if (result != null) {
                return result
            }
            return null
        }

        private fun parseFullReferenceLink(result: MutableCollection<SequentialParser.Node>, delegateIndices: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
            val startIndex = iterator.index
            var it : TokensCache.Iterator? = iterator

            it = LinkParserUtil.parseLinkText(result, delegateIndices, it!!)
            if (it == null) {
                return null
            }
            it = it.advance()

            if (it.type == MarkdownTokenTypes.EOL) {
                it = it.advance()
            }

            it = LinkParserUtil.parseLinkLabel(result, delegateIndices, it)
            if (it == null) {
                return null
            }

            result.add(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.FULL_REFERENCE_LINK))
            return it
        }

        private fun parseShortReferenceLink(result: MutableCollection<SequentialParser.Node>, delegateIndices: MutableList<Int>, iterator: TokensCache.Iterator): TokensCache.Iterator? {
            val startIndex = iterator.index
            var it : TokensCache.Iterator? = iterator

            it = LinkParserUtil.parseLinkLabel(result, delegateIndices, it!!)
            if (it == null) {
                return null
            }

            val shortcutLinkEnd = it

            it = it.advance()
            if (it.type == MarkdownTokenTypes.EOL) {
                it = it.advance()
            }

            if (it.type == MarkdownTokenTypes.LBRACKET && it.rawLookup(1) == MarkdownTokenTypes.RBRACKET) {
                it = it.advance()
            } else {
                it = shortcutLinkEnd
            }

            result.add(SequentialParser.Node(startIndex..it.index + 1, MarkdownElementTypes.SHORT_REFERENCE_LINK))
            return it
        }

    }

}
