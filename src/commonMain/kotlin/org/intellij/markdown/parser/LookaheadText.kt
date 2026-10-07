package org.intellij.markdown.parser

import org.intellij.markdown.lexer.Compat.assert
import kotlin.math.max

class LookaheadText(private val text: CharSequence) {
    private val lineStarts = ArrayList<Int>().apply {
        add(0)
        var cr = text.indexOf('\r')
        var lf = text.indexOf('\n')
        while (cr >= 0 || lf >= 0) {
            val offset = if (cr < 0) lf else if (lf < 0) cr else minOf(cr, lf)
            val end = offset + if (offset == cr && lf == cr + 1) 2 else 1
            add(end)
            if (cr in 0 until end) cr = text.indexOf('\r', end)
            if (lf in 0 until end) lf = text.indexOf('\n', end)
        }
    }.toIntArray()
    // Keep views of the original text, including its offsets and cancellation checks.
    private val lines = lineStarts.mapIndexed { index, start ->
        var end = if (index + 1 < lineStarts.size) lineStarts[index + 1] - 1 else text.length
        if (end > start && text[end - 1] == '\r' && end < text.length && text[end] == '\n') end--
        text.subSequence(start, end)
    }

    private fun lineEnd(index: Int): Int = lineStarts[index] + lines[index].length

    val startPosition: Position? = if (text.isNotEmpty())
        Position(0, -1, -1).nextPosition()
    else
        null


    inner class Position internal constructor(private val lineN: Int,
                                                     private val localPos: Int, // -1 if on newline before
                                                     private val globalPos: Int) {

        val originalText: CharSequence get() = text

        val currentLine = lines[lineN]

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
            get() = if (lineN + 1 < lines.size) lineEnd(lineN) else null

        val nextLineOrEofOffset: Int
            get() = lineEnd(lineN)

        val textFromPosition: CharSequence
            get() = text.subSequence(globalPos, text.length)

        val currentLineFromPosition: CharSequence
            get() = currentLine.subSequence(offsetInCurrentLine, currentLine.length)

        val nextLine: CharSequence?
            get() = if (lineN + 1 < lines.size) {
                lines[lineN + 1]
            } else {
                null
            }

        val prevLine: CharSequence?
            get() = if (lineN > 0) {
                lines[lineN - 1]
            } else {
                null
            }

        val char: Char
            get() = text[globalPos]

        fun nextPosition(delta: Int = 1): Position? {
            if (delta == 0) return this
            if (delta > 0 && globalPos >= text.length - delta) return null
            val target = globalPos + delta
            var nextLineN = lineN
            while (nextLineN + 1 < lines.size && target >= lineEnd(nextLineN)) {
                nextLineN++
                if (target < lineStarts[nextLineN]) {
                    // A CRLF is one line ending; -1 denotes its last character so offset + 1
                    // still points to the first character of the following line.
                    return Position(nextLineN, -1, lineStarts[nextLineN] - 1)
                }
            }
            return Position(nextLineN, target - lineStarts[nextLineN], target)
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