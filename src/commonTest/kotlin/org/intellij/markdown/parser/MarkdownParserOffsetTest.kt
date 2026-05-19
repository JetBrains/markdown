package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownParserOffsetTest {
    @Test
    fun buildMarkdownTreeFromStringCanApplyBaseOffset() {
        val tree = MarkdownParser(GFMFlavourDescriptor())
            .buildMarkdownTreeFromString("world", 7)

        val paragraph = tree.children.single()

        assertEquals(MarkdownElementTypes.PARAGRAPH, paragraph.type)
        assertEquals(7, paragraph.startOffset)
        assertEquals(12, paragraph.endOffset)
        assertEquals(7, paragraph.children.single().startOffset)
        assertEquals(12, paragraph.children.single().endOffset)
    }

    @Test
    fun parseCanApplyBaseOffset() {
        val tree = MarkdownParser(GFMFlavourDescriptor())
            .parse(MarkdownElementTypes.MARKDOWN_FILE, "world", parseInlines = true, baseOffset = 7)

        val paragraph = tree.children.single()

        assertEquals(MarkdownElementTypes.PARAGRAPH, paragraph.type)
        assertEquals(7, paragraph.startOffset)
        assertEquals(12, paragraph.endOffset)
        assertEquals(7, paragraph.children.single().startOffset)
        assertEquals(12, paragraph.children.single().endOffset)
    }
}
