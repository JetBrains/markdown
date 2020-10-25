package org.intellij.markdown

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.*

class MarkdownSrcPosTest : TestCase() {
    private fun defaultTest(flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor()) {
        val src = readFromFile(getTestDataPath() + "/" + testName + ".md")
        val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
        val html = HtmlGenerator(src, tree, flavour, includeSrcPositions = true).generateHtml()

        val result = HtmlGeneratorTestBase.formatHtmlForTests(html)

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + ".pos.txt", result)
    }

    @Test
    fun testPuppetApache() {
        defaultTest(GFMFlavourDescriptor())
    }

    private fun getTestDataPath(): String {
        return getIntellijMarkdownHome() + "/${MARKDOWN_TEST_DATA_PATH}/html"
    }
}