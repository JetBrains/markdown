package org.intellij.markdown

import kotlin.jvm.JvmField

open class MarkdownTokenTypes {
    companion object {
        @JvmField
        val TEXT: IElementType = MarkdownElementType("TEXT", true)

        @JvmField
        val CODE_LINE: IElementType = MarkdownElementType("CODE_LINE", true)

        @JvmField
        val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE", true)

        @JvmField
        val HTML_BLOCK_CONTENT: IElementType = MarkdownElementType("HTML_BLOCK_CONTENT", true)

        @JvmField
        val SINGLE_QUOTE: IElementType = MarkdownElementType("'", true)

        @JvmField
        val DOUBLE_QUOTE: IElementType = MarkdownElementType("\"", true)

        @JvmField
        val LPAREN: IElementType = MarkdownElementType("(", true)

        @JvmField
        val RPAREN: IElementType = MarkdownElementType(")", true)

        @JvmField
        val LBRACKET: IElementType = MarkdownElementType("[", true)

        @JvmField
        val RBRACKET: IElementType = MarkdownElementType("]", true)

        @JvmField
        val LT: IElementType = MarkdownElementType("<", true)

        @JvmField
        val GT: IElementType = MarkdownElementType(">", true)

        @JvmField
        val COLON: IElementType = MarkdownElementType(":", true)

        @JvmField
        val EXCLAMATION_MARK: IElementType = MarkdownElementType("!", true)

        @JvmField
        val HARD_LINE_BREAK: IElementType = MarkdownElementType("BR", true)

        @JvmField
        val EOL: IElementType = MarkdownElementType("EOL", true)

        @JvmField
        val LINK_ID: IElementType = MarkdownElementType("LINK_ID", true)

        @JvmField
        val ATX_HEADER: IElementType = MarkdownElementType("ATX_HEADER", true)

        @JvmField
        val ATX_CONTENT: IElementType = MarkdownElementType("ATX_CONTENT", true)

        @JvmField
        val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1", true)

        @JvmField
        val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2", true)

        @JvmField
        val SETEXT_CONTENT: IElementType = MarkdownElementType("SETEXT_CONTENT", true)

        @JvmField
        val EMPH: IElementType = MarkdownElementType("EMPH", true)

        @JvmField
        val BACKTICK: IElementType = MarkdownElementType("BACKTICK", true)

        @JvmField
        val ESCAPED_BACKTICKS: IElementType = MarkdownElementType("ESCAPED_BACKTICKS", true)

        @JvmField
        val LIST_BULLET: IElementType = MarkdownElementType("LIST_BULLET", true)

        @JvmField
        val URL: IElementType = MarkdownElementType("URL", true)

        @JvmField
        val HORIZONTAL_RULE: IElementType = MarkdownElementType("HORIZONTAL_RULE", true)

        @JvmField
        val LIST_NUMBER: IElementType = MarkdownElementType("LIST_NUMBER", true)

        @JvmField
        val FENCE_LANG: IElementType = MarkdownElementType("FENCE_LANG", true)

        @JvmField
        val CODE_FENCE_START: IElementType = MarkdownElementType("CODE_FENCE_START", true)

        @JvmField
        val CODE_FENCE_CONTENT: IElementType = MarkdownElementType("CODE_FENCE_CONTENT", true)

        @JvmField
        val CODE_FENCE_END: IElementType = MarkdownElementType("CODE_FENCE_END", true)

        @JvmField
        val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE", true)

        @JvmField
        val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK", true)

        @JvmField
        val EMAIL_AUTOLINK: IElementType = MarkdownElementType("EMAIL_AUTOLINK", true)

        @JvmField
        val HTML_TAG: IElementType = MarkdownElementType("HTML_TAG", true)

        @JvmField
        val BAD_CHARACTER: IElementType = MarkdownElementType("BAD_CHARACTER", true)

        @JvmField
        val WHITE_SPACE: IElementType = object : MarkdownElementType("WHITE_SPACE", true) {
            override fun toString(): String {
                return "WHITE_SPACE"
            }
        }
    }
}
