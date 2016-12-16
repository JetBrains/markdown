package org.intellij.markdown.parser

class LookaheadText(private val text: CharSequence) {
    private val lines: List<String> = text.split('\n')

    val startPosition: Position? = if (text.isNotEmpty())
        Position(0, -1, -1).nextPosition()
    else
        null


    inner class Position internal constructor(private val lineN: Int,
                                                     private val localPos: Int, // -1 if on newline before
                                                     private val globalPos: Int) {
        init {
            assert(lineN < lines.size)
            assert(localPos >= -1 && localPos < lines.get(lineN).length)
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
            get() = if (lineN + 1 < lines.size) {
                globalPos + (currentLine.length - localPos)
            } else {
                null
            }

        val nextLineOrEofOffset: Int
            get() = globalPos + (currentLine.length - localPos)

        val textFromPosition: CharSequence
            get() = text.subSequence(globalPos, text.length)

        val currentLine: String
            get() = lines.get(lineN)

        val currentLineFromPosition: CharSequence
            get() = currentLine.substring(offsetInCurrentLine)

        val nextLine: String?
            get() = if (lineN + 1 < lines.size) {
                lines.get(lineN + 1)
            } else {
                null
            }

        val prevLine: String?
            get() = if (lineN > 0) {
                lines.get(lineN - 1)
            } else {
                null
            }

        val char: Char
            get() = text[globalPos]

        fun nextPosition(delta: Int = 1): Position? {
            var remaining = delta
            var currentPosition = this

            while (true) {
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
            var offset = Math.max(localPos, 0)
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
            if (other?.javaClass != javaClass) return false

            other as Position

            return globalPos == other.globalPos
        }

        override fun hashCode() = globalPos

    }

}