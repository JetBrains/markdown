package org.intellij.markdown

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import kotlin.test.Test

/**
 * Blank lines do not end a list: the "two blank lines break out of all lists" rule
 * was removed from the CommonMark spec in version 0.26.
 */
class CommonMarkListsTest : SpecTest(CommonMarkFlavourDescriptor()) {
    // An ordered list may interrupt a paragraph only when it starts with 1.
    @Test
    fun testOrderedListStartingWithTwoDoesNotInterruptParagraph() = doTest(
        markdown = "To repro, 1) the line must contain a number as shown, and furthermore,\n" +
                "2) it must be of a length that breaks exactly before the number.",
        html = "<p>To repro, 1) the line must contain a number as shown, and furthermore,\n" +
                "2) it must be of a length that breaks exactly before the number.</p>"
    )

    @Test
    fun testNonOneOrderedMarkersStayInParagraph() {
        for (marker in listOf("0.", "2.", "2)", "123456789)", "   2)")) {
            doTest(markdown = "paragraph\n$marker text", html = "<p>paragraph\n$marker text</p>")
        }
    }

    @Test
    fun testOrderedListStartingWithTwoDoesNotInterruptNestedParagraph() = doTest(
        markdown = "- paragraph\n  2) text",
        html = "<ul>\n<li>paragraph\n  2) text</li>\n</ul>"
    )

    @Test
    fun testOrderedListInterruptionAllowsOneAndPreservesOtherListStarts() {
        for (marker in listOf("1.", "1)", "01)")) {
            doTest(markdown = "paragraph\n$marker text", html = "<p>paragraph</p>\n<ol>\n<li>text</li>\n</ol>")
        }
        doTest(markdown = "paragraph\n\n2) text", html = "<p>paragraph</p>\n<ol start=\"2\">\n<li>text</li>\n</ol>")
        doTest(markdown = "1) first\n2) second", html = "<ol>\n<li>first</li>\n<li>second</li>\n</ol>")
    }

    // IJPL-96507: the second item used to start a separate list and render as "1."
    @Test
    fun testOrderedListSurvivesTwoBlankLinesBetweenItems() = doTest(
        markdown = "1. number 1\n\n\n2. number 2\n",
        html = "<ol>\n<li>\n<p>number 1</p>\n</li>\n<li>\n<p>number 2</p>\n</li>\n</ol>\n"
    )

    @Test
    fun testUnorderedListSurvivesTwoBlankLinesBetweenItems() = doTest(
        markdown = "- foo\n\n\n- bar\n",
        html = "<ul>\n<li>\n<p>foo</p>\n</li>\n<li>\n<p>bar</p>\n</li>\n</ul>\n"
    )

    @Test
    fun testListItemContentSurvivesTwoBlankLines() = doTest(
        markdown = "1. foo\n\n\n   bar\n",
        html = "<ol>\n<li>\n<p>foo</p>\n<p>bar</p>\n</li>\n</ol>\n"
    )

    @Test
    fun testUnindentedTextAfterTwoBlankLinesStillEndsTheList() = doTest(
        markdown = "1. foo\n\n\nbar\n",
        html = "<ol>\n<li>foo</li>\n</ol>\n<p>bar</p>\n"
    )
}
