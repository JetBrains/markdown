package org.intellij.markdown.flavours.gfm.table

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider

class GitHubTableMarkerProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position, productionHolder: ProductionHolder, stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        val currentConstraints = stateInfo.currentConstraints
        if (stateInfo.nextConstraints != currentConstraints) {
            return emptyList()
        }

        if (!pos.currentLineFromPosition.contains('|')) {
            return emptyList()
        }
        if (getNextLineFromConstraints(pos, currentConstraints)?.let { isGoodSecondLine(it) } == true) {
            return listOf(GitHubTableMarkerBlock(pos, currentConstraints, productionHolder))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return false
    }

    private fun getNextLineFromConstraints(pos: LookaheadText.Position, constraints: MarkdownConstraints): CharSequence? {
        val line = pos.nextLine ?: return null
        val nextLineConstraints = MarkdownConstraints.fillFromPrevious(line, 0, constraints)
        if (nextLineConstraints.extendsPrev(constraints)) {
            return nextLineConstraints.eatItselfFromString(line)
        } else {
            return null
        }
    }

    companion object {
        fun CharSequence.contains(char: Char): Boolean {
            for (c in this) {
                if (c == char) {
                    return true
                }
            }
            return false
        }

        fun isGoodSecondLine(line: CharSequence): Boolean {
            var offset = passWhiteSpaces(line, 0)
            if (offset < line.length && line[offset] == '|') {
                offset++
            }

            while (offset < line.length) {
                offset = passWhiteSpaces(line, offset)
                if (offset < line.length && line[offset] == ':') {
                    offset++
                }
                offset = passWhiteSpaces(line, offset)

                var dashes = 0
                while (offset < line.length && line[offset] == '-') {
                    offset++
                    dashes++
                }

                if (dashes < 3) {
                    return false
                }

                offset = passWhiteSpaces(line, offset)
                if (offset < line.length && line[offset] == ':') {
                    offset++
                }
                offset = passWhiteSpaces(line, offset)

                if (offset < line.length && line[offset] == '|') {
                    offset++
                } else {
                    break
                }
            }

            offset = passWhiteSpaces(line, offset)

            return offset == line.length
        }

        fun passWhiteSpaces(line: CharSequence, offset: Int): Int {
            var curOffset = offset;
            while (curOffset < line.length) {
                if (line[curOffset] != ' ' && line[curOffset] != '\t') {
                    break
                }
                curOffset++
            }
            return curOffset
        }
    }
}