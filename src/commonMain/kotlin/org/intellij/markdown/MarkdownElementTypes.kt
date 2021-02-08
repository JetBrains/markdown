package org.intellij.markdown

import kotlin.jvm.JvmField

object MarkdownElementTypes {
    @JvmField
    val MARKDOWN_FILE: IElementType = MarkdownElementType("MARKDOWN_FILE")
    @JvmField
    val UNORDERED_LIST: IElementType = MarkdownElementType("UNORDERED_LIST")
    @JvmField
    val ORDERED_LIST: IElementType = MarkdownElementType("ORDERED_LIST")
    @JvmField
    val LIST_ITEM: IElementType = MarkdownElementType("LIST_ITEM")
    @JvmField
    val BLOCK_QUOTE: IElementType = MarkdownElementType("BLOCK_QUOTE")
    @JvmField
    val CODE_FENCE: IElementType = MarkdownElementType("CODE_FENCE")
    @JvmField
    val CODE_BLOCK: IElementType = MarkdownElementType("CODE_BLOCK")
    @JvmField
    val CODE_SPAN: IElementType = MarkdownElementType("CODE_SPAN")
    @JvmField
    val HTML_BLOCK: IElementType = MarkdownElementType("HTML_BLOCK")
    @JvmField
    val PARAGRAPH: IElementType = MarkdownElementType("PARAGRAPH", true)
    @JvmField
    val EMPH: IElementType = MarkdownElementType("EMPH")
    @JvmField
    val STRONG: IElementType = MarkdownElementType("STRONG")

    @JvmField
    val LINK_DEFINITION: IElementType = MarkdownElementType("LINK_DEFINITION")
    @JvmField
    val LINK_LABEL: IElementType = MarkdownElementType("LINK_LABEL", true)
    @JvmField
    val LINK_DESTINATION: IElementType = MarkdownElementType("LINK_DESTINATION", true)
    @JvmField
    val LINK_TITLE: IElementType = MarkdownElementType("LINK_TITLE", true)
    @JvmField
    val LINK_TEXT: IElementType = MarkdownElementType("LINK_TEXT", true)
    @JvmField
    val INLINE_LINK: IElementType = MarkdownElementType("INLINE_LINK")
    @JvmField
    val FULL_REFERENCE_LINK: IElementType = MarkdownElementType("FULL_REFERENCE_LINK")
    @JvmField
    val SHORT_REFERENCE_LINK: IElementType = MarkdownElementType("SHORT_REFERENCE_LINK")
    @JvmField
    val IMAGE: IElementType = MarkdownElementType("IMAGE")

    @JvmField
    val AUTOLINK: IElementType = MarkdownElementType("AUTOLINK")

    @JvmField
    val SETEXT_1: IElementType = MarkdownElementType("SETEXT_1")
    @JvmField
    val SETEXT_2: IElementType = MarkdownElementType("SETEXT_2")

    @JvmField
    val ATX_1: IElementType = MarkdownElementType("ATX_1")
    @JvmField
    val ATX_2: IElementType = MarkdownElementType("ATX_2")
    @JvmField
    val ATX_3: IElementType = MarkdownElementType("ATX_3")
    @JvmField
    val ATX_4: IElementType = MarkdownElementType("ATX_4")
    @JvmField
    val ATX_5: IElementType = MarkdownElementType("ATX_5")
    @JvmField
    val ATX_6: IElementType = MarkdownElementType("ATX_6")
}
