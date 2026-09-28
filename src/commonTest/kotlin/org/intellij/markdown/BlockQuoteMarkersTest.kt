package org.intellij.markdown

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import kotlin.test.Test

/**
 * The `>` markers on the continuation lines of a block quote are parsed as [MarkdownTokenTypes.BLOCK_QUOTE] tokens,
 * which must not change the rendered HTML.
 */
class BlockQuoteMarkersTest {
    private val flavours = listOf(CommonMarkFlavourDescriptor(), GFMFlavourDescriptor())

    private fun doTest(markdown: String, html: String) {
        for (flavour in flavours) {
            object : SpecTest(flavour) {}.doTest(markdown, html)
        }
    }

    @Test
    fun testLooseListInBlockQuote() = doTest(
        markdown = "> - a\n>\n> - b\n",
        html = "<blockquote>\n<ul>\n<li>\n<p>a</p>\n</li>\n<li>\n<p>b</p>\n</li>\n</ul>\n</blockquote>\n"
    )

    @Test
    fun testTightListInBlockQuote() = doTest(
        markdown = "> - a\n> - b\n",
        html = "<blockquote>\n<ul>\n<li>a</li>\n<li>b</li>\n</ul>\n</blockquote>\n"
    )

    @Test
    fun testLooseListItemInBlockQuote() = doTest(
        markdown = "> - a\n>\n>   b\n",
        html = "<blockquote>\n<ul>\n<li>\n<p>a</p>\n<p>b</p>\n</li>\n</ul>\n</blockquote>\n"
    )

    @Test
    fun testCodeFenceContentAfterMarkerWithSpace() = doTest(
        markdown = ">```\n> aaa\n>```\n",
        html = "<blockquote>\n<pre><code>aaa\n</code></pre>\n</blockquote>\n"
    )

    @Test
    fun testCodeFenceContentAfterMarkerWithoutSpace() = doTest(
        markdown = "> ```\n>aaa\n> ```\n",
        html = "<blockquote>\n<pre><code>aaa\n</code></pre>\n</blockquote>\n"
    )

    @Test
    fun testHtmlBlockContentAfterMarkerWithSpace() = doTest(
        markdown = "><div>\n> foo\n",
        html = "<blockquote>\n<div>\nfoo\n</blockquote>\n"
    )

    @Test
    fun testSetextHeading2InBlockQuote() = doTest(
        markdown = "> Foo\n> ---\n",
        html = "<blockquote>\n<h2>Foo</h2>\n</blockquote>\n"
    )

    @Test
    fun testSetextHeadingInIndentedBlockQuote() = doTest(
        markdown = "  > Foo\n  > ===\n",
        html = "<blockquote>\n<h1>Foo</h1>\n</blockquote>\n"
    )
}
