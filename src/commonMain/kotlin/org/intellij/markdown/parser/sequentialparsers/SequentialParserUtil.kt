package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.html.isPunctuation
import org.intellij.markdown.html.isWhitespace

class SequentialParserUtil {
    companion object {
        

        fun isWhitespace(info: TokensCache.Iterator, lookup: Int): Boolean {
            return isWhitespace(info.charLookup(lookup))
        }

        fun isPunctuation(info: TokensCache.Iterator, lookup: Int): Boolean {
            return isPunctuation(info.charLookup(lookup))
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
        lastStart = -239
        lastEnd = -239
        return list
    }

}
