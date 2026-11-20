package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache

class BacktickParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.BACKTICK || iterator.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {

                val endIterator = findClosingBackticks(iterator)

                if (endIterator != null) {
                    result.withNode(SequentialParser.Node(iterator.index..endIterator.index + 1, MarkdownElementTypes.CODE_SPAN))
                    iterator = endIterator.advance()
                    continue
                }
            }
            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    companion object {
        internal fun findClosingBackticks(opener: TokensCache.Iterator): TokensCache.Iterator? {
            val length = getLength(opener, true)
            var iterator = opener.advance()
            while (iterator.type != null) {
                if (iterator.type == MarkdownTokenTypes.BACKTICK || iterator.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {
                    if (getLength(iterator, false) == length) {
                        return iterator
                    }
                }
                iterator = iterator.advance()
            }
            return null
        }

        private fun getLength(info: TokensCache.Iterator, canEscape: Boolean): Int {
            var toSubtract = 0
            if (info.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {
                if (canEscape) {
                    toSubtract = 2
                } else {
                    toSubtract = 1
                }
            }
            return info.length - toSubtract
        }
    }
}
