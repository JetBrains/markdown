package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.MarkdownConstraints

public object MarkdownParserUtil {

    public fun calcNumberOfConsequentEols(pos: LookaheadText.Position): Int {
        assert(pos.char == '\n')

        var currentPos = pos;
        var result = 1
        while (currentPos.charsToNonWhitespace() == null) {
            currentPos = currentPos.nextLinePosition()
                    ?: break

            result++
            if (result > 4) {
                break
            }
        }
        return result
    }

    public fun getFirstNonWhitespaceLinePos(pos: LookaheadText.Position): LookaheadText.Position? {
        var currentPos = pos;
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
            currentPos = MarkdownParserUtil.getFirstNonWhitespaceLinePos(currentPos)
                    ?: return null

            val nextLineConstraints = MarkdownConstraints.fromBase(currentPos, constraints)
            // kinda equals
            if (!(nextLineConstraints.upstreamWith(constraints) && nextLineConstraints.extendsPrev(constraints))) {
                return null
            }

            val stringAfterConstaints = currentPos.currentLine.subSequence(nextLineConstraints.getIndent(),
                    currentPos.currentLine.length())

            if (!MarkdownParserUtil.isEmptyOrSpaces(stringAfterConstaints)) {
                return currentPos
            } else {
                currentPos = currentPos.nextLinePosition()
                        ?: return null
            }
        }
    }

}
