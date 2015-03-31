package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.TokensCache
import java.util.ArrayList

public class SequentialParserUtil {
    class object {
        public fun textRangesToIndices(ranges: Collection<Range<Int>>): List<Int> {
            val result = ArrayList<Int>()
            for (range in ranges) {
                for (i in range.start..range.end - 1) {
                    result.add(i)
                }
            }
            result.sort()
            return result
        }

        public fun indicesToTextRanges(indices: List<Int>): Collection<Range<Int>> {
            val result = ArrayList<Range<Int>>()

            var starting = 0
            for (i in indices.indices) {
                if (i + 1 == indices.size() || indices.get(i) + 1 != indices.get(i + 1)) {
                    result.add(indices.get(starting)..indices.get(i) + 1)
                    starting = i + 1
                }
            }

            return result
        }

        public fun isWhitespace(info: TokensCache.Iterator, lookup: Int): Boolean {
            val `type` = info.rawLookup(lookup)
            if (`type` == null) {
                return false
            }
            if (`type` == MarkdownTokenTypes.EOL || `type` == MarkdownTokenTypes.WHITE_SPACE) {
                return true
            }
            if (lookup == -1) {
                return info.rollback().text.endsWith(' ', ignoreCase = false)
            } else {
                return info.advance().text.startsWith(' ', ignoreCase = false)
            }
        }


        public fun filterBlockquotes(tokensCache: TokensCache, textRange: Range<Int>): Collection<Range<Int>> {
            val result = ArrayList<Range<Int>>()
            var lastStart = textRange.start

            val R = textRange.end
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
