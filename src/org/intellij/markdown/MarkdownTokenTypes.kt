package org.intellij.markdown

public open class MarkdownTokenTypes {
    companion object {

        public val TEXT: IElementType = MarkdownElementType("TEXT", true)

        public val CODE: IElementType = MarkdownElementType("CODE", true)

        public val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE", true)

        public val HTML_BLOCK_CONTENT: IElementType = MarkdownElementType("HTML_BLOCK_CONTENT", true)

        public val SINGLE_QUOTE: IElementType = MarkdownElementType("'", true)
        public val DOUBLE_QUOTE: IElementType = MarkdownElementType("\"", true)
        public val LPAREN: IElementType = MarkdownElementType("(", true)
        public val RPAREN: IElementType = MarkdownElementType(")", true)
        public val LBRACKET: IElementType = MarkdownElementType("[", true)
        public val RBRACKET: IElementType = MarkdownElementType("]", true)
        public val LT: IElementType = MarkdownElementType("<", true)
        public val GT: IElementType = MarkdownElementType(">", true)

        public val COLON: IElementType = MarkdownElementType(":", true)
        public val EXCLAMATION_MARK: IElementType = MarkdownElementType("!", true)


        public val HARD_LINE_BREAK: IElementType = MarkdownElementType("BR", true)
        public val EOL: IElementType = MarkdownElementType("EOL", true)

        public val LINK_ID: IElementType = MarkdownElementType("LINK_ID", true)
        public val ATX_HEADER: IElementType = MarkdownElementType("ATX_HEADER", true)
        public val ATX_CONTENT: IElementType = MarkdownElementType("ATX_CONTENT", true)
        public val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1", true)
        public val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2", true)
        public val SETEXT_CONTENT: IElementType = MarkdownElementType("SETEXT_CONTENT", true)
        public val EMPH: IElementType = MarkdownElementType("EMPH", true)

        public val BACKTICK: IElementType = MarkdownElementType("BACKTICK", true)
        public val ESCAPED_BACKTICKS: IElementType = MarkdownElementType("ESCAPED_BACKTICKS", true)

        public val LIST_BULLET: IElementType = MarkdownElementType("LIST_BULLET", true)
        public val URL: IElementType = MarkdownElementType("URL", true)
        public val HORIZONTAL_RULE: IElementType = MarkdownElementType("HORIZONTAL_RULE", true)
        public val LIST_NUMBER: IElementType = MarkdownElementType("LIST_NUMBER", true)
        public val FENCE_LANG: IElementType = MarkdownElementType("FENCE_LANG", true)
        public val CODE_FENCE_START: IElementType = MarkdownElementType("CODE_FENCE_START", true)
        public val CODE_FENCE_CONTENT: IElementType = MarkdownElementType("CODE_FENCE_CONTENT", true)
        public val CODE_FENCE_END: IElementType = MarkdownElementType("CODE_FENCE_END", true)
        public val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE", true)

        public val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK", true)
        public val EMAIL_AUTOLINK: IElementType = MarkdownElementType("EMAIL_AUTOLINK", true)
        public val HTML_TAG: IElementType = MarkdownElementType("HTML_TAG", true)

        public val BAD_CHARACTER: IElementType = MarkdownElementType("BAD_CHARACTER", true)
        public val WHITE_SPACE: IElementType = object : MarkdownElementType("WHITE_SPACE", true) {
            override fun toString(): String {
                return "WHITE_SPACE";
            }
        }

    }
}
