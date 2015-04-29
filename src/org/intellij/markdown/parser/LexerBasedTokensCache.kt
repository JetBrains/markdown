package org.intellij.markdown.parser

import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import java.util.ArrayList

public class LexerBasedTokensCache(lexer: MarkdownLexer) : TokensCache() {
    override val cachedTokens: List<TokensCache.TokenInfo>
    override val filteredTokens: List<TokensCache.TokenInfo>
    override val originalText: CharSequence

    init {
        val (_cachedTokens, _filteredTokens) = cacheTokens(lexer)

        cachedTokens = _cachedTokens
        filteredTokens = _filteredTokens
        originalText = lexer.originalText

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
                val info = TokensCache.TokenInfo(lexer.type!!, lexer.tokenStart, lexer.tokenEnd, cachedTokens.size(), -1)
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
