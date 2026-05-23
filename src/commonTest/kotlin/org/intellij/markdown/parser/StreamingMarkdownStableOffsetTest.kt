package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownElementTypes.MARKDOWN_FILE
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals

class StreamingMarkdownStableOffsetTest {
    private val parser = MarkdownParser(GFMFlavourDescriptor())

    @Test
    fun emptyTextIsStable() {
        assertUnstableStartOffset("", 0)
    }

    @Test
    fun unterminatedParagraphIsUnstableFromLineStart() {
        assertUnstableStartOffset("hello", 0)
    }

    @Test
    fun terminatedParagraphWithBlankLineIsStable() {
        assertUnstableStartOffset("hello\n\n", 7)
    }

    @Test
    fun unterminatedTailAfterStablePrefixIsUnstableFromTailLineStart() {
        assertUnstableStartOffset("hello\n\nworld", 7)
    }

    @Test
    fun unterminatedAtxHeadingIsUnstableFromLineStart() {
        assertUnstableStartOffset("# title", 0)
    }

    @Test
    fun terminatedAtxHeadingStaysUnstableBeforeBlankLine() {
        assertUnstableStartOffset("# title\n", 0)
    }

    @Test
    fun atxHeadingWithBlankLineIsStable() {
        val text = "# title\n\n"

        assertUnstableStartOffset(text, text.length)
    }

    @Test
    fun unterminatedSetextHeadingIsUnstableFromParagraphStart() {
        assertUnstableStartOffset("hello\n---", 0)
    }

    @Test
    fun terminatedSetextHeadingStaysUnstableBeforeBlankLine() {
        assertUnstableStartOffset("hello\n---\n", 0)
    }

    @Test
    fun setextHeadingWithBlankLineIsStable() {
        val text = "hello\n---\n\n"

        assertUnstableStartOffset(text, text.length)
    }

    @Test
    fun unclosedCodeFenceIsUnstableFromFenceStart() {
        assertUnstableStartOffset("```\ncode\n", 0)
    }

    @Test
    fun closedCodeFenceWithUnterminatedClosingLineIsUnstableFromFenceStart() {
        assertUnstableStartOffset("```\ncode\n```", 0)
    }

    @Test
    fun terminatedCodeFenceStaysUnstableBeforeBlankLine() {
        assertUnstableStartOffset("```\ncode\n```\n", 0)
    }

    @Test
    fun codeFenceWithBlankLineIsStable() {
        val text = "```\ncode\n```\n\n"

        assertUnstableStartOffset(text, text.length)
    }

    @Test
    fun baseOffsetIsAppliedToUnstableStartOffset() {
        assertUnstableStartOffset("hello\n\nworld", 17, baseOffset = 10)
    }

    private fun assertUnstableStartOffset(
        text: String,
        expectedOffset: Int,
        baseOffset: Int = 0
    ) {
        val result = parser.parseStreaming(MARKDOWN_FILE, text, parseInlines = true, baseOffset = baseOffset)

        assertEquals(expectedOffset, result.unstableStartOffset)
    }
}
