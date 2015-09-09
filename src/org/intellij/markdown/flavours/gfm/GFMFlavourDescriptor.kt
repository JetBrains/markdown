package org.intellij.markdown.flavours.gfm

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.lexer._GFMLexer
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.*
import java.io.Reader

public class GFMFlavourDescriptor : CommonMarkFlavourDescriptor() {
    override fun createInlinesLexer(): MarkdownLexer {
        return MarkdownLexer(_GFMLexer(null as Reader?))
    }

    override val sequentialParserManager = object : SequentialParserManager() {
        override fun getParserSequence(): List<SequentialParser> {
            return listOf(AutolinkParser(),
                    BacktickParser(),
                    ImageParser(),
                    InlineLinkParser(),
                    ReferenceLinkParser(),
                    StrikeThroughParser(),
                    EmphStrongParser())
        }
    }
}