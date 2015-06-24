package org.intellij.markdown.parser.markerblocks.providers

import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.MarkerProcessor
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockProvider
import org.intellij.markdown.parser.markerblocks.impl.CodeFenceMarkerBlock
import kotlin.text.Regex

public class CodeFenceProvider : MarkerBlockProvider<MarkerProcessor.PositionInfo> {
    override fun createMarkerBlock(pos: LookaheadText.Position, positionInfo: MarkerProcessor.PositionInfo): MarkerBlock? {
        if (matches(pos)) {
            return CodeFenceMarkerBlock(positionInfo.currentConstraints, positionInfo.productionHolder.mark())
        } else {
            return null
        }
    }

    override fun interruptsParagraph(pos: LookaheadText.Position): Boolean {
        return matches(pos)
    }

    private fun matches(pos: LookaheadText.Position): Boolean {
        return REGEX.match(pos.currentLine.subSequence(pos.offsetInCurrentLine, pos.currentLine.length())) != null
    }

    companion object {
        val REGEX: Regex = Regex("^ {0,3}(~~~|```)")
    }
}