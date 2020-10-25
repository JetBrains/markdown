package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType

object GFMTokenTypes {

    val TILDE: IElementType = MarkdownElementType("~", true)

    val TABLE_SEPARATOR: IElementType = MarkdownElementType("TABLE_SEPARATOR", true)

    val GFM_AUTOLINK: IElementType = MarkdownElementType("GFM_AUTOLINK", true)

    val CHECK_BOX: IElementType = MarkdownElementType("CHECK_BOX", true)

    val CELL: IElementType = MarkdownElementType("CELL", true)
}

object  GFMElementTypes {
    val STRIKETHROUGH: IElementType = MarkdownElementType("STRIKETHROUGH")

    val TABLE: IElementType = MarkdownElementType("TABLE")

    val HEADER: IElementType = MarkdownElementType("HEADER")

    val ROW: IElementType = MarkdownElementType("ROW")
}