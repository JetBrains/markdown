package org.intellij.markdown

import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import org.intellij.markdown.html.*
import kotlin.test.*

class HtmlGeneratorCommonTest : HtmlGeneratorTestBase() {
    override fun getTestDataPath(): String {
        return getIntellijMarkdownHome() + "/${MARKDOWN_TEST_DATA_PATH}/html"
    }

    @Test
    fun testSimple() {
        defaultTest()
    }

    @Test
    fun testMarkers() {
        defaultTest()
    }

    @Test
    fun testTightLooseLists() {
        defaultTest()
    }

    @Test
    fun testPuppetApache() {
        defaultTest()
    }

    @Test
    fun testGoPlugin() {
        defaultTest()
    }

    @Test
    fun testHeaders() {
        defaultTest()
    }

    @Test
    fun testCodeFence() {
        defaultTest()
    }

    @Test
    fun testEscaping() {
        defaultTest()
    }

    @Test
    fun testHtmlBlocks() {
        defaultTest()
    }

    @Test
    fun testLinks() {
        defaultTest()
    }

    @Test
    fun testBlockquotes() {
        defaultTest()
    }

    @Test
    fun testExample226() {
        defaultTest()
    }

    @Test
    fun testExample2() {
        defaultTest()
    }

    @Test
    fun testEntities() {
        defaultTest()
    }

    @Test
    fun testImages() {
        defaultTest(tagRenderer = HtmlGenerator.DefaultTagRenderer(customizer = { node, _, attributes ->
            when {
                node.type == MarkdownElementTypes.IMAGE -> attributes + "style=\"max-width: 100%\""
                else -> attributes
            }
        }, includeSrcPositions = false))
    }

    @Test
    fun testRuby17052() {
        defaultTest()
    }

    @Test
    fun testRuby17351() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testBug28() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testStrikethrough() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testGfmAutolink() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testSfmAutolink() {
        defaultTest(SFMFlavourDescriptor(false))
    }

    @Test
    fun testGfmTable() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testCheckedLists() {
        defaultTest(GFMFlavourDescriptor())
    }

    @Test
    fun testGitBook() {
        defaultTest()
    }

    @Test
    fun testBaseUriHttp() {
        defaultTest(baseURI = URI("http://example.com/foo/bar.html"))
    }

    @Test
    fun testBaseUriRelativeRoot() {
        defaultTest(baseURI = URI("/user/repo-name/blob/master"))
    }

    @Test
    fun testBaseUriRelativeNoRoot() {
        defaultTest(baseURI = URI("user/repo-name/blob/master"))
    }

    @Test
    fun testBaseUriWithBadRelativeUrl() {
        try {
            generateHtmlFromFile(baseURI = URI("user/repo-name/blob/master"))
        }
        catch (t: Throwable) {
            fail("Expected to work without exception, got: $t")
        }
    }

    @Test
    fun testBaseUriWithAnchorLink() {
        defaultTest(baseURI = URI("/user/repo-name/blob/master"))
    }

    @Test
    fun testCustomRenderer() {
        defaultTest(tagRenderer = object: HtmlGenerator.TagRenderer {
            override fun openTag(node: ASTNode, tagName: CharSequence, vararg attributes: CharSequence?, autoClose: Boolean): CharSequence {
                return "OPEN TAG($tagName)\n"
            }

            override fun closeTag(tagName: CharSequence): CharSequence {
                return "CLOSE TAG($tagName)\n"
            }

            override fun printHtml(html: CharSequence): CharSequence {
                return "HTML($html)\n"
            }
        })
    }

    @Test
    fun testXssProtection() {
        val disallowedLinkMd1 = "[Click me](javascript:alert(document.domain))"
        val disallowedLinkMd2 = "[Click me](file:///123)"
        val disallowedLinkMd3 = "[Click me](  VBSCRIPT:alert(1))"
        val disallowedLinkMd4 = "<VBSCRIPT:alert(1))>"

        val disallowedLinkHtml = """
             <body><p><a href="#">Click me</a></p></body>
             """.trimIndent()
        val disallowedAutolinkHtml = """
             <body><p><a href="#">VBSCRIPT:alert(1))</a></p></body>
             """.trimIndent()

        assertEqualsIgnoreLines(disallowedLinkHtml,  generateHtmlFromString(disallowedLinkMd1))
        assertEqualsIgnoreLines(disallowedLinkHtml,  generateHtmlFromString(disallowedLinkMd2))
        assertEqualsIgnoreLines(disallowedLinkHtml,  generateHtmlFromString(disallowedLinkMd3))
        assertEqualsIgnoreLines(disallowedAutolinkHtml,  generateHtmlFromString(disallowedLinkMd4))

        val disallowedImgMd = "![](javascript:alert('XSS');)"

        val disallowedImgHtml = """
             <body><p><img src="#" alt="" /></p></body>
         """.trimIndent()

        assertEqualsIgnoreLines(disallowedImgHtml,  generateHtmlFromString(disallowedImgMd))

        val allowedImgMd =
            "![](data:image/gif;base64,R0lGODlhEAAQAMQAAORHHOVSKudfOulrSOp3WOyDZu6QdvCchPGolfO0o/XBs/fNwfjZ0frl3/zy7////wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABAALAAAAAAQABAAAAVVICSOZGlCQAosJ6mu7fiyZeKqNKToQGDsM8hBADgUXoGAiqhSvp5QAnQKGIgUhwFUYLCVDFCrKUE1lBavAViFIDlTImbKC5Gm2hB0SlBCBMQiB0UjIQA7)"

        val allowedImgHtml = """
             <body><p><img src="data:image/gif;base64,R0lGODlhEAAQAMQAAORHHOVSKudfOulrSOp3WOyDZu6QdvCchPGolfO0o/XBs/fNwfjZ0frl3/zy7////wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABAALAAAAAAQABAAAAVVICSOZGlCQAosJ6mu7fiyZeKqNKToQGDsM8hBADgUXoGAiqhSvp5QAnQKGIgUhwFUYLCVDFCrKUE1lBavAViFIDlTImbKC5Gm2hB0SlBCBMQiB0UjIQA7" alt="" /></p></body>
         """.trimIndent()

        assertEqualsIgnoreLines(allowedImgHtml,  generateHtmlFromString(allowedImgMd))
    }

}

private fun assertEqualsIgnoreLines(expected: String, actual: String) {
    return assertEquals(expected.replace("\n", ""), actual.replace("\n", ""))
}