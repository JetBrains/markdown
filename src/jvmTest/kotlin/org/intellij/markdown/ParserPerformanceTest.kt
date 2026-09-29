package org.intellij.markdown

import junit.framework.TestCase
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.junit.experimental.categories.Category
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

@Category(ParserPerformanceTest::class) class ParserPerformanceTest : TestCase() {
    protected fun getTestDataPath(): String {
        return File(getIntellijMarkdownHome() + "/src/jvmTest/resources/data/performance").absolutePath
    }

    private fun assertFast(
        content: String,
        fullParse: Boolean,
        expectedTimeMs: Int? = 1000,
        flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor(),
    ) {
        val runnable = { i: Int ->
            val root = MarkdownParser(flavour).
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

    @Test
    fun testRejectedMathClosersAreLinear() {
        val input = (1..20_000).joinToString(" ") { "\$x\$1" }
        assertFast(input, false, flavour = GFMFlavourDescriptor())
    }

    @Test
    fun testMathLinkRangeLookupIsLinear() {
        val input = (1..10_000).joinToString(" ") { i ->
            "[link$i](url$i) \$x\$"
        }
        assertFast(input, false, flavour = GFMFlavourDescriptor())
    }

    @Test
    fun testDeeplyNestedListMarkersOnOneLine() {
        val input = "- ".repeat(2_000) + "a\n" + "b\n".repeat(2_000) + "\n"
        assertFast(input, false, 250)
    }

    @Test
    fun testDeeplyNestedCheckboxMarkersOnOneLine() {
        val input = "- [ ] ".repeat(2_000) + "a\n" + "b\n".repeat(2_000) + "\n"
        assertFast(input, false, 250, flavour = GFMFlavourDescriptor())
    }

    @Test
    fun testDeeplyNestedListByIndentation() {
        val input = (1..1_200).joinToString("\n") { "  ".repeat(it) + "- a" }
        assertFast(input, false, 200)
    }

    @Test
    fun testDeeplyNestedListFollowedByIndentedParagraphs() {
        val depth = 800
        val input = (1..depth).joinToString("\n") { "  ".repeat(it) + "- a" } + "\n" +
                ("  ".repeat(depth) + "  b\n").repeat(depth)
        assertFast(input, false, 200)
    }

    @Test
    fun testDeeplyNestedBlockQuotes() {
        val input = (1..1_000).joinToString("\n") { "> ".repeat(1_000) + "a" }
        assertFast(input, false, 150)
    }

    /**
     * IJPL-96392: every `[` used to trigger a link-text scan up to its matching `]`,
     * so nested matched brackets made inline parsing quadratic.
     */
    @Test
    fun testNestedMatchedBracketsAreLinear() {
        assertLinkCandidatesFast(
            "[".repeat(4_000) + "a" + "]".repeat(4_000),
            "[".repeat(4_000) + "a" + "](".repeat(4_000),
            "[".repeat(4_000) + "a" + "][]".repeat(4_000),
        )
    }

    /**
     * IJPL-96392: every `[...](` candidate used to scan for its destination and title up to
     * their closing tokens, so unclosed ones made inline parsing quadratic.
     */
    @Test
    fun testUnclosedInlineLinkCandidatesAreLinear() {
        assertLinkCandidatesFast(
            "[ (](".repeat(4_000),
            "[a](b (".repeat(4_000) + ")",
            "[a](<".repeat(4_000) + ")",
            "[a](((".repeat(4_000) + ")",
        )
    }

    /** Also checks the inputs with every link candidate turned into an image one. */
    private fun assertLinkCandidatesFast(vararg inputs: String) {
        for (input in inputs.flatMap { listOf(it, it.replace("[", "![")) }) {
            assertFast(input, true, 100)
            assertFast(input, true, 100, flavour = GFMFlavourDescriptor())
        }
    }

    @Test
    fun testLongDigitRunIsNotScannedAsListMarker() {
        val input = "- a\n" + ("1".repeat(100_000) + "\n").repeat(50)
        assertFast(input, false, 50)
    }

    companion object {
        val WARM_UP_NUM = 10
        val TEST_NUM = 100
    }
}
