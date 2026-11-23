package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.lexer.GeneratedLexer
import org.intellij.markdown.lexer.MarkdownLexer
import kotlin.test.Test

class MarkdownParserInlineStartStateTest {
    @Test
    fun customFlavourLexerMayUseItsOwnStateNumbering() {
        class CustomLexer : GeneratedLexer {
            override var state: Int = 0
            override var tokenStart: Int = 0
            override var tokenEnd: Int = 0

            override fun reset(buffer: CharSequence, start: Int, end: Int, initialState: Int) {
                check(initialState == 0) { "Unexpected lexer state: $initialState" }
                tokenStart = start
                tokenEnd = end
            }

            override fun advance(): IElementType? = null
        }

        val flavour = object : CommonMarkFlavourDescriptor() {
            override fun createInlinesLexer(): MarkdownLexer = MarkdownLexer(CustomLexer())
        }

        MarkdownParser(flavour).buildMarkdownTreeFromString("# > a\n")
    }
}
