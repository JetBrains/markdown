package org.intellij.markdown.lexer

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import java.io.IOException

public open class MarkdownLexer(private val baseLexer: GeneratedLexer) {

    public var type: IElementType? = null
        private set
    private var nextType: IElementType? = null

    public var originalText: CharSequence = ""
        private set
    public var bufferStart: Int = 0
        private set
    public var bufferEnd: Int = 0
        private set

    public var tokenStart: Int = 0
        private set
    public var tokenEnd: Int = 0
        private set

    public fun start(originalText: CharSequence,
                     bufferStart: Int = 0,
                     bufferEnd: Int = originalText.length()) {
        this.originalText = originalText
        this.bufferStart = bufferStart
        this.bufferEnd = bufferEnd

        baseLexer.reset(originalText, bufferStart, bufferEnd, 0)
        type = advanceBase()
        tokenStart = baseLexer.getTokenStart()

        calcNextType()
    }

    public fun advance(): Boolean {
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
            tokenEnd = baseLexer.getTokenEnd()
            nextType = advanceBase()
        } while (nextType == `type` && TOKENS_TO_MERGE.contains(`type`))
    }

    private fun advanceBase(): IElementType? {
        try {
            return baseLexer.advance()
        } catch (e: IOException) {
            e.printStackTrace()
            throw AssertionError("This could not be!")
        }

    }

    companion object {
        private val TOKENS_TO_MERGE = setOf(
                MarkdownTokenTypes.TEXT,
                MarkdownTokenTypes.WHITE_SPACE,
                MarkdownTokenTypes.CODE,
                MarkdownTokenTypes.LINK_ID,
                MarkdownTokenTypes.LINK_TITLE,
                MarkdownTokenTypes.URL,
                MarkdownTokenTypes.AUTOLINK,
                MarkdownTokenTypes.EMAIL_AUTOLINK,
                MarkdownTokenTypes.BAD_CHARACTER)
    }
}
