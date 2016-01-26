package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType

public object GFMTokenTypes {

    @JvmField
    public val TILDE: IElementType = MarkdownElementType("~", true);

    @JvmField
    public val TABLE_SEPARATOR: IElementType = MarkdownElementType("TABLE_SEPARATOR", true)

    @JvmField
    public val GFM_AUTOLINK: IElementType = MarkdownElementType("GFM_AUTOLINK", true);

    @JvmField
    public val CHECK_BOX: IElementType = MarkdownElementType("CHECK_BOX", true);

    @JvmField
    public val CELL: IElementType = MarkdownElementType("CELL", true)
}

public object  GFMElementTypes {
    @JvmField
    public val STRIKETHROUGH: IElementType = MarkdownElementType("STRIKETHROUGH");

    @JvmField
    public val TABLE: IElementType = MarkdownElementType("TABLE")

    @JvmField
    public val HEADER: IElementType = MarkdownElementType("HEADER")

    @JvmField
    public val ROW: IElementType = MarkdownElementType("ROW")
}