package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import java.io.File

public class ParserPerformanceTest : TestCase() {
    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/performance").absolutePath;
    }

    private fun defaultTest(fullParse: Boolean) {
        val fileName = testName.let {
            if (it.endsWith("Full")) {
                it.substring(0, it.length - 4)
            } else {
                it
            }
        }
        val src = File(getTestDataPath() + "/" + fileName + ".md").readText();

        val runnable = { i: Int ->
            val root = MarkdownParser(CommonMarkFlavourDescriptor()).
                    parse(MarkdownElementTypes.MARKDOWN_FILE, src, fullParse)
            assert(root.children.size > 0)
        }

        repeat(WARM_UP_NUM, runnable)

        val startTime = System.nanoTime()
        repeat(TEST_NUM, runnable)
        val testTime = System.nanoTime() - startTime

        println("$fileName: ${(testTime / TEST_NUM / 1e6)}ms")
    }

    public fun testGitBook() {
        defaultTest(false)
    }

    public fun testGitBookFull() {
        defaultTest(true)
    }

    public fun testCommonMarkSpec() {
        defaultTest(false)
    }

    public fun testCommonMarkSpecFull() {
        defaultTest(true)
    }

    companion object {
        val WARM_UP_NUM = 10
        val TEST_NUM = 100
    }
}