package org.intellij.markdown;

import junit.framework.TestCase
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor
import java.io.File
import kotlin.text.Regex

public class HtmlGeneratorTest : TestCase() {
    private fun defaultTest() {
        val src = File(getTestDataPath() + "/" + testName + ".md").readText();
        val tree = MarkdownParser(CommonMarkMarkerProcessor.Factory).buildMarkdownTreeFromString(src);
        val html = HtmlGenerator(src, tree).generateHtml()

        val result = formatHtmlForTests(html)

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".txt", result);
    }

    private fun formatHtmlForTests(html: String): String {
        val tags = Regex("</?[a-zA-Z1-6]+>")

        val split = tags.replace(html as CharSequence, { matchResult ->
            val next = matchResult.next()
            if (html.charAt(matchResult.range.start + 1) != '/'
                    && next != null
                    && html.charAt(next.range.start + 1) == '/') {
                matchResult.value
            } else {
                matchResult.value + "\n"
            }
        })
        return split
    }

    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/html").getAbsolutePath();
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
}
