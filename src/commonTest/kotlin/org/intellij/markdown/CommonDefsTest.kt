package org.intellij.markdown

import org.intellij.markdown.html.isPunctuation
import org.intellij.markdown.html.isWhitespace
import kotlin.test.*

class CommonDefsTest {
    @Test fun spaceIsWhitespace() {
        assertTrue(isWhitespace(' '))
    }

    @Test fun exclamationIsNotWhitespace() {
        assertFalse(isWhitespace('!'))
    }
    
    @Test fun commonPunctuationCharsPassCheck() {
        val asciiPunctuationChars = "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
        for (asciiPunctuationChar in asciiPunctuationChars) {
            assertTrue(isPunctuation(asciiPunctuationChar), "$asciiPunctuationChar should be punctuation")
        }
        assertFalse(isPunctuation(' '), "' ' should not be punctuation")
    }
}