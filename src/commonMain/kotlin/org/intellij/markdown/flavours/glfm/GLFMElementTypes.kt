package org.intellij.markdown.flavours.glfm

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import kotlin.jvm.JvmField

/**
 * GitLab Flavored Markdown specific element types.
 *
 * The design mirrors [org.intellij.markdown.flavours.gfm.GFMElementTypes]
 * to keep integration simple.
 */
object GLFMElementTypes {
    /** `>>> … >>>` multiline blockquote block. */
    @JvmField
    val MULTILINE_BLOCK_QUOTE: IElementType = MarkdownElementType("MULTILINE_BLOCK_QUOTE")

    /** `{+ added text +}` inline diff addition. */
    @JvmField
    val INLINE_DIFF_ADDITION: IElementType = MarkdownElementType("INLINE_DIFF_ADDITION")

    /** `[- deleted text -]` inline diff deletion. */
    @JvmField
    val INLINE_DIFF_DELETION: IElementType = MarkdownElementType("INLINE_DIFF_DELETION")

    /** GitLab references: `@user`, `#issue`, `!mr`, `$snippet`, `&epic`, `~label`, `%milestone`. */
    @JvmField
    val GITLAB_REFERENCE: IElementType = MarkdownElementType("GITLAB_REFERENCE")

    /** `:emoji_name:` shortcode. */
    @JvmField
    val EMOJI: IElementType = MarkdownElementType("EMOJI")

    // Alert blocks ( > [!note], etc. ) are represented as blockquotes in the
    // block parser and recognized during HTML generation, so no dedicated
    // element types are needed.
}
