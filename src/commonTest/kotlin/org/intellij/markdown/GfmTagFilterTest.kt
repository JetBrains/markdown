package org.intellij.markdown

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import kotlin.test.Test

class GfmTagFilterTest : SpecTest(GFMFlavourDescriptor(useTagFilter = true)) {
    @Test
    fun testDisallowedRawHTMLExample653() = doTest(
        markdown = "<strong> <title> <style> <em>\n\n<blockquote>\n  <xmp> is disallowed.  <XMP> is also disallowed.\n</blockquote>\n",
        html = "<p><strong> &lt;title> &lt;style> <em></p>\n<blockquote>\n  &lt;xmp> is disallowed.  &lt;XMP> is also disallowed.\n</blockquote>\n"
    )

    @Test
    fun testDisallowedRawHtmlTagsAreFiltered() = doTest(
        markdown = "before <title> <TEXTAREA rows=\"2\"> </style> <xmp> <iframe> <noembed> <noframes> <script> <plaintext> after",
        html = "<p>before &lt;title> &lt;TEXTAREA rows=\"2\"> &lt;/style> &lt;xmp> &lt;iframe> &lt;noembed> &lt;noframes> &lt;script> &lt;plaintext> after</p>"
    )

    @Test
    fun testDisallowedRawHtmlTagsAreFilteredInHtmlBlocks() = doTest(
        markdown = "<script>\nalert('test');\n</script>\n",
        html = "&lt;script>\nalert('test');\n&lt;/script>\n"
    )

    @Test
    fun testOtherRawHtmlTagsAndSimilarNamesAreNotFiltered() = doTest(
        markdown = "before <strong> <scripture> </scripted> after",
        html = "<p>before <strong> <scripture> </scripted> after</p>"
    )

    @Test
    fun testSelfClosingDisallowedRawHtmlTagsAreFiltered() = doTest(
        markdown = "before <script/> <IFRAME/> after",
        html = "<p>before &lt;script/> &lt;IFRAME/> after</p>"
    )

    @Test
    fun testSfmDisallowedRawHtmlTagsAreFiltered() {
        object : SpecTest(SFMFlavourDescriptor(useTagFilter = true)) {}.doTest(
            markdown = "before <title> <script> after\n\n<script>\nalert('test');\n</script>\n",
            html = "<p>before &lt;title> &lt;script> after</p>\n&lt;script>\nalert('test');\n&lt;/script>\n"
        )
    }
}
