package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.junit.experimental.categories.Category
import java.io.File
import kotlin.test.*

@Category(ParserPerformanceTest::class) class ParserPerformanceTest : TestCase() {
    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/src/jvmTest/resources/data/performance").absolutePath
    }

    private fun assertFast(content: String, fullParse: Boolean, expectedTimeMs: Int? = 1000) {
        val runnable = { i: Int ->
            val root = MarkdownParser(CommonMarkFlavourDescriptor()).
            parse(MarkdownElementTypes.MARKDOWN_FILE, content, fullParse)
            assert(root.children.size > 0)
        }

        repeat(WARM_UP_NUM, runnable)

        val startTime = System.nanoTime()
        repeat(TEST_NUM, runnable)
        val testTime = System.nanoTime() - startTime

        val timeMs = testTime / TEST_NUM / 1e6
        println("$testName: ${timeMs}ms")
        if (expectedTimeMs != null) {
            assertTrue(timeMs <= expectedTimeMs, "Expected $expectedTimeMs ms, got $timeMs")
        }
    }

    private fun defaultTest(fullParse: Boolean) {
        val fileName = testName.let {
            if (it.endsWith("Full")) {
                it.substring(0, it.length - 4)
            } else {
                it
            }
        }
        val content = File(getTestDataPath() + "/" + fileName + ".md").readText()
        assertFast(content, fullParse, null)
    }

    @Test
    fun testGitBook() {
        defaultTest(false)
    }

    @Test
    fun testGitBookFull() {
        defaultTest(true)
    }

    @Test
    fun testCommonMarkSpec() {
        defaultTest(false)
    }

    @Test
    fun testCommonMarkSpecFull() {
        defaultTest(true)
    }

    @Test
    fun testFogChangelog() {
        defaultTest(false)
    }

    @Test
    fun testUnmatchedBrackets() {
        assertFast("[".repeat(10000), true, 250)
    }

    @Test
    fun testHugeUnterminatedLinkTitle() {
        assertFast("[a]: x (" + "y".repeat(20_000_000), false)
        assertFast("[a]: <" + "y".repeat(20_000_000), false)
    }

    companion object {
        val WARM_UP_NUM = 10
        val TEST_NUM = 100
    }
}
