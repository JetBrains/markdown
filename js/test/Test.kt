package org.intellij.markdown.html

import kotlin.test.*

class UriTest {
    @Test fun passingTest() {
        assertTrue(isWhitespace(' '), "WAT?!")
    }

    @Test fun failingTest() {
        assertTrue(isWhitespace('!'), "RLY?")
    }
}