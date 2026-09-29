package org.intellij.markdown

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockQuoteLazyContinuationTest {
    private val nonLazyFlavours = listOf(NonLazyBlockQuoteCommonMarkFlavour(), NonLazyBlockQuoteGfmFlavour())

    private fun doTest(markdown: String, html: String) {
        for (flavour in nonLazyFlavours) {
            object : SpecTest(flavour) {}.doTest(markdown, html)
        }
    }

    private fun assertSameAsLazy(markdown: String) {
        val lazyFlavours = listOf(CommonMarkFlavourDescriptor(), GFMFlavourDescriptor())
        for ((lazyFlavour, nonLazyFlavour) in lazyFlavours.zip(nonLazyFlavours)) {
            assertEquals(generateHtml(markdown, lazyFlavour), generateHtml(markdown, nonLazyFlavour))
        }
    }

    private fun generateHtml(markdown: String, flavour: MarkdownFlavourDescriptor): String {
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
        return HtmlGenerator(markdown, tree, flavour).generateHtml()
    }

    private fun topLevelNodes(markdown: String, flavour: MarkdownFlavourDescriptor): List<String> {
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
        return tree.children.map { "${it.type} ${it.startOffset}..${it.endOffset}" }
    }

    @Test
    fun testLineWithoutMarkerEndsBlockQuote() = doTest(
        markdown = "> First line.\nSecond line.\n",
        html = "<blockquote>\n<p>First line.</p>\n</blockquote>\n<p>Second line.</p>\n"
    )

    @Test
    fun testBlockQuoteEndsBeforeLineWithoutMarker() {
        for (flavour in nonLazyFlavours) {
            assertEquals(
                listOf("Markdown:BLOCK_QUOTE 0..13", "Markdown:EOL 13..14", "Markdown:PARAGRAPH 14..26"),
                topLevelNodes("> First line.\nSecond line.", flavour)
            )
        }
    }

    @Test
    fun testQuotedLineAfterLineWithoutMarker() = doTest(
        markdown = "> bar\nbaz\n> foo\n",
        html = "<blockquote>\n<p>bar</p>\n</blockquote>\n<p>baz</p>\n<blockquote>\n<p>foo</p>\n</blockquote>\n"
    )

    @Test
    fun testLineWithoutMarkerEndsAllNestedBlockQuotes() = doTest(
        markdown = "> > > foo\nbar\n",
        html = "<blockquote>\n<blockquote>\n<blockquote>\n<p>foo</p>\n</blockquote>\n</blockquote>\n</blockquote>\n<p>bar</p>\n"
    )

    @Test
    fun testLineWithOuterMarkerOnlyEndsInnerBlockQuote() = doTest(
        markdown = "> > foo\n> bar\n",
        html = "<blockquote>\n<blockquote>\n<p>foo</p>\n</blockquote>\n<p>bar</p>\n</blockquote>\n"
    )

    @Test
    fun testLineWithoutMarkerEndsBlockQuoteInListItem() {
        // GFM renders a paragraph that follows another block in a list item as `<p>` even in a tight list
        object : SpecTest(NonLazyBlockQuoteCommonMarkFlavour()) {}.doTest(
            markdown = "- > foo\n  bar\n",
            html = "<ul>\n<li>\n<blockquote>\n<p>foo</p>\n</blockquote>\nbar</li>\n</ul>\n"
        )
    }

    @Test
    fun testListItemInBlockQuoteStaysLazy() = assertSameAsLazy("> - foo\n> bar\n")

    @Test
    fun testLineWithoutMarkerEndsListInBlockQuote() = doTest(
        markdown = "> - foo\nbar\n",
        html = "<blockquote>\n<ul>\n<li>foo</li>\n</ul>\n</blockquote>\n<p>bar</p>\n"
    )

    @Test
    fun testListItemStaysLazy() = assertSameAsLazy("- foo\nbar\n")

    @Test
    fun testLineWithoutMarkerStartsSetextHeading() = doTest(
        markdown = "> foo\nbar\n===\n",
        html = "<blockquote>\n<p>foo</p>\n</blockquote>\n<h1>bar</h1>\n"
    )

    @Test
    fun testLineWithoutMarkerEndsAlert() {
        assertEquals(
            listOf("Markdown:ALERT 0..17", "Markdown:EOL 17..18", "Markdown:PARAGRAPH 18..29"),
            topLevelNodes("> [!NOTE]\n> Note.\nNot a note.", NonLazyBlockQuoteGfmFlavour())
        )
    }

    @Test
    fun testLazyContinuationByDefault() {
        for (flavour in listOf(CommonMarkFlavourDescriptor(), GFMFlavourDescriptor(), SFMFlavourDescriptor())) {
            object : SpecTest(flavour) {}.doTest(
                markdown = "> First line.\nSecond line.\n",
                html = "<blockquote>\n<p>First line.\nSecond line.</p>\n</blockquote>\n"
            )
        }
    }
}
