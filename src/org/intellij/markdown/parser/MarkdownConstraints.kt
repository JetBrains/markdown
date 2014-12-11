package org.intellij.markdown.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes

public class MarkdownConstraints private(private var indents: IntArray,
                                         private var types: CharArray,
                                         private var isExplicit: BooleanArray) {

    public fun getIndent(): Int {
        if (indents.size == 0) {
            return 0
        }

        return indents[indents.size - 1]
    }

    public fun upstreamWith(other: MarkdownConstraints): Boolean {
        return other.startsWith(this) && !containsListMarkers()
    }

    public fun extendsPrev(other: MarkdownConstraints): Boolean {
        return startsWith(other) && !containsListMarkers(other.types.size)
    }

    public fun extendsList(other: MarkdownConstraints): Boolean {
        if (other.types.size == 0) {
            throw IllegalArgumentException("List constraints should contain at least one item")
        }
        return startsWith(other) && !containsListMarkers(other.types.size - 1)
    }

    private fun startsWith(other: MarkdownConstraints): Boolean {
        val n = indents.size
        val m = other.indents.size

        if (n < m) {
            return false
        }
        for (i in 0..m - 1) {
            if (types[i] != other.types[i]) {
                return false
            }
        }
        return true
    }

    private fun containsListMarkers(): Boolean {
        return containsListMarkers(types.size)
    }

    private fun containsListMarkers(upToIndex: Int): Boolean {
        for (i in 0..upToIndex - 1) {
            if (types[i] != BQ_CHAR && isExplicit[i]) {
                return true
            }
        }
        return false
    }

    public fun fillImplicitsOnWhiteSpace(iterator: TokensCache.Iterator, rawIndex: Int, prevLineConstraints: MarkdownConstraints): MarkdownConstraints {
        var result = this
        val whitespaceLen = iterator.rawStart(rawIndex + 1) - iterator.rawStart(rawIndex)
        assert(whitespaceLen > 0, "Token of zero length?")

        var eaten = 0

        val n = indents.size
        val m = prevLineConstraints.indents.size

        if (n > 0 && types[n - 1] == BQ_CHAR) {
            eaten++
        }

        for (i in n..m - 1) {
            if (prevLineConstraints.types[i] == BQ_CHAR) {
                break
            }
            // Else it's list marker

            val indentDelta = prevLineConstraints.indents[i] - (if (i == 0) 0 else prevLineConstraints.indents[i - 1])
            if (eaten + indentDelta <= whitespaceLen) {
                eaten += indentDelta
                result = MarkdownConstraints(result, result.getIndent() + indentDelta, prevLineConstraints.types[i], false)
            } else {
                break
            }
        }

        return result
    }

    public fun addModifierIfNeeded(`type`: IElementType?, iterator: TokensCache.Iterator): MarkdownConstraints {
        var result = this
        if (isConstraintType(`type`)) {
            result = result.addModifier(`type`!!, iterator, 0)
        }
        return result
    }

    public fun addModifier(`type`: IElementType, iterator: TokensCache.Iterator, rawOffset: Int): MarkdownConstraints {
        val modifierChar = getModifierCharAtRawIndex(iterator, rawOffset)

        val lineStartOffset = calcLineStartOffset(iterator, rawOffset)
        val currentIndent = getIndent()
        val markerStartOffset = iterator.rawStart(rawOffset) - lineStartOffset
        val whiteSpaceBefore = markerStartOffset - currentIndent
        assert(whiteSpaceBefore == 0 || iterator.rawLookup(rawOffset - 1) == MarkdownTokenTypes.WHITE_SPACE, "If some indent is present, it should have been whitespace")
        assert(whiteSpaceBefore < 4, "Should not add modifier: indent of 4 is a code block")

        if (`type` == MarkdownTokenTypes.LIST_BULLET || `type` == MarkdownTokenTypes.LIST_NUMBER) {
            val indentAddition = calcIndentAdditionForList(iterator, rawOffset)
            return MarkdownConstraints(this, currentIndent + whiteSpaceBefore + indentAddition, modifierChar, true)
        } else if (`type` == MarkdownTokenTypes.BLOCK_QUOTE) {
            return MarkdownConstraints(this, currentIndent + whiteSpaceBefore + 2, modifierChar, true)
        }

        throw IllegalArgumentException("modifier must be either a list marker or a blockquote marker")
    }

    // overridable
    protected fun calcIndentAdditionForList(iterator: TokensCache.Iterator, rawOffset: Int): Int {
        val markerWidth = iterator.rawStart(rawOffset + 1) - iterator.rawStart(rawOffset)

        if (iterator.rawLookup(1 + rawOffset) != MarkdownTokenTypes.WHITE_SPACE) {
            return markerWidth
        } else {
            val whitespaceAfterLength = iterator.rawStart(2 + rawOffset) - iterator.rawStart(1 + rawOffset)
            if (whitespaceAfterLength >= 4) {
                return markerWidth + 1
            } else {
                return markerWidth + whitespaceAfterLength
            }
        }
    }

    override fun toString(): String {
        return "MdConstraints: " + String(types) + "(" + getIndent() + ")"
    }

    class object {
        public val BASE: MarkdownConstraints = MarkdownConstraints(IntArray(0), CharArray(0), BooleanArray(0))

        public val BQ_CHAR: Char = '>'

        private fun MarkdownConstraints(parent: MarkdownConstraints, newIndent: Int, newType: Char, newExplicit: Boolean): MarkdownConstraints {
            val n = parent.indents.size
            val _indents = IntArray(n + 1)
            val _types = CharArray(n + 1)
            val _isExplicit = BooleanArray(n + 1)
            System.arraycopy(parent.indents, 0, _indents, 0, n)
            System.arraycopy(parent.types, 0, _types, 0, n)
            System.arraycopy(parent.isExplicit, 0, _isExplicit, 0, n)

            _indents[n] = newIndent
            _types[n] = newType
            _isExplicit[n] = newExplicit
            return MarkdownConstraints(_indents, _types, _isExplicit)
        }

        public fun isConstraintType(`type`: IElementType?): Boolean {
            return `type` == MarkdownTokenTypes.LIST_NUMBER || `type` == MarkdownTokenTypes.LIST_BULLET || `type` == MarkdownTokenTypes.BLOCK_QUOTE
        }

        public fun fromBase(iterator: TokensCache.Iterator, rawIndex: Int, prevLineConstraints: MarkdownConstraints): MarkdownConstraints {
            val myStartOffset = iterator.rawStart(rawIndex)

            var result = BASE
            var isAlignedWithPrev = true

            var offset = rawIndex
            while (true) {
                val `type` = iterator.rawLookup(offset)
                if (`type` != MarkdownTokenTypes.WHITE_SPACE && !isConstraintType(`type`)) {
                    break
                }

                // We could jump into code block while scanning "blockquotes", for example.
                if (iterator.rawStart(offset) - myStartOffset >= result.getIndent() + 4) {
                    break
                }

                type!!
                if (`type` == MarkdownTokenTypes.WHITE_SPACE) {
                    if (isAlignedWithPrev) {
                        result = result.fillImplicitsOnWhiteSpace(iterator, offset, prevLineConstraints)
                    }
                    // Here we hope that two whitespace tokens would not appear so we would not update isAlignedWithPrev
                } else {
                    val newConstraints = result.addModifier(`type`, iterator, offset)
                    isAlignedWithPrev = prevLineConstraints.startsWith(newConstraints)

                    result = newConstraints
                }
                offset++
            }

            return result
        }

        private fun calcLineStartOffset(iterator: TokensCache.Iterator, rawOffset: Int): Int {
            var index = rawOffset - 1
            while (true) {
                val `type` = iterator.rawLookup(index)
                if (`type` == null) {
                    return 0
                }
                if (`type` == MarkdownTokenTypes.EOL) {
                    return iterator.rawStart(index + 1)
                }
                --index
            }
        }

        private fun getModifierCharAtRawIndex(iterator: TokensCache.Iterator, index: Int): Char {
            val `type` = iterator.rawLookup(index)
            if (`type` == MarkdownTokenTypes.BLOCK_QUOTE) {
                return BQ_CHAR
            }

            val s = iterator.rawText(index)
            assert(s != null)
            if (`type` == MarkdownTokenTypes.LIST_NUMBER) {
                return s!!.charAt(s.length() - 1)
            }
            if (`type` == MarkdownTokenTypes.LIST_BULLET) {
                return s!!.charAt(0)
            }

            throw IllegalArgumentException("modifier must be either a list marker or a blockquote marker")
        }
    }

}
