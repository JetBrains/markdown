package org.intellij.markdown.html

expect class URI(str: String) {
    fun resolve(str: String): URI
}

expect fun isWhitespace(char: Char): Boolean

expect fun isPunctuation(char: Char): Boolean

expect fun urlEncode(str: String): String

expect fun String(charArray: CharArray, start: Int, length: Int): String