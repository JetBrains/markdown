package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.AtxHeaderMarkerBlock
import kotlin.text.Regex

public class AtxHeaderProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): List<MarkerBlock> {
        val headerRange = matches(pos)
        if (headerRange != null) {
            return listOf(AtxHeaderMarkerBlock(stateInfo.currentConstraints,
                    productionHolder,
                    headerRange,
                    calcTailStartPos(pos, headerRange.end),
                    pos.nextLineOrEofOffset))
        } else {
            return emptyList()
        }

    }

    private fun calcTailStartPos(pos: LookaheadText.Position, headerSize: Int): Int {
        val line = pos.currentLineFromPosition
        var offset = line.length() - 1
        while (offset > headerSize && Character.isWhitespace(line[offset])) {
            offset--
        }
        while (offset > headerSize && line[offset] == '#' && line[offset - 1] != '\\') {
            offset--
        }
        if (offset + 1 < line.length() && Character.isWhitespace(line[offset]) && line[offset + 1] == '#') {
            return pos.offset + offset + 1
        } else {
            return pos.offset + line.length()
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position, constraints: MarkdownConstraints): Boolean {
        return matches(pos) != null
    }

    private fun matches(pos: LookaheadText.Position): Range<Int>? {
        if (pos.offsetInCurrentLine != -1) {
            val matchResult = REGEX.match(pos.currentLineFromPosition)
            if (matchResult != null) {
                return matchResult.groups[1]!!.range
            }
        }
        return null
    }

    companion object {
        val REGEX: Regex = Regex("^ {0,3}(#{1,6})( |$)")
    }
}