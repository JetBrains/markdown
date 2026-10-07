package org.intellij.markdown.parser

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownLineEndingsTest {
    @Test
    fun codeFenceClosesWithEveryLineEnding() {
        for (flavour in listOf(CommonMarkFlavourDescriptor(), GFMFlavourDescriptor())) {
            for (eol in listOf("\n", "\r\n", "\r")) {
                val text = listOf(
                    "# Code Snippet", "", "``` python", "def hello_world():",
                    "    print(\"Hello, world!\")", "```", "markdown end"
                ).joinToString(eol)
                val tree = MarkdownParser(flavour, cancellationToken = CancellationToken.NonCancellable)
                    .buildMarkdownTreeFromString(text as CharSequence, 7)
                val fence = tree.children.single { it.type == MarkdownElementTypes.CODE_FENCE }
                val end = fence.children.single { it.type == MarkdownTokenTypes.CODE_FENCE_END }
                assertEquals("```", text.substring(end.startOffset - 7, end.endOffset - 7))
                val paragraph = tree.children.last()
                assertEquals(MarkdownElementTypes.PARAGRAPH, paragraph.type)
                assertEquals(
                    "markdown end", text.substring(paragraph.startOffset - 7, paragraph.endOffset - 7)
                )
                assertEquals(
                    listOf(eol, eol, eol),
                    fence.children.filter { it.type == MarkdownTokenTypes.EOL }.map {
                        text.substring(it.startOffset - 7, it.endOffset - 7)
                    }
                )
            }
        }
    }

    @Test
    fun lineEndingsProduceEquivalentHtml() {
        val samples = listOf(
            "first\n\nsecond",
            "first\nsecond",
            "first  \nsecond",
            "# heading\n\nparagraph",
            "- first\n- second",
            "> first\n> second",
            "```python\nfirst\n\nsecond\n```\nafter",
            "first\n---\nsecond",
            "first\\\nsecond",
            "`first\nsecond`"
        )
        for (flavour in listOf(CommonMarkFlavourDescriptor(), GFMFlavourDescriptor(), SFMFlavourDescriptor())) {
            val parser = MarkdownParser(flavour, cancellationToken = CancellationToken.NonCancellable)
            fun render(text: String): String {
                val tree = parser.buildMarkdownTreeFromString(text as CharSequence)
                return HtmlGenerator(text, tree, flavour, false).generateHtml()
            }
            for (sample in samples) {
                val expected = render(sample)
                for (eol in listOf("\r\n", "\r")) {
                    val text = sample.replace("\n", eol)
                    val actual = render(text)
                    assertEquals(
                        expected, actual.replace("\r\n", "\n").replace('\r', '\n'),
                        "Sample: $sample, EOL: ${eol.toList().map { it.code }}"
                    )
                }
            }
        }
    }

    @Test
    fun mixedLineEndingsKeepOriginalOffsets() {
        val text = "before\r\n\r```python\nprint(1)\r\n```\rafter"
        val tree = MarkdownParser(GFMFlavourDescriptor(), cancellationToken = CancellationToken.NonCancellable)
            .buildMarkdownTreeFromString(text as CharSequence)
        val paragraph = tree.children.last()
        assertEquals(MarkdownElementTypes.PARAGRAPH, paragraph.type)
        assertEquals(text.indexOf("after"), paragraph.startOffset)
        assertEquals(text.length, paragraph.endOffset)
    }
}
