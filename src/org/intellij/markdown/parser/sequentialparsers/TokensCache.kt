package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.IElementType

abstract class TokensCache {
    abstract val cachedTokens: List<TokenInfo>
    abstract val filteredTokens: List<TokenInfo>
    abstract val originalText: CharSequence
    abstract val originalTextRange: IntRange

    fun getRawCharAt(index: Int): Char {
        if (index < originalTextRange.start) return 0.toChar()
        if (index > originalTextRange.endInclusive) return 0.toChar()
        return originalText[index]
    }

    protected fun verify() {
        for (i in cachedTokens.indices) {
            assert(cachedTokens[i].rawIndex == i)
        }
        for (i in filteredTokens.indices) {
            assert(filteredTokens[i].normIndex == i)
        }
    }

    inner class RangesListIterator private constructor(private val ranges: List<IntRange>,
                                                       private val listIndex: Int,
                                                       value: Int) : Iterator(value) {
        constructor(ranges: List<IntRange>) : this(ranges, 0, ranges.firstOrNull()?.start ?: -1)

        override fun advance(): RangesListIterator {
            if (listIndex >= ranges.size) {
                return this
            }
            if (index == ranges[listIndex].endInclusive) {
                return RangesListIterator(ranges, listIndex + 1, ranges.getOrNull(listIndex + 1)?.start ?: filteredTokens.size)
            }
            return RangesListIterator(ranges, listIndex, index + 1)
        }

        override fun rollback(): RangesListIterator {
            if (listIndex < 0) {
                return this
            }
            if (index == ranges[listIndex].start) {
                return RangesListIterator(ranges, listIndex - 1, ranges.getOrNull(listIndex - 1)?.endInclusive ?: -1)
            }
            return RangesListIterator(ranges, listIndex, index - 1)
        }

        override fun rawLookup(steps: Int): IElementType? {
            if (index + steps in ranges.getOrNull(listIndex) ?: return null) {
                return super.rawLookup(steps)
            }
            return null
        }
    }

    inner open class Iterator(val index: Int) {

        val type : IElementType?
            get() {
                return info(0).type
            }

        val text: String
            get() {
                return originalText.subSequence(info(0).tokenStart, info(0).tokenEnd).toString()
            }

        val start: Int
            get() {
                return info(0).tokenStart
            }

        val end: Int
            get() {
                return info(0).tokenEnd
            }

        open fun advance(): Iterator {
            return Iterator(index + 1)
        }

        open fun rollback(): Iterator {
            return Iterator(index - 1)
        }

        private fun info(rawSteps: Int): TokenInfo {
            if (index < 0) {
                return TokenInfo(null, originalTextRange.start, originalTextRange.start, 0, 0)
            } else if (index > filteredTokens.size) {
                return TokenInfo(null, originalTextRange.endInclusive + 1, originalTextRange.endInclusive + 1, 0, 0)
            }

            val rawIndex = if (index < filteredTokens.size)
                filteredTokens[index].rawIndex + rawSteps
            else
                cachedTokens.size + rawSteps

            if (rawIndex < 0) {
                return TokenInfo(null, originalTextRange.start, originalTextRange.start, 0, 0)
            } else if (rawIndex >= cachedTokens.size) {
                return TokenInfo(null, originalTextRange.endInclusive + 1, originalTextRange.endInclusive + 1, 0, 0)
            }

            return cachedTokens[rawIndex]
        }


        open fun rawLookup(steps: Int): IElementType? {
            return info(steps).`type`
        }

        fun rawStart(steps: Int): Int {
            return info(steps).tokenStart
        }

        open fun charLookup(steps: Int): Char {
            if (steps == 1) {
                return getRawCharAt(end)
            } else if (steps == -1) {
                return getRawCharAt(start - 1)
            } else {
                val pos = if (steps > 0) rawStart(steps) else rawStart(steps + 1) - 1
                return getRawCharAt(pos)
            }
        }

        override fun toString(): String {
            return "Iterator: $index: $type"
        }
    }

    class TokenInfo(val `type`: IElementType?,
                           val tokenStart: Int,
                           val tokenEnd: Int,
                           val rawIndex: Int,
                           var normIndex: Int) {

        override fun toString(): String {
            return "TokenInfo: " + `type`.toString() + " [" + tokenStart + ", " + tokenEnd + ")"
        }
    }

}
