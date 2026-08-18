package org.intellij.markdown

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import kotlin.test.Test

class GfmTest: SpecTest(org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor()) {
    @Test
    fun testAutolinkInsideATag() = doTest(
        markdown = "<a href=\"https://jb.gg\">https://www.jb.gg/?q=19</a>",
        html = "<p><a href=\"https://jb.gg\"><a href=\"https://www.jb.gg/?q=19\">https://www.jb.gg/?q=19</a></a></p>"
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
    fun testCommonMarkRawHtmlIsNotFiltered() {
        object : SpecTest(CommonMarkFlavourDescriptor()) {}.doTest(
            markdown = "before <title> <script> after",
            html = "<p>before <title> <script> after</p>"
        )
    }
}
