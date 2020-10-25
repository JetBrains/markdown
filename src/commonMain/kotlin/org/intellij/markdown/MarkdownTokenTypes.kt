package org.intellij.markdown

open class MarkdownTokenTypes {
    companion object {
        val TEXT: IElementType = MarkdownElementType("TEXT", true)

        val CODE_LINE: IElementType = MarkdownElementType("CODE_LINE", true)

        val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE", true)

        val HTML_BLOCK_CONTENT: IElementType = MarkdownElementType("HTML_BLOCK_CONTENT", true)

        val SINGLE_QUOTE: IElementType = MarkdownElementType("'", true)

        val DOUBLE_QUOTE: IElementType = MarkdownElementType("\"", true)

        val LPAREN: IElementType = MarkdownElementType("(", true)

        val RPAREN: IElementType = MarkdownElementType(")", true)

        val LBRACKET: IElementType = MarkdownElementType("[", true)

        val RBRACKET: IElementType = MarkdownElementType("]", true)

        val LT: IElementType = MarkdownElementType("<", true)

        val GT: IElementType = MarkdownElementType(">", true)

        val COLON: IElementType = MarkdownElementType(":", true)

        val EXCLAMATION_MARK: IElementType = MarkdownElementType("!", true)

        val HARD_LINE_BREAK: IElementType = MarkdownElementType("BR", true)

        val EOL: IElementType = MarkdownElementType("EOL", true)

        val LINK_ID: IElementType = MarkdownElementType("LINK_ID", true)

        val ATX_HEADER: IElementType = MarkdownElementType("ATX_HEADER", true)

        val ATX_CONTENT: IElementType = MarkdownElementType("ATX_CONTENT", true)

        val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1", true)

        val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2", true)

        val SETEXT_CONTENT: IElementType = MarkdownElementType("SETEXT_CONTENT", true)

        val EMPH: IElementType = MarkdownElementType("EMPH", true)

        val BACKTICK: IElementType = MarkdownElementType("BACKTICK", true)

        val ESCAPED_BACKTICKS: IElementType = MarkdownElementType("ESCAPED_BACKTICKS", true)

        val LIST_BULLET: IElementType = MarkdownElementType("LIST_BULLET", true)

        val URL: IElementType = MarkdownElementType("URL", true)

        val HORIZONTAL_RULE: IElementType = MarkdownElementType("HORIZONTAL_RULE", true)

        val LIST_NUMBER: IElementType = MarkdownElementType("LIST_NUMBER", true)

        val FENCE_LANG: IElementType = MarkdownElementType("FENCE_LANG", true)

        val CODE_FENCE_START: IElementType = MarkdownElementType("CODE_FENCE_START", true)

        val CODE_FENCE_CONTENT: IElementType = MarkdownElementType("CODE_FENCE_CONTENT", true)

        val CODE_FENCE_END: IElementType = MarkdownElementType("CODE_FENCE_END", true)

        val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE", true)

        val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK", true)

        val EMAIL_AUTOLINK: IElementType = MarkdownElementType("EMAIL_AUTOLINK", true)

        val HTML_TAG: IElementType = MarkdownElementType("HTML_TAG", true)

        val BAD_CHARACTER: IElementType = MarkdownElementType("BAD_CHARACTER", true)

        val WHITE_SPACE: IElementType = object : MarkdownElementType("WHITE_SPACE", true) {
            override fun toString(): String {
                return "WHITE_SPACE"
            }
        }

    }
}
