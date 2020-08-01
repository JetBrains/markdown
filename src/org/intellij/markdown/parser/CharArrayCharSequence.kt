package org.intellij.markdown.parser

class CharArrayCharSequence(private val chars: CharArray,
                            private val start: Int = 0,
                            override val length: Int = chars.size - start) : CharSequence {

    private var hash: Int = 0

    override fun get(index: Int): Char = chars[start + index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        if (startIndex > endIndex || !checkIndex(startIndex) || !checkIndex(endIndex)) {
            throw IndexOutOfBoundsException()
        }
        return CharArrayCharSequence(chars, start + startIndex, endIndex - startIndex)
    }
    
    private fun checkIndex(index: Int) = index in 0..length

    
    
    override fun toString(): String {
        return String(chars, start, length)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as CharArrayCharSequence
        
        if (other.length != length) return false
        for (i in 0 until length) {
            if (get(i) != other.get(i)) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var h = hash
        if (h == 0 && length > 0) {
            for (offset in start until start + length) {
                h = 31 * h + chars[offset].toInt()
            }
            hash = h
        }
        return h
    }
}