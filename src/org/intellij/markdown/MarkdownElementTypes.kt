package org.intellij.markdown

public object MarkdownElementTypes {
    @JvmField
    public val MARKDOWN_FILE: IElementType = MarkdownElementType("MARKDOWN_FILE")

    @JvmField
    public val UNORDERED_LIST: IElementType = MarkdownElementType("UNORDERED_LIST")

    @JvmField
    public val ORDERED_LIST: IElementType = MarkdownElementType("ORDERED_LIST")

    @JvmField
    public val LIST_ITEM: IElementType = MarkdownElementType("LIST_ITEM")

    @JvmField
    public val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE")

    @JvmField
    public val CODE_FENCE: IElementType = MarkdownElementType("CODE_FENCE")

    @JvmField
    public val CODE_BLOCK: IElementType = MarkdownElementType("CODE_BLOCK")

    @JvmField
    public val CODE_SPAN: IElementType = MarkdownElementType("CODE_SPAN")

    @JvmField
    public val HTML_BLOCK: IElementType = MarkdownElementType("HTML_BLOCK")

    @JvmField
    public val PARAGRAPH: IElementType = MarkdownElementType("PARAGRAPH", true)

    @JvmField
    public val EMPH: IElementType = MarkdownElementType("EMPH")

    @JvmField
    public val STRONG: IElementType = MarkdownElementType("STRONG")

    @JvmField
    public val LINK_DEFINITION: IElementType = MarkdownElementType("LINK_DEFINITION")
    @JvmField
    public val LINK_LABEL: IElementType = MarkdownElementType("LINK_LABEL", true)
    @JvmField
    public val LINK_DESTINATION: IElementType = MarkdownElementType("LINK_DESTINATION", true)
    @JvmField
    public val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE", true)
    @JvmField
    public val LINK_TEXT: IElementType = MarkdownElementType("LINK_TEXT", true)
    @JvmField
    public val INLINE_LINK: IElementType = MarkdownElementType("INLINE_LINK")
    @JvmField
    public val FULL_REFERENCE_LINK: IElementType = MarkdownElementType("FULL_REFERENCE_LINK")
    @JvmField
    public val SHORT_REFERENCE_LINK: IElementType = MarkdownElementType("SHORT_REFERENCE_LINK")
    @JvmField
    public val IMAGE: IElementType = MarkdownElementType("IMAGE")

    @JvmField
    public val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK")

    @JvmField
    public val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1")
    @JvmField
    public val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2")

    @JvmField
    public val ATX_1: IElementType = MarkdownElementType("ATX_1")
    @JvmField
    public val ATX_2: IElementType = MarkdownElementType("ATX_2")
    @JvmField
    public val ATX_3: IElementType = MarkdownElementType("ATX_3")
    @JvmField
    public val ATX_4: IElementType = MarkdownElementType("ATX_4")
    @JvmField
    public val ATX_5: IElementType = MarkdownElementType("ATX_5")
    @JvmField
    public val ATX_6: IElementType = MarkdownElementType("ATX_6")
}
