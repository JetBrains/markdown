package org.intellij.markdown

import kotlin.test.Test

class GfmTest: SpecTest(org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor()) {
    @Test
    fun testPipeInsideCodeSpanInTableCell() = doTest(
        markdown = "| code | description |\n|------|-------------|\n| `a || b` | Either a or b |",
        html = "<table><thead><tr><th>code</th><th>description</th></tr></thead><tbody><tr><td><code>a || b</code></td><td>Either a or b</td></tr></tbody></table>"
    )

    @Test
    fun testEscapedBackticksInTableCell() = doTest(
        markdown = "| code | description |\n|------|-------------|\n| \\`code\\` | Escaped backticks |",
        html = "<table><thead><tr><th>code</th><th>description</th></tr></thead><tbody><tr><td>`code`</td><td>Escaped backticks</td></tr></tbody></table>"
    )

    @Test
    fun testLinkWithPipeInUrlInTableCell() = doTest(
        markdown = "| link | description |\n|------|-------------|\n| [example](https://example.com?a=1\\|2) | Link with pipe |",
        html = "<table><thead><tr><th>link</th><th>description</th></tr></thead><tbody><tr><td><a href=\"https://example.com?a=1%7C2\">example</a></td><td>Link with pipe</td></tr></tbody></table>"
    )

    @Test
    fun testAutolinkInsideATag() = doTest(
        markdown = "<a href=\"https://jb.gg\">https://www.jb.gg/?q=19</a>",
        html = "<p><a href=\"https://jb.gg\"><a href=\"https://www.jb.gg/?q=19\">https://www.jb.gg/?q=19</a></a></p>"
    )
}