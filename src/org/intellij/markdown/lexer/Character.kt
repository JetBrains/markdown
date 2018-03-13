package org.intellij.markdown.lexer

object Character {
    fun offsetByCodePoints(seq: CharSequence, index: Int,
                           codePointOffset: Int): Int {
        val length = seq.length
        if (index < 0 || index > length) {
            throw IndexOutOfBoundsException()
        }

        var x = index
        if (codePointOffset >= 0) {
            var i: Int
            i = 0
            while (x < length && i < codePointOffset) {
                if (seq[x++].isHighSurrogate() && x < length &&
                        seq[x].isLowSurrogate()) {
                    x++
                }
                i++
            }
            if (i < codePointOffset) {
                throw IndexOutOfBoundsException()
            }
        } else {
            var i: Int
            i = codePointOffset
            while (x > 0 && i < 0) {
                if (seq[--x].isLowSurrogate() && x > 0 &&
                        seq[x - 1].isHighSurrogate()) {
                    x--
                }
                i++
            }
            if (i < 0) {
                throw IndexOutOfBoundsException()
            }
        }
        return x
    }

    fun codePointBefore(seq: CharSequence, index: Int): Int {
        var index = index
        val c2 = seq[--index]
        if (c2.isLowSurrogate() && index > 0) {
            val c1 = seq[--index]
            if (c1.isHighSurrogate()) {
                return toCodePoint(c1, c2)
            }
        }
        return c2.toInt()
    }

    private val MIN_SUPPLEMENTARY_CODE_POINT = 0x010000

    fun charCount(char: Int): Int {
        return if (char >= MIN_SUPPLEMENTARY_CODE_POINT) 2 else 1
    }

    fun toCodePoint(high: Char, low: Char): Int {
        // Optimized form of:
        // return ((high - MIN_HIGH_SURROGATE) << 10)
        //         + (low - MIN_LOW_SURROGATE)
        //         + MIN_SUPPLEMENTARY_CODE_POINT;
        return (high.toInt() shl 10) + low.toInt() + (MIN_SUPPLEMENTARY_CODE_POINT
                - (Char.MIN_HIGH_SURROGATE.toInt() shl 10)
                - Char.MIN_LOW_SURROGATE.toInt())
    }

    fun codePointAt(seq: CharSequence, index: Int): Int {
        var index = index
        val c1 = seq[index]
        if (c1.isHighSurrogate() && ++index < seq.length) {
            val c2 = seq[index]
            if (c2.isLowSurrogate()) {
                return toCodePoint(c1, c2)
            }
        }
        return c1.toInt()
    }
}