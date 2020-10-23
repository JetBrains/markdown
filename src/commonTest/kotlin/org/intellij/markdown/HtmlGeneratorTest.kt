package org.intellij.markdown

import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import org.intellij.markdown.html.*
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.*

class HtmlGeneratorTest : TestCase() {
    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor(),
                            baseURI: URI? = null,
                            tagRenderer: HtmlGenerator.TagRenderer = HtmlGenerator.DefaultTagRenderer(DUMMY_ATTRIBUTES_CUSTOMIZER, false)) {

        val src = readFromFile(getTestDataPath() + "/" + testName + ".md")
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
        val htmlGeneratingProviders = flavour.createHtmlGeneratingProviders(LinkMap.buildLinkMap(tree, src), baseURI)
        val html = HtmlGenerator(src, tree, htmlGeneratingProviders, includeSrcPositions = false).generateHtml(tagRenderer)

        val result = formatHtmlForTests(html)

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".txt", result)
    }

    private fun getTestDataPath(): String {
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
        defaultTest(SFMFlavourDescriptor())
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
    fun testBaseUriFile() {
        defaultTest(baseURI = URI("file:///c:/foo/bar.html"))
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
        defaultTest(baseURI = URI("user/repo-name/blob/master"))
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

    companion object {
        fun formatHtmlForTests(html: String): String {
            val tags = Regex("</?[^>]+>")

            val split = tags.replace(html as CharSequence, { matchResult ->
                val next = matchResult.next()
                if (html[matchResult.range.first + 1] != '/'
                        && next != null
                        && html[next.range.first + 1] == '/') {
                    matchResult.value
                } else if (matchResult.value.contains("pre") && next?.value?.contains("code") == true
                        || matchResult.value.contains("/code") && next?.value?.contains("/pre") == true) {
                    matchResult.value
                } else {
                    matchResult.value + "\n"
                }
            })
            return split
        }

    }
}
