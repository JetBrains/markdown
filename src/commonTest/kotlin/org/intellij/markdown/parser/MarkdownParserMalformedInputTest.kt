package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import kotlin.test.Test

class MarkdownParserMalformedInputTest {
    @Test
    fun malformedLinkBeforeEscapedReferenceDoesNotBreakParsing() {
        val text = "[link-v1(https://example.com)\n\n- \\[F]:  \"A B\","

        MarkdownParser(GFMFlavourDescriptor()).parse(MarkdownElementTypes.MARKDOWN_FILE, text)
    }
}
