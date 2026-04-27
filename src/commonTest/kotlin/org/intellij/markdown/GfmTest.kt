package org.intellij.markdown

import kotlin.test.Test

class GfmTest: SpecTest(org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor()) {
    @Test
    fun testPipeInsideCodeSpanInTableCell() = doTest(
        markdown = "| code | description |\n|------|-------------|\n| `a || b` | Either a or b |",
        html = "<table><thead><tr><th>code</th><th>description</th></tr></thead><tbody><tr><td><code>a || b</code></td><td>Either a or b</td></tr></tbody></table>"
    )

    @Test
    fun testAutolinkInsideATag() = doTest(
        markdown = "<a href=\"https://jb.gg\">https://www.jb.gg/?q=19</a>",
        html = "<p><a href=\"https://jb.gg\"><a href=\"https://www.jb.gg/?q=19\">https://www.jb.gg/?q=19</a></a></p>"
    )
}