package org.intellij.markdown

import org.intellij.markdown.html.HtmlGenerator
import kotlin.test.*

class HtmlIndentTrimmerTest: TestCase() {
    private fun defaultTest(maxIndent: Int) {
        val src = readFromFile(getTestDataPath() + "/" + testName + ".txt")

        val result = buildString {
            for (indent in 0..maxIndent) {
                append(HtmlGenerator.trimIndents(src, indent))
                append("\n----------($indent)-----------\n")
            }
        }

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + "_after.txt", result)
    }

    private fun getTestDataPath(): String {
        return getIntellijMarkdownHome() + "/test/data/htmlTrimming"
    }

    @Test
    fun testSimple() {
        defaultTest(4)
    }

    @Test
    fun testExcessTrimming() {
        defaultTest(8)
    }

    @Test
    fun testSeveralNewlines() {
        defaultTest(3)
    }

    @Test
    fun testConserveWhiteLines() {
        defaultTest(3)
    }

    @Test
    fun testTabs() {
        defaultTest(6)
    }
}