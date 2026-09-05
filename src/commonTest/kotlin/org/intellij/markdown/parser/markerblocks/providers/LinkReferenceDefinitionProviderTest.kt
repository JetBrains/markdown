package org.intellij.markdown.parser.markerblocks.providers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkReferenceDefinitionProviderTest {
    @Test
    fun labelStopsAtBlankLine() {
        assertNull(LinkReferenceDefinitionProvider.matchLinkLabel("[foo\n\nbar]: /url", 0))
    }

    @Test
    fun labelStopsAtLineWithOnlySpaceAndTab() {
        assertNull(LinkReferenceDefinitionProvider.matchLinkLabel("[foo\n \t \nbar]: /url", 0))
    }

    @Test
    fun labelStopsAtCrLfBlankLine() {
        assertNull(LinkReferenceDefinitionProvider.matchLinkLabel("[foo\r\n\r\nbar]: /url", 0))
    }

    @Test
    fun labelStopsAtBareCrBlankLine() {
        assertNull(LinkReferenceDefinitionProvider.matchLinkLabel("[foo\r\rbar]: /url", 0))
    }

    @Test
    fun labelStopsAtBareCrBeforeCrLfBlankLine() {
        assertNull(LinkReferenceDefinitionProvider.matchLinkLabel("[foo\r\r\nbar]: /url", 0))
    }

    @Test
    fun labelKeepsCrLfLineBreak() {
        assertEquals(0..9, LinkReferenceDefinitionProvider.matchLinkLabel("[foo\r\nbar]: /url", 0))
    }

    @Test
    fun labelKeepsBareCrLineBreak() {
        assertEquals(0..8, LinkReferenceDefinitionProvider.matchLinkLabel("[foo\rbar]: /url", 0))
    }

    @Test
    fun labelKeepsLineWithOnlyVerticalTab() {
        assertEquals(0..10, LinkReferenceDefinitionProvider.matchLinkLabel("[foo\n\u000B\nbar]: /url", 0))
    }

    @Test
    fun labelKeepsLineWithOnlyFormFeed() {
        assertEquals(0..10, LinkReferenceDefinitionProvider.matchLinkLabel("[foo\n\u000C\nbar]: /url", 0))
    }

    @Test
    fun labelStopsAtVeryLongWhitespaceLine() {
        val text = "[foo\n" + " ".repeat(5000) + "\nbar]: /url"

        assertNull(LinkReferenceDefinitionProvider.matchLinkLabel(text, 0))
    }
}
