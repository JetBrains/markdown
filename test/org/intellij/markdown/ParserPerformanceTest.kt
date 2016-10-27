package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.junit.experimental.categories.Category
import java.io.File

@Category(ParserPerformanceTest::class) class ParserPerformanceTest : TestCase() {
    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/test/data/performance").absolutePath
    }

    private fun defaultTest(fullParse: Boolean) {
        val fileName = testName.let {
            if (it.endsWith("Full")) {
                it.substring(0, it.length - 4)
            } else {
                it
            }
        }
        val src = File(getTestDataPath() + "/" + fileName + ".md").readText()

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

    fun testGitBook() {
        defaultTest(false)
    }

    fun testGitBookFull() {
        defaultTest(true)
    }

    fun testCommonMarkSpec() {
        defaultTest(false)
    }

    fun testCommonMarkSpecFull() {
        defaultTest(true)
    }
    
    fun testFogChangelog() {
        defaultTest(false)
    }

    companion object {
        val WARM_UP_NUM = 10
        val TEST_NUM = 100
    }
}