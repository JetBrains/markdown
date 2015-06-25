package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.AtxHeaderMarkerBlock
import kotlin.text.Regex

public class AtxHeaderProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position,
                                   productionHolder: ProductionHolder,
                                   stateInfo: MarkerProcessor.StateInfo): MarkerBlock? {
        if (matches(pos)) {
            return AtxHeaderMarkerBlock(stateInfo.currentConstraints,
                    productionHolder,
                    calcHeaderSize(pos))
        } else {
            return null
        }

    }

    private fun calcHeaderSize(pos: LookaheadText.Position): Int {
        val line = pos.currentLine
        var result = 0
        while (result < line.length() && line[result] == '#') {
            result++
        }
        return result
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        return matches(pos)
    }

    private fun matches(pos: LookaheadText.Position): Boolean {
        return pos.offsetInCurrentLine == 0 && REGEX.match(pos.currentLine) != null
    }

    companion object {
        val REGEX: Regex = Regex("^ {0,3}#{1,6} ")
    }
}