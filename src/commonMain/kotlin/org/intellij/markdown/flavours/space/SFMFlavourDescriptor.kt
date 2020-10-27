package org.intellij.markdown.flavours.space

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.lexer._SFMLexer
import org.intellij.markdown.lexer.MarkdownLexer

/**
 * JetBrains Space-flavoured markdown
 */
open class SFMFlavourDescriptor(useSafeLinks: Boolean = true) : GFMFlavourDescriptor(useSafeLinks) {
    override fun createInlinesLexer(): MarkdownLexer {
        return MarkdownLexer(_SFMLexer())
    }
}