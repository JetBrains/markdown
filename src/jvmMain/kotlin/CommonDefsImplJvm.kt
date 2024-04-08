package org.intellij.markdown.html

import org.intellij.markdown.lexer.Stack
import java.net.URLEncoder

actual class BitSet actual constructor(size: Int): java.util.BitSet(size){
    actual val size = size()
}

actual class IntStack: Stack<Int>()

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
    return isAsciiPunctuationFix(char) || (PUNCTUATION_MASK shr Character.getType(char)) and 1 != 0
}

private fun isAsciiPunctuationFix(char: Char): Boolean {
    // the ones which are not covered by a more general check
    return "$^`".contains(char)
}

actual fun urlEncode(str: String): String {
    return URLEncoder.encode(str, "UTF-8")
}
