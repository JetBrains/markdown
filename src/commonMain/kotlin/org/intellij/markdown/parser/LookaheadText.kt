package org.intellij.markdown.parser

import org.intellij.markdown.lexer.Compat.assert
import kotlin.math.max

class LookaheadText internal constructor(private val text: CharSequence, startOffset: Int) {
    constructor(text: CharSequence) : this(text, 0)

    // CharSequence.split() always copies each piece into a new String (substring() = subSequence().toString()).
    // This keeps pieces as CharSequence views via subSequence(), so a bombed/cancellable CharSequence isn't flattened.
    private val lines = ArrayList<CharSequence>()
    private var nextLineStart = startOffset

    val startPosition: Position? = if (startOffset < text.length)
        Position(0, -1, startOffset - 1).nextPosition()
    else
        null

    private fun getLine(lineN: Int): CharSequence? {
        while (lineN >= lines.size && nextLineStart != -1) {
            val lineEnd = text.indexOf('\n', nextLineStart)
            if (lineEnd == -1) {
                lines.add(text.subSequence(nextLineStart, text.length))
                nextLineStart = -1
            } else {
                lines.add(text.subSequence(nextLineStart, lineEnd))
                nextLineStart = lineEnd + 1
            }
        }
        return lines.getOrNull(lineN)
    }

    inner class Position internal constructor(private val lineN: Int,
                                                     private val localPos: Int, // -1 if on newline before
                                                     private val globalPos: Int) {

        val originalText: CharSequence get() = text

        val currentLine = getLine(lineN)!!

        init {
            assert(localPos >= -1 && localPos < currentLine.length)
        }

        override fun toString(): String {
            return "Position: '${
                if (localPos == -1) {
                    "\\n" + currentLine
                } else {
                    currentLine.substring(localPos)
                }
            }'"
        }

        val offset: Int
            get() = globalPos

        val offsetInCurrentLine: Int
            get() = localPos

        val nextLineOffset: Int?
            get() = if (getLine(lineN + 1) != null) {
                globalPos + (currentLine.length - localPos)
            } else {
                null
            }

        val nextLineOrEofOffset: Int
            get() = globalPos + (currentLine.length - localPos)

        val textFromPosition: CharSequence
            get() = text.subSequence(globalPos, text.length)

        val currentLineFromPosition: CharSequence
            get() = currentLine.subSequence(offsetInCurrentLine, currentLine.length)

        val nextLine: CharSequence?
            get() = getLine(lineN + 1)

        val prevLine: CharSequence?
            get() = if (lineN > 0) {
                getLine(lineN - 1)
            } else {
                null
            }

        val char: Char
            get() = text[globalPos]

        fun nextPosition(delta: Int = 1): Position? {
            var remaining = delta
            var currentPosition = this

            while (true) {
                if (remaining == 0) {
                    return currentPosition
                }
                if (currentPosition.localPos + remaining < currentPosition.currentLine.length) {
                    return Position(currentPosition.lineN,
                            currentPosition.localPos + remaining,
                            currentPosition.globalPos + remaining)
                } else {
                    val nextLine = currentPosition.nextLineOffset
                    if (nextLine == null) {
                        return null
                    } else {
                        val payload = currentPosition.currentLine.length - currentPosition.localPos

                        currentPosition = Position(currentPosition.lineN + 1, -1, currentPosition.globalPos + payload)
                        remaining -= payload
                    }
                }
            }
        }

        fun nextLinePosition(): Position? {
            val nextLine = nextLineOffset
                    ?: return null
            return nextPosition(nextLine - offset)
        }

        fun charsToNonWhitespace(): Int? {
            val line = currentLine
            var offset = max(localPos, 0)
            while (offset < line.length) {
                val c = line[offset]
                if (c != ' ' && c != '\t') {
                    return offset - localPos
                }
                offset++
            }
            return null
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null) return false
            if (other::class != this::class) return false

            other as Position

            return globalPos == other.globalPos
        }

        override fun hashCode() = globalPos

    }

}
