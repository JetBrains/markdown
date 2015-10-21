package org.intellij.markdown;

import junit.framework.TestCase
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import kotlin.text.Regex

public class HtmlGeneratorTest : TestCase() {
    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()) {
        val src = File(getTestDataPath() + "/" + testName + ".md").readText();
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(src);
        val html = HtmlGenerator(src, tree, flavour).generateHtml()

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

    public fun testStrikethrough() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testGfmAutolink() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testCheckedLists() {
        defaultTest(GFMFlavourDescriptor())
    }

    public fun testGitBook() {
        defaultTest()
    }

    companion object {
        public fun formatHtmlForTests(html: String): String {
            val tags = Regex("</?[^>]+>")

            val split = tags.replace(html as CharSequence, { matchResult ->
                val next = matchResult.next()
                if (html.charAt(matchResult.range.start + 1) != '/'
                        && next != null
                        && html.charAt(next.range.start + 1) == '/') {
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
