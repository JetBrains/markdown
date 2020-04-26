package org.intellij.markdown.flavours.gfm.table

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.constraints.eatItselfFromString
import org.intellij.markdown.parser.constraints.extendsPrev
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider

class GitHubTableMarkerProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position, productionHolder: ProductionHolder, stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        val currentConstraints = stateInfo.currentConstraints
        if (stateInfo.nextConstraints != currentConstraints) {
            return emptyList()
        }

        val currentLineFromPosition = pos.currentLineFromPosition
        if (!currentLineFromPosition.contains('|')) {
            return emptyList()
        }

        val split = SPLIT_REGEX.split(currentLineFromPosition)
        val numberOfHeaderCells = split
                .mapIndexed { i, s -> (i > 0 && i < split.lastIndex) || s.isNotBlank() }
                .count { it }
        if (getNextLineFromConstraints(pos, currentConstraints)
                ?.let { countSecondLineCells(it) == numberOfHeaderCells } == true) {
            return listOf(GitHubTableMarkerBlock(pos, currentConstraints, productionHolder, numberOfHeaderCells))
        } else {
            return emptyList()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return false
    }

    private fun getNextLineFromConstraints(pos: LookaheadText.Position, constraints: MarkdownConstraints): CharSequence? {
        val line = pos.nextLine ?: return null
        val nextLineConstraints = constraints.applyToNextLine(pos.nextLinePosition())
        if (nextLineConstraints.extendsPrev(constraints)) {
            return nextLineConstraints.eatItselfFromString(line)
        } else {
            return null
        }
    }

    companion object {
        val SPLIT_REGEX = Regex("\\|")

        fun CharSequence.contains(char: Char): Boolean {
            for (c in this) {
                if (c == char) {
                    return true
                }
            }
            return false
        }

        /**
         * @return number of cells in the separator line
         */
        fun countSecondLineCells(line: CharSequence): Int {
            var offset = passWhiteSpaces(line, 0)
            if (offset < line.length && line[offset] == '|') {
                offset++
            }

            var result = 0
            while (offset < line.length) {
                offset = passWhiteSpaces(line, offset)
                if (offset < line.length && line[offset] == ':') {
                    offset++
                    offset = passWhiteSpaces(line, offset)
                }

                var dashes = 0
                while (offset < line.length && line[offset] == '-') {
                    offset++
                    dashes++
                }

                if (dashes < 3) {
                    return 0
                }
                result++

                offset = passWhiteSpaces(line, offset)
                if (offset < line.length && line[offset] == ':') {
                    offset++
                    offset = passWhiteSpaces(line, offset)
                }

                if (offset < line.length && line[offset] == '|') {
                    offset++
                    offset = passWhiteSpaces(line, offset)
                } else {
                    break
                }
            }

            if (offset == line.length) {
                return result
            } else {
                return 0
            }
        }

        fun passWhiteSpaces(line: CharSequence, offset: Int): Int {
            var curOffset = offset
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