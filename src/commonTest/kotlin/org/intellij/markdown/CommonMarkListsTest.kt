package org.intellij.markdown

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import kotlin.test.Test

/**
 * Blank lines do not end a list: the "two blank lines break out of all lists" rule
 * was removed from the CommonMark spec in version 0.26.
 */
class CommonMarkListsTest : SpecTest(CommonMarkFlavourDescriptor()) {
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
