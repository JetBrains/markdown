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
        val numberOfHeaderCells = getTableColumns(pos, currentConstraints) ?: return emptyList()
        return listOf(GitHubTableMarkerBlock(pos, currentConstraints, productionHolder, numberOfHeaderCells))
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return getTableColumns(pos, constraints) != null
    }

    private fun getTableColumns(pos: LookaheadText.Position, constraints: MarkdownConstraints): Int? {
        val currentLineFromPosition = pos.currentLineFromPosition
        if (!currentLineFromPosition.contains('|')) {
            return null
        }

        val numberOfHeaderCells = countHeaderCells(currentLineFromPosition)
        if (numberOfHeaderCells == 0) {
            return null
        }
        val nextLine = getNextLineFromConstraints(pos, constraints) ?: return null
        return numberOfHeaderCells.takeIf { countSecondLineCells(nextLine) == it }
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
        private fun countHeaderCells(line: CharSequence): Int {
            var result = 0
            var cellStart = 0
            var cellIndex = 0
            for (index in line.indices) {
                if (line[index] != '|' || index > 0 && line[index - 1] == '\\') {
                    continue
                }
                if (cellIndex > 0 || line.hasNonWhitespace(cellStart, index)) {
                    result++
                }
                cellStart = index + 1
                cellIndex++
            }
            if (line.hasNonWhitespace(cellStart, line.length)) {
                result++
            }
            return result
        }

        private fun CharSequence.hasNonWhitespace(start: Int, end: Int): Boolean {
            for (index in start until end) {
                if (!this[index].isWhitespace()) {
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

                if (dashes < 1) {
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