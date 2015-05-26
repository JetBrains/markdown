package org.intellij.markdown;

import junit.framework.TestCase
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor
import java.io.File

public class HtmlGeneratorTest : TestCase() {
        private fun defaultTest() {
                val src = File(getTestDataPath() + "/" + testName + ".md").readText();
                val tree = MarkdownParser(CommonMarkMarkerProcessor.Factory).buildMarkdownTreeFromString(src);
                val result = HtmlGenerator(src, tree).generateHtml()

                assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".txt", result);
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

    public fun testPuppetApache() {
        defaultTest()
    }

    public fun testGoPlugin() {
        defaultTest()
    }
}
