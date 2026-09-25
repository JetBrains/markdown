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

    // IJPL-91081: a link followed immediately by a word (no space or punctuation in between,
    // as is usual in Korean or Japanese) must still be parsed as a link
    @Test
    fun testInlineLinkFollowedByWordCharacters() = doTest(
        markdown = "Check [here](https://www.jetbrains.com/)asdf",
        html = "<p>Check <a href=\"https://www.jetbrains.com/\">here</a>asdf</p>"
    )

    @Test
    fun testInlineLinkFollowedByKoreanText() = doTest(
        markdown = "[여기](https://www.jetbrains.com/)에서 확인하세요.",
        html = "<p><a href=\"https://www.jetbrains.com/\">여기</a>에서 확인하세요.</p>"
    )

    // IJPL-91081: an autolink must not end in the middle of a word following the domain.
    // Like on github.com, the whole run of non-space characters becomes part of the link.
    @Test
    fun testAutolinkFollowedByKoreanTextDoesNotSplitTheWord() = doTest(
        markdown = "https://www.jetbrains.com에서 확인",
        html = "<p><a href=\"https://www.jetbrains.com%EC%97%90%EC%84%9C\">https://www.jetbrains.com에서</a> 확인</p>"
    )

    @Test
    fun testAutolinkWithUnicodeDomain() = doTest(
        markdown = "https://例え.テスト/ link",
        html = "<p><a href=\"https://%E4%BE%8B%E3%81%88.%E3%83%86%E3%82%B9%E3%83%88/\">https://例え.テスト/</a> link</p>"
    )

    private fun doPlainParagraphTests(vararg markdowns: String) {
        markdowns.forEach { markdown ->
            doTest(markdown = markdown, html = "<p>$markdown</p>")
        }
    }
}
