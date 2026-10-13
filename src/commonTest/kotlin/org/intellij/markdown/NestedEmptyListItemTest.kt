package org.intellij.markdown

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import kotlin.test.Test

class NestedEmptyListItemTest {
    @Test
    fun testEmptyHyphenCreatesNestedItem() {
        doTest("- hallo\n    -", "<ul><li>hallo<ul><li></li></ul></li></ul>")
        doTest("- hallo\n    - ", "<ul><li>hallo<ul><li></li></ul></li></ul>")
        doTest("- hallo\n\t-", "<ul><li>hallo<ul><li></li></ul></li></ul>")
    }

    @Test
    fun testOtherSetextUnderlinesInsideList() {
        doTest("- hallo\n    --", "<ul><li><h2>hallo</h2></li></ul>")
        doTest("- hallo\n    =", "<ul><li><h1>hallo</h1></li></ul>")
    }

    @Test
    fun testOtherEmptyMarkersDoNotInterruptParagraph() {
        doTest("- hallo\n    +", "<ul><li>hallo\n    +</li></ul>")
        doTest("- hallo\n    1.", "<ul><li>hallo\n    1.</li></ul>")
    }

    private fun doTest(markdown: String, html: String) {
        for (flavour in listOf(CommonMarkFlavourDescriptor(), GFMFlavourDescriptor())) {
            object : SpecTest(flavour) {}.doTest(markdown, html)
        }
    }
}