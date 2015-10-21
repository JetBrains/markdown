package org.intellij.markdown.parser.sequentialparsers.impl

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserUtil
import org.intellij.markdown.parser.sequentialparsers.TokensCache
import java.util.ArrayList

public class BacktickParser : SequentialParser {
    override fun parse(tokens: TokensCache, rangesToGlue: Collection<Range<Int>>): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResult()

        val indices = SequentialParserUtil.textRangesToIndices(rangesToGlue)
        val delegateIndices = ArrayList<Int>()

        var i = 0
        while (i < indices.size) {
            val iterator = tokens.ListIterator(indices, i)
            if (iterator.type == MarkdownTokenTypes.BACKTICK || iterator.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {

                val j = findOfSize(tokens, indices, i + 1, getLength(iterator, true))

                if (j != -1) {
                    result.withNode(SequentialParser.Node(indices.get(i)..indices.get(j) + 1, MarkdownElementTypes.CODE_SPAN))
                    i = j + 1
                    continue
                }
            }
            delegateIndices.add(indices.get(i))
            ++i
        }

        return result.withFurtherProcessing(SequentialParserUtil.indicesToTextRanges(delegateIndices))
    }

    private fun findOfSize(tokens: TokensCache, indices: List<Int>, from: Int, length: Int): Int {
        for (i in from..indices.size - 1) {
            val iterator = tokens.ListIterator(indices, i)
            if (iterator.type != MarkdownTokenTypes.BACKTICK && iterator.type != MarkdownTokenTypes.ESCAPED_BACKTICKS) {
                continue
            }

            if (getLength(iterator, false) == length) {
                return i
            }
        }
        return -1
    }


    private fun getLength(info: TokensCache.Iterator, canEscape: Boolean): Int {
        val tokenText = info.text

        var toSubtract = 0
        if (info.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {
            if (canEscape) {
                toSubtract = 2
            } else {
                toSubtract = 1
            }
        }

        return tokenText.length - toSubtract
    }
}
