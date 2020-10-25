package org.intellij.markdown.lexer

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes

open class MarkdownLexer(private val baseLexer: GeneratedLexer) {

    var type: IElementType? = null
        private set
    private var nextType: IElementType? = null

    var originalText: CharSequence = ""
        private set
    var bufferStart: Int = 0
        private set
    var bufferEnd: Int = 0
        private set

    var tokenStart: Int = 0
        private set
    var tokenEnd: Int = 0
        private set

    fun start(originalText: CharSequence,
                     bufferStart: Int = 0,
                     bufferEnd: Int = originalText.length) {
        this.originalText = originalText
        this.bufferStart = bufferStart
        this.bufferEnd = bufferEnd

        baseLexer.reset(originalText, bufferStart, bufferEnd, 0)
        type = advanceBase()
        tokenStart = baseLexer.tokenStart

        calcNextType()
    }

    fun advance(): Boolean {
        return locateToken()
    }

    private fun locateToken(): Boolean {
        `type` = nextType
        tokenStart = tokenEnd
        if (`type` == null) {
            return false
        }

        calcNextType()
        return true
    }

    private fun calcNextType() {
        do {
            tokenEnd = baseLexer.tokenEnd
            nextType = advanceBase()
        } while (type.let { nextType == it && it != null && it in TOKENS_TO_MERGE })
    }

    private fun advanceBase(): IElementType? {
        try {
            return baseLexer.advance()
        } catch (e: Exception) {
            throw AssertionError("This could not be!")
        }

    }

    companion object {
        private val TOKENS_TO_MERGE = setOf(
                MarkdownTokenTypes.TEXT,
                MarkdownTokenTypes.WHITE_SPACE,
                MarkdownTokenTypes.CODE_LINE,
                MarkdownTokenTypes.LINK_ID,
                MarkdownTokenTypes.LINK_TITLE,
                MarkdownTokenTypes.URL,
                MarkdownTokenTypes.AUTOLINK,
                MarkdownTokenTypes.EMAIL_AUTOLINK,
                MarkdownTokenTypes.BAD_CHARACTER)
    }
}
