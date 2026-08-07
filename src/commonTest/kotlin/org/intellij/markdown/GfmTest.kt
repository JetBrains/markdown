package org.intellij.markdown

import kotlin.test.Test

class GfmTest: SpecTest(org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor()) {
    @Test
    fun testAutolinkInsideATag() = doTest(
        markdown = "<a href=\"https://jb.gg\">https://www.jb.gg/?q=19</a>",
        html = "<p><a href=\"https://jb.gg\"><a href=\"https://www.jb.gg/?q=19\">https://www.jb.gg/?q=19</a></a></p>"
    )

    @Test
    fun testDollarInsideStrongIsNotMath() = doTest(
        markdown = "**$0.85 EPS** (vs. $0.70 est.)",
        html = "<p><strong>$0.85 EPS</strong> (vs. $0.70 est.)</p>"
    )

    @Test
    fun testInlineMathDoesNotStartInsideWordsOrAfterPunctuation() = doPlainParagraphTests(
        "foo$1+2$ bar",
        ".$1+2$",
        "-$1+2$",
        "_$1+2$",
        "/$1+2$",
        "+$1+2$",
        "@$1+2$",
        "#$1+2$",
    )

    @Test
    fun testInlineMathDoesNotEndBeforeWordCharacters() = doPlainParagraphTests(
        "$1+2$3",
        "$1+2\$a",
        "\$x\$_",
    )

    @Test
    fun testMathIsNotParsedInsideLinkText() = doTest(
        markdown = "[$1+2$](https://example.com)",
        html = "<p><a href=\"https://example.com\">$1+2$</a></p>"
    )

    @Test
    fun testMathIsNotParsedInsideLinkDestination() = doTest(
        markdown = "[x](https://example.com/$1+2$) $1+2$",
        html = "<p><a href=\"https://example.com/$1+2$\">x</a> <span class=\"math\" inline = \"true\">1+2</span></p>"
    )

    @Test
    fun testMathIsNotParsedInsideImageText() = doTest(
        markdown = "![$1+2$](x.png)",
        html = "<p><img src=\"x.png\" alt=\"$1+2$\" /></p>"
    )

    @Test
    fun testBlockMathDoesNotStartInsideWords() = doPlainParagraphTests(
        "foo\$\$x+y\$\$bar",
    )

    @Test
    fun testBlockMathDoesNotEndBeforeWordCharacters() = doPlainParagraphTests(
        "\$\$x+y\$\$3",
        "\$\$x+y\$\$a",
    )

    private fun doPlainParagraphTests(vararg markdowns: String) {
        markdowns.forEach { markdown ->
            doTest(markdown = markdown, html = "<p>$markdown</p>")
        }
    }
}
