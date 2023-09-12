package org.intellij.markdown.html

expect class URI(str: String) {
    fun resolve(str: String): URI
}

fun URI.resolveToStringSafe(str: String): String {
    return try {
        resolve(str).toString()
    }
    catch (e: Throwable) {
        str
    }
}

expect class BitSet(size: Int){
    val size: Int
    fun get(index: Int): Boolean
    fun set(index: Int, value: Boolean)
}

expect fun isWhitespace(char: Char): Boolean

expect fun isPunctuation(char: Char): Boolean

expect fun urlEncode(str: String): String
