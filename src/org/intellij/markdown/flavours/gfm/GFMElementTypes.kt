package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType

public object GFMTokenTypes {

    public val TILDE: IElementType = MarkdownElementType("~", true);

    public val TABLE_SEPARATOR: IElementType = MarkdownElementType("TABLE_SEPARATOR", true)

    public val GFM_AUTOLINK: IElementType = MarkdownElementType("GFM_AUTOLINK", true);

    public val CHECK_BOX: IElementType = MarkdownElementType("CHECK_BOX", true);

}

public interface GFMElementTypes {
    companion object {
        public val STRIKETHROUGH: IElementType = MarkdownElementType("STRIKETHROUGH");

        public val TABLE: IElementType = MarkdownElementType("TABLE")

        public val HEADER: IElementType = MarkdownElementType("HEADER")

        public val ROW: IElementType = MarkdownElementType("ROW")

        public val CELL: IElementType = MarkdownElementType("CELL")
    }
}