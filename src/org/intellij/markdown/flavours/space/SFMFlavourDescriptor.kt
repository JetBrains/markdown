package org.intellij.markdown.flavours.space

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.lexer._SFMLexer
import org.intellij.markdown.lexer.MarkdownLexer
import java.io.Reader

/**
 * JetBrains Space-flavoured markdown
 */
open class SFMFlavourDescriptor : GFMFlavourDescriptor() {
    override fun createInlinesLexer(): MarkdownLexer {
        return MarkdownLexer(_SFMLexer(null as Reader?))
    }
}