package org.intellij.markdown.html

import java.net.URLEncoder

private const val PUNCTUATION_MASK: Int = (1 shl Character.DASH_PUNCTUATION.toInt()) or
        (1 shl Character.START_PUNCTUATION.toInt())     or
        (1 shl Character.END_PUNCTUATION.toInt())       or
        (1 shl Character.CONNECTOR_PUNCTUATION.toInt()) or
        (1 shl Character.OTHER_PUNCTUATION.toInt())     or
        (1 shl Character.INITIAL_QUOTE_PUNCTUATION.toInt()) or
        (1 shl Character.FINAL_QUOTE_PUNCTUATION.toInt()) or
        (1 shl Character.MATH_SYMBOL.toInt())

actual typealias URI = java.net.URI

actual fun isWhitespace(char: Char): Boolean {
    return char == 0.toChar() || Character.isSpaceChar(char) || char.isWhitespace()
}

actual fun isPunctuation(char: Char): Boolean {
    return (PUNCTUATION_MASK shr Character.getType(char)) and 1 != 0
}

actual fun isLetterOrDigit(char: Char): Boolean {
    return Character.isLetterOrDigit(char)
}

actual fun urlEncode(str: String): String {
    return URLEncoder.encode(str, "UTF-8")
}
