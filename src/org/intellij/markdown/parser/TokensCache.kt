package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.lexer.MarkdownLexer
import java.util.ArrayList

public class TokensCache(lexer: MarkdownLexer) {
    private val cachedTokens: List<TokenInfo>
    private val filteredTokens: List<TokenInfo>
    private val originalText: String

    {
        val (_cachedTokens, _filteredTokens) = cacheTokens(lexer)

        cachedTokens = _cachedTokens
        filteredTokens = _filteredTokens
        originalText = lexer.originalText

        verify()
    }

    public fun getRawCharAt(index: Int): Char {
        if (index < 0) return 0.toChar()
        if (index >= originalText.length()) return 0.toChar()
        return originalText.charAt(index)
    }

    private fun verify() {
        for (i in cachedTokens.indices) {
            assert(cachedTokens.get(i).rawIndex == i)
        }
        for (i in filteredTokens.indices) {
            assert(filteredTokens.get(i).normIndex == i)
        }
    }

    private fun getIndexForIterator(indices: List<Int>, startIndex: Int): Int {
        if (startIndex < 0) {
            return -1
        }
        if (startIndex >= indices.size()) {
            return filteredTokens.size()
        }
        return indices.get(startIndex)
    }


    public inner class ListIterator(private val indices: List<Int>, private val listIndex: Int) : Iterator(getIndexForIterator(indices, listIndex)) {

        override fun advance(): Iterator {
            return ListIterator(indices, listIndex + 1)
        }

        override fun rollback(): Iterator {
            return ListIterator(indices, listIndex - 1)
        }

        override fun rawLookup(steps: Int): IElementType? {
            if (steps > 0 && advance().index != super.advance().index
                    || steps < 0 && rollback().index != super.rollback().index) {
                return null
            }

            return super.rawLookup(steps)
        }
    }

    public inner open class Iterator(public val index: Int) {

        public val type : IElementType?
            get() {
                return info(0).type
            }

        public val text: String
            get() {
                return originalText.subSequence(info(0).tokenStart, info(0).tokenEnd).toString()
            }

        public val start: Int
            get() {
                return info(0).tokenStart
            }

        public val end: Int
            get() {
                return info(0).tokenEnd
            }

        public open fun advance(): Iterator {
            return Iterator(index + 1)
        }

        public open fun rollback(): Iterator {
            return Iterator(index - 1)
        }

        private fun info(rawSteps: Int): TokenInfo {
            if (index < 0) {
                return TokenInfo(null, 0, 0, 0, 0)
            } else if (index >= filteredTokens.size()) {
                return TokenInfo(null, originalText.length(), 0, 0, 0)
            }

            val rawIndex = filteredTokens.get(index).rawIndex + rawSteps
            if (rawIndex < 0) {
                return TokenInfo(null, 0, 0, 0, 0)
            } else if (rawIndex >= cachedTokens.size()) {
                return TokenInfo(null, originalText.length(), 0, 0, 0)
            }

            return cachedTokens.get(rawIndex)
        }


        public open fun rawLookup(steps: Int): IElementType? {
            return info(steps).`type`
        }

        public fun rawStart(steps: Int): Int {
            return info(steps).tokenStart
        }

        public fun rawText(steps: Int): String? {
            val info = info(steps)
            if (info.`type` == null) {
                return null
            }
            return originalText.subSequence(info.tokenStart, info.tokenEnd).toString()
        }

        override fun toString(): String {
            return "Iterator: " + index + ": " + type
        }
    }

    private class TokenInfo(val `type`: IElementType?,
                            val tokenStart: Int,
                            val tokenEnd: Int,
                            val rawIndex: Int,
                            var normIndex: Int) {

        override fun toString(): String {
            return "TokenInfo: " + `type`.toString() + " [" + tokenStart + ", " + tokenEnd + ")"
        }
    }

    class object {
        private fun isWhitespace(elementType: IElementType?): Boolean {
            return elementType == MarkdownTokenTypes.WHITE_SPACE
        }

        data class ResultOfCaching(val cachedTokens : List<TokenInfo>, val filteredTokens : List<TokenInfo>)
        private fun cacheTokens(lexer: MarkdownLexer) : ResultOfCaching {
            val cachedTokens = ArrayList<TokenInfo>()
            val filteredTokens = ArrayList<TokenInfo>()

            while (lexer.type != null) {
                val info = TokenInfo(lexer.type!!, lexer.tokenStart, lexer.tokenEnd, cachedTokens.size(), -1)
                cachedTokens.add(info)

                if (!isWhitespace(info.type)) {
                    info.normIndex = filteredTokens.size()
                    filteredTokens.add(info)
                }

                lexer.advance()
            }

            return ResultOfCaching(cachedTokens, filteredTokens)
        }
    }
}
