package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File

public class MarkdownSrcPosTest : TestCase() {
    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()) {
        val src = File(getTestDataPath() + "/" + testName + ".md").readText();
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(src);
        val html = HtmlGenerator(src, tree, flavour, includeSrcPositions = true).generateHtml()

        val result = HtmlGeneratorTest.formatHtmlForTests(html)

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".pos.txt", result);
    }

    public fun testPuppetApache() {
        defaultTest(GFMFlavourDescriptor())
    }

    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/html").absolutePath;
    }
}