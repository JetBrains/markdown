package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.MarkdownTokenTypes
import java.util.*

public class SequentialParserUtil {
    companion object {
        private val PUNCTUATION_MASK: Int = (1 shl Character.DASH_PUNCTUATION.toInt()) or
                (1 shl Character.START_PUNCTUATION.toInt())     or
                (1 shl Character.END_PUNCTUATION.toInt())       or
                (1 shl Character.CONNECTOR_PUNCTUATION.toInt()) or
                (1 shl Character.OTHER_PUNCTUATION.toInt())     or
                (1 shl Character.INITIAL_QUOTE_PUNCTUATION.toInt()) or
                (1 shl Character.FINAL_QUOTE_PUNCTUATION.toInt()) or
                (1 shl Character.MATH_SYMBOL.toInt())

        public fun textRangesToIndices(ranges: Collection<IntRange>): List<Int> {
            val result = ArrayList<Int>()
            for (range in ranges) {
                for (i in range.start..range.endInclusive - 1) {
                    result.add(i)
                }
            }
            return result.sorted()
        }

        public fun indicesToTextRanges(indices: List<Int>): Collection<IntRange> {
            val result = ArrayList<IntRange>()

            var starting = 0
            for (i in indices.indices) {
                if (i + 1 == indices.size || indices[i] + 1 != indices[i + 1]) {
                    result.add(indices[starting]..indices[i] + 1)
                    starting = i + 1
                }
            }

            return result
        }

        public fun isWhitespace(info: TokensCache.Iterator, lookup: Int): Boolean {
            val char = info.charLookup(lookup)
            return char == 0.toChar() || Character.isSpaceChar(char) || Character.isWhitespace(char);
        }

        public fun isPunctuation(info: TokensCache.Iterator, lookup: Int): Boolean {
            val char = info.charLookup(lookup)
            return (PUNCTUATION_MASK shr Character.getType(char)) and 1 != 0;
        }

       public fun filterBlockquotes(tokensCache: TokensCache, textRange: IntRange): Collection<IntRange> {
            val result = ArrayList<IntRange>()
            var lastStart = textRange.start

            val R = textRange.endInclusive
            for (i in lastStart..R - 1) {
                if (tokensCache.Iterator(i).type == MarkdownTokenTypes.BLOCK_QUOTE) {
                    if (lastStart < i) {
                        result.add(lastStart..i)
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
