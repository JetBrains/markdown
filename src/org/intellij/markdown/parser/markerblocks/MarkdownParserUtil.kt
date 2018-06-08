package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.lexer.Compat.assert
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.MarkdownConstraints

object MarkdownParserUtil {

    fun calcNumberOfConsequentEols(pos: LookaheadText.Position, constraints: MarkdownConstraints): Int {
        assert(pos.offsetInCurrentLine == -1)

        var currentPos = pos
        var result = 1

        val isClearLine: (LookaheadText.Position) -> Boolean = { pos ->
            val currentConstraints = MarkdownConstraints.fillFromPrevious(pos, constraints)
            val constraintsLength = currentConstraints.getCharsEaten(pos.currentLine)

            currentConstraints.upstreamWith(constraints) && (
                    constraintsLength >= pos.currentLine.length ||
                    pos.nextPosition(1 + constraintsLength)?.charsToNonWhitespace() == null)
        }

        while (isClearLine(currentPos)) {
            currentPos = currentPos.nextLinePosition()
                    ?: break//return 5

            result++
            if (result > 4) {
                break
            }
        }
        return result
    }

    fun getFirstNonWhitespaceLinePos(pos: LookaheadText.Position, eolsToSkip: Int): LookaheadText.Position? {
        var currentPos = pos
        repeat(eolsToSkip - 1) {
            currentPos = pos.nextLinePosition() ?: return null
        }
        while (currentPos.charsToNonWhitespace() == null) {
            currentPos = currentPos.nextLinePosition()
                    ?: return null
        }
        return currentPos
    }

    fun hasCodeBlockIndent(pos: LookaheadText.Position,
                                  constraints: MarkdownConstraints): Boolean {
        val constraintsLength = constraints.getCharsEaten(pos.currentLine)

        if (pos.offsetInCurrentLine >= constraintsLength + 4) {
            return true
        }
        for (i in constraintsLength..pos.offsetInCurrentLine) {
            if (pos.currentLine[i] == '\t') {
                return true
            }
        }
        return false
    }

    fun isEmptyOrSpaces(s: CharSequence): Boolean {
        for (c in s) {
            if (c != ' ' && c != '\t') {
                return false
            }
        }
        return true
    }

    fun findNonEmptyLineWithSameConstraints(constraints: MarkdownConstraints,
                                                   pos: LookaheadText.Position): LookaheadText.Position? {
        var currentPos = pos

        while (true) {
//            currentPos = currentPos.nextLinePosition() ?: return null

            val nextLineConstraints = MarkdownConstraints.fromBase(currentPos, constraints)
            // kinda equals
            if (!(nextLineConstraints.upstreamWith(constraints) && nextLineConstraints.extendsPrev(constraints))) {
                return null
            }

            val stringAfterConstraints = nextLineConstraints.eatItselfFromString(currentPos.currentLine)

            if (!MarkdownParserUtil.isEmptyOrSpaces(stringAfterConstraints)) {
                return currentPos
            } else {
                currentPos = currentPos.nextLinePosition()
                        ?: return null
            }
        }
    }

}
