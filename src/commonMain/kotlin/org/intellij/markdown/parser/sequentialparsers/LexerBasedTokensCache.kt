package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.lexer.TokenInfo

class LexerBasedTokensCache(lexer: MarkdownLexer) : TokensCache() {
    override val cachedTokens: List<TokenInfo>
    override val filteredTokens: List<TokenInfo>
    override val originalText: CharSequence
    override val originalTextRange: IntRange

    init {
        val (_cachedTokens, _filteredTokens) = cacheTokens(lexer)

        cachedTokens = _cachedTokens
        filteredTokens = _filteredTokens
        originalText = lexer.originalText
        originalTextRange = lexer.bufferStart until lexer.bufferEnd

        verify()
    }

    companion object {
        private fun isWhitespace(elementType: IElementType?): Boolean {
            return elementType == MarkdownTokenTypes.WHITE_SPACE
        }

        data class ResultOfCaching(val cachedTokens: List<TokenInfo>, val filteredTokens: List<TokenInfo>)

        private fun cacheTokens(lexer: MarkdownLexer): ResultOfCaching {
            val cachedTokens = ArrayList<TokenInfo>()
            val filteredTokens = ArrayList<TokenInfo>()

            while (lexer.type != null) {
                val isWhitespace = isWhitespace(lexer.type)
                val info = TokenInfo(
                    type = lexer.type,
                    tokenStart = lexer.tokenStart,
                    tokenEnd = lexer.tokenEnd,
                    rawIndex = cachedTokens.size,
                    normIndex = if (isWhitespace) -1 else filteredTokens.size
                )

                cachedTokens.add(info)
                if (!isWhitespace) {
                    filteredTokens.add(info)
                }

                lexer.advance()
            }

            return ResultOfCaching(cachedTokens, filteredTokens)
        }
    }
}
