package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.MarkdownConstraints

public object MarkdownParserUtil {

    public fun calcNumberOfConsequentEols(pos: LookaheadText.Position, constraints: MarkdownConstraints): Int {
        assert(pos.char == '\n')

        var currentPos = pos;
        var result = 1

        val isClearLine: (LookaheadText.Position) -> Boolean = { pos ->
            val currentConstraints = MarkdownConstraints.fromBase(pos, constraints)

            currentConstraints.upstreamWith(constraints) && (
                    currentConstraints.getIndent() >= pos.currentLine.length() ||
                    pos.nextPosition(1 + currentConstraints.getIndent())?.charsToNonWhitespace() == null)
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

    public fun getFirstNonWhitespaceLinePos(pos: LookaheadText.Position, eolsToSkip: Int): LookaheadText.Position? {
        var currentPos = pos;
        repeat(eolsToSkip - 1) {
            currentPos = pos.nextLinePosition() ?: return null
        }
        while (currentPos.charsToNonWhitespace() == null) {
            currentPos = currentPos.nextLinePosition()
                    ?: return null
        }
        return currentPos
    }

    public fun getIndentBeforeRawToken(pos: LookaheadText.Position): Int {
        return pos.offsetInCurrentLine
    }

    public fun hasCodeBlockIndent(pos: LookaheadText.Position,
                                  constraints: MarkdownConstraints): Boolean {
        return getIndentBeforeRawToken(pos) >= constraints.getIndent() + 4
    }

    public fun isEmptyOrSpaces(s: CharSequence): Boolean {
        for (c in s) {
            if (c != ' ' && c != '\t') {
                return false
            }
        }
        return true
    }

    public fun findNonEmptyLineWithSameConstraints(constraints: MarkdownConstraints,
                                                   pos: LookaheadText.Position): LookaheadText.Position? {
        var currentPos = pos;

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
