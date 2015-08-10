package org.intellij.markdown

public interface MarkdownElementTypes {
    companion object {
        public val MARKDOWN_FILE: IElementType = MarkdownElementType("MARKDOWN_FILE")

        public val UNORDERED_LIST: IElementType = MarkdownElementType("UNORDERED_LIST")

        public val ORDERED_LIST: IElementType = MarkdownElementType("ORDERED_LIST")

        public val LIST_ITEM: IElementType = MarkdownElementType("LIST_ITEM")

        public val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE")

        public val CODE_FENCE: IElementType = MarkdownElementType("CODE_FENCE")

        public val CODE_BLOCK: IElementType = MarkdownElementType("CODE_BLOCK", true)

        public val CODE_SPAN: IElementType = MarkdownElementType("CODE_SPAN")

        public val PARAGRAPH: IElementType = MarkdownElementType("PARAGRAPH", true)

        public val EMPH: IElementType = MarkdownElementType("EMPH")

        public val STRONG: IElementType = MarkdownElementType("STRONG")

        public val LINK_DEFINITION: IElementType = MarkdownElementType("LINK_DEFINITION")
        public val LINK_LABEL: IElementType = MarkdownElementType("LINK_LABEL")
        public val LINK_DESTINATION: IElementType = MarkdownElementType("LINK_DESTINATION")
        public val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE")
        public val LINK_TEXT: IElementType = MarkdownElementType("LINK_TEXT")
        public val INLINE_LINK: IElementType = MarkdownElementType("INLINE_LINK")
        public val FULL_REFERENCE_LINK: IElementType = MarkdownElementType("FULL_REFERENCE_LINK")
        public val SHORT_REFERENCE_LINK: IElementType = MarkdownElementType("SHORT_REFERENCE_LINK")

        public val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK")

        public val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1", true)
        public val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2", true)

        public val ATX_1: IElementType = MarkdownElementType("ATX_1", true)
        public val ATX_2: IElementType = MarkdownElementType("ATX_2", true)
        public val ATX_3: IElementType = MarkdownElementType("ATX_3", true)
        public val ATX_4: IElementType = MarkdownElementType("ATX_4", true)
        public val ATX_5: IElementType = MarkdownElementType("ATX_5", true)
        public val ATX_6: IElementType = MarkdownElementType("ATX_6", true)
    }
}
