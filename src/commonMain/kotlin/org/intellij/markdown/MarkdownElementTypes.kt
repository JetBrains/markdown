package org.intellij.markdown

object MarkdownElementTypes {
    val MARKDOWN_FILE: IElementType = MarkdownElementType("MARKDOWN_FILE")

    val UNORDERED_LIST: IElementType = MarkdownElementType("UNORDERED_LIST")

    val ORDERED_LIST: IElementType = MarkdownElementType("ORDERED_LIST")

    val LIST_ITEM: IElementType = MarkdownElementType("LIST_ITEM")

    val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE")

    val CODE_FENCE: IElementType = MarkdownElementType("CODE_FENCE")

    val CODE_BLOCK: IElementType = MarkdownElementType("CODE_BLOCK")

    val CODE_SPAN: IElementType = MarkdownElementType("CODE_SPAN")

    val HTML_BLOCK: IElementType = MarkdownElementType("HTML_BLOCK")

    val PARAGRAPH: IElementType = MarkdownElementType("PARAGRAPH", true)

    val EMPH: IElementType = MarkdownElementType("EMPH")

    val STRONG: IElementType = MarkdownElementType("STRONG")

    val LINK_DEFINITION: IElementType = MarkdownElementType("LINK_DEFINITION")
    val LINK_LABEL: IElementType = MarkdownElementType("LINK_LABEL", true)
    val LINK_DESTINATION: IElementType = MarkdownElementType("LINK_DESTINATION", true)
    val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE", true)
    val LINK_TEXT: IElementType = MarkdownElementType("LINK_TEXT", true)
    val INLINE_LINK: IElementType = MarkdownElementType("INLINE_LINK")
    val FULL_REFERENCE_LINK: IElementType = MarkdownElementType("FULL_REFERENCE_LINK")
    val SHORT_REFERENCE_LINK: IElementType = MarkdownElementType("SHORT_REFERENCE_LINK")
    val IMAGE: IElementType = MarkdownElementType("IMAGE")

    val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK")

    val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1")
    val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2")

    val ATX_1: IElementType = MarkdownElementType("ATX_1")
    val ATX_2: IElementType = MarkdownElementType("ATX_2")
    val ATX_3: IElementType = MarkdownElementType("ATX_3")
    val ATX_4: IElementType = MarkdownElementType("ATX_4")
    val ATX_5: IElementType = MarkdownElementType("ATX_5")
    val ATX_6: IElementType = MarkdownElementType("ATX_6")
}
