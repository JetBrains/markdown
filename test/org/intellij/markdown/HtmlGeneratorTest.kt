package org.intellij.markdown;

import junit.framework.TestCase
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.net.URI

public class HtmlGeneratorTest : TestCase() {
    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor(), baseURI: URI? = null) {
        val src = File(getTestDataPath() + "/" + testName + ".md").readText();
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(src);
        val html = HtmlGenerator(src, tree, flavour, includeSrcPositions = false, baseURI = baseURI).generateHtml()

        val result = formatHtmlForTests(html)

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".txt", result);
    }

    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/html").absolutePath;
    }

    public fun testSimple() {
        defaultTest()
    }

    public fun testMarkers() {
        defaultTest()
    }

    public fun testTightLooseLists() {
        defaultTest()
    }

    public fun testPuppetApache() {
        defaultTest()
    }

    public fun testGoPlugin() {
        defaultTest()
    }

    public fun testHeaders() {
        defaultTest()
    }

    public fun testCodeFence() {
        defaultTest()
    }

    public fun testEscaping() {
        defaultTest()
    }

    public fun testHtmlBlocks() {
        defaultTest()
    }

    public fun testLinks() {
        defaultTest()
    }

    public fun testBlockquotes() {
        defaultTest()
    }

    public fun testExample226() {
        defaultTest()
    }

    public fun testExample2() {
        defaultTest()
    }

    public fun testEntities() {
        defaultTest()
    }

    public fun testImages() {
        defaultTest()
    }

    public fun testRuby17052() {
        defaultTest()
    }

    public fun testRuby17351() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testStrikethrough() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testGfmAutolink() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testGfmTable() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testCheckedLists() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testGitBook() {
        defaultTest()
    }

    public fun testBaseUriHttp() {
        defaultTest(baseURI = URI("http://example.com/foo/bar.html"))
    }

    public fun testBaseUriFile() {
        defaultTest(baseURI = URI("file:///c:/foo/bar.html"))
    }

    public fun testBaseUriRelativeRoot() {
        defaultTest(baseURI = URI("/user/repo-name/blob/master"))
    }

    public fun testBaseUriRelativeNoRoot() {
        defaultTest(baseURI = URI("user/repo-name/blob/master"))
    }

    companion object {
        public fun formatHtmlForTests(html: String): String {
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
