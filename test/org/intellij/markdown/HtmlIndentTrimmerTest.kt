package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.html.HtmlGenerator
import java.io.File

public class HtmlIndentTrimmerTest : TestCase() {
    private fun defaultTest(maxIndent: Int) {
        val src = File(getTestDataPath() + "/" + testName + ".txt").readText();

        val result = StringBuilder {
            for (indent in 0..maxIndent) {
                append(HtmlGenerator.trimIndents(src, indent))
                append("\n----------(${indent})-----------\n")
            }
        }.toString()

        assertSameLinesWithFile(getTestDataPath() + "/" + testName + "_after.txt", result);
    }

    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/htmlTrimming").getAbsolutePath();
    }

    public fun testSimple() {
        defaultTest(4)
    }

    public fun testExcessTrimming() {
        defaultTest(8)
    }

    public fun testSeveralNewlines() {
        defaultTest(3)
    }

    public fun testConserveWhiteLines() {
        defaultTest(3)
    }

    public fun testTabs() {
        defaultTest(6)
    }
}