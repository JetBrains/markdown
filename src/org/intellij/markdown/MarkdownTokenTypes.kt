package org.intellij.markdown

public open class MarkdownTokenTypes {
    companion object {
        @JvmField
        public val TEXT: IElementType = MarkdownElementType("TEXT", true)

        @JvmField
        public val CODE_LINE: IElementType = MarkdownElementType("CODE_LINE", true)

        @JvmField
        public val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE", true)

        @JvmField
        public val HTML_BLOCK_CONTENT: IElementType = MarkdownElementType("HTML_BLOCK_CONTENT", true)

        @JvmField
        public val SINGLE_QUOTE: IElementType = MarkdownElementType("'", true)

        @JvmField
        public val DOUBLE_QUOTE: IElementType = MarkdownElementType("\"", true)

        @JvmField
        public val LPAREN: IElementType = MarkdownElementType("(", true)

        @JvmField
        public val RPAREN: IElementType = MarkdownElementType(")", true)

        @JvmField
        public val LBRACKET: IElementType = MarkdownElementType("[", true)

        @JvmField
        public val RBRACKET: IElementType = MarkdownElementType("]", true)

        @JvmField
        public val LT: IElementType = MarkdownElementType("<", true)

        @JvmField
        public val GT: IElementType = MarkdownElementType(">", true)

        @JvmField
        public val COLON: IElementType = MarkdownElementType(":", true)

        @JvmField
        public val EXCLAMATION_MARK: IElementType = MarkdownElementType("!", true)

        @JvmField
        public val HARD_LINE_BREAK: IElementType = MarkdownElementType("BR", true)

        @JvmField
        public val EOL: IElementType = MarkdownElementType("EOL", true)

        @JvmField
        public val LINK_ID: IElementType = MarkdownElementType("LINK_ID", true)

        @JvmField
        public val ATX_HEADER: IElementType = MarkdownElementType("ATX_HEADER", true)

        @JvmField
        public val ATX_CONTENT: IElementType = MarkdownElementType("ATX_CONTENT", true)

        @JvmField
        public val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1", true)

        @JvmField
        public val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2", true)

        @JvmField
        public val SETEXT_CONTENT: IElementType = MarkdownElementType("SETEXT_CONTENT", true)

        @JvmField
        public val EMPH: IElementType = MarkdownElementType("EMPH", true)

        @JvmField
        public val BACKTICK: IElementType = MarkdownElementType("BACKTICK", true)

        @JvmField
        public val ESCAPED_BACKTICKS: IElementType = MarkdownElementType("ESCAPED_BACKTICKS", true)

        @JvmField
        public val LIST_BULLET: IElementType = MarkdownElementType("LIST_BULLET", true)

        @JvmField
        public val URL: IElementType = MarkdownElementType("URL", true)

        @JvmField
        public val HORIZONTAL_RULE: IElementType = MarkdownElementType("HORIZONTAL_RULE", true)

        @JvmField
        public val LIST_NUMBER: IElementType = MarkdownElementType("LIST_NUMBER", true)

        @JvmField
        public val FENCE_LANG: IElementType = MarkdownElementType("FENCE_LANG", true)

        @JvmField
        public val CODE_FENCE_START: IElementType = MarkdownElementType("CODE_FENCE_START", true)

        @JvmField
        public val CODE_FENCE_CONTENT: IElementType = MarkdownElementType("CODE_FENCE_CONTENT", true)

        @JvmField
        public val CODE_FENCE_END: IElementType = MarkdownElementType("CODE_FENCE_END", true)

        @JvmField
        public val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE", true)

        @JvmField
        public val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK", true)

        @JvmField
        public val EMAIL_AUTOLINK: IElementType = MarkdownElementType("EMAIL_AUTOLINK", true)

        @JvmField
        public val HTML_TAG: IElementType = MarkdownElementType("HTML_TAG", true)

        @JvmField
        public val BAD_CHARACTER: IElementType = MarkdownElementType("BAD_CHARACTER", true)

        @JvmField
        public val WHITE_SPACE: IElementType = object : MarkdownElementType("WHITE_SPACE", true) {
            override fun toString(): String {
                return "WHITE_SPACE";
            }
        }

    }
}
