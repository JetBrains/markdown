package org.intellij.markdown.parser


class CharArrayCharSequence(private val chars: String,
                            private val start: Int = 0,
                            override val length: Int = chars.length - start) : CharSequence {

    override fun get(index: Int): Char = chars[start + index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        if (startIndex > endIndex || !checkIndex(startIndex) || !checkIndex(endIndex)) {
            throw IndexOutOfBoundsException("$startIndex..$endIndex does not lie within 0..$length")
        }
        return CharArrayCharSequence(chars, start + startIndex, endIndex - startIndex)
    }

    private fun checkIndex(index: Int) = index >= 0 && index <= length

    override fun toString(): String {
        return chars.substring(start, start + length)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other::class != this::class) return false

        other as CharArrayCharSequence

        if (other.length != length) return false
        for (i in 0..length - 1) {
            if (get(i) != other.get(i)) {
                return false
            }
        }

        return true
    }

    private val hashCode: Int by lazy {
        var h = 0
        for (offset in start..start + length - 1) {
            h = 31 * h + chars[offset].toInt()
        }
        return@lazy h
    }

    override fun hashCode(): Int {
        return hashCode
    }
}