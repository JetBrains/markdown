package org.intellij.markdown

import org.intellij.markdown.html.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.TimeSource

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(kotlin.time.ExperimentalTime::class) // required for tests
class NativeUrlTest {
    @Test
    fun resolvesLongPathAndFragmentFast() {
        val payload = "a".repeat(50_000)
        val mailto = "mailto:$payload"
        val fragment = "https://example.com/#$payload"
        val query = "https://example.com/?$payload"

        assertFast(250) {
            assertEquals(mailto, URI("https://example.com/").resolve(mailto).toString())
            assertEquals(fragment, URI("https://example.com/").resolve(fragment).toString())
            assertEquals(query, URI("https://example.com/").resolve(query).toString())
        }
    }

    private fun assertFast(expectedTimeMs: Int, block: () -> Unit) {
        repeat(10) { block() }
        val startTime = TimeSource.Monotonic.markNow()
        repeat(100) { block() }
        val timeMs = startTime.elapsedNow().inWholeMilliseconds.toDouble() / 100
        assertTrue(timeMs <= expectedTimeMs, "Expected $expectedTimeMs ms, got $timeMs")
    }
}
