package org.intellij.markdown.flavours.space

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.lexer._SFMLexer
import org.intellij.markdown.lexer.MarkdownLexer

/**
 * JetBrains Space-flavoured markdown
 *
 * @param useSafeLinks `true` if all rendered links should be checked for XSS and `false` otherwise.
 * See [GFMFlavourDescriptor]
 *
 * @param useTagFilter `true` if the GFM tagfilter extension should be applied when rendering raw HTML and `false`
 * otherwise. See [GFMFlavourDescriptor]
 */
open class SFMFlavourDescriptor(
        useSafeLinks: Boolean = true,
        useTagFilter: Boolean = false
) : GFMFlavourDescriptor(useSafeLinks = useSafeLinks, useTagFilter = useTagFilter) {
    /**
     * For ABI compatibility.
     */
    constructor(useSafeLinks: Boolean = true) : this(useSafeLinks, useTagFilter = false)

    override fun createInlinesLexer(): MarkdownLexer {
        return MarkdownLexer(_SFMLexer())
    }
}