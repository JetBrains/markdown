package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import org.intellij.markdown.html.*
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.net.URI

class HtmlGeneratorTest : TestCase() {
    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor(),
                            baseURI: URI? = null,
                            tagRenderer: HtmlGenerator.TagRenderer = HtmlGenerator.DefaultTagRenderer(DUMMY_ATTRIBUTES_CUSTOMIZER, false)) {

        val src = File(getTestDataPath() + "/" + testName + ".md").readText()
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
        val htmlGeneratingProviders = flavour.createHtmlGeneratingProviders(LinkMap.buildLinkMap(tree, src), baseURI)
        val html = HtmlGenerator(src, tree, htmlGeneratingProviders, includeSrcPositions = false).generateHtml(tagRenderer)

        val result = formatHtmlForTests(html)

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".txt", result)
    }

    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/html").absolutePath
    }

    fun testSimple() {
        defaultTest()
    }

    fun testMarkers() {
        defaultTest()
    }

    fun testTightLooseLists() {
        defaultTest()
    }

    fun testPuppetApache() {
        defaultTest()
    }

    fun testGoPlugin() {
        defaultTest()
    }

    fun testHeaders() {
        defaultTest()
    }

    fun testCodeFence() {
        defaultTest()
    }

    fun testEscaping() {
        defaultTest()
    }

    fun testHtmlBlocks() {
        defaultTest()
    }

    fun testLinks() {
        defaultTest()
    }

    fun testBlockquotes() {
        defaultTest()
    }

    fun testExample226() {
        defaultTest()
    }

    fun testExample2() {
        defaultTest()
    }

    fun testEntities() {
        defaultTest()
    }

    fun testImages() {
        defaultTest(tagRenderer = HtmlGenerator.DefaultTagRenderer(customizer = { node, _, attributes ->
            when {
                node.type == MarkdownElementTypes.IMAGE -> attributes + "style=\"max-width: 100%\""
                else -> attributes
            }
        }, includeSrcPositions = false))
    }

    fun testRuby17052() {
        defaultTest()
    }

    fun testRuby17351() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testBug28() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testStrikethrough() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testGfmAutolink() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testSfmAutolink() {
        defaultTest(SFMFlavourDescriptor())
    }

    fun testGfmTable() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testCheckedLists() {
        defaultTest(GFMFlavourDescriptor())
    }

    fun testGitBook() {
        defaultTest()
    }

    fun testBaseUriHttp() {
        defaultTest(baseURI = URI("http://example.com/foo/bar.html"))
    }

    fun testBaseUriFile() {
        defaultTest(baseURI = URI("file:///c:/foo/bar.html"))
    }

    fun testBaseUriRelativeRoot() {
        defaultTest(baseURI = URI("/user/repo-name/blob/master"))
    }

    fun testBaseUriRelativeNoRoot() {
        defaultTest(baseURI = URI("user/repo-name/blob/master"))
    }

    fun testBaseUriWithBadRelativeUrl() {
        defaultTest(baseURI = URI("user/repo-name/blob/master"))
    }

    fun testBaseUriWithAnchorLink() {
        defaultTest(baseURI = URI("/user/repo-name/blob/master"))
    }

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
                if (html[matchResult.range.start + 1] != '/'
                        && next != null
                        && html[next.range.start + 1] == '/') {
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
