package org.intellij.markdown.parser.sequentialparsers

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.lexer.MarkdownLexer
import java.util.*

class LexerBasedTokensCache(lexer: MarkdownLexer) : TokensCache() {
    override val cachedTokens: List<TokensCache.TokenInfo>
    override val filteredTokens: List<TokensCache.TokenInfo>
    override val originalText: CharSequence
    override val originalTextRange: IntRange

    init {
        val (_cachedTokens, _filteredTokens) = cacheTokens(lexer)

        cachedTokens = _cachedTokens
        filteredTokens = _filteredTokens
        originalText = lexer.originalText
        originalTextRange = lexer.bufferStart..lexer.bufferEnd - 1

        verify()
    }

    companion object {
        private fun isWhitespace(elementType: IElementType?): Boolean {
            return elementType == MarkdownTokenTypes.WHITE_SPACE
        }

        data class ResultOfCaching(val cachedTokens : List<TokensCache.TokenInfo>, val filteredTokens : List<TokensCache.TokenInfo>)
        private fun cacheTokens(lexer: MarkdownLexer) : ResultOfCaching {
            val cachedTokens = ArrayList<TokensCache.TokenInfo>()
            val filteredTokens = ArrayList<TokensCache.TokenInfo>()

            while (lexer.type != null) {
                val info = TokensCache.TokenInfo(lexer.type!!, lexer.tokenStart, lexer.tokenEnd, cachedTokens.size, -1)
                cachedTokens.add(info)

                if (!isWhitespace(info.type)) {
                    info.normIndex = filteredTokens.size
                    filteredTokens.add(info)
                }

                lexer.advance()
            }

            return ResultOfCaching(cachedTokens, filteredTokens)
        }
    }
}
