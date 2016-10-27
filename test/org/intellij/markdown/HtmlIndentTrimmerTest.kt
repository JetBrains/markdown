package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.html.HtmlGenerator
import java.io.File

class HtmlIndentTrimmerTest : TestCase() {
    private fun defaultTest(maxIndent: Int) {
        val src = File(getTestDataPath() + "/" + testName + ".txt").readText()

        val result = buildString {
            for (indent in 0..maxIndent) {
                append(HtmlGenerator.trimIndents(src, indent))
                append("\n----------($indent)-----------\n")
            }
        }

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + "_after.txt", result)
    }

    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/htmlTrimming").absolutePath
    }

    fun testSimple() {
        defaultTest(4)
    }

    fun testExcessTrimming() {
        defaultTest(8)
    }

    fun testSeveralNewlines() {
        defaultTest(3)
    }

    fun testConserveWhiteLines() {
        defaultTest(3)
    }

    fun testTabs() {
        defaultTest(6)
    }
}