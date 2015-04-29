package org.intellij.markdown

public trait MarkdownTokenTypes {
    companion object {

        public val TEXT: IElementType = MarkdownElementType("TEXT")

        public val CODE: IElementType = MarkdownElementType("CODE")

        public val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE")

        public val HTML_BLOCK: IElementType = MarkdownElementType("HTML_BLOCK")

        public val SINGLE_QUOTE: IElementType = MarkdownElementType("'")
        public val DOUBLE_QUOTE: IElementType = MarkdownElementType("\"")
        public val LPAREN: IElementType = MarkdownElementType("(")
        public val RPAREN: IElementType = MarkdownElementType(")")
        public val LBRACKET: IElementType = MarkdownElementType("[")
        public val RBRACKET: IElementType = MarkdownElementType("]")
        public val LT: IElementType = MarkdownElementType("<")
        public val GT: IElementType = MarkdownElementType(">")

        public val COLON: IElementType = MarkdownElementType(":")
        public val EXCLAMATION_MARK: IElementType = MarkdownElementType("!")


        public val HTML_ENTITY: IElementType = MarkdownElementType("HTML_ENTITY")

        public val HARD_LINE_BREAK: IElementType = MarkdownElementType("BR")
        public val EOL: IElementType = MarkdownElementType("EOL")

        public val LINK_ID: IElementType = MarkdownElementType("LINK_ID")
        public val ATX_HEADER: IElementType = MarkdownElementType("ATX_HEADER")
        public val EMPH: IElementType = MarkdownElementType("EMPH")

        public val BACKTICK: IElementType = MarkdownElementType("BACKTICK")
        public val ESCAPED_BACKTICKS: IElementType = MarkdownElementType("ESCAPED_BACKTICKS")

        public val TAG_NAME: IElementType = MarkdownElementType("TAG_NAME")
        public val LIST_BULLET: IElementType = MarkdownElementType("LIST_BULLET")
        public val URL: IElementType = MarkdownElementType("URL")
        public val HORIZONTAL_RULE: IElementType = MarkdownElementType("HORIZONTAL_RULE")
        public val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1")
        public val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2")
        public val LIST_NUMBER: IElementType = MarkdownElementType("LIST_NUMBER")
        public val FENCE_LANG: IElementType = MarkdownElementType("FENCE_LANG")
        public val CODE_FENCE_START: IElementType = MarkdownElementType("CODE_FENCE_START")
        public val CODE_FENCE_END: IElementType = MarkdownElementType("CODE_FENCE_END")
        public val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE")

        public val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK")
        public val EMAIL_AUTOLINK: IElementType = MarkdownElementType("EMAIL_AUTOLINK")
        public val HTML_TAG: IElementType = MarkdownElementType("HTML_TAG")

        public val BAD_CHARACTER: IElementType = IElementType("BAD_CHARACTER")
        public val WHITE_SPACE: IElementType = IElementType("WHITE_SPACE")
    }
}
