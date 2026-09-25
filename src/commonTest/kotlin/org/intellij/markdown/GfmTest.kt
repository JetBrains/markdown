package org.intellij.markdown

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import kotlin.test.Test

class GfmTest: SpecTest(org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor()) {
    @Test
    fun testAutolinkInsideATag() = doTest(
        markdown = "<a href=\"https://jb.gg\">https://www.jb.gg/?q=19</a>",
        html = "<p><a href=\"https://jb.gg\"><a href=\"https://www.jb.gg/?q=19\">https://www.jb.gg/?q=19</a></a></p>"
    )

    // IJPL-91082: the GFM autolink lexer used to swallow the closing paren of an inline link
    // destination together with the non-ASCII separator and the following link's text
    @Test
    fun testInlineLinksSeparatedByIdeographicComma() = doTest(
        markdown = "[zenblo](https://github.com/zenblo)、[samyu2000](https://github.com/samyu2000)",
        html = "<p><a href=\"https://github.com/zenblo\">zenblo</a>、<a href=\"https://github.com/samyu2000\">samyu2000</a></p>"
    )

    @Test
    fun testRawHtmlIsNotFilteredByDefault() = doTest(
        markdown = "before <title> <TEXTAREA rows=\"2\"> </style> <xmp> <iframe> <noembed> <noframes> <script> <plaintext> after",
        html = "<p>before <title> <TEXTAREA rows=\"2\"> </style> <xmp> <iframe> <noembed> <noframes> <script> <plaintext> after</p>"
    )

    @Test
    fun testRawHtmlBlocksAreNotFilteredByDefault() = doTest(
        markdown = "<script>\nalert('test');\n</script>\n",
        html = "<script>\nalert('test');\n</script>\n"
    )

    @Test
    fun testCommonMarkRawHtmlIsNotFiltered() {
        object : SpecTest(CommonMarkFlavourDescriptor()) {}.doTest(
            markdown = "before <title> <script> after",
            html = "<p>before <title> <script> after</p>"
        )
    }

    @Test
    fun testSfmRawHtmlIsNotFilteredByDefault() {
        object : SpecTest(SFMFlavourDescriptor()) {}.doTest(
            markdown = "before <title> <script> after\n\n<script>\nalert('test');\n</script>\n",
            html = "<p>before <title> <script> after</p>\n<script>\nalert('test');\n</script>\n"
        )
    }

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

    @Test
    fun testBlockMathAllowsSurroundingWhitespace() = doTest(
        markdown = "\$\$ x+y \$\$",
        html = "<p><span class=\"math\" inline = \"false\">x+y</span></p>"
    )

    @Test
    fun testBlockMathAllowsDelimitersOnOwnLines() = doTest(
        markdown = "\$\$\nx+y\n\$\$",
        html = "<p><span class=\"math\" inline = \"false\">x+y</span></p>"
    )

    @Test
    fun testInlineMathDoesNotAllowSurroundingWhitespace() = doPlainParagraphTests(
        "\$ x+y \$",
    )

    @Test
    fun testBlockMathDoesNotPairCurrencyAmounts() = doPlainParagraphTests(
        "Cost is \$\$5 and \$\$10",
    )

    private fun doPlainParagraphTests(vararg markdowns: String) {
        markdowns.forEach { markdown ->
            doTest(markdown = markdown, html = "<p>$markdown</p>")
        }
    }

    // IJPL-91041: a trailing entity reference is excluded from an autolink as a whole,
    // not just its closing semicolon
    @Test
    fun testAutolinkFollowedByEntityReference() = doTest(
        markdown = "https://travis-ci.com/TheAlgorithms/Java&nbsp;",
        html = "<p><a href=\"https://travis-ci.com/TheAlgorithms/Java\">https://travis-ci.com/TheAlgorithms/Java</a>\u00A0</p>"
    )

    @Test
    fun testLinkedImageFollowedByEntityReference() = doTest(
        markdown = "[![Build Status](https://api.travis-ci.com/TheAlgorithms/Java.svg?branch=master)](https://travis-ci.com/TheAlgorithms/Java)&nbsp;",
        html = "<p><a href=\"https://travis-ci.com/TheAlgorithms/Java\">" +
                "<img src=\"https://api.travis-ci.com/TheAlgorithms/Java.svg?branch=master\" alt=\"Build Status\" /></a>\u00A0</p>"
    )

    @Test
    fun testAutolinkFollowedByMultipleEntityReferences() = doTest(
        markdown = "https://example.com/a&amp;&nbsp;",
        html = "<p><a href=\"https://example.com/a\">https://example.com/a</a>&amp;\u00A0</p>"
    )

    @Test
    fun testAutolinkKeepsAmpersandWithoutEntityName() = doTest(
        markdown = "https://example.com/foo&;",
        html = "<p><a href=\"https://example.com/foo&amp;\">https://example.com/foo&amp;</a>;</p>"
    )

    @Test
    fun testSfmAutolinkFollowedByEntityReference() {
        // SFM only linkifies a URL followed by whitespace or punctuation; with the entity
        // excluded from the autolink, the following '&' glues into a word, so no link is made
        // (just like `www.foo.com&nbsp;`), instead of an autolink with `&nbsp` inside the URL
        object : SpecTest(SFMFlavourDescriptor()) {}.doTest(
            markdown = "https://travis-ci.com/TheAlgorithms/Java&nbsp;",
            html = "<p>https://travis-ci.com/TheAlgorithms/Java\u00A0</p>"
        )
    }
}
