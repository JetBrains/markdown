package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.MarkdownTokenTypes
import java.util.*

class SequentialParserUtil {
    companion object {
        private val PUNCTUATION_MASK: Int = (1 shl Character.DASH_PUNCTUATION.toInt()) or
                (1 shl Character.START_PUNCTUATION.toInt())     or
                (1 shl Character.END_PUNCTUATION.toInt())       or
                (1 shl Character.CONNECTOR_PUNCTUATION.toInt()) or
                (1 shl Character.OTHER_PUNCTUATION.toInt())     or
                (1 shl Character.INITIAL_QUOTE_PUNCTUATION.toInt()) or
                (1 shl Character.FINAL_QUOTE_PUNCTUATION.toInt()) or
                (1 shl Character.MATH_SYMBOL.toInt())

        fun isWhitespace(info: TokensCache.Iterator, lookup: Int): Boolean {
            val char = info.charLookup(lookup)
            return char == 0.toChar() || Character.isSpaceChar(char) || Character.isWhitespace(char)
        }

        fun isPunctuation(info: TokensCache.Iterator, lookup: Int): Boolean {
            val char = info.charLookup(lookup)
            return (PUNCTUATION_MASK shr Character.getType(char)) and 1 != 0
        }

       fun filterBlockquotes(tokensCache: TokensCache, textRange: IntRange): List<IntRange> {
            val result = ArrayList<IntRange>()
            var lastStart = textRange.first

            val R = textRange.last
            for (i in lastStart..R - 1) {
                if (tokensCache.Iterator(i).type == MarkdownTokenTypes.BLOCK_QUOTE) {
                    if (lastStart < i) {
                        result.add(lastStart..i - 1)
                    }
                    lastStart = i + 1
                }
            }
            if (lastStart < R) {
                result.add(lastStart..R)
            }
            return result
        }
    }

}

class RangesListBuilder {
    private val list = ArrayList<IntRange>()
    private var lastStart = -239
    private var lastEnd = -239

    fun put(index: Int) {
        if (lastEnd + 1 == index) {
            lastEnd = index
            return
        }
        if (lastStart != -239) {
            list.add(lastStart..lastEnd)
        }
        lastStart = index
        lastEnd = index
    }

    fun get(): List<IntRange> {
        if (lastStart != -239) {
            list.add(lastStart..lastEnd)
        }
        return list
    }

}
