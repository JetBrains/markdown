package org.intellij.markdown.lexer

object Compat {
    private val MIN_SUPPLEMENTARY_CODE_POINT = 0x010000


    /**
     * General category "Pd" in the Unicode specification.
     * @since   1.1
     */
    val DASH_PUNCTUATION: Byte = 20

    /**
     * General category "Ps" in the Unicode specification.
     * @since   1.1
     */
    val START_PUNCTUATION: Byte = 21

    /**
     * General category "Pe" in the Unicode specification.
     * @since   1.1
     */
    val END_PUNCTUATION: Byte = 22

    /**
     * General category "Pc" in the Unicode specification.
     * @since   1.1
     */
    val CONNECTOR_PUNCTUATION: Byte = 23

    /**
     * General category "Po" in the Unicode specification.
     * @since   1.1
     */
    val OTHER_PUNCTUATION: Byte = 24

    /**
     * General category "Sm" in the Unicode specification.
     * @since   1.1
     */
    val MATH_SYMBOL: Byte = 25

    /**
     * General category "Sc" in the Unicode specification.
     * @since   1.1
     */
    val CURRENCY_SYMBOL: Byte = 26

    /**
     * General category "Sk" in the Unicode specification.
     * @since   1.1
     */
    val MODIFIER_SYMBOL: Byte = 27

    /**
     * General category "So" in the Unicode specification.
     * @since   1.1
     */
    val OTHER_SYMBOL: Byte = 28

    /**
     * General category "Pi" in the Unicode specification.
     * @since   1.4
     */
    val INITIAL_QUOTE_PUNCTUATION: Byte = 29

    /**
     * General category "Pf" in the Unicode specification.
     * @since   1.4
     */
    val FINAL_QUOTE_PUNCTUATION: Byte = 30

    /**
     * Error flag. Use int (code point) to avoid confusion with U+FFFF.
     */
    internal val ERROR = -0x1


    /**
     * Undefined bidirectional character type. Undefined `char`
     * values have undefined directionality in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_UNDEFINED: Byte = -1

    /**
     * Strong bidirectional character type "L" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_LEFT_TO_RIGHT: Byte = 0

    /**
     * Strong bidirectional character type "R" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_RIGHT_TO_LEFT: Byte = 1

    /**
     * Strong bidirectional character type "AL" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC: Byte = 2

    /**
     * Weak bidirectional character type "EN" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_EUROPEAN_NUMBER: Byte = 3

    /**
     * Weak bidirectional character type "ES" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR: Byte = 4

    /**
     * Weak bidirectional character type "ET" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR: Byte = 5

    /**
     * Weak bidirectional character type "AN" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_ARABIC_NUMBER: Byte = 6

    /**
     * Weak bidirectional character type "CS" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_COMMON_NUMBER_SEPARATOR: Byte = 7

    /**
     * Weak bidirectional character type "NSM" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_NONSPACING_MARK: Byte = 8

    /**
     * Weak bidirectional character type "BN" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_BOUNDARY_NEUTRAL: Byte = 9

    /**
     * Neutral bidirectional character type "B" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_PARAGRAPH_SEPARATOR: Byte = 10

    /**
     * Neutral bidirectional character type "S" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_SEGMENT_SEPARATOR: Byte = 11

    /**
     * Neutral bidirectional character type "WS" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_WHITESPACE: Byte = 12

    /**
     * Neutral bidirectional character type "ON" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_OTHER_NEUTRALS: Byte = 13

    /**
     * Strong bidirectional character type "LRE" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING: Byte = 14

    /**
     * Strong bidirectional character type "LRO" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE: Byte = 15

    /**
     * Strong bidirectional character type "RLE" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING: Byte = 16

    /**
     * Strong bidirectional character type "RLO" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE: Byte = 17

    /**
     * Weak bidirectional character type "PDF" in the Unicode specification.
     * @since 1.4
     */
    val DIRECTIONALITY_POP_DIRECTIONAL_FORMAT: Byte = 18

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

    inline fun assert(condition: Boolean, messageProducer: () -> String = { "" }) {
        if (!condition) {
            throw AssertionError(messageProducer())
        }
    }
}

class Stack<E> : MutableList<E> by ArrayList<E>() {
    fun push(e: E) = add(e)

    fun pop(): E {
        val result = last()
        removeAt(size - 1)
        return result
    }

    fun peek() = last()
}