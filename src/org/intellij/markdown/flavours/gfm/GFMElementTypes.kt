package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType

public interface GFMTokenTypes {
    companion object {
        public val TILDE: IElementType = MarkdownElementType("~", true);
    }
}

public interface GFMElementTypes {
    companion object {
        public val STRIKETHROUGH: IElementType = MarkdownElementType("STRIKETHROUGH");
    }
}