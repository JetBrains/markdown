package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor
import java.io.File

public class ParserPerformanceTest : TestCase() {
    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/performance").getAbsolutePath();
    }

    private fun defaultTest(fullParse: Boolean) {
        val src = File(getTestDataPath() + "/" + testName + ".md").readText();

        val runnable = { i: Int ->
            val root = MarkdownParser(CommonMarkMarkerProcessor.Factory).
                    parse(MarkdownElementTypes.MARKDOWN_FILE, src, fullParse)
            assert(root.children.size() > 0)
        }

        repeat(WARM_UP_NUM, runnable)

        val startTime = System.nanoTime()
        repeat(TEST_NUM, runnable)
        val testTime = System.nanoTime() - startTime

        println("$testName: ${(testTime / TEST_NUM / 1e6)}ms")
    }

    public fun testGitBook() {
        defaultTest(false)
    }

    companion object {
        val WARM_UP_NUM = 10
        val TEST_NUM = 100
    }
}